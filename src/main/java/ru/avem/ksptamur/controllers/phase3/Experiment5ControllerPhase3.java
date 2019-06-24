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
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment5ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.PM130_ID;
import static ru.avem.ksptamur.communication.devices.DeviceController.PR200_ID;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment5ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final int WIDDING1320 = 1320;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 1000;
    private static final double POWER = 100;

    @FXML
    private TableView<ru.avem.ksptamur.model.phase3.Experiment5ModelPhase3> tableViewExperiment5;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnUBH;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIA;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIB;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIC;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIAPercent;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIBPercent;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnICPercent;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIADiff;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnIBDiff;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnICDiff;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnPP;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnCOS;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnResultExperiment5;
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
    private double coef = 1;
    private double Pkva = currentProtocol.getP();
    private double UHHTestItem = currentProtocol.getUhh();
    private double IxxPercent = currentProtocol.getIxx();
    private double Inom = Pkva * 1000 / (UBHTestItem * Math.sqrt(3));
    private double Ixx = Inom / 100 * IxxPercent;
    private double Time = currentProtocol.getXxtime();
    private int XXTime = (int) Time;
    private double UBHTestItem418 = (int) (UBHTestItem / 1.1);
    private double UBHTestItem1312 = (int) (UBHTestItem / 3.158);
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment5ModelPhase3 Experiment5ModelPhase3;
    private ObservableList<Experiment5ModelPhase3> Experiment5Data = FXCollections.observableArrayList();


    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isNeedToWaitDelta;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentRunning;
    private volatile boolean isExperimentEnd = true;
    private volatile boolean isStartButtonOn;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isDeltaReady50;
    private volatile boolean isDeltaReady0;
    private volatile boolean isPM130Responding;
    private volatile boolean isPressedOk;

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
    private volatile double iAOld = -1;
    private volatile double iA;
    private volatile double iB;
    private volatile double iC;
    private volatile double fParma;
    private volatile double measuringP;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double iAPercentD;
    private volatile double iBPercentD;
    private volatile double iCPercentD;
    private double cosParma;

    private double IAvr;
    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        Experiment5ModelPhase3 = mainModel.getExperiment5ModelPhase3();
        Experiment5Data.add(Experiment5ModelPhase3);
        tableViewExperiment5.setItems(Experiment5Data);
        tableViewExperiment5.setSelectionModel(null);
        communicationModel.addObserver(this);


        tableColumnUBH.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnIA.setCellValueFactory(cellData -> cellData.getValue().IAProperty());
        tableColumnIB.setCellValueFactory(cellData -> cellData.getValue().IBProperty());
        tableColumnIC.setCellValueFactory(cellData -> cellData.getValue().ICProperty());
        tableColumnIAPercent.setCellValueFactory(cellData -> cellData.getValue().IAPercentProperty());
        tableColumnIBPercent.setCellValueFactory(cellData -> cellData.getValue().IBPercentProperty());
        tableColumnICPercent.setCellValueFactory(cellData -> cellData.getValue().ICPercentProperty());
        tableColumnIADiff.setCellValueFactory(cellData -> cellData.getValue().IADiffProperty());
        tableColumnIBDiff.setCellValueFactory(cellData -> cellData.getValue().IBDiffProperty());
        tableColumnICDiff.setCellValueFactory(cellData -> cellData.getValue().ICDiffProperty());
        tableColumnPP.setCellValueFactory(cellData -> cellData.getValue().PProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().FProperty());
        tableColumnCOS.setCellValueFactory(cellData -> cellData.getValue().COSProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
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


    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE6UBH(Experiment5ModelPhase3.getUBH());
        currentProtocol.setE6IA(Experiment5ModelPhase3.getIA());
        currentProtocol.setE6IB(Experiment5ModelPhase3.getIB());
        currentProtocol.setE6IC(Experiment5ModelPhase3.getIC());
        currentProtocol.setE6IAPercent(Experiment5ModelPhase3.getIAPercent());
        currentProtocol.setE6IBPercent(Experiment5ModelPhase3.getIBPercent());
        currentProtocol.setE6ICPercent(Experiment5ModelPhase3.getICPercent());
        currentProtocol.setE6IADiff(Experiment5ModelPhase3.getIADiff());
        currentProtocol.setE6IBDiff(Experiment5ModelPhase3.getIBDiff());
        currentProtocol.setE6ICDiff(Experiment5ModelPhase3.getICDiff());
        currentProtocol.setE6Pp(Experiment5ModelPhase3.getPP());
        currentProtocol.setE6F(Experiment5ModelPhase3.getF());
        currentProtocol.setE6Cos(Experiment5ModelPhase3.getCOS());
        currentProtocol.setE6Result(Experiment5ModelPhase3.getResult());
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
        communicationModel.stopObject();
        communicationModel.finalizeAllDevices();
        communicationModel.offAllKms();
    }

    private void startExperiment() {
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);

        communicationModel.offAllKms();
        communicationModel.finalizeAllDevices();
        Experiment5ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnd = false;

        isDeltaResponding = false;
        isPM130Responding = false;

        isCurrentVIU = true;

        isPressedOk = false;
        cause = "";

        new Thread(() -> {

            if (isExperimentRunning) {
                Platform.runLater(() -> {
                    View.showConfirmDialog("Подключите ОИ для определения ХХ",
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
                Experiment5ModelPhase3.setTime(String.valueOf(XXTime));
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
                communicationModel.initExperiment5Devices();
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
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с HH до 418В");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный HH в объекте испытания.");
                    isExperimentRunning = false;
                }
                is5to5State = false;
                is40to5State = false;
                is200to5State = true;
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog("Идет подбор токовой ступени");
                sleep(5000);
                pickUpState();
                sleep(100);
                appendOneMessageToLog("Токовая ступень подобрана");
                appendOneMessageToLog("Измерение тока первичной обмотки и мощности потерь");
            }

            if (isExperimentRunning && isDevicesResponding()) {
                XXTime = (int) currentProtocol.getXxtime();
                appendOneMessageToLog("Ждем " + XXTime + " секунд");
            }

            while (isExperimentRunning && isDevicesResponding() && (XXTime-- > 0)) {
                sleep(1000);
                Experiment5ModelPhase3.setTime(String.valueOf(XXTime));
            }

            if (isExperimentRunning && isDevicesResponding()) {
                Experiment5ModelPhase3.setTime(String.valueOf((int) currentProtocol.getXxtime()));
            }

            isNeedToRefresh = false;
            isExperimentRunning = false;
            isExperimentEnd = true;

            while (isExperimentRunning && !isDeltaReady0 && isDeltaResponding) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }

            communicationModel.offAllKms();
            communicationModel.finalizeAllDevices();

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                Experiment5ModelPhase3.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                Experiment5ModelPhase3.setResult("Неуспешно");
            } else {
                Experiment5ModelPhase3.setResult("Успешно");
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
        return isOwenPRResponding && isPM130Responding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isPM130Responding ? "" : "Парма ");
    }

    private void pickUpState() {
        if (is200to5State) {
            if (IAvr < 12.0 && IAvr > 4) {
                appendOneMessageToLog("Выставляем токовую ступень 40/5");
                communicationModel.onKM6();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is200to5State = false;
                is40to5State = true;
                is5to5State = false;
                communicationModel.offPR6();
                communicationModel.offPR4();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 4) {
                appendOneMessageToLog("Выставляем токовую ступень 5/5");
                communicationModel.onKM7();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is200to5State = false;
                is40to5State = false;
                is5to5State = true;
                communicationModel.offPR4();
                communicationModel.offPR5();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else {
                appendOneMessageToLog("Выставляем токовую ступень 200/5");
            }
        } else if (is40to5State) {
            if (IAvr > 12) {
                appendOneMessageToLog("Выставляем токовую ступень 200/5");
                communicationModel.onKM5();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is200to5State = true;
                is40to5State = false;
                is5to5State = false;
                communicationModel.offPR5();
                communicationModel.offPR6();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 4) {
                appendOneMessageToLog("Выставляем токовую ступень 5/5");
                communicationModel.onKM7();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is200to5State = false;
                is40to5State = false;
                is5to5State = true;
                communicationModel.offPR4();
                communicationModel.offPR5();
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is5to5State) {
            if (IAvr > 4) {
                appendOneMessageToLog("Выставляем токовую ступень 40/5");
                communicationModel.onKM6();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is200to5State = false;
                is40to5State = true;
                is5to5State = false;
                communicationModel.offPR6();
                communicationModel.offPR4();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 4) {
                appendOneMessageToLog("Выставляем токовую ступень 5/5");
                communicationModel.onKM7();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is200to5State = false;
                is40to5State = false;
                is5to5State = true;
                communicationModel.offPR4();
                communicationModel.offPR5();
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        }
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
                                String iAString = String.format("%.2f", iA);
                                Experiment5ModelPhase3.setIA(iAString);
                                iAPercentD = Ixx / iA;
                                String iAPercent = String.format("%.2f", iAPercentD);
                                Experiment5ModelPhase3.setIAPercent(iAPercent);
                                String iADiff = String.format("%.2f", IxxPercent - iAPercentD);
                                Experiment5ModelPhase3.setIADiff(iADiff);
                            }
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
                                String iBString = String.format("%.2f", iB);
                                Experiment5ModelPhase3.setIB(iBString);
                                iBPercentD = Ixx / iB;
                                String iBPercent = String.format("%.2f", iBPercentD);
                                Experiment5ModelPhase3.setIBPercent(iBPercent);
                                String iBDiff = String.format("%.2f", IxxPercent - iBPercentD);
                                Experiment5ModelPhase3.setIBDiff(iBDiff);
                            }
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
                                String iCString = String.format("%.2f", iC);
                                Experiment5ModelPhase3.setIC(iCString);
                                IAvr = (iA + iB + iC) / 3;
                                iCPercentD = Ixx / iC;
                                String iCPercent = String.format("%.2f", iCPercentD);
                                Experiment5ModelPhase3.setICPercent(iCPercent);
                                String iCDiff = String.format("%.2f", IxxPercent - iCPercentD);
                                Experiment5ModelPhase3.setICDiff(iCDiff);
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
                            String PParma = String.format("%.2f", measuringP);
                            Experiment5ModelPhase3.setPP(PParma);
                        }
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            fParma = (float) value;
                            Experiment5ModelPhase3.setF(String.format("%.2f", fParma));
                        }
                        break;
                    case PM130Model.COS_PARAM:
                        if (isNeedToRefresh) {
                            cosParma = (float) value;
                            if (cosParma > 0.001) {
                                Experiment5ModelPhase3.setCOS(String.format("%.2f", cosParma));
                            }
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value * coef;
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value * coef;
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value * coef;
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            String UInAvr = String.format("%.2f", measuringUInAvr);
                            if (measuringUInAvr > 0.001) {
                                Experiment5ModelPhase3.setUBH(UInAvr);
                            }
                        }
                        break;
                }
                break;
        }
    }
}