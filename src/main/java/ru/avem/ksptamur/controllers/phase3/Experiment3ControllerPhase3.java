package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptamur.communication.devices.parmaT400.ParmaT400Model;
import ru.avem.ksptamur.communication.devices.phasemeter.PhaseMeterModel;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.phase3.Experiment3ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.formatRealNumber;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment3ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 400;

    @FXML
    private TableView<Experiment3ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnGroupBH;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnGroupHH;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnResultExperiment3;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUBH;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUHH;

    private double UHHTestItem = currentProtocol.getUhh();

    private Experiment3ModelPhase3 experiment3ModelPhase3;
    private ObservableList<Experiment3ModelPhase3> experiment3Data = FXCollections.observableArrayList();

    private volatile boolean isNeedToWaitDelta;
    private volatile int windingGroup0;
    private volatile int windingGroup1;
    private volatile double measuringUOutAB;
    private volatile double measuringUOutBC;
    private volatile double measuringUOutCA;
    private volatile double measuringUOutAvr;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment3ModelPhase3 = experimentsValuesModel.getExperiment3ModelPhase3();
        experiment3Data.add(experiment3ModelPhase3);
        experiment3ModelPhase3 = experimentsValuesModel.getExperiment3ModelPhase3();
        tableViewExperimentValues.setItems(experiment3Data);
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnGroupBH.setCellValueFactory(cellData -> cellData.getValue().groupBHProperty());
        tableColumnGroupHH.setCellValueFactory(cellData -> cellData.getValue().groupHHProperty());
        tableColumnUBH.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnUHH.setCellValueFactory(cellData -> cellData.getValue().UHHProperty());
        tableColumnResultExperiment3.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
        currentProtocol.setE4WindingBH(experiment3ModelPhase3.getGroupBH());
        currentProtocol.setE4WindingHH(experiment3ModelPhase3.getGroupHH());
        currentProtocol.setE4UBH(experiment3ModelPhase3.getUBH());
        currentProtocol.setE4UHH(experiment3ModelPhase3.getUHH());
        currentProtocol.setE4Result(experiment3ModelPhase3.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment3ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isParmaResponding = false;
        setDeviceState(deviceStateCirclePM130, View.DeviceState.UNDEFINED);
        isPhaseMeterResponding = false;
        setDeviceState(deviceStateCirclePhaseMeter, View.DeviceState.UNDEFINED);

        isNeedToWaitDelta = false;

        isNeedToRefresh = true;

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            showRequestDialog("Подключите ОИ для определения ГС. После нажмите <Да>", true);

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment3Devices();
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

            while (isExperimentRunning && !isStartButtonOn) {
                appendOneMessageToLog("Включите кнопочный пост");
                sleep(1);
                isNeedToWaitDelta = true;
            }

            if (isExperimentRunning && isNeedToWaitDelta && isStartButtonOn) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(6000);
                communicationModel.initExperiment3Devices();
                sleep(3000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment3Devices();
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                if (isExperimentRunning && UHHTestItem < WIDDING400) {
                    communicationModel.onKM11();
                    communicationModel.onKM5();
                    communicationModel.onKM13();
                    sleep(5000);
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с HH до 418В");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный HH в объекте испытания.");
                    isExperimentRunning = false;
                }
            }

            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.startPhaseMeter();
                appendOneMessageToLog("Началось измерение");
                sleep(2000);
                experiment3ModelPhase3.setGroupBH(String.valueOf(windingGroup0));
                experiment3ModelPhase3.setGroupHH(String.valueOf(windingGroup1));
                appendOneMessageToLog("Измерение завершено");
            }

            finalizeExperiment();
        }).start();
    }


    @Override
    protected void finalizeExperiment() {
        isNeedToRefresh = false;
        sleep(100);

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

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment3ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment3ModelPhase3.setResult("Неуспешно");
        } else {
            experiment3ModelPhase3.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isParmaResponding && isPM130Responding && isPhaseMeterResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isParmaResponding ? "" : "Парма ",
                isPM130Responding ? "" : "ПМ130 ",
                isPhaseMeterResponding ? "" : "Фазометр ");
    }

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.RESPONDING_PARAM:
                        isOwenPRResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI1:
                        break;
                    case OwenPRModel.PRI2:
                        break;
                    case OwenPRModel.PRI3:
                        break;
                    case OwenPRModel.PRI4:
                        break;
                    case OwenPRModel.PRI5:
                        break;
                    case OwenPRModel.PRI6:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRI6_FIXED:
                        break;
                    case OwenPRModel.PRI7:
                        break;
                }
                break;
            case PM130_ID:
                switch (param) {
                    case PM130Model.RESPONDING_PARAM:
                        isPM130Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutAB = (float) value;
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutBC = (float) value;
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutCA = (float) value;
                            measuringUOutAvr = (measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0;
                            String UOutAvr = formatRealNumber(measuringUOutAvr);
                            experiment3ModelPhase3.setUBH(UOutAvr);
                            sleep(100);
                        }
                        break;
                }
                break;
            case PARMA400_ID:
                switch (param) {
                    case ParmaT400Model.RESPONDING_PARAM:
                        isParmaResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleParma400.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (double) value;
                        }
                        break;
                    case ParmaT400Model.UBC_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (double) value;
                        }
                        break;
                    case ParmaT400Model.UCA_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (double) value;
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            String UInAvr = formatRealNumber(measuringUInAvr);
                            if (measuringUInAvr > 0.001) {
                                experiment3ModelPhase3.setUHH(UInAvr);
                                sleep(100);
                            }
                        }
                        break;
                }
                break;
            case PHASEMETER_ID:
                switch (param) {
                    case PhaseMeterModel.RESPONDING_PARAM:
                        isPhaseMeterResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePhaseMeter.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PhaseMeterModel.WINDING_GROUP0_PARAM:
                        windingGroup0 = (short) value;
                        break;
                    case PhaseMeterModel.WINDING_GROUP1_PARAM:
                        windingGroup1 = (short) value;
                        break;
                }
                break;
        }
    }
}