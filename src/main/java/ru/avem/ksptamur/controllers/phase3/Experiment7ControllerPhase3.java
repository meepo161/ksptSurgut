
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
import ru.avem.ksptamur.model.phase3.Experiment7ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment7ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final int WIDDING400 = 400;
    private static final int WIDDING1320 = 1320;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;
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
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment7ModelPhase3 experiment7ModelPhase3;
    private ObservableList<Experiment7ModelPhase3> experiment7Data = FXCollections.observableArrayList();

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
    private volatile boolean isDeltaReady200;
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
        experiment7ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isExperimentStart = true;
        isExperimentEnd = false;

        isDeltaResponding = false;
        isPM130Responding = false;

        isCurrentVIU = true;

        isPressedOk = false;
        cause = "";
        iAOld = -1;
        iBOld = -1;
        iCOld = -1;

        isPressedOk = false;

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
                communicationModel.initExperiment7Devices();
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
                    communicationModel.onKM2M1();
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с ВН до 1320В ");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный ВН в объекте испытания.");
                }
                communicationModel.onKM4();
                communicationModel.onKM1M1();
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                communicationModel.setObjectParams(200 * 100, 50 * 10, 200 * 100);
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog("Запускаем ЧП");
            }

            while (isExperimentStart && !isDeltaReady200 && isStopButtonOn) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь выйдет к заданным характеристикам");
            }

            if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
                if (UBHTestItem < WIDDING400) {
                    appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItem);
                    regulation(5 * 10, 25, 5, (int) (UHHTestItem), 0.4, 2, 100, 200);
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
                            iA = (double) value;
                            if (is200to5State) {
                                iA *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                iA *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                iA *= STATE_5_TO_5_MULTIPLIER;
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
                    case PM130Model.I2_PARAM:
                        if (isNeedToRefresh) {
                            iB = (double) value;
                            if (is200to5State) {
                                iB *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                iB *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                iB *= STATE_5_TO_5_MULTIPLIER;
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
                    case PM130Model.I3_PARAM:
                        if (isNeedToRefresh) {
                            iC = (double) value;
                            if (is200to5State) {
                                iC *= STATE_200_TO_5_MULTIPLIER;
                            } else if (is40to5State) {
                                iC *= STATE_40_TO_5_MULTIPLIER;
                            } else if (is5to5State) {
                                iC *= STATE_5_TO_5_MULTIPLIER;
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
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (double) value;
                            measuringUInABWithCoef = measuringUInAB;
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