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
import ru.avem.ksptamur.model.phase1.Experiment6ModelPhase1;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment6ControllerPhase1 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final int WIDDING1320 = 1320;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 200;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment6ModelPhase1> tableViewExperiment6;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnUBH;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnI;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnIPercent;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnIDiff;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnPP;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnF;
    @FXML
    private TableColumn<Experiment6ModelPhase1, String> tableColumnResultExperiment6;
    @FXML
    private TextArea textAreaExperiment6Log;
    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;

    private MainModel mainModel = MainModel.getInstance();
    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private double UBHTestItem = currentProtocol.getUbh();
    private double Pkva = currentProtocol.getP();
    private double UHHTestItem = currentProtocol.getUhh();
    private double IxxPercent = currentProtocol.getIxx();
    private double Inom = Pkva * 1000 / (UBHTestItem);
    private double Ixx = Inom / 100 * IxxPercent;
    private double Time = currentProtocol.getXxtime();
    private int XXTime = (int) Time;
    private double UBHTestItem418 = (int) (UBHTestItem / 1.1);
    private double UBHTestItem1312 = (int) (UBHTestItem / 3.158);
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment6ModelPhase1 experiment6ModelPhase1;
    private ObservableList<Experiment6ModelPhase1> experiment6Data = FXCollections.observableArrayList();

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
    private volatile double iAOld = -1;
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
    private volatile double iAPercentD;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double measuringF;
    private volatile double measuringPp;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment6ModelPhase1 = mainModel.getExperiment6ModelPhase1();
        experiment6Data.add(experiment6ModelPhase1);
        tableViewExperiment6.setItems(experiment6Data);
        tableViewExperiment6.setSelectionModel(null);
        communicationModel.addObserver(this);


        tableColumnUBH.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnI.setCellValueFactory(cellData -> cellData.getValue().IProperty());
        tableColumnIPercent.setCellValueFactory(cellData -> cellData.getValue().IPercentProperty());
        tableColumnIDiff.setCellValueFactory(cellData -> cellData.getValue().IDiffProperty());
        tableColumnPP.setCellValueFactory(cellData -> cellData.getValue().PProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().FProperty());
        tableColumnResultExperiment6.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
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
        currentProtocol.setE6UBH(experiment6ModelPhase1.getUBH());
        currentProtocol.setE6IA(experiment6ModelPhase1.getI());
        currentProtocol.setE6IAPercent(experiment6ModelPhase1.getIPercent());
        currentProtocol.setE6IADiff(experiment6ModelPhase1.getIDiff());
        currentProtocol.setE6Pp(experiment6ModelPhase1.getPP());
        currentProtocol.setE6F(experiment6ModelPhase1.getF());
        currentProtocol.setE6Cos(experiment6ModelPhase1.getCOS());
        currentProtocol.setE6Result(experiment6ModelPhase1.getResult());
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
        experiment6ModelPhase1.clearProperties();
        isDeltaResponding = false;
        isParmaResponding = false;
        cause = "";

        new Thread(() -> {
            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment6Devices();
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
                isStartButtonOn = true;
                is75to5State = true;
                sleep(1000);
            }

            while (isExperimentRunning && !isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Включите кнопочный пост");
            }

            if (isExperimentRunning) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(8000);
                communicationModel.initExperiment6Devices();
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            isDeviceOn = true;

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                communicationModel.onPR2();
                communicationModel.onPR7();
                is75to5State = true;
                if (UBHTestItem <= WIDDING400) {
                    communicationModel.onPR2M1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 418В");
                } else if (UBHTestItem > WIDDING400) {
                    communicationModel.onPR3M1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
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
                if (UBHTestItem <= WIDDING400) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UBHTestItem);
                    regulation(5 * VOLT, 15, 2, UBHTestItem, 0.10, 2, 100, 200);
                } else if (UBHTestItem > WIDDING400) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItem);
                    regulation(5 * VOLT, 10, 2, UHHTestItem, 0.1, 2, 100, 200);
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректные данные в объекте испытания.");
                }
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Идет подбор токовой ступени");
                pickUpState();
                sleep(100);
                appendOneMessageToLog("Токовая ступень подобрана");
                appendOneMessageToLog("Измерение тока первичной обмотки и мощности потерь");
            }

            XXTime = (int) currentProtocol.getXxtime();
            while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (XXTime-- > 0)) {
                sleep(1000);

                appendOneMessageToLog("Ждем " + XXTime + " секунд");
                experiment6ModelPhase1.setTime(String.valueOf(XXTime));
            }

            isNeedToRefresh = false;
            isExperimentRunning = false;
            isExperimentEnd = true;
            communicationModel.stopObject();
            while (isExperimentRunning && !isDeltaReady0 && isDeltaResponding) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }
            sleep(500);

            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                experiment6ModelPhase1.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                experiment6ModelPhase1.setResult("Неуспешно");
            } else {
                experiment6ModelPhase1.setResult("Успешно");
                appendMessageToLog("Испытание завершено успешно");
            }
            appendMessageToLog("------------------------------------------------\n");

            isDeviceOn = false;

            Platform.runLater(() -> {
                buttonStartStop.setText("Запустить");
                buttonStartStop.setDisable(false);
                buttonNext.setDisable(false);
                buttonCancelAll.setDisable(false);
            });
        }).start();
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

    private void pickUpState() {
        if (is75to5State) {
            if (iA < 12.0 && iA > 1.2) {
                communicationModel.onPR8();
                sleep(200);
                is75to5State = false;
                is10to5State = true;
                is1to5State = false;
                communicationModel.offPR7();
                communicationModel.offKM1M1();
                appendOneMessageToLog("Выставляем токовую ступень 10/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (iA < 1.2) {
                communicationModel.onPR1M1();
                sleep(200);
                is75to5State = false;
                is10to5State = false;
                is1to5State = true;
                communicationModel.offPR7();
                communicationModel.offPR8();
                appendOneMessageToLog("Выставляем токовую ступень 1/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is10to5State) {
            if (iA > 12) {
                communicationModel.onPR7();
                sleep(200);
                is75to5State = true;
                is10to5State = false;
                is1to5State = false;
                communicationModel.offKM1M1();
                communicationModel.offPR8();
                appendOneMessageToLog("Выставляем токовую ступень 75/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (iA < 1.2) {
                communicationModel.onPR1M1();
                sleep(100);
                is75to5State = false;
                is10to5State = false;
                is1to5State = true;
                communicationModel.offPR7();
                communicationModel.offPR8();
                appendOneMessageToLog("Выставляем токовую ступень 1/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is1to5State) {
            if (iA > 1.2) {
                communicationModel.onPR8();
                sleep(100);
                is75to5State = false;
                is10to5State = true;
                is1to5State = false;
                communicationModel.offPR8();
                communicationModel.offKM1M1();
                appendOneMessageToLog("Выставляем токовую ступень 10/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (iA < 1.2) {
                communicationModel.onPR1M1();
                sleep(100);
                is75to5State = false;
                is10to5State = false;
                is1to5State = true;
                communicationModel.offPR7();
                communicationModel.offPR8();
                appendOneMessageToLog("Выставляем токовую ступень 1/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        }
    }

    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperiment6Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
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
                isDeltaResponding ? "" : "Дельта ",
                isParmaResponding ? "" : "Парма ");
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
                            iA = (double) ((int) (iA * 10000)) / 10000;
                            if (iA > 0.001) {
                                experiment6ModelPhase1.setI(iA);
                                iAPercentD = Ixx / iA;
                                String iAPercent = String.format("%.2f", iAPercentD);
                                experiment6ModelPhase1.setIPercent(iAPercent);
                                String iADiff = String.format("%.2f", IxxPercent - iAPercentD);
                                experiment6ModelPhase1.setIDiff(iADiff);
                            }
                        }
                        break;
                    case ParmaT400Model.F_PARAM:
                        if (isNeedToRefresh) {
                            fParma = (double) value;
                            experiment6ModelPhase1.setF(String.valueOf(fParma));
                        }
                        break;
                    case ParmaT400Model.P_PARAM:
                        if (isNeedToRefresh) {
                            String PParma = String.format("%.2f", (double) value);
                            experiment6ModelPhase1.setPP(PParma);
                        }
                        break;
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (double) value;
                            String UInAB = String.format("%.2f", measuringUInAB);
                            if (measuringUInAB > 0.001) {
                                experiment6ModelPhase1.setUBH(UInAB);
                            }
                        }
                        break;
                }
                break;
          // case DELTACP2000_ID:
          //     switch (param) {
          //         case DeltaCP2000Model.RESPONDING_PARAM:
          //             isDeltaResponding = (boolean) value;
          //             Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));

          //             break;
          //         case DeltaCP2000Model.CURRENT_FREQUENCY_PARAM:
          //             setCurrentFrequencyObject((short) value);
          //             break;
          //     }
          //     break;
        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 50 * HZ;
        isDeltaReady0 = value == 0;
    }
}