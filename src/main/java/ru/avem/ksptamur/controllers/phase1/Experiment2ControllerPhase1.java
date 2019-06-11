package ru.avem.ksptamur.controllers.phase1;

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
import ru.avem.ksptamur.model.phase1.Experiment2ModelPhase1;
import ru.avem.ksptamur.model.phase1.Experiment4ModelPhase1;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment2ControllerPhase1 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
    private static final double POWER = 100;


    @FXML
    private TableView<Experiment2ModelPhase1> tableViewExperiment2;
    @FXML
    private TableColumn<Experiment2ModelPhase1, String> tableColumnUInputAvr;
    @FXML
    private TableColumn<Experiment2ModelPhase1, String> tableColumnUOutputAvr;
    @FXML
    private TableColumn<Experiment2ModelPhase1, String> tableColumnUDiff;
    @FXML
    private TableColumn<Experiment2ModelPhase1, String> tableColumnF;
    @FXML
    private TableColumn<Experiment2ModelPhase1, String> tableColumnResultExperiment2;
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
    private double UBHTestItem418 = (int) (UBHTestItem / 1.1);
    private double UBHTestItem1312 = (int) (UBHTestItem / 3.158);
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment2ModelPhase1 experiment2ModelPhase1;
    private Experiment4ModelPhase1 experiment4ModelPhase1;
    private ObservableList<Experiment2ModelPhase1> experiment2Data = FXCollections.observableArrayList();

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
    private volatile int windingGroup0;
    private volatile int windingGroup1;
    private volatile double measuringUOut;
    private volatile double measuringUIn;
    private volatile double measuringF;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment2ModelPhase1 = mainModel.getExperiment2ModelPhase1();
        experiment2Data.add(experiment2ModelPhase1);
        experiment4ModelPhase1 = mainModel.getExperiment4ModelPhase1();
        tableViewExperiment2.setItems(experiment2Data);
        tableViewExperiment2.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnUOutputAvr.setCellValueFactory(cellData -> cellData.getValue().UOutputProperty());
        tableColumnUInputAvr.setCellValueFactory(cellData -> cellData.getValue().UInputProperty());
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
        currentProtocol.setE2UInputAB(experiment2ModelPhase1.getUInput());
        currentProtocol.setE2UOutputAB(experiment2ModelPhase1.getUOutput());
        currentProtocol.setE2DiffU(experiment2ModelPhase1.getUDiff());
        currentProtocol.setE2F(experiment2ModelPhase1.getF());
        currentProtocol.setE2Result(experiment2ModelPhase1.getResult());
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
        experiment2ModelPhase1.clearProperties();
        isDeltaResponding = false;
        isParmaResponding = false;
        isPM130Responding = false;
        cause = "";

        new Thread(() -> {
            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment2Devices();
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
                communicationModel.onKM1();
                sleep(1000);
            }

            while (isExperimentStart && !isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Включите кнопочный пост");
            }

            if (isExperimentStart) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(8000);
                communicationModel.initExperiment2Devices();
            }

            while (isExperimentStart && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            isDeviceOn = true;

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                communicationModel.onKM2();
                communicationModel.onKM7();
                is75to5State = true;
                if (UBHTestItem < WIDDING400) {
                    communicationModel.onKM2M1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 418В");
                } else if (UBHTestItem > WIDDING400) {
                    communicationModel.onKM3M1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
                }
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                communicationModel.onKM5();
                appendOneMessageToLog("Подключена обмотка НН");
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
                    regulation(5 * 10, 30, 5, UBHTestItem, 0.1, 2, 100, 200);
                } else if (UBHTestItem > WIDDING400) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UBHTestItem);
                    regulation(5 * 10, 30, 5, UBHTestItem, 0.1, 2, 100, 200);
                }
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                sleep(4000);
                isNeedToRefresh = false;
                sleep(1000);
                experiment2ModelPhase1.setUDiff(String.valueOf(((int) ((measuringUIn / measuringUOut * POWER)) / POWER)));
            }

            isNeedToRefresh = false;
            isDeviceOn = false;
            isExperimentStart = false;
            isExperimentEnd = true;
            sleep(1000);
            communicationModel.stopObject();
            while (isExperimentStart && !isDeltaReady0 && isDeltaResponding) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }

            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                experiment2ModelPhase1.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                experiment2ModelPhase1.setResult("Неуспешно");
            } else {
                experiment2ModelPhase1.setResult("Успешно");
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
        while (isExperimentStart && ((measuringUIn < end * coarseMinLimit) || (measuringUIn > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUIn < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUIn > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentStart && ((measuringUIn < end - fineLimit) || (measuringUIn > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
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
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.RESPONDING_PARAM:
                        isOwenPRResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI5:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRDI6:
                        isStopButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRDI6_FIXED:
                        if ((boolean) value) {
                            cause = "Нажата кнопка (СТОП)";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI1:
                        isCurrent1On = (boolean) value;
                        if (!isCurrent1On) {
                            cause = "сработала токовая защита 1";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI2:
                        isCurrent2On = (boolean) value;
                        if (!isCurrent2On) {
                            cause = "сработала токовая защита 2";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI3:
                        isDoorLockOn = (boolean) value;
                        if (!isDoorLockOn) {
                            cause = "открыта дверь";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI4:
                        isInsulationOn = (boolean) value;
                        if (!isInsulationOn) {
                            cause = "пробита изоляция";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI7:
                        isDoorZoneOn = (boolean) value;
                        if (!isDoorZoneOn) {
                            cause = "открыта дверь зоны";
                            isExperimentStart = false;
                        }
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
                            measuringUOut = (float) value;
                            String UOutAB = String.format("%.2f", measuringUOut);
                            experiment2ModelPhase1.setUOutput(UOutAB);
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
                    case ParmaT400Model.F_PARAM:
                        if (isNeedToRefresh) {
                            String fParma = String.format("%.2f", (double) value);
                            experiment2ModelPhase1.setF(fParma);
                        }
                        break;
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUIn = (double) value;
                            String UInAB = String.format("%.2f", measuringUIn);
                            if (measuringUIn > 0.001) {
                                experiment2ModelPhase1.setUInput(UInAB);
                            }
                        }
                        break;
                }
                break;
//            case DELTACP2000_ID:
//                switch (param) {
//                    case DeltaCP2000Model.RESPONDING_PARAM:
//                        isDeltaResponding = (boolean) value;
//                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case DeltaCP2000Model.CURRENT_FREQUENCY_PARAM:
//                        setCurrentFrequencyObject((short) value);
//                        break;
//                }
//                break;
        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 5000;
        isDeltaReady0 = value == 0;
    }

}
