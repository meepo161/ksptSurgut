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
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase1.Experiment5ModelPhase1;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment5ControllerPhase1 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
    private static final double POWER = 100;
    @FXML
    private TableView<Experiment5ModelPhase1> tableViewExperiment5;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnUBHKZ;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnUKZPercent;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnUKZDiff;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnI;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnPp;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnF;
    @FXML
    private TableColumn<Experiment5ModelPhase1, String> tableColumnResultExperiment5;
    @FXML
    private TextArea textAreaExperiment5Log;
    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;

    private MainModel mainModel = MainModel.getInstance();
    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private double UBHTestItem = currentProtocol.getUbh();
    private double UKZTestItem = currentProtocol.getUkz();
    private double UBHTestItem418 = (int) (UBHTestItem / 1.1);
    private double UBHTestItem1312 = (int) (UBHTestItem / 3.158);
    private double Ukz = UBHTestItem * (UKZTestItem / 100.0) / 4;
    private double PInKVA = currentProtocol.getP();
    private double P = PInKVA * 1000;
    private double Iном = P / UBHTestItem;
    private double Ikz = Iном / 4.0;
    private double Pp;
    private double F;
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment5ModelPhase1 experiment5ModelPhase1;
    private ObservableList<Experiment5ModelPhase1> experiment5Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentRunning;
    private volatile boolean isExperimentEnd = true;
    private volatile boolean isDeviceOn = false;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isDeltaReady50;
    private volatile boolean isDeltaReady0;
    private volatile boolean isParmaResponding;

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
    private volatile double iAkz;
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
    private double Unom;
    private double measuringUkzPercent;
    private double ukzPercent;
    private double ukzDif;
    private double pParma;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment5ModelPhase1 = mainModel.getExperiment5ModelPhase1();
        experiment5Data.add(experiment5ModelPhase1);
        tableViewExperiment5.setItems(experiment5Data);
        tableViewExperiment5.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnUBHKZ.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnUKZPercent.setCellValueFactory(cellData -> cellData.getValue().UKZPercentProperty());
        tableColumnUKZDiff.setCellValueFactory(cellData -> cellData.getValue().UKZDiffProperty());
        tableColumnI.setCellValueFactory(cellData -> cellData.getValue().IProperty());
        tableColumnPp.setCellValueFactory(cellData -> cellData.getValue().PProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().FProperty());
        tableColumnResultExperiment5.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
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
        currentProtocol.setE5UKZV(experiment5ModelPhase1.getUBH());
        currentProtocol.setE5UKZPercent(experiment5ModelPhase1.getUKZPercent());
        currentProtocol.setE5UKZDiff(experiment5ModelPhase1.getUKZDiff());
        currentProtocol.setE5IA(experiment5ModelPhase1.getI());
        currentProtocol.setE5Pp(experiment5ModelPhase1.getPP());
        currentProtocol.setE5F(experiment5ModelPhase1.getF());
        currentProtocol.setE5Result(experiment5ModelPhase1.getResult());
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
        isExperimentRunning = false;
    }


    private void startExperiment() {
        isCurrent1On = true;
        isCurrent2On = true;
        isDoorLockOn = true;
        isInsulationOn = true;
        isDoorZoneOn = true;
        isNeedToRefresh = true;
        isExperimentRunning = true;
        isExperimentEnd = false;
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        experiment5ModelPhase1.clearProperties();
        isDeltaResponding = false;
        isParmaResponding = false;
        cause = "";

        new Thread(() -> {
            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment5Devices();
                sleep(3000);
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
                communicationModel.onPR1();
                sleep(1000);
            }

            while (isExperimentRunning && !isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Включите кнопочный пост");
            }

            if (isExperimentRunning) {
                sleep(8000);
                communicationModel.initExperiment5Devices();
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }


            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                communicationModel.onPR3();
                if (Ikz < 1) {
                    appendOneMessageToLog("1к5 токовая ступень");
                    communicationModel.onPR1M1();
                    is1to5State = true;
                    is10to5State = false;
                    is75to5State = false;
                } else if (Ikz > 1 && Ikz < 11) {
                    appendOneMessageToLog("10к5 токовая ступень");
                    communicationModel.onPR8();
                    is1to5State = false;
                    is10to5State = true;
                    is75to5State = false;
                } else {
                    appendOneMessageToLog("75к5 токовая ступень");
                    communicationModel.onPR7();
                    is1to5State = false;
                    is10to5State = false;
                    is75to5State = true;
                }
                communicationModel.onPR6();
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                if (Ukz < 3.0) {
                    communicationModel.onPR5M1();
                    appendOneMessageToLog("KM5M1TP4");
                } else if (Ukz >= 3.0) {
                    communicationModel.onPR4M1();
                    appendOneMessageToLog("KM4M1TP12");
                } else if (Ukz > 12.0) {
                    appendOneMessageToLog("Напряжение короткого больше допустимого");
                    appendOneMessageToLog("Проверьте корректность введенных данных в БД");
                    communicationModel.offAllKms();
                }
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                communicationModel.setObjectParams(50 * HZ, 5 * VOLT, 50 * HZ);
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog("Запускаем ЧП");
            }

            while (isExperimentRunning && !isDeltaReady50 && isStopButtonOn) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь выйдет к заданным характеристикам");
            }


            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Поднимаем напряжение до " + UBHTestItem);
                regulation(5 * VOLT, 30, 5, UBHTestItem, 0.15, 2, 100, 200);
            }


            isNeedToRefresh = false;
            isExperimentRunning = false;
            isExperimentEnd = true;
            communicationModel.stopObject();
            communicationModel.offAllKms(); //разбираем все возможные схемы


            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства


            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                experiment5ModelPhase1.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                experiment5ModelPhase1.setResult("Неуспешно");
            } else {
                experiment5ModelPhase1.setResult("Успешно");
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
        Platform.runLater(() -> textAreaExperiment5Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private boolean isThereAreAccidents() {
        if (!isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn) {
            isExperimentRunning = false;
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
        return isOwenPRResponding && isParmaResponding &&
                isDeltaResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isParmaResponding ? "" : "Парма ",
                isDeltaResponding ? "" : "Дельта ");
    }

    private int regulation(int start, int coarseStep, int fineStep, double end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentRunning && ((measuringUInAB < end * coarseMinLimit) || (measuringUInAB > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUInAB < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUInAB > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringUInAB < end - fineLimit) || (measuringUInAB > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUInAB < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringUInAB > end + fineLimit) {
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
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI1:
                        isCurrent1On = (boolean) value;
                        if (!isCurrent1On) {
                            cause = "сработала токовая защита 1";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI2:
                        isCurrent2On = (boolean) value;
                        if (!isCurrent2On) {
                            cause = "сработала токовая защита 2";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI3:
                        isDoorLockOn = (boolean) value;
                        if (!isDoorLockOn) {
                            cause = "открыта дверь";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI4:
                        isInsulationOn = (boolean) value;
                        if (!isInsulationOn) {
                            cause = "пробита изоляция";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI7:
                        isDoorZoneOn = (boolean) value;
                        if (!isDoorZoneOn) {
                            cause = "открыта дверь зоны";
                            isExperimentRunning = false;
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
                    case ParmaT400Model.IA_PARAM:
                        if (isNeedToRefresh) {
                            iA = (double) value;
                            if (is75to5State) {
                                iA *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is10to5State) {
                                iA *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is1to5State) {
                                iA *= STATE_5_TO_5_MULTIPLIER;
                            }
                            if (iA > 0.001) {
                                experiment5ModelPhase1.setI((double) ((int) (iA * 10000)) / 10000);
                            }
                        }
                        if (iA > Ikz) {
                            appendOneMessageToLog("Достигли I короткого замыкания. Испытание остановлено");
                            isExperimentRunning = false;
                        }
                        break;
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (double) value;
                            String UInAB = String.format("%.2f", measuringUInAB);
                            experiment5ModelPhase1.setUBH(UInAB);
                            Unom = measuringUInAB * 4.0;
                            measuringUkzPercent = (Unom * 100.0) / UBHTestItem;
                            ukzPercent = ((int) ((double) measuringUkzPercent * POWER) / POWER);
                            experiment5ModelPhase1.setUKZPercent(String.valueOf(ukzPercent));
                            String ukzDif = String.format("%.2f", UKZTestItem - ukzPercent);
                            experiment5ModelPhase1.setUKZDiff(String.valueOf(ukzDif));
                            if (measuringUInAB > UBHTestItem) {
                                appendOneMessageToLog("Напряжение достигло номинального, испытание прервано");
                                isExperimentRunning = false;
                                cause = "Неуспешно";
                            }
                        }
                        break;
                    case ParmaT400Model.P_PARAM:
                        if (isNeedToRefresh) {
                            pParma = (double) value;
                            if (is75to5State) {
                                fParma *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is10to5State) {
                                fParma *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is1to5State) {
                                fParma *= STATE_5_TO_5_MULTIPLIER;
                            }
                            String pParmaString = String.format("%.2f", pParma * 16);
                            experiment5ModelPhase1.setPP(pParmaString);
                        }
                        break;
                    case ParmaT400Model.F_PARAM:
                        if (isNeedToRefresh) {
                            String fParma = String.format("%.2f", (double) value);
                            experiment5ModelPhase1.setF(fParma);
                            sleep(100);
                        }
                        break;
                }
                break;
//            case DELTACP2000_ID:
//                switch (param) {
//                    case DeltaCP2000Model.RESPONDING_PARAM:
//                        isDeltaResponding = (boolean) value;
//                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
//
//                        break;
//                    case DeltaCP2000Model.CURRENT_FREQUENCY_PARAM:
//                        setCurrentFrequencyObject((short) value);
//                        sleep(100);
//                        break;
//                }
//                break;
        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 50 * HZ;
        isDeltaReady0 = value == 0;
    }
}
