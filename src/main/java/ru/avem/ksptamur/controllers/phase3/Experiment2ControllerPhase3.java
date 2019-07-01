package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.communication.devices.parmaT400.ParmaT400Model;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.model.phase3.Experiment2ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.formatRealNumber;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment2ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 400;

    @FXML
    private TableView<Experiment2ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUInputAB;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUInputBC;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUInputCA;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUInputAvr;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUOutputAB;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUOutputBC;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUOutputCA;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUOutputAvr;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnUDiff;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnResultExperiment2;

    private double UHHTestItem = currentProtocol.getUhh();

    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment2ModelPhase3 experiment2ModelPhase3;
    private ObservableList<Experiment2ModelPhase3> experiment2Data = FXCollections.observableArrayList();

    private volatile boolean isNeedToWaitDelta;

    private volatile double measuringUOutAB;
    private volatile double measuringUOutBC;
    private volatile double measuringUOutCA;
    private volatile double measuringUOutAvr;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double measuringF;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment2ModelPhase3 = experimentsValuesModel.getExperiment2ModelPhase3();
        experiment2Data.add(experiment2ModelPhase3);
        tableViewExperimentValues.setItems(experiment2Data);
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnUOutputAB.setCellValueFactory(cellData -> cellData.getValue().UOutputABProperty());
        tableColumnUOutputBC.setCellValueFactory(cellData -> cellData.getValue().UOutputBCProperty());
        tableColumnUOutputCA.setCellValueFactory(cellData -> cellData.getValue().UOutputCAProperty());
        tableColumnUOutputAvr.setCellValueFactory(cellData -> cellData.getValue().UOutputAvrProperty());
        tableColumnUInputAB.setCellValueFactory(cellData -> cellData.getValue().UInputABProperty());
        tableColumnUInputBC.setCellValueFactory(cellData -> cellData.getValue().UInputBCProperty());
        tableColumnUInputCA.setCellValueFactory(cellData -> cellData.getValue().UInputCAProperty());
        tableColumnUInputAvr.setCellValueFactory(cellData -> cellData.getValue().UInputAvrProperty());
        tableColumnUDiff.setCellValueFactory(cellData -> cellData.getValue().UDiffProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().fProperty());
        tableColumnResultExperiment2.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE2UInputAB(experiment2ModelPhase3.getUInputAB());
        currentProtocol.setE2UInputBC(experiment2ModelPhase3.getUInputBC());
        currentProtocol.setE2UInputCA(experiment2ModelPhase3.getUInputCA());
        currentProtocol.setE2UInputAvr(experiment2ModelPhase3.getUInputAvr());

        currentProtocol.setE2UOutputAB(experiment2ModelPhase3.getUOutputAB());
        currentProtocol.setE2UOutputBC(experiment2ModelPhase3.getUOutputBC());
        currentProtocol.setE2UOutputCA(experiment2ModelPhase3.getUOutputCA());
        currentProtocol.setE2UOutputAvr(experiment2ModelPhase3.getUOutputAvr());

        currentProtocol.setE2DiffU(experiment2ModelPhase3.getUDiff());
        currentProtocol.setE2F(experiment2ModelPhase3.getF());
        currentProtocol.setE2Result(experiment2ModelPhase3.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment2ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isParmaResponding = false;
        setDeviceState(deviceStateCirclePM130, View.DeviceState.UNDEFINED);


        isNeedToRefresh = true;

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            showRequestDialog("Подключите ОИ для определения КТР. После нажмите <Да>", true);

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment2Devices();
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
                communicationModel.initExperiment2Devices();
                sleep(3000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment2Devices();
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
                isNeedToRefresh = false;
                experiment2ModelPhase3.setUDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
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
            buttonStartStop.setText("Запустить повторно");
            buttonStartStop.setDisable(false);
            buttonNext.setDisable(false);
        });

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment2ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment2ModelPhase3.setResult("Неуспешно");
        } else {
            experiment2ModelPhase3.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isParmaResponding && isPM130Responding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isParmaResponding ? "" : "Парма ",
                isPM130Responding ? "" : "ПМ130 ");
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
//                        isDoorZone = (boolean) value;
//                        if (isDoorZone) {
//                            cause = "открыта дверь зоны";
//                            isExperimentRunning = false;
//                        }
                        break;
                    case OwenPRModel.PRI2:
//                        isCurrentVIU = (boolean) value;
//                        if (isCurrentVIU) {
//                            cause = "открыта дверь шкафа";
//                            isExperimentRunning = false;
//                        }
                        break;
                    case OwenPRModel.PRI3:
//                        isCurrent = (boolean) value;
//                        if (isCurrent) {
//                            cause = "сработала токовая защита";
//                            isExperimentRunning = false;
//                        }
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
//                        isCurrentVIU = (boolean) value;
//                        if (isCurrentVIU) {
//                            cause = "сработала токовая защита ВИУ";
//                            isExperimentRunning = false;
//                        }
                        break;
                }
                break;
            case PM130_ID:
                switch (param) {
                    case PM130Model.RESPONDING_PARAM:
                        isPM130Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            measuringF = (float) value;
                            String freq = formatRealNumber(measuringF);
                            experiment2ModelPhase3.setF(freq);
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                            String UInAB = formatRealNumber(measuringUInAB);
                            if (measuringUInAB > 0.001) {
                                experiment2ModelPhase3.setUInputAB(UInAB);
                            }
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value;
                            String UInBC = formatRealNumber(measuringUInBC);
                            if (measuringUInBC > 0.001) {
                                experiment2ModelPhase3.setUInputBC(UInBC);
                            }
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value;
                            String UInCA = formatRealNumber(measuringUInCA);
                            if (measuringUInCA > 0.001) {
                                experiment2ModelPhase3.setUInputCA(UInCA);
                            }
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            String UInAvr = formatRealNumber(measuringUInAvr);
                            if (measuringUInAvr > 0.001) {
                                experiment2ModelPhase3.setUInputAvr(UInAvr);
                            }
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
                            measuringUOutAB = (double) value;
                            String UOutAB = formatRealNumber(measuringUOutAB);
                            experiment2ModelPhase3.setUOutputAB(UOutAB);
                        }
                        break;
                    case ParmaT400Model.UBC_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutBC = (double) value;
                            String UOutBC = formatRealNumber(measuringUOutBC);
                            experiment2ModelPhase3.setUOutputBC(UOutBC);
                        }
                        break;
                    case ParmaT400Model.UCA_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutCA = (double) value;
                            String UOutCA = formatRealNumber(measuringUOutCA);
                            experiment2ModelPhase3.setUOutputCA(UOutCA);
                            measuringUOutAvr = (measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0;
                            experiment2ModelPhase3.setUOutputAvr(formatRealNumber(measuringUOutAvr));
                        }
                        break;
                }
                break;
        }
    }
}