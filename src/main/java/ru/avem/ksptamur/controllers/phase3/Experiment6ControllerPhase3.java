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
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment6ModelPhase3;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment6ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING418 = 418;
    private static final int WIDDING1320 = 1320;
    private static final float STATE_1_TO_5_MULTIPLIER = 1f / 5f;
    private static final float STATE_10_TO_5_MULTIPLIER = 10f / 5f;
    private static final float STATE_75_TO_5_MULTIPLIER = 75f / 5f;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
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
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentStart;
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
    private volatile double pParma;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double iAPercentD;
    private volatile double iBPercentD;
    private volatile double iCPercentD;
    private double cosParma;

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


    private void stopExperiment() {
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
        experiment6ModelPhase3.clearProperties();
        isDeltaResponding = false;
        isParmaResponding = false;
        cause = "";

        new Thread(() -> {
            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment6Devices();
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
                communicationModel.onKM1PermissionButtonPost();
                isStartButtonOn = true;
                is75to5State = true;
                sleep(1000);
            }

            while (isExperimentStart && !isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Включите кнопочный пост");
            }

            if (isExperimentStart) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(8000);
                communicationModel.initExperiment6Devices();
            }

            while (isExperimentStart && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            isDeviceOn = true;

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                communicationModel.onKM2TP1();
                communicationModel.onKM7CurrentProtection75A();
                is75to5State = true;
                if (UBHTestItem <= WIDDING418) {
                    communicationModel.onKM2M1TP418();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 418В");
                } else if (UBHTestItem > WIDDING418) {
                    communicationModel.onKM3M1TP1320();
                    communicationModel.onKM4M2Parma418();
                    communicationModel.onKM5M2ChangeWinding();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
                }
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
                if (UBHTestItem <= WIDDING418) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UBHTestItem);
                    regulation(5 * 10, 15, 2, UBHTestItem, 0.10, 2, 100, 200);
                } else if (UBHTestItem > WIDDING418) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItem);
                    regulation(5 * 10, 10, 2, UHHTestItem, 0.1, 2, 100, 200);
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректные данные в объекте испытания.");
                }
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Идет подбор токовой ступени");
                pickUpState();
                sleep(100);
                appendOneMessageToLog("Токовая ступень подобрана");
                appendOneMessageToLog("Измерение тока первичной обмотки и мощности потерь");
            }

            XXTime = (int) currentProtocol.getXxtime();
            while (isExperimentStart && isStartButtonOn && isDevicesResponding() && (XXTime-- > 0)) {
                sleep(1000);

                appendOneMessageToLog("Ждем " + XXTime + " секунд");
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
            sleep(500);

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
        if (is75to5State) {
            if (iA < 12.0 && iA > 1.2) {
                communicationModel.onKM8CurrentProtection10A();
                sleep(200);
                is75to5State = false;
                is10to5State = true;
                is1to5State = false;
                communicationModel.offKM7CurrentProtection75A();
                communicationModel.offKM1M1CurrentProtection1A();
                appendOneMessageToLog("Выставляем токовую ступень 10/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (iA < 1.2) {
                communicationModel.onKM1M1CurrentProtection1A();
                sleep(200);
                is75to5State = false;
                is10to5State = false;
                is1to5State = true;
                communicationModel.offKM7CurrentProtection75A();
                communicationModel.offKM8CurrentProtection10A();
                appendOneMessageToLog("Выставляем токовую ступень 1/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is10to5State) {
            if (iA > 12) {
                communicationModel.onKM7CurrentProtection75A();
                sleep(200);
                is75to5State = true;
                is10to5State = false;
                is1to5State = false;
                communicationModel.offKM1M1CurrentProtection1A();
                communicationModel.offKM8CurrentProtection10A();
                appendOneMessageToLog("Выставляем токовую ступень 75/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (iA < 1.2) {
                communicationModel.onKM1M1CurrentProtection1A();
                sleep(100);
                is75to5State = false;
                is10to5State = false;
                is1to5State = true;
                communicationModel.offKM7CurrentProtection75A();
                communicationModel.offKM8CurrentProtection10A();
                appendOneMessageToLog("Выставляем токовую ступень 1/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is1to5State) {
            if (iA > 1.2) {
                communicationModel.onKM8CurrentProtection10A();
                sleep(100);
                is75to5State = false;
                is10to5State = true;
                is1to5State = false;
                communicationModel.offKM8CurrentProtection10A();
                communicationModel.offKM1M1CurrentProtection1A();
                appendOneMessageToLog("Выставляем токовую ступень 10/5");
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (iA < 1.2) {
                communicationModel.onKM1M1CurrentProtection1A();
                sleep(100);
                is75to5State = false;
                is10to5State = false;
                is1to5State = true;
                communicationModel.offKM7CurrentProtection75A();
                communicationModel.offKM8CurrentProtection10A();
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
                    case OwenPRModel.DI5_START_BTN:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.DI6_STOP_BTN:
                        isStopButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.DI6_STOP_BTN_FIXED:
                        if ((boolean) value) {
                            cause = "Нажата кнопка (СТОП)";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.DI1_CURRENT_1:
                        isCurrent1On = (boolean) value;
                        if (!isCurrent1On) {
                            cause = "сработала токовая защита 1";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.DI2_CURRENT_DELTA:
                        isCurrent2On = (boolean) value;
                        if (!isCurrent2On) {
                            cause = "сработала токовая защита 2";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.DI3_DOOR_BLOCK:
                        isDoorLockOn = (boolean) value;
                        if (!isDoorLockOn) {
                            cause = "открыта дверь";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.DI4_INSULATION:
                        isInsulationOn = (boolean) value;
                        if (!isInsulationOn) {
                            cause = "пробита изоляция";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.DI7_DOOR_ZONE:
                        isDoorZoneOn = (boolean) value;
                        if (!isDoorZoneOn) {
                            cause = "открыта дверь зоны";
                            isExperimentStart = false;
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
                                iA *= STATE_75_TO_5_MULTIPLIER;
                            } else if (is10to5State) {
                                iA *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1to5State) {
                                iA *= STATE_1_TO_5_MULTIPLIER;
                            }
                            iA = (double) ((int) (iA * 10000)) / 10000;
                            if (iA > 0.001) {
                                experiment6ModelPhase3.setIA(iA);
                                iAPercentD = Ixx / iA;
                                String iAPercent = String.format("%.2f", iAPercentD);
                                experiment6ModelPhase3.setIAPercent(iAPercent);
                                String iADiff = String.format("%.2f", IxxPercent - iAPercentD);
                                experiment6ModelPhase3.setIADiff(iADiff);
                            }
                        }
                        break;
                    case ParmaT400Model.IB_PARAM:
                        if (isNeedToRefresh) {
                            iB = (double) value;
                            if (is75to5State) {
                                iB *= STATE_75_TO_5_MULTIPLIER;
                            } else if (is10to5State) {
                                iB *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1to5State) {
                                iB *= STATE_1_TO_5_MULTIPLIER;
                            }
                            iB = (double) ((int) (iB * 10000)) / 10000;
                            if (iB > 0.001) {
                                experiment6ModelPhase3.setIB(iB);
                                iBPercentD = Ixx / iB;
                                String iBPercent = String.format("%.2f", iBPercentD);
                                experiment6ModelPhase3.setIBPercent(iBPercent);
                                String iBDiff = String.format("%.2f", IxxPercent - iBPercentD);
                                experiment6ModelPhase3.setIBDiff(iBDiff);
                            }
                        }
                        break;
                    case ParmaT400Model.IC_PARAM:
                        if (isNeedToRefresh) {
                            iC = (double) value;
                            if (is75to5State) {
                                iC *= STATE_75_TO_5_MULTIPLIER;
                            } else if (is10to5State) {
                                iC *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1to5State) {
                                iC *= STATE_1_TO_5_MULTIPLIER;
                            }
                            iC = (double) ((int) (iB * 10000)) / 10000;
                            if (iC > 0.001) {
                                experiment6ModelPhase3.setIC(iC);
                                iCPercentD = Ixx / iC;
                                String iCPercent = String.format("%.2f", iCPercentD);
                                experiment6ModelPhase3.setICPercent(iCPercent);
                                String iCDiff = String.format("%.2f", IxxPercent - iCPercentD);
                                experiment6ModelPhase3.setICDiff(iCDiff);
                            }
                        }
                        break;
                    case ParmaT400Model.P_PARAM:
                        if (isNeedToRefresh) {
                            pParma = (double) value;
                            if (is75to5State) {
                                pParma *= STATE_75_TO_5_MULTIPLIER;
                            } else if (is10to5State) {
                                pParma *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1to5State) {
                                pParma *= STATE_1_TO_5_MULTIPLIER;
                            }
                            String PParma = String.format("%.2f", pParma);
                            experiment6ModelPhase3.setPP(PParma);
                        }
                        break;
                    case ParmaT400Model.F_PARAM:
                        if (isNeedToRefresh) {
                            fParma = (double) value;
                            experiment6ModelPhase3.setF(String.valueOf(fParma));
                        }
                        break;
                    case ParmaT400Model.COS_PARAM:
                        if (isNeedToRefresh) {
                            cosParma = (double) value;
                            if (cosParma > 0.001) {
                                experiment6ModelPhase3.setCOS(String.valueOf(cosParma));
                            }
                        }
                        break;
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (double) value;
                        }
                        break;
                    case ParmaT400Model.UBC_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (double) value;
                        }
                        break;
                    case ParmaT400Model.UCA_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (double) value;
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