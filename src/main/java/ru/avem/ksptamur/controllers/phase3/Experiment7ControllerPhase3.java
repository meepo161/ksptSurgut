
package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.ExperimentValuesModel;
import ru.avem.ksptamur.model.phase3.Experiment7ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.formatRealNumber;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment7ControllerPhase3 extends AbstractExperiment {
    private static final double mA = 1000;

    @FXML
    private TableView<Experiment7ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnType;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnU;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnI;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnResult;

    private Experiment7ModelPhase3 experiment7ModelPhase3BH = experimentsValuesModel.getExperiment7ModelPhase3BH();
    private Experiment7ModelPhase3 experiment7ModelPhase3HH = experimentsValuesModel.getExperiment7ModelPhase3HH();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b1) > 0;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b10) > 0;

    private ObservableList<Experiment7ModelPhase3> experiment7Data = FXCollections.observableArrayList();

    private double UInsulation = currentProtocol.getUinsulation();

    private volatile double iA;
    private volatile double coef = 500;
    private volatile double iAOld;
    private volatile double measuringUIn;
    private volatile int currentStage;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment7ModelPhase3BH = experimentsValuesModel.getExperiment7ModelPhase3BH();
        experiment7ModelPhase3HH = experimentsValuesModel.getExperiment7ModelPhase3HH();
        experiment7Data.add(experiment7ModelPhase3BH);
        experiment7Data.add(experiment7ModelPhase3HH);
        tableViewExperimentValues.setItems(experiment7Data);
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);


        tableColumnType.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        tableColumnU.setCellValueFactory(cellData -> cellData.getValue().UINProperty());
        tableColumnI.setCellValueFactory(cellData -> cellData.getValue().IBHProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResult.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
        currentProtocol.setE8TypeBHandCorps(experiment7ModelPhase3BH.getType());
        currentProtocol.setE8UBHandCorps(experiment7ModelPhase3BH.getUIN());
        currentProtocol.setE8IBHandCorps(experiment7ModelPhase3BH.getIBH());
        currentProtocol.setE8TimeBHandCorps(experiment7ModelPhase3BH.getTime());
        currentProtocol.setE8ResultBHandCorps(experiment7ModelPhase3BH.getResult());

        currentProtocol.setE8TypeHHandCorps(experiment7ModelPhase3HH.getType());
        currentProtocol.setE8UHHandCorps(experiment7ModelPhase3HH.getUIN());
        currentProtocol.setE8IHHandCorps(experiment7ModelPhase3HH.getIBH());
        currentProtocol.setE8TimeHHandCorps(experiment7ModelPhase3HH.getTime());
        currentProtocol.setE8ResultHHandCorps(experiment7ModelPhase3HH.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment7ModelPhase3BH.clearProperties();
        experiment7ModelPhase3HH.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isDeltaResponding = false;
        setDeviceState(deviceStateCircleDELTACP2000, View.DeviceState.UNDEFINED);


        isNeedToRefresh = true;

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            if (experimentsValuesModel.getExperiment7Choice() == ExperimentValuesModel.EXPERIMENT7_BOTH) {
                startBH();
                sleep(3000);
                startHH();
            } else if (experimentsValuesModel.getExperiment7Choice() == ExperimentValuesModel.EXPERIMENT7_BH) {
                startBH();
            } else {
                startHH();
            }
            finalizeExperiment();
        }).start();
    }

    private void startBH() {

        showRequestDialog("Подключите крокодилы к ВН и корпусу. После нажмите <Да>", true);

        if (isExperimentRunning) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
        }

        if (isExperimentRunning && !isOwenPRResponding) {
            appendOneMessageToLog("Нет связи с ПР");
            sleep(100);
        }

        if (isExperimentRunning && isThereAreAccidents()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            isStartButtonOn = false;
            sleep(1000);
        }

        while (isExperimentRunning && !isStartButtonOn) {
            appendOneMessageToLog("Включите кнопочный пост");
            sleep(1);
            isNeedToWaitDelta = true;
        }

        if (isExperimentRunning && isNeedToWaitDelta && isStartButtonOn) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(8000);
            communicationModel.initExperiment7Devices();
        }

        while (isExperimentRunning && !isDevicesResponding()) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(100);
        }


        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.initExperiment7Devices();
            communicationModel.onK9();
            communicationModel.onK8();
            communicationModel.onKM4();
            communicationModel.onKM12();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * HZ, 5 * VOLT, 50 * HZ);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            regulation(5 * VOLT, 40, 15, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            experiment7ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }
        experimentTime = 60;
        experiment7ModelPhase3BH.setTime(String.valueOf(experimentTime));
        currentStage = 3;

        isNeedToRefresh = false;
        communicationModel.stopObject();

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment7ModelPhase3BH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment7ModelPhase3BH.setResult("Неуспешно");
        } else {
            experiment7ModelPhase3BH.setResult("Успешно");
            appendMessageToLog("Испытание BH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");

    }

    private void startHH() {

        showRequestDialog("Подключите крокодилы к HН и корпусу. После нажмите <Да>", true);

        if (isExperimentRunning) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
        }

        if (isExperimentRunning && !isOwenPRResponding) {
            appendOneMessageToLog("Нет связи с ПР");
            sleep(100);
        }

        if (isExperimentRunning && isThereAreAccidents()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            isStartButtonOn = false;
            sleep(1000);
        }

        while (isExperimentRunning && !isStartButtonOn) {
            appendOneMessageToLog("Включите кнопочный пост");
            sleep(1);
            isNeedToWaitDelta = true;
        }

        if (isExperimentRunning && isNeedToWaitDelta && isStartButtonOn) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(8000);
            communicationModel.initExperiment7Devices();
        }

        while (isExperimentRunning && !isDevicesResponding()) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(100);
        }


        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onK9();
            communicationModel.onK8();
            communicationModel.onKM4();
            communicationModel.onKM12();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * HZ, 5 * VOLT, 50 * HZ);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            regulation(5 * VOLT, 40, 15, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            experiment7ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }
        experimentTime = 60;
        experiment7ModelPhase3HH.setTime(String.valueOf(experimentTime));
        currentStage = 3;

        isNeedToRefresh = false;
        communicationModel.stopObject();

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment7ModelPhase3HH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment7ModelPhase3HH.setResult("Неуспешно");
        } else {
            experiment7ModelPhase3HH.setResult("Успешно");
            appendMessageToLog("Испытание HH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");

    }

    @Override
    protected void finalizeExperiment() {
        isNeedToRefresh = false;
        sleep(100);

        appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        communicationModel.stopObject();
        sleep(3000);

        communicationModel.offAllKms();
        communicationModel.deinitPR();
        communicationModel.finalizeAllDevices();

        Platform.runLater(() -> {
            isExperimentRunning = false;
            isExperimentEnded = true;
            buttonCancelAll.setDisable(false);
            buttonStartStop.setText("Запустить");
            buttonStartStop.setDisable(false);
            buttonNext.setDisable(false);
        });

        if (!cause.isEmpty()) {
            if (isBHSelected) {
                if (experiment7ModelPhase3BH.getResult().isEmpty()) {
                    experiment7ModelPhase3BH.setResult("Прервано");
                }
            }
            if (isHHSelected) {
                if (experiment7ModelPhase3HH.getResult().isEmpty()) {
                    experiment7ModelPhase3HH.setResult("Прервано");
                }
            }
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
        } else if (!isStartButtonOn) {
            if (isBHSelected) {
                if (experiment7ModelPhase3BH.getResult().isEmpty()) {
                    experiment7ModelPhase3BH.setResult("Прервано");
                }
            }
            if (isHHSelected) {
                if (experiment7ModelPhase3HH.getResult().isEmpty()) {
                    experiment7ModelPhase3HH.setResult("Прервано");
                }
            }
            appendMessageToLog("Испытание прервано по причине: нажали кнопку <Стоп>");
        } else if (!isDevicesResponding()) {
            if (isBHSelected) {
                if (experiment7ModelPhase3BH.getResult().isEmpty()) {
                    experiment7ModelPhase3BH.setResult("Прервано");
                }
            }
            if (isHHSelected) {
                if (experiment7ModelPhase3HH.getResult().isEmpty()) {
                    experiment7ModelPhase3HH.setResult("Прервано");
                }
            }
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
        }
        appendMessageToLog("------------------------------------------------\n");

    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isPM130Responding && isDeltaResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isPM130Responding ? "" : "Парма ");
    }

    private int regulation(int start, int coarseStep, int fineStep, int end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentRunning && ((measuringUIn < end * coarseMinLimit) || (measuringUIn > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUIn < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUIn > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringUIn < end - fineLimit) || (measuringUIn > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUIn < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringUIn > end + fineLimit) {
                communicationModel.setObjectUMax(start -= fineStep);
            }
            sleep(fineSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения точно");
        }
        return start;
    }

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
            case PM130_ID:
                switch (param) {
                    case PM130Model.RESPONDING_PARAM:
                        isPM130Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PM130Model.I1_PARAM:
                        setI((float) value);
                        break;
                    case PM130Model.V1_PARAM:
                        setU((float) value);
                        break;
                }
                break;
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.RESPONDING_PARAM:
                        isOwenPRResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI6:
                        isStartButtonOn = (boolean) value;
                        break;
                }
                break;
            case DELTACP2000_ID:
                switch (param) {
                    case DeltaCP2000Model.RESPONDING_PARAM:
                        isDeltaResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                }
                break;
        }
    }

    private void setU(float value) {
        if (isNeedToRefresh) {
            measuringUIn = value * coef;
            switch (currentStage) {
                case 1:
                    experiment7ModelPhase3BH.setUIN(formatRealNumber(measuringUIn));
                    break;
                case 2:
                    experiment7ModelPhase3HH.setUIN(formatRealNumber(measuringUIn));
                    break;
            }
        }
    }

    private void setI(float value) {
        if (isNeedToRefresh) {
            iA = value * mA;
            switch (currentStage) {
                case 1:
                    experiment7ModelPhase3BH.setIBH(formatRealNumber(value * mA));
                    break;
                case 2:
                    experiment7ModelPhase3HH.setIBH(formatRealNumber(value * mA));
                    break;
            }
            if (iA > 1000.0) {
                cause = "ток превысил";
                isExperimentRunning = false;
            } else {
                iAOld = iA;
            }
        }
    }
}