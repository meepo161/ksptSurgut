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
import ru.avem.ksptamur.model.phase3.Experiment6ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment6ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final int WIDDING1320 = 1320;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 1000;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment6ModelPhase3> tableViewExperiment6;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnUBH;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIA;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIB;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIC;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIAPercent;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIBPercent;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnICPercent;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIADiff;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIBDiff;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnICDiff;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnPP;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnCOS;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnResultExperiment6;
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
    private double Inom = Pkva * 1000 / (UBHTestItem * Math.sqrt(3));
    private double Ixx = Inom / 100 * IxxPercent;
    private double Time = currentProtocol.getXxtime();
    private int XXTime = (int) Time;
    private double UBHTestItem418 = (int) (UBHTestItem / 1.1);
    private double UBHTestItem1312 = (int) (UBHTestItem / 3.158);
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment6ModelPhase3 experiment6ModelPhase3;
    private ObservableList<Experiment6ModelPhase3> experiment6Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isNeedToWaitDelta;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentStart;
    private volatile boolean isExperimentEnd = true;

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
    private volatile double pPM130;
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
        experiment6ModelPhase3 = mainModel.getExperiment6ModelPhase3();
        experiment6Data.add(experiment6ModelPhase3);
        tableViewExperiment6.setItems(experiment6Data);
        tableViewExperiment6.setSelectionModel(null);
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


    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE6UBH(experiment6ModelPhase3.getUBH());
        currentProtocol.setE6IA(experiment6ModelPhase3.getIA());
        currentProtocol.setE6IB(experiment6ModelPhase3.getIB());
        currentProtocol.setE6IC(experiment6ModelPhase3.getIC());
        currentProtocol.setE6IAPercent(experiment6ModelPhase3.getIAPercent());
        currentProtocol.setE6IBPercent(experiment6ModelPhase3.getIBPercent());
        currentProtocol.setE6ICPercent(experiment6ModelPhase3.getICPercent());
        currentProtocol.setE6IADiff(experiment6ModelPhase3.getIADiff());
        currentProtocol.setE6IBDiff(experiment6ModelPhase3.getIBDiff());
        currentProtocol.setE6ICDiff(experiment6ModelPhase3.getICDiff());
        currentProtocol.setE6Pp(experiment6ModelPhase3.getPP());
        currentProtocol.setE6F(experiment6ModelPhase3.getF());
        currentProtocol.setE6Cos(experiment6ModelPhase3.getCOS());
        currentProtocol.setE6Result(experiment6ModelPhase3.getResult());
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
        experiment6ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isExperimentStart = true;
        isExperimentEnd = false;

        isDeltaResponding = false;
        isPM130Responding = false;

        isCurrentVIU = true;

        isPressedOk = false;
        cause = "";

        new Thread(() -> {

            if (isExperimentStart) {
                Platform.runLater(() -> {
                    View.showConfirmDialog("Подключите ОИ для определения ХХ",
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

            if (isExperimentStart && isNeedToWaitDelta) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(8000);
            }

            if (isExperimentStart) {
                communicationModel.initExperiment6Devices();
            }

            while (isExperimentStart && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }


            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                if (isExperimentStart && UHHTestItem < WIDDING400) {
                    communicationModel.onKM1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 418В");
                } else if (isExperimentStart && UHHTestItem > WIDDING400) {
                    communicationModel.onKM2();
                    communicationModel.onKM2M1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
                }
                communicationModel.onKM4();
                is5to5State = false;
                is40to5State = false;
                is200to5State = true;
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
//                regulation(5 * 10, 60, 10, UHHTestItem, 0.05, 2, 100, 200);
                communicationModel.setObjectUMax(330 * 10);
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Идет подбор токовой ступени");
                sleep(5000);
                pickUpState();
                sleep(100);
                appendOneMessageToLog("Токовая ступень подобрана");
                appendOneMessageToLog("Измерение тока первичной обмотки и мощности потерь");
            }

            XXTime = (int) currentProtocol.getXxtime();
            appendOneMessageToLog("Ждем " + XXTime + " секунд");

            while (isExperimentStart && isStartButtonOn && isDevicesResponding() && (XXTime-- > 0)) {
                sleep(1000);
                experiment6ModelPhase3.setTime(String.valueOf(XXTime));
            }

            isNeedToRefresh = false;
            isExperimentStart = false;
            isExperimentEnd = true;
            communicationModel.stopObject();

            while (isExperimentStart && !isDeltaReady0 && isDeltaResponding) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }
            sleep(5000);

            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                experiment6ModelPhase3.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                experiment6ModelPhase3.setResult("Неуспешно");
            } else {
                experiment6ModelPhase3.setResult("Успешно");
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
        Platform.runLater(() -> textAreaExperiment6Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
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
        return isOwenPRResponding && isPM130Responding && isDeltaResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isPM130Responding ? "" : "Парма ");
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

    private void pickUpState() {
//        for (int i = 0; i < 3; i++) {
        if (is200to5State) {
            if (IAvr < 12.0 && IAvr > 4) {
                appendOneMessageToLog("Выставляем токовую ступень 40/5");
                communicationModel.onKM5();
                sleep(200);
                is200to5State = false;
                is40to5State = true;
                is5to5State = false;
                communicationModel.offKM6();
                communicationModel.offKM4();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 4) {
                appendOneMessageToLog("Выставляем токовую ступень 5/5");
                communicationModel.onKM6();
                sleep(200);
                is200to5State = false;
                is40to5State = false;
                is5to5State = true;
                communicationModel.offKM4();
                communicationModel.offKM5();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else {
                appendOneMessageToLog("Выставляем токовую ступень 200/5");
            }
        } else if (is40to5State) {
            if (IAvr > 12) {
                appendOneMessageToLog("Выставляем токовую ступень 200/5");
                communicationModel.onKM4();
                sleep(200);
                is200to5State = true;
                is40to5State = false;
                is5to5State = false;
                communicationModel.offKM5();
                communicationModel.offKM6();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 4) {
                appendOneMessageToLog("Выставляем токовую ступень 5/5");
                communicationModel.onKM6();
                sleep(200);
                is200to5State = false;
                is40to5State = false;
                is5to5State = true;
                communicationModel.offKM4();
                communicationModel.offKM5();
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is5to5State) {
            if (IAvr > 4) {
                appendOneMessageToLog("Выставляем токовую ступень 40/5");
                communicationModel.onKM5();
                sleep(200);
                is200to5State = false;
                is40to5State = true;
                is5to5State = false;
                communicationModel.offKM6();
                communicationModel.offKM4();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 4) {
                appendOneMessageToLog("Выставляем токовую ступень 5/5");
                communicationModel.onKM6();
                sleep(200);
                is200to5State = false;
                is40to5State = false;
                is5to5State = true;
                communicationModel.offKM4();
                communicationModel.offKM5();
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        }
//        }
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
                                String iAString = String.format("%.2f", iA);
                                experiment6ModelPhase3.setIA(iAString);
                                iAPercentD = Ixx / iA;
                                String iAPercent = String.format("%.2f", iAPercentD);
                                experiment6ModelPhase3.setIAPercent(iAPercent);
                                String iADiff = String.format("%.2f", IxxPercent - iAPercentD);
                                experiment6ModelPhase3.setIADiff(iADiff);
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
                                experiment6ModelPhase3.setIB(iBString);
                                iBPercentD = Ixx / iB;
                                String iBPercent = String.format("%.2f", iBPercentD);
                                experiment6ModelPhase3.setIBPercent(iBPercent);
                                String iBDiff = String.format("%.2f", IxxPercent - iBPercentD);
                                experiment6ModelPhase3.setIBDiff(iBDiff);
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
                                experiment6ModelPhase3.setIC(iCString);
                                IAvr = (iA + iB + iC) / 3;
                                iCPercentD = Ixx / iC;
                                String iCPercent = String.format("%.2f", iCPercentD);
                                experiment6ModelPhase3.setICPercent(iCPercent);
                                String iCDiff = String.format("%.2f", IxxPercent - iCPercentD);
                                experiment6ModelPhase3.setICDiff(iCDiff);
                            }
                        }
                        break;
                    case PM130Model.P_PARAM:
                        if (isNeedToRefresh) {
                            pPM130 = (float) value;
                            if (is200to5State) {
                                pPM130 *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                pPM130 *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                pPM130 *= STATE_5_TO_5_MULTIPLIER;
                            }
                            String PParma = String.format("%.2f", pPM130);
                            experiment6ModelPhase3.setPP(PParma);
                        }
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            fParma = (float) value;
                            experiment6ModelPhase3.setF(String.format("%.2f", fParma));
                        }
                        break;
                    case PM130Model.COS_PARAM:
                        if (isNeedToRefresh) {
                            cosParma = (float) value;
                            if (cosParma > 0.001) {
                                experiment6ModelPhase3.setCOS(String.format("%.2f", cosParma));
                            }
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value;
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value;
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            String UInAvr = String.format("%.2f", measuringUInAvr);
                            if (measuringUInAvr > 0.001) {
                                experiment6ModelPhase3.setUBH(UInAvr);
                            }
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