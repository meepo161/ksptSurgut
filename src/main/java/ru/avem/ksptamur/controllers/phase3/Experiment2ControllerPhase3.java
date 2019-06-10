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
import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Model;
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
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
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
    private double UBHTestItem = currentProtocol.getUbh();
    private double UHHTestItem = currentProtocol.getUhh();

    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment2ModelPhase3 experiment2ModelPhase3;
    private ObservableList<Experiment2ModelPhase3> experiment2Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh = true;
    private volatile boolean isStartButtonOn;
    private volatile boolean isNeedToWaitDelta;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentStart;
    private volatile boolean isExperimentEnd = true;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isDeltaReady50;
    private volatile boolean isDeltaReady0;
    private volatile boolean isParmaResponding;
    private volatile boolean isPM130Responding;
    private volatile boolean isPressedOk;
    private volatile boolean isDeviceOn = false;

    private volatile boolean isDoorSHSO;
    private volatile boolean isDoorZone;
    private volatile boolean isCurrent;
    private volatile boolean isCurrentVIU;

    private boolean is200to5State;
    private boolean is40to5State;
    private boolean is5to5State;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private volatile String cause;
    private volatile double temperature;
    private volatile double iA;
    private volatile double iB;
    private volatile double iC;
    private volatile double fParma;
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
        isExperimentStart = false;
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
        isExperimentStart = true;
        isExperimentEnd = false;

        isDeltaResponding = false;
        isParmaResponding = false;
        isPM130Responding = false;

        isCurrentVIU = true;

        isPressedOk = false;
        cause = "";

        new Thread(() -> {

            if (isExperimentStart) {
                Platform.runLater(() -> {
                    View.showConfirmDialog("Подключите ОИ для определения Ктр: провода с маркировкой А-В-С (ШСО) к стороне ВН и А-В-С (стойка приборов) к НН",
                            () -> {
                                isPressedOk = true;
                                isNeedToRefresh = true;
                            },
                            () -> {
                                cause = "Отменено";
                                isExperimentStart = false;
                                isPressedOk = false;
                            });
                });
            }

            while (!isPressedOk) {
                sleep(1);
            }

            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
            }

            if (isExperimentStart && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
                isExperimentStart = false;
            }

            if (isExperimentStart && isThereAreAccidents()) {
                appendOneMessageToLog(getAccidentsString("Аварии"));
                isExperimentStart = false;
            }

            if (isExperimentStart && isOwenPRResponding) {
                appendOneMessageToLog("Инициализация кнопочного поста...");
            }

            while (isExperimentStart && !isStartButtonOn) {
                appendOneMessageToLog("Включите кнопочный пост");
                sleep(1);
                isNeedToWaitDelta = true;
            }

            if (isExperimentStart) {
                appendOneMessageToLog("Идет загрузка ЧП");
            }

            if (isExperimentStart && isNeedToWaitDelta) {
                sleep(8000);
            }

            if (isExperimentStart) {
                communicationModel.initExperiment2Devices();
            }

            while (isExperimentStart && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                is200to5State = true;
                if (isExperimentStart && UHHTestItem < WIDDING400) {
                    communicationModel.onKM1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 418В");
                } else if (isExperimentStart && UHHTestItem > WIDDING400) {
                    communicationModel.onKM2();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
                }
                communicationModel.onKM4();
                communicationModel.onKM1M1();
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                sleep(3000);
                communicationModel.setObjectParams(50 * 100, 5 * 10, 50 * 100);
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog("Запускаем ЧП");
            }

            while (isExperimentStart && !isDeltaReady50 && isStopButtonOn) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь выйдет к заданным характеристикам");
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItem);
                regulation(5 * 10, 30, 5, UHHTestItem, 0.1, 2, 100, 200);
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                sleep(4000);
                isNeedToRefresh = false;
                experiment2ModelPhase3.setUDiff(String.valueOf(((int) ((measuringUInAvr / measuringUOutAvr * POWER)) / POWER)));
            }

            isNeedToRefresh = false;
            isDeviceOn = false;
            isExperimentStart = false;
            isExperimentEnd = true;
            sleep(500);
            communicationModel.stopObject();

            while (isExperimentStart && !isDeltaReady0 && isDeltaResponding) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }

            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

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
            appendMessageToLog("\n------------------------------------------------\n");


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
            isExperimentStart = false;
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
        return isOwenPRResponding && isParmaResponding && isPM130Responding && isDeltaResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isParmaResponding ? "" : "Парма ",
                isPM130Responding ? "" : "ПМ130 ");
    }

    private int regulation(int start, int coarseStep, int fineStep, double end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentStart && ((measuringUInAvr < end * coarseMinLimit) || (measuringUInAvr > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUInAvr < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUInAvr > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentStart && ((measuringUInAvr < end - fineLimit) || (measuringUInAvr > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUInAvr < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringUInAvr > end + fineLimit) {
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
//                            isExperimentStart = false;
//                        }
                        break;
                    case OwenPRModel.PRDI2:
//                        isCurrentVIU = (boolean) value;
//                        if (isCurrentVIU) {
//                            cause = "открыта дверь шкафа";
//                            isExperimentStart = false;
//                        }
                        break;
                    case OwenPRModel.PRDI3:
//                        isCurrent = (boolean) value;
//                        if (isCurrent) {
//                            cause = "сработала токовая защита";
//                            isExperimentStart = false;
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
//                            isExperimentStart = false;
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
            case DELTACP2000_ID:
                switch (param) {
                    case DeltaCP2000Model.RESPONDING_PARAM:
                        isDeltaResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));

                        break;
                    case DeltaCP2000Model.CURRENT_FREQUENCY_PARAM:
                        setCurrentFrequencyObject((short) value);
                        break;
                }
                break;
        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 5000;
        isDeltaReady0 = value == 0;
    }
}