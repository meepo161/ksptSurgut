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

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment4ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment4ModelPhase3> tableViewExperiment4;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUBHKZ;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUKZPercent;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUKZDiff;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnIA;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnIB;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnIC;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnPp;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnResultExperiment4;
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
    private double UHHTestItem = currentProtocol.getUhh();
    private double UKZTestItem = currentProtocol.getUkz();
    private double coef = 1.16;
    private double UBHTestItem418 = (int) (UBHTestItem / 1.1);
    private double UBHTestItem1312 = (int) (UBHTestItem / 3.158);
    private double Ukz = UBHTestItem * (UKZTestItem / 100.0) / 4;
    private double PInKVA = currentProtocol.getP();
    private double P = PInKVA * 1000;
    private double Iном = P / (Math.sqrt(3) * UBHTestItem);
    private double Ikz = Iном / 4.0;
    private double Pp;
    private double F;
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment4ModelPhase3 Experiment4ModelPhase3;
    private ObservableList<Experiment4ModelPhase3> Experiment4Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isNeedToWaitDelta;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentRunning;
    private volatile boolean isExperimentEnd = true;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isDeltaReady50;
    private volatile boolean isDeltaReady0;
    private volatile boolean isParmaResponding;
    private volatile boolean isPM130Responding;
    private volatile boolean isPressedOk;

    private volatile boolean isDoorSHSO;
    private volatile boolean isDoorZone;
    private volatile boolean isCurrent;
    private volatile boolean isCurrentVIU;

    private boolean is200to5State;
    private boolean is40to5State;
    private boolean is5to5State;

    private double iA;
    private double iB;
    private double iC;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private volatile String cause;
    private volatile double temperature;
    private volatile double iAvr;
    private volatile double iAkz;
    private volatile double measuringP;
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
    private String PParma;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        Experiment4ModelPhase3 = mainModel.getExperiment4ModelPhase3();
        Experiment4Data.add(Experiment4ModelPhase3);
        tableViewExperiment4.setItems(Experiment4Data);
        tableViewExperiment4.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnUBHKZ.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnUKZPercent.setCellValueFactory(cellData -> cellData.getValue().UKZPercentProperty());
        tableColumnUKZDiff.setCellValueFactory(cellData -> cellData.getValue().UKZDiffProperty());
        tableColumnIA.setCellValueFactory(cellData -> cellData.getValue().IAProperty());
        tableColumnIB.setCellValueFactory(cellData -> cellData.getValue().IBProperty());
        tableColumnIC.setCellValueFactory(cellData -> cellData.getValue().ICProperty());
        tableColumnPp.setCellValueFactory(cellData -> cellData.getValue().PPProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().fProperty());
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


    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE5UKZV(Experiment4ModelPhase3.getUBH());
        currentProtocol.setE5UKZPercent(Experiment4ModelPhase3.getUKZPercent());
        currentProtocol.setE5UKZDiff(Experiment4ModelPhase3.getUKZDiff());
        currentProtocol.setE5IA(Experiment4ModelPhase3.getIA());
        currentProtocol.setE5IB(Experiment4ModelPhase3.getIB());
        currentProtocol.setE5IC(Experiment4ModelPhase3.getIC());
        currentProtocol.setE5Pp(Experiment4ModelPhase3.getPP());
        currentProtocol.setE5F(Experiment4ModelPhase3.getF());
        currentProtocol.setE5Result(Experiment4ModelPhase3.getResult());
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
        communicationModel.finalizeAllDevices();
        communicationModel.stopObject();
        communicationModel.offAllKms();
    }


    private void startExperiment() {
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);

        communicationModel.offAllKms();
        communicationModel.finalizeAllDevices();
        Experiment4ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isExperimentRunning = true;
        isExperimentEnd = false;
        isNeedToWaitDelta = false;

        isDeltaResponding = false;
        isParmaResponding = false;
        isPM130Responding = false;

        isCurrentVIU = true;

        isPressedOk = false;
        cause = "";
        isPressedOk = false;

        new Thread(() -> {

            if (isExperimentRunning) {
                Platform.runLater(() -> {
                    View.showConfirmDialog("Подключите ШСО к НН",
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
                appendOneMessageToLog("Идет загрузка ЧП");
            }

            if (isExperimentRunning && isNeedToWaitDelta) {
                sleep(8000);
            }

            if (isExperimentRunning) {
                communicationModel.initExperiment4Devices();
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                if (Ikz < 1) {
                    appendOneMessageToLog("5к5 токовая ступень");
                    communicationModel.onKM7();
                    is5to5State = true;
                    is40to5State = false;
                    is200to5State = false;
                } else if (Ikz > 1 && Ikz < 46) {
                    appendOneMessageToLog("40к5 токовая ступень");
                    communicationModel.onKM6();
                    is5to5State = false;
                    is40to5State = true;
                    is200to5State = false;
                } else {
                    appendOneMessageToLog("200к5 токовая ступень");
                    communicationModel.onKM5();
                    is5to5State = false;
                    is40to5State = false;
                    is200to5State = true;
                }
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                if (Ukz < 380) {
                    communicationModel.onKM2();
                    communicationModel.onKM13();
                } else if (Ukz > 380) {
                    communicationModel.onKM3();
                    communicationModel.onKM13();
                }
                if (Ukz > 380.0) {
                    appendOneMessageToLog("Напряжение короткого больше допустимого");
                    appendOneMessageToLog("Проверьте корректность введенных данных в БД");
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

            while (isExperimentRunning && !isDeltaReady50) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь выйдет к заданным характеристикам");
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItem);
//                communicationModel.setObjectUMax((int) (UHHTestItem / coef) * VOLT);
                regulation(5 * VOLT, 30, 5, UHHTestItem / coef, 0.1, 2, 100, 200);
            }

            isNeedToRefresh = false;
            isExperimentRunning = false;
            isExperimentEnd = true;
            communicationModel.stopObject();

            while (isExperimentRunning && !isDeltaReady0 && isDeltaResponding) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }

            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                Experiment4ModelPhase3.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                Experiment4ModelPhase3.setResult("Неуспешно");
            } else {
                Experiment4ModelPhase3.setResult("Успешно");
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
        Platform.runLater(() -> textAreaExperiment4Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
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
        return isOwenPRResponding && isParmaResponding &&
                isDeltaResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isPM130Responding ? "" : "ПМ130 ");
    }

    private int regulation(int start, int coarseStep, int fineStep, double end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentRunning && ((measuringUInAvr < end * coarseMinLimit) || (measuringUInAvr > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUInAvr < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUInAvr > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringUInAvr < end - fineLimit) || (measuringUInAvr > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
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
                        isParmaResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleParma400.setFill(((boolean) value) ? Color.LIME : Color.RED));

                        break;
                    case PM130Model.I1_PARAM:
                        if (isNeedToRefresh) {
                            iA = (float) value;


                            if (is200to5State) {
                                iA *= STATE_200_TO_5_MULTIPLIER;


                            } else if (is40to5State) {
                                iA *= STATE_40_TO_5_MULTIPLIER;


                            } else if (is5to5State) {
                                iA *= STATE_5_TO_5_MULTIPLIER;
                            }


                            if (iA > 0.001) {
                                Experiment4ModelPhase3.setIA(String.format("%.3f", iA));
                            }
                        }
                        if (iA > Ikz) {
                            appendOneMessageToLog("Достигли I короткого замыкания на фазе A. Испытание остановлено");
                            isExperimentRunning = false;
                        }
                        break;
                    case PM130Model.I2_PARAM:
                        if (isNeedToRefresh) {
                            iB = (float) value;
                            if (is200to5State) {
                                iB *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                iB *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                iB *= STATE_5_TO_5_MULTIPLIER;
                            }
                            if (iB > 0.001) {
                                Experiment4ModelPhase3.setIB(String.format("%.3f", iB));
                            }
                        }
                        if (iB > Ikz) {
                            appendOneMessageToLog("Достигли I короткого замыкания на фазе B. Испытание остановлено");
                            isExperimentRunning = false;
                        }
                        break;
                    case PM130Model.I3_PARAM:
                        if (isNeedToRefresh) {
                            iC = (float) value;
                            if (is200to5State) {
                                iC *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                iC *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                iC *= STATE_5_TO_5_MULTIPLIER;
                            }
                            if (iC > 0.001) {
                                Experiment4ModelPhase3.setIC(String.format("%.3f", iC));
                            }
                        }
                        if (iC > Ikz) {
                            appendOneMessageToLog("Достигли I короткого замыкания на фазе C. Испытание остановлено");
                            isExperimentRunning = false;
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                            String UInAB = String.format("%.2f", measuringUInAB);
                            Experiment4ModelPhase3.setUBH(UInAB);
                            measuringUkzPercent = (measuringUInAB * 100.0) / UBHTestItem;
                            ukzPercent = (measuringUkzPercent * POWER) / POWER;
                            Experiment4ModelPhase3.setUKZPercent(String.format("%.2f", ukzPercent));
                            Experiment4ModelPhase3.setUKZDiff(String.format("%.2f", ukzPercent - UKZTestItem));
                            if (measuringUInAB > UHHTestItem) {
                                appendOneMessageToLog("Напряжение достигло номинального, испытание прервано");
                                isExperimentRunning = false;
                                cause = "Неуспешно";
                            }
                        }
                        break;
                    case PM130Model.P_PARAM:
                        if (isNeedToRefresh) {
                            measuringP = (float) value;
                            if (is200to5State) {
                                measuringP *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                measuringP *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                measuringP *= STATE_5_TO_5_MULTIPLIER;
                            }
                            String PP = String.format("%.2f", measuringP * 16);
                            Experiment4ModelPhase3.setPP(PP);
                        }
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            String freq = String.format("%.2f", (float) value);
                            Experiment4ModelPhase3.setF(freq);
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
        isDeltaReady50 = value == 50 * HZ;
        isDeltaReady0 = value == 0;
    }
}