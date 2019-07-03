
package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.phase3.Experiment6ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.formatRealNumber;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment6ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 400;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;

    @FXML
    private TableView<Experiment6ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnUInput;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnUOutput;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnIBH;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment6ModelPhase3, String> tableColumnResult;

    private double UHHTestItem = currentProtocol.getUhh();
    private double coef = 2.16;

    private Experiment6ModelPhase3 experiment6ModelPhase3;
    private ObservableList<Experiment6ModelPhase3> experiment6Data = FXCollections.observableArrayList();

    private boolean is200to5State;
    private boolean is40to5State;
    private boolean is5to5State;

    private volatile double iA;
    private volatile double iAOld;
    private volatile double iB;
    private volatile double iBOld;
    private volatile double iC;
    private volatile double iCOld;
    private volatile double measuringU;
    private volatile double measuringUA;
    private volatile double measuringUB;
    private volatile double measuringUC;
    private volatile double measuringF;
    private volatile double measuringIAvr;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment6ModelPhase3 = experimentsValuesModel.getExperiment6ModelPhase3();
        experiment6Data.add(experiment6ModelPhase3);
        tableViewExperimentValues.setItems(experiment6Data);
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnUInput.setCellValueFactory(cellData -> cellData.getValue().UINProperty());
        tableColumnIBH.setCellValueFactory(cellData -> cellData.getValue().IBHProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().fProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResult.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
        currentProtocol.setE6UInput(experiment6ModelPhase3.getUIN());
        currentProtocol.setE6IBH(experiment6ModelPhase3.getIBH());
        currentProtocol.setE6F(experiment6ModelPhase3.getF());
        currentProtocol.setE6Result(experiment6ModelPhase3.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment6ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isDeltaResponding = false;
        setDeviceState(deviceStateCircleDELTACP2000, View.DeviceState.UNDEFINED);
        isPM130Responding = false;
        setDeviceState(deviceStateCirclePM130, View.DeviceState.UNDEFINED);

        isNeedToWaitDelta = false;
        cause = "";
        iAOld = -1;
        iBOld = -1;
        iCOld = -1;

        isNeedToRefresh = true;

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            showRequestDialog("Подключите ОИ для определения МВЗ. После нажмите <Да>", true);

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment6Devices();
            }

            if (isExperimentRunning && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
            }

            if (isExperimentRunning && isThereAreAccidents()) {
                appendOneMessageToLog(getAccidentsString("Аварии"));
            }

            if (isExperimentRunning && isOwenPRResponding) {
                appendOneMessageToLog("Инициализация кнопочного поста...");
            }

            while (isExperimentRunning && !isStartButtonOn) {
                appendOneMessageToLog("Включите кнопочный пост");
                sleep(1);
                isNeedToWaitDelta = true;
            }

            if (isExperimentRunning && isNeedToWaitDelta && isStartButtonOn) {
                appendOneMessageToLog("Идет загрузка ЧП");
                sleep(6000);
                communicationModel.initExperiment6Devices();
                sleep(3000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment2Devices();
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                if (isExperimentRunning && UHHTestItem < WIDDING400) {
                    communicationModel.onKM2();
                    communicationModel.onKM5();
                    communicationModel.onKM13();
                    communicationModel.onK10();
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

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                communicationModel.setObjectParams(200 * HZ, 5 * VOLT, 200 * HZ);
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog("Запускаем ЧП");
                sleep(3000);
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Поднимаем напряжение до " + (UHHTestItem * 2));
                regulation(5 * VOLT, 40, 8, UHHTestItem * 2, 0.1, 2, 100, 200);
            }

            int experimentTime = 30;
            while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
                sleep(1000);
                appendOneMessageToLog("Ждем 30 секунд");
                experiment6ModelPhase3.setTime(String.valueOf(experimentTime));
            }

            finalizeExperiment();
        }).start();
    }


    @Override
    protected void finalizeExperiment() {
        isNeedToRefresh = false;
        sleep(100);

        appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        communicationModel.stopObject();
        sleep(6000);

        communicationModel.offAllKms();
        communicationModel.deinitPR();
        communicationModel.finalizeAllDevices();

        Platform.runLater(() -> {
            isExperimentRunning = false;
            isExperimentEnded = true;
            buttonCancelAll.setDisable(false);
            buttonStartStop.setText("Запустить повторно");
            buttonStartStop.setDisable(false);
            buttonNext.setDisable(false);
        });

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
        appendMessageToLog("------------------------------------------------\n");
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isPM130Responding && isDeltaResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isPM130Responding ? "" : "PM130 ");
    }

    private int regulation(int start, int coarseStep, int fineStep, double end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentRunning && ((measuringU < end * coarseMinLimit) || (measuringU > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringU < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringU > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringU < end - fineLimit) || (measuringU > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringU < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringU > end + fineLimit) {
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
                            if (iAOld != -1) {
                                if (iA > iAOld * 4 && iA > 2) {
                                    cause = "ток A превысил";
                                    isExperimentRunning = false;
                                } else {
                                    iAOld = iA;
                                }
                            } else {
                                iAOld = iA;
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
                            if (iBOld != -1) {
                                if (iB > iBOld * 4 && iB > 2) {
                                    cause = "ток B превысил";
                                    isExperimentRunning = false;
                                } else {
                                    iBOld = iB;
                                }
                            } else {
                                iBOld = iB;
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
                            if (iCOld != -1) {
                                if (iC > iCOld * 4 && iC > 2) {
                                    cause = "ток C превысил";
                                    isExperimentRunning = false;
                                } else {
                                    iCOld = iC;
                                }
                            } else {
                                iCOld = iC;
                            }
                            measuringIAvr = (iA + iB + iC) / 3;
                            experiment6ModelPhase3.setIBH(formatRealNumber(measuringIAvr));
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUA = (float) value * coef;
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUB = (float) value * coef;
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUC = (float) value * coef;
                            measuringU = (measuringUA + measuringUB + measuringUC) / 3;
                            experiment6ModelPhase3.setUIN(formatRealNumber(measuringU));
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
                        measuringF = (short) value / HZ;
                        experiment6ModelPhase3.setF(formatRealNumber(measuringF));
                        break;
                }
                break;
        }
    }
}