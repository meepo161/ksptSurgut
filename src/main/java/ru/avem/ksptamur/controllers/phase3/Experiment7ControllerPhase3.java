
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
import ru.avem.ksptamur.model.phase3.Experiment7ModelPhase3;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment7ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING418 = 418;
    private static final int WIDDING1320 = 1320;
    private static final float STATE_1_TO_5_MULTIPLIER = 1f / 5f;
    private static final int STATE_10_TO_5_MULTIPLIER = 10 / 5;
    private static final int STATE_75_TO_5_MULTIPLIER = 75 / 5;
    private static final int TIME_DELAY_CURRENT_STAGES = 100;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment7ModelPhase3> tableViewExperiment7;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnUInput;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnUOutput;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnIBH;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnResult;
    @FXML
    private TextArea textAreaExperiment7Log;
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
    private double UHHTestItemX2 = UHHTestItem * 2;
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment7ModelPhase3 experiment7ModelPhase3;
    private ObservableList<Experiment7ModelPhase3> experiment7Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentStart;
    private volatile boolean isExperimentEnd = true;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isDeltaReady200;
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
    private float temperature;
    private double iA;
    private double iAOld;
    private double iB;
    private double iBOld;
    private double iC;
    private double iCOld;
    private int phaseMeterState;
    private int windingGroup0;
    private int windingGroup1;
    private float measuringUOutAB;
    private double measuringUInAB;
    private float measuringF;
    private double coef;
    private volatile double F;
    private volatile double measuringIAvr;
    private volatile double measuringIA;
    private volatile double measuringIB;
    private volatile double measuringIC;
    private double measuringUInABWithCoef;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment7ModelPhase3 = mainModel.getExperiment7ModelPhase3();
        experiment7Data.add(experiment7ModelPhase3);
        tableViewExperiment7.setItems(experiment7Data);
        tableViewExperiment7.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnUInput.setCellValueFactory(cellData -> cellData.getValue().UINProperty());
        tableColumnIBH.setCellValueFactory(cellData -> cellData.getValue().IBHProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().FProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResult.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
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
        currentProtocol.setE7UInput(experiment7ModelPhase3.getUIN());
        currentProtocol.setE7IBH(experiment7ModelPhase3.getIBH());
        currentProtocol.setE7F(experiment7ModelPhase3.getF());
        currentProtocol.setE7Result(experiment7ModelPhase3.getResult());
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
        experiment7ModelPhase3.clearProperties();
        isDeltaResponding = false;
        isParmaResponding = false;
        cause = "";
        iAOld = -1;
        iBOld = -1;
        iCOld = -1;

        new Thread(() -> {
            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment7Devices();
                sleep(3000);
                experiment7ModelPhase3.setF("200,00");
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
                isStartButtonOn = true;
                is75to5State = true;
                sleep(1000);
            }

            while (isExperimentStart && !isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Включите кнопочный пост");
            }

            if (isExperimentStart && isStartButtonOn) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(8000);
                communicationModel.initExperiment7Devices();
            }


            while (isExperimentStart && !isDevicesResponding() && isStartButtonOn) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                communicationModel.onKM2();
                communicationModel.onKM7();
                communicationModel.onKM5M2();
                is75to5State = true;
                if (UHHTestItemX2 <= 380) {
                    coef = 1;
                    communicationModel.onKM2M1();
                    appendOneMessageToLog("Собрана схема для испытания 418В трансформатора");
                } else if (UHHTestItemX2 > 380) {
                    coef = 3.158;
                    communicationModel.onKM3M1();
                    appendOneMessageToLog("Собрана схема для испытания 1320В трансформатора");
                    communicationModel.onKM4M2();
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный HH в объекте испытания.");
                }
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                communicationModel.setObjectParams(200 * 100, 50 * 10, 200 * 100);
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
            }


            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                if (UBHTestItem < WIDDING418) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItemX2);
                    regulation(5 * 10, 25, 5, (int) (UHHTestItemX2 / coef), 0.4, 2, 100, 200);
                } else if (UBHTestItem < WIDDING1320) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItemX2);
                    regulation(5 * 10, 25, 5, (int) (UHHTestItemX2 / coef), 0.4, 2, 100, 200);
                }
            }

            int experimentTime = 30;
            while (isExperimentStart && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
                sleep(1000);
                appendOneMessageToLog("Ждем 30 секунд");
                experiment7ModelPhase3.setTime(String.valueOf(experimentTime));
            }

            isNeedToRefresh = false;
            isExperimentStart = false;
            isExperimentEnd = true;
            sleep(1000);
            communicationModel.stopObject();
            while (isExperimentStart && !isDeltaReady0 && isDeltaResponding && isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }

            sleep(500);

            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                experiment7ModelPhase3.setResult("Неуспешно");
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                experiment7ModelPhase3.setResult("Неуспешно");
            } else {
                experiment7ModelPhase3.setResult("Успешно");
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
        Platform.runLater(() -> textAreaExperiment7Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
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
        return isOwenPRResponding && isParmaResponding && isDeltaResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isParmaResponding ? "" : "Парма ");
    }

    private int regulation(int start, int coarseStep, int fineStep, int end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentStart && ((measuringUInAB < end * coarseMinLimit) || (measuringUInAB > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUInAB < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUInAB > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentStart && ((measuringUInAB < end - fineLimit) || (measuringUInAB > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
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
                            if (iAOld != -1) {
                                if (iA > iAOld * 4 && iA > 2) {
                                    cause = "ток A превысил";
                                    isExperimentStart = false;
                                } else {
                                    iAOld = iA;
                                }
                            } else {
                                iAOld = iA;
                            }
                            measuringIA = (double) ((int) (iA * 10000)) / 10000;
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
                            iB = (int) ((double) value * POWER) / POWER;
                            if (iBOld != -1) {
                                if (iB > iBOld * 4 && iB > 2) {
                                    cause = "ток B превысил";
                                    isExperimentStart = false;
                                } else {
                                    iBOld = iB;
                                }
                            } else {
                                iBOld = iB;
                            }
                            measuringIB = (double) ((int) (iB * 10000)) / 10000;
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
                            iC = ((int) ((double) value * POWER) / POWER);
                            if (iCOld != -1) {
                                if (iC > iCOld * 4 && iC > 2) {
                                    cause = "ток C превысил";
                                    isExperimentStart = false;
                                } else {
                                    iCOld = iC;
                                }
                            } else {
                                iCOld = iC;
                            }
                            measuringIC = (double) ((int) (iC * 10000)) / 10000;
                            measuringIAvr = (int) (((measuringIA + measuringIB + measuringIC) / 3.0) * POWER) / POWER;
                            experiment7ModelPhase3.setIBH((double) ((int) (measuringIAvr * 10000)) / 10000);
                        }
                        break;
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (double) value * coef;
                            measuringUInABWithCoef = measuringUInAB * coef;
                            String UInAB = String.format("%.2f", measuringUInABWithCoef);
                            if (measuringUInAB > 0.001) {
                                experiment7ModelPhase3.setUIN(UInAB);
                                sleep(50);
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
        isDeltaReady200 = value == 20000;
        isDeltaReady0 = value == 0;
    }
}