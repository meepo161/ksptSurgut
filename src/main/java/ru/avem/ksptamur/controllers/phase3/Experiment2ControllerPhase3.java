package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.communication.devices.parmaT400.ParmaT400Model;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment2ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment2ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final double POWER = 100;


    @FXML
    private TableView<Experiment2ModelPhase3> tableViewExperiment2;
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
    @FXML
    private TextArea textAreaExperiment2Log;
    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;

    private MainModel mainModel = MainModel.getInstance();
    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private double UHHTestItem = currentProtocol.getUhh();
    private double coef = 1.16;

    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment2ModelPhase3 experiment2ModelPhase3;
    private ObservableList<Experiment2ModelPhase3> experiment2Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh = true;
    private volatile boolean isNeedToWaitDelta;
    private volatile boolean isExperimentRunning;
    private volatile boolean isExperimentEnd = true;
    private volatile boolean isStartButtonOn;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isParmaResponding;
    private volatile boolean isPM130Responding;
    private volatile boolean isPressedOk;

    private volatile boolean isDoorSHSO;
    private volatile boolean isDoorZone;
    private volatile boolean isCurrent;
    private volatile boolean isCurrentVIU;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private volatile String cause;
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
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment2ModelPhase3 = mainModel.getExperiment2ModelPhase3();
        experiment2Data.add(experiment2ModelPhase3);
        tableViewExperiment2.setItems(experiment2Data);
        tableViewExperiment2.setSelectionModel(null);
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
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }


    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
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

    @FXML
    private void handleNextExperiment() {
        fillProtocolExperimentFields();
        dialogStage.close();
    }

    @FXML
    private void handleStartExperiment() {
        if (isExperimentEnd) {
            startExperiment();
        } else {
            stopExperiment();
        }
    }

    @FXML
    private void handleExperimentCancel() {
        dialogStage.close();
        isCanceled = true;
    }

    private void stopExperiment() {
        isNeedToRefresh = false;
        buttonStartStop.setDisable(true);
        cause = "Отменено оператором";
        isExperimentRunning = false;
    }

    private void startExperiment() {
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);

        communicationModel.offAllKms();
        communicationModel.finalizeAllDevices();
        experiment2ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnd = false;

        isDeltaResponding = false;
        isParmaResponding = false;
        isPM130Responding = false;

        isCurrentVIU = true;

        isPressedOk = false;
        cause = "";

        new Thread(() -> {

            if (isExperimentRunning) {
                Platform.runLater(() -> {
                    View.showConfirmDialog("Подключите ОИ для определения Ктр: провода с маркировкой А-В-С (ШСО) к стороне ВН и А-В-С (стойка приборов) к НН",
                            () -> {
                                isPressedOk = true;
                                isNeedToRefresh = true;
                            },
                            () -> {
                                cause = "Отменено";
                                isExperimentRunning = false;
                                isPressedOk = false;
                            });
                });
            }

            while (!isPressedOk) {
                sleep(1);
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
            }

            if (isExperimentRunning && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
                isExperimentRunning = false;
            }

            if (isExperimentRunning && isThereAreAccidents()) {
                appendOneMessageToLog(getAccidentsString("Аварии"));
                isExperimentRunning = false;
            }

            if (isExperimentRunning && isOwenPRResponding) {
                appendOneMessageToLog("Инициализация кнопочного поста...");
            }

            while (isExperimentRunning && !isStartButtonOn) {
                appendOneMessageToLog("Включите кнопочный пост");
                sleep(1);
                isNeedToWaitDelta = true;
            }

            if (isExperimentRunning) {
                communicationModel.initExperiment2Devices();
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
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
                experiment2ModelPhase3.setUDiff(String.format("%.2f", measuringUInAvr / measuringUOutAvr));
            }

            isNeedToRefresh = false;
            isExperimentRunning = false;
            isExperimentEnd = true;
            communicationModel.stopObject();

            communicationModel.offAllKms();
            communicationModel.finalizeAllDevices();

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


            Platform.runLater(() -> {
                buttonStartStop.setText("Запустить");
                buttonStartStop.setDisable(false);
                buttonNext.setDisable(false);
                buttonCancelAll.setDisable(false);
            });
        }).start();
    }

    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperiment2Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private boolean isThereAreAccidents() {
        if (!isCurrentVIU) {
            isExperimentRunning = false;
            isExperimentEnd = true;
        }
        return !isCurrentVIU || isCanceled;
    }

    private String getAccidentsString(String mainText) {
        return String.format("%s: %s%s",
                mainText,
                isCurrentVIU ? "" : "сработала токовая защита 1, ",
                isCanceled ? "" : "нажата кнопка отмены, ");
    }

    private boolean isDevicesResponding() {
        return isOwenPRResponding && isParmaResponding && isPM130Responding;
    }

    private String getNotRespondingDevicesString(String mainText) {
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
                    case OwenPRModel.PRDI1:
//                        isDoorZone = (boolean) value;
//                        if (isDoorZone) {
//                            cause = "открыта дверь зоны";
//                            isExperimentRunning = false;
//                        }
                        break;
                    case OwenPRModel.PRDI2:
//                        isCurrentVIU = (boolean) value;
//                        if (isCurrentVIU) {
//                            cause = "открыта дверь шкафа";
//                            isExperimentRunning = false;
//                        }
                        break;
                    case OwenPRModel.PRDI3:
//                        isCurrent = (boolean) value;
//                        if (isCurrent) {
//                            cause = "сработала токовая защита";
//                            isExperimentRunning = false;
//                        }
                        break;
                    case OwenPRModel.PRDI4:
                        break;
                    case OwenPRModel.PRDI5:
                        break;
                    case OwenPRModel.PRDI6:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRDI6_FIXED:
                        break;
                    case OwenPRModel.PRDI7:
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
                            String freq = String.format("%.2f", measuringF);
                            experiment2ModelPhase3.setF(freq);
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                            String UInAB = String.format("%.2f", measuringUInAB);
                            if (measuringUInAB > 0.001) {
                                experiment2ModelPhase3.setUInputAB(UInAB);
                            }
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value;
                            String UInBC = String.format("%.2f", measuringUInBC);
                            if (measuringUInBC > 0.001) {
                                experiment2ModelPhase3.setUInputBC(UInBC);
                            }
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value;
                            String UInCA = String.format("%.2f", measuringUInCA);
                            if (measuringUInCA > 0.001) {
                                experiment2ModelPhase3.setUInputCA(UInCA);
                            }
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            String UInAvr = String.format("%.2f", measuringUInAvr);
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
                            String UOutAB = String.format("%.2f", measuringUOutAB);
                            experiment2ModelPhase3.setUOutputAB(UOutAB);
                        }
                        break;
                    case ParmaT400Model.UBC_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutBC = (double) value;
                            String UOutBC = String.format("%.2f", measuringUOutBC);
                            experiment2ModelPhase3.setUOutputBC(UOutBC);
                        }
                        break;
                    case ParmaT400Model.UCA_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutCA = (double) value;
                            String UOutCA = String.format("%.2f", measuringUOutCA);
                            experiment2ModelPhase3.setUOutputCA(UOutCA);
                            measuringUOutAvr = (int) (((measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0) * POWER) / POWER;
                            String UOutAvr = String.format("%.2f", measuringUOutAvr);
                            experiment2ModelPhase3.setUOutputAvr(UOutAvr);
                        }
                        break;
                }
                break;
        }
    }
}