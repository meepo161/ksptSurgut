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
import ru.avem.ksptamur.communication.devices.phasemeter.PhaseMeterModel;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment4ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment4ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final float STATE_1_TO_5_MULTIPLIER = 1f / 5f;
    private static final float STATE_10_TO_5_MULTIPLIER = 10f / 5f;
    private static final float STATE_75_TO_5_MULTIPLIER = 75f / 5f;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment4ModelPhase3> tableViewExperiment4;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnGroupBH;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnGroupHH;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnResultExperiment4;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUBH;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUHH;
    @FXML
    private TextArea textAreaExperiment4Log;
    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;

    private MainModel mainModel = MainModel.getInstance();
    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private double UBHTestItem = currentProtocol.getUbh();
    private double coef;
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment4ModelPhase3 experiment4ModelPhase3;
    private ObservableList<Experiment4ModelPhase3> experiment4Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentStart;
    private volatile boolean isExperimentEnd = true;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isDeltaReady50;
    private volatile boolean isDeltaReady0;
    private volatile boolean isParmaResponding;
    private volatile boolean isPM130Responding;
    private volatile boolean isPhaseMeterResponding;
    private volatile boolean isDeviceOn = false;

    private volatile boolean isCurrent1On;
    private volatile boolean isCurrent2On;
    private volatile boolean isDoorLockOn;
    private volatile boolean isInsulationOn;
    private volatile boolean isDoorZoneOn;

    private boolean is75to5State;
    private boolean is10to5State;
    private boolean is1to5State;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private volatile String cause;
    private volatile double temperature;
    private volatile double iA;
    private volatile double iB;
    private volatile double iC;
    private volatile double fParma;
    private volatile int phaseMeterState;
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
        experiment4ModelPhase3 = mainModel.getExperiment4ModelPhase3();
        experiment4Data.add(experiment4ModelPhase3);
        experiment4ModelPhase3 = mainModel.getExperiment4ModelPhase3();
        tableViewExperiment4.setItems(experiment4Data);
        tableViewExperiment4.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnGroupBH.setCellValueFactory(cellData -> cellData.getValue().groupBHProperty());
        tableColumnGroupHH.setCellValueFactory(cellData -> cellData.getValue().groupHHProperty());
        tableColumnUBH.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnUHH.setCellValueFactory(cellData -> cellData.getValue().UHHProperty());
        tableColumnResultExperiment4.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @FXML
    private void handleExperimentCancel() {
        dialogStage.close();
        isCanceled = true;
    }

    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE4WindingBH(experiment4ModelPhase3.getGroupBH());
        currentProtocol.setE4WindingHH(experiment4ModelPhase3.getGroupHH());
        currentProtocol.setE4UBH(experiment4ModelPhase3.getUBH());
        currentProtocol.setE4UHH(experiment4ModelPhase3.getUHH());
        currentProtocol.setE4Result(experiment4ModelPhase3.getResult());
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

    private void stopExperiment() {
        isNeedToRefresh = false;
        buttonStartStop.setDisable(true);
        cause = "Отменено оператором";
        isExperimentStart = false;
    }


    private void startExperiment() {
        isCurrent1On = true;
        isCurrent2On = true;
        isDoorLockOn = true;
        isInsulationOn = true;
        isDoorZoneOn = true;
        isNeedToRefresh = true;
        isExperimentStart = true;
        isExperimentEnd = false;
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        experiment4ModelPhase3.clearProperties();
        isDeltaResponding = false;
        isParmaResponding = false;
        isPM130Responding = false;
        isPhaseMeterResponding = false;
        cause = "";

        new Thread(() -> {

            if (isExperimentStart) {
                AtomicBoolean isPressed = new AtomicBoolean(false);
                Platform.runLater(() -> {
                    View.showConfirmDialog("Подключите ОИ для определения группы соединений: провода с маркировкой А-В-С (ШСО) к стороне ВН и А-В-С (стойка приборов) к НН",
                            () -> {
                                isPressed.set(true);
                            },
                            () -> {
                                cause = "Отменено";
                                isExperimentStart = false;
                                isPressed.set(true);
                            });
                });
            }

            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                sleep(3000);
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
            }

            if (isExperimentStart) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(8000);
                communicationModel.initExperiment4Devices();
            }

            while (isExperimentStart && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            isDeviceOn = true;

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                is75to5State = true;
                if (isExperimentStart && UBHTestItem < WIDDING400) {
                    communicationModel.onKM1();
                    coef = 1;
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 418В");
                } else if (isExperimentStart && UBHTestItem > WIDDING400) {
                    communicationModel.onKM2();
                    communicationModel.onKM2M2();
                    coef = 3.158;
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
                }
                communicationModel.onKM4();
                communicationModel.onKM1M1();
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
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
                if (UBHTestItem <= WIDDING400) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UBHTestItem);
                    regulation(5 * 10, 30, 7, UBHTestItem, 0.10, 2, 100, 100);
                } else if (UBHTestItem > WIDDING400) {
                    coef = 3.158;
                    appendOneMessageToLog("Поднимаем напряжение до " + UBHTestItem);
                    regulation(5 * 10, 30, 7, UBHTestItem, 0.10, 2, 100, 100);
                }
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                communicationModel.startPhaseMeter();
                appendOneMessageToLog("Началось измерение");
                sleep(2000);
                experiment4ModelPhase3.setGroupBH(String.valueOf(windingGroup0));
                experiment4ModelPhase3.setGroupHH(String.valueOf(windingGroup1));
                appendOneMessageToLog("Измерение завершено");
                isNeedToRefresh = false;
                sleep(1000);
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
                experiment4ModelPhase3.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                experiment4ModelPhase3.setResult("Неуспешно");
            } else {
                experiment4ModelPhase3.setResult("Успешно");
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
        Platform.runLater(() -> textAreaExperiment4Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private boolean isThereAreAccidents() {
        if (!isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn) {
            isExperimentStart = false;
            isExperimentEnd = true;
        }
        return !isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn;
    }

    private String getAccidentsString(String mainText) {
        return String.format("%s: %s%s%s%s%s%s",
                mainText,
                isCurrent1On ? "" : "сработала токовая защита 1, ",
                isCurrent2On ? "" : "сработала токовая защита 2, ",
                isDoorLockOn ? "" : "открылась дверь, ",
                isInsulationOn ? "" : "обрыв изоляции, ",
                isCanceled ? "" : "нажата кнопка отмены, ",
                isDoorZoneOn ? "" : "открылась дверь зоны");
    }

    private boolean isDevicesResponding() {
        return isOwenPRResponding && isParmaResponding && isPM130Responding &&
                isDeltaResponding && isPhaseMeterResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isParmaResponding ? "" : "Парма ",
                isPM130Responding ? "" : "ПМ130 ",
                isPhaseMeterResponding ? "" : "Фазометр ");
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
                        break;
                    case OwenPRModel.PRDI2:
                        break;
                    case OwenPRModel.PRDI3:
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
                            measuringUOutAvr = (int) (((measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0) * POWER) / POWER;
                            String UOutAvr = String.format("%.2f", measuringUOutAvr);
                            experiment4ModelPhase3.setUHH(UOutAvr);
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
                            measuringUInAB = (double) value * coef;
                        }
                        break;
                    case ParmaT400Model.UBC_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (double) value * coef;
                        }
                        break;
                    case ParmaT400Model.UCA_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (double) value * coef;
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            String UInAvr = String.format("%.2f", measuringUInAvr);
                            if (measuringUInAvr > 0.001) {
                                experiment4ModelPhase3.setUBH(UInAvr);
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
            case DELTACP2000_ID:
                switch (param) {
                    case DeltaCP2000Model.RESPONDING_PARAM:
                        isDeltaResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));

                        break;
                    case DeltaCP2000Model.CURRENT_FREQUENCY_PARAM:
                        setCurrentFrequencyObject((short) value);
                        break;
//                    case DeltaCP2000Model.ERROR1_PARAM:
//                        if ((short) value > 0) {
//                            appendMessageToLog(String.valueOf(value));
//                        }
//
//                        break;
//                    case DeltaCP2000Model.ERROR2_PARAM:
//                        if ((short) value > 0) {
//                            appendMessageToLog(String.valueOf(value));
//                        }
//                        break;
                }
                break;

        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 5000;
        isDeltaReady0 = value == 0;
    }
}
