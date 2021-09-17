
package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptsurgut.communication.devices.avem_voltmeter.AvemVoltmeterModel;
import ru.avem.ksptsurgut.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.model.phase3.Experiment7ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;

import static ru.avem.ksptsurgut.Constants.Measuring.HZ;
import static ru.avem.ksptsurgut.Constants.Measuring.VOLT;
import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;
import static ru.avem.ksptsurgut.utils.Utils.formatRealNumber;
import static ru.avem.ksptsurgut.utils.Utils.sleep;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;

public class Experiment7ControllerPhase3 extends AbstractExperiment {
    private static final double mA = 1000;

    @FXML
    private TableView<Experiment7ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnType;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnUGiven;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnUAVEM;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnI;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnResult;

    private Experiment7ModelPhase3 experiment7ModelPhase3BH = experimentsValuesModel.getExperiment7ModelPhase3BH();
    private Experiment7ModelPhase3 experiment7ModelPhase3HH = experimentsValuesModel.getExperiment7ModelPhase3HH();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment7Choice() & 0b1) > 0;
    private boolean isBHStarted;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment7Choice() & 0b10) > 0;
    private boolean isHHStarted;

    private ObservableList<Experiment7ModelPhase3> experiment7Data = FXCollections.observableArrayList();

    private double UInsulation = currentProtocol.getUinsulation();

    private double coefAvem = 488;
    private double coef = 250;

    private volatile double iA;
    private volatile double iAOld;
    private volatile double measuringUAvem;
    private volatile double measuringUIn;
    private volatile double coefTransformationRatio;


    private int timeOut = 0;

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
        tableColumnUGiven.setCellValueFactory(cellData -> cellData.getValue().UGivenProperty());
        tableColumnUAVEM.setCellValueFactory(cellData -> cellData.getValue().UAVEMProperty());
        tableColumnI.setCellValueFactory(cellData -> cellData.getValue().IBHProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResult.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();

        currentProtocol.setE7TypeBHandCorps(experiment7ModelPhase3BH.getType());
        currentProtocol.setE7UGiven(experiment7ModelPhase3BH.getUGiven());
        currentProtocol.setE7UBHAvem(experiment7ModelPhase3BH.getUAVEM());
        currentProtocol.setE7IBHandCorps(experiment7ModelPhase3BH.getIBH());
        currentProtocol.setE7TimeBHandCorps(experiment7ModelPhase3BH.getTime());
        currentProtocol.setE7ResultBHandCorps(experiment7ModelPhase3BH.getResult());

        currentProtocol.setE7TypeHHandCorps(experiment7ModelPhase3HH.getType());
        currentProtocol.setE7UGiven(experiment7ModelPhase3HH.getUGiven());
        currentProtocol.setE7UHHAvem(experiment7ModelPhase3HH.getUAVEM());
        currentProtocol.setE7IHHandCorps(experiment7ModelPhase3HH.getIBH());
        currentProtocol.setE7TimeHHandCorps(experiment7ModelPhase3HH.getTime());
        currentProtocol.setE7ResultHHandCorps(experiment7ModelPhase3HH.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        isBHStarted = false;
        isHHStarted = false;

        experiment7ModelPhase3BH.clearProperties();
        experiment7ModelPhase3HH.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isDeltaResponding = false;
        setDeviceState(deviceStateCircleDELTACP2000, View.DeviceState.UNDEFINED);
        isAvemResponding = false;
        setDeviceState(deviceStateCircleAVEM, View.DeviceState.UNDEFINED);


        isNeedToRefresh = true;

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            if (isBHSelected) {
                startBH();
            }
            if (isHHSelected) {
                startHH();
            }
            finalizeExperiment();
        }).start();
    }

    private void startBH() {

        showRequestDialog("Отсоедините все провода и кабели от ОИ.\n" +
                "Подключите провод ИОМ к ВН.\n" +
                "После нажмите <Да>", true);

        if (isExperimentRunning) {
            experiment7ModelPhase3BH.setUGiven(String.valueOf(currentProtocol.getUinsulation()));
            isBHStarted = true;
        }

        if (isExperimentRunning) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
            communicationModel.initExperiment7Devices();
            sleep(2000);
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
        }

        timeOut = 30;
        while (isExperimentRunning && !isStartButtonOn && timeOut-- > 0) {
            appendOneMessageToLog("Включите кнопочный пост");
            sleep(1000);
            isNeedToWaitDelta = true;
        }

        if (isExperimentRunning && isNeedToWaitDelta && isStartButtonOn) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(6000);
            communicationModel.initExperiment7Devices();
        }

        timeOut = 30;
        while (isExperimentRunning && !isDevicesResponding() && timeOut-- > 0) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(1000);
            communicationModel.initExperiment7Devices();
        }


        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onK10();
            communicationModel.onK9();
            communicationModel.onK8();
//            communicationModel.onKM4();
            communicationModel.onKM12();
            sleep(1000);
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * HZ, 1 * VOLT, 50 * HZ);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
            sleep(1000);
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            communicationModel.setObjectUMax(5 * VOLT);
            sleep(500);
            communicationModel.setObjectUMax(15 * VOLT);
            sleep(500);
            communicationModel.setObjectUMax(25 * VOLT);
            sleep(1000);
            if (coefTransformationRatio < 0.7 && coefTransformationRatio > 1.7) {
                setCause("Коэффициент трансформации выходит за пределы");
            }
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            regulation(25 * VOLT, 6, 2, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            experiment7ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }
        experimentTime = 60;
        if (isExperimentRunning) {
            experiment7ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }
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

        isBHStarted = false;
        isNeedToRefresh = false;

        appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        communicationModel.stopObject();

        int time = 300;
        while ((time-- > 0)) {
            sleep(10);
        }

        communicationModel.offPR3M1();
        time = 300;
        while (isExperimentRunning && (time-- > 0)) {
            sleep(10);
        }
        communicationModel.offAllKms();
        communicationModel.deinitPR();
        communicationModel.finalizeAllDevices();

    }

    private void startHH() {

        showRequestDialog("Отсоедините все провода и кабели от ОИ.\n" +
                "Подключите провод ИОМ к НН.\n" +
                "После нажмите <Да>", true);

        isNeedToRefresh = true;

        if (isExperimentRunning) {
            experiment7ModelPhase3HH.setUGiven(String.valueOf(currentProtocol.getUinsulation()));
            isHHStarted = true;
        }

        if (isExperimentRunning) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
            communicationModel.initExperiment7Devices();
            sleep(2000);
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
        }

        timeOut = 30;
        while (isExperimentRunning && !isStartButtonOn && timeOut-- > 0) {
            appendOneMessageToLog("Включите кнопочный пост");
            sleep(1000);
            isNeedToWaitDelta = true;
        }

        if (isExperimentRunning && isNeedToWaitDelta && isStartButtonOn) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(6000);
            communicationModel.initExperiment7Devices();
        }

        timeOut = 30;
        while (isExperimentRunning && !isDevicesResponding() && timeOut-- > 0) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(1000);
            communicationModel.initExperiment7Devices();
        }


        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onK10();
            communicationModel.onK9();
            communicationModel.onK8();
//            communicationModel.onKM4();
            communicationModel.onKM12();
            sleep(1000);
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * HZ, 1 * VOLT, 50 * HZ);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
            sleep(1000);
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            communicationModel.setObjectUMax(5 * VOLT);
            sleep(500);
            communicationModel.setObjectUMax(15 * VOLT);
            sleep(500);
            communicationModel.setObjectUMax(25 * VOLT);
            sleep(1000);
            if (coefTransformationRatio < 0.7 && coefTransformationRatio > 1.7) {
                setCause("Коэффициент трансформации выходит за пределы");
            }
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            regulation(25 * VOLT, 6, 2, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            experiment7ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }
        experimentTime = 60;
        if (isExperimentRunning) {
            experiment7ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }
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

        isHHStarted = false;
        finalizeExperiment();
    }

    @Override
    protected void finalizeExperiment() {
        isNeedToRefresh = false;

        appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        communicationModel.stopObject();

        int time = 300;
        while ((time-- > 0)) {
            sleep(10);
        }

        communicationModel.offPR3M1();
        time = 300;
        while (isExperimentRunning && (time-- > 0)) {
            sleep(10);
        }
        communicationModel.offAllKms();
        communicationModel.deinitPR();
        communicationModel.finalizeAllDevices();

        Platform.runLater(() -> {
            isExperimentRunning = false;
            isExperimentEnded = true;
            buttonCancelAll.setDisable(false);
            buttonStartStop.setText("Запустить повторно");
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
                isPM130Responding ? "" : "ПМ130 ");
    }

    private int regulation(int start, int coarseStep, int fineStep, int end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentRunning && ((measuringUAvem < end * coarseMinLimit) || (measuringUAvem > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUAvem < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUAvem > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringUAvem < end - fineLimit) || (measuringUAvem > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUAvem < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringUAvem > end + fineLimit) {
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
                        measuringUIn = ((float) value);
                        break;
                }
                break;
            case AVEM_ID:
                switch (param) {
                    case AvemVoltmeterModel.RESPONDING_PARAM:
                        isAvemResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleAVEM.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case AvemVoltmeterModel.U_PARAM:
                        if (isNeedToRefresh) {
                            setUAvem((float) value);
                            coefTransformationRatio = measuringUIn / measuringUAvem;
                        }
                        break;
                }
                break;
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.RESPONDING_PARAM:
                        isOwenPRResponding = (boolean) value;
                        setDeviceState(deviceStateCirclePR200, (isOwenPRResponding) ? View.DeviceState.RESPONDING : View.DeviceState.NOT_RESPONDING);
                        break;
                    case OwenPRModel.PRI1_FIXED:
                        isDoorZone = (boolean) value;
                        if (!isDoorZone) {
                            setCause("открыты двери зоны");
                        }
                        break;
                    case OwenPRModel.PRI2_FIXED:
                        isDoorSHSO = (boolean) value;
                        if (!isDoorSHSO) {
                            setCause("открыты двери ШСО");
                        }
                        break;
                    case OwenPRModel.PRI3_FIXED:
                        isCurrentOI = (boolean) value;
                        if (!isCurrentOI) {
                            setCause("токовая защита ОИ");
                        }
                        break;
                    case OwenPRModel.PRI4_FIXED:
                        isCurrentVIU = (boolean) value;
                        if (!isCurrentVIU) {
                            setCause("токовая защита ВИУ");
                        }
                        break;
                    case OwenPRModel.PRI5_FIXED:
                        isCurrentInput = (boolean) value;
                        if (!isCurrentInput) {
                            setCause("токовая защита по входу");
                        }
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

    private void setUAvem(double value) {
        if (isNeedToRefresh) {
            measuringUAvem = value * coefAvem;
            if (isBHStarted) {
                experiment7ModelPhase3BH.setUAVEM(formatRealNumber(measuringUAvem));
            }
            if (isHHStarted) {
                experiment7ModelPhase3HH.setUAVEM(formatRealNumber(measuringUAvem));
            }
        }
    }

    private void setI(float value) {
        if (isNeedToRefresh) {
            iA = value * mA;
            if (isBHStarted) {
                experiment7ModelPhase3BH.setIBH(formatRealNumber(value * mA));
            }
            if (isHHStarted) {
                experiment7ModelPhase3HH.setIBH(formatRealNumber(value * mA));
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