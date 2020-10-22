
package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.model.phase3.Experiment6ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;

import static ru.avem.ksptsurgut.Constants.Measuring.HZ;
import static ru.avem.ksptsurgut.Constants.Measuring.VOLT;
import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;
import static ru.avem.ksptsurgut.utils.Utils.formatRealNumber;
import static ru.avem.ksptsurgut.utils.Utils.sleep;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;

public class Experiment6ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 401;
    private static final double STATE_1_TO_5_MULTIPLIER = 0.2;
    private static final double STATE_10_TO_5_MULTIPLIER = 2.0;
    private static final double STATE_50_TO_5_MULTIPLIER = 10.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 1000;

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

    private double UBHTestItem = currentProtocol.getUbh();
    private double UHHTestItem = currentProtocol.getUhh();
    private final double coef = 6.66;
    private double PInKVA = currentProtocol.getP() * 1000;
    private double Iном = PInKVA / (Math.sqrt(3) * UHHTestItem);
    private double Ixx = Iном / 20;

    private Experiment6ModelPhase3 experiment6ModelPhase3;
    private ObservableList<Experiment6ModelPhase3> experiment6Data = FXCollections.observableArrayList();

    private boolean is50AState;
    private boolean is10AState;
    private boolean is1AState;

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

        scrollPaneLog.vvalueProperty().bind(vBoxLog.heightProperty());

        new Thread(() -> showRequestDialog("Отсоедините все провода и кабели от ВН объекта испытания.\n" +
                "После нажмите <Да>", true)).start();
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
        currentProtocol.setE6UInput(experiment6ModelPhase3.getUIN());
        currentProtocol.setE6IBH(experiment6ModelPhase3.getIBH());
        currentProtocol.setE6F(experiment6ModelPhase3.getF());
        currentProtocol.setE6Time(experiment6ModelPhase3.getTime());
        currentProtocol.setE6Result(experiment6ModelPhase3.getResult());

//        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
//        currentProtocol.setE6UInput("1");
//        currentProtocol.setE6IBH("2");
//        currentProtocol.setE6F("3");
//        currentProtocol.setE6Time("4");
//        currentProtocol.setE6Result("5");
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
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isDeltaResponding = false;
        setDeviceState(deviceStateCircleDELTACP2000, View.DeviceState.UNDEFINED);
        isPM130Responding = false;
        setDeviceState(deviceStateCirclePM130, View.DeviceState.UNDEFINED);

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

            if (isExperimentRunning) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Начало испытания");
                communicationModel.initOwenPrController();
                sleep(2000);
                communicationModel.initExperiment6Devices();
            }

            if (isExperimentRunning && !isOwenPRResponding) {
                setCause("Нет связи с ПР");
                sleep(100);
            }

            if (isExperimentRunning && isThereAreAccidents()) {
                appendOneMessageToLog(Constants.LogTag.RED, getAccidentsString("Аварии"));
            }

            if (isExperimentRunning && isOwenPRResponding) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация кнопочного поста...");
            }

            if (isExperimentRunning) {
                appendOneMessageToLog(Constants.LogTag.ORANGE, "Включите кнопочный пост");
                showInformDialogForButtonPost("Нажмите <ПУСК> кнопочного поста");
            }

            if (isExperimentRunning && isStartButtonOn) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Идет загрузка ЧП");
                communicationModel.onKM1();
                sleep(6000);
            }

            if (isExperimentRunning) {
                communicationModel.initDeltaCP();
                sleep(3000);
            }

            if (isExperimentRunning && !isDevicesResponding()) {
                setCause(getNotRespondingDevicesString("Нет связи с устройствами "));
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
                if (isExperimentRunning && UHHTestItem < WIDDING400) {
                    communicationModel.onKM2();

                    communicationModel.onKM11();
                    communicationModel.onKM14();
                    appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                    isExperimentRunning = false;
                }
                is50AState = true;
                is10AState = false;
                is1AState = false;
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
                if (Ixx < 1) {
                    appendOneMessageToLog(Constants.LogTag.BLUE, "1А токовая ступень");
                    communicationModel.onKM69();
                    is1AState = true;
                    is10AState = false;
                    is50AState = false;
                } else if (Ixx > 1 && Ixx < 10) {
                    appendOneMessageToLog(Constants.LogTag.BLUE, "10А токовая ступень");
                    communicationModel.onKM58();
                    is1AState = false;
                    is10AState = true;
                    is50AState = false;
                } else if (Ixx >= 10) {
                    appendOneMessageToLog(Constants.LogTag.BLUE, "50А токовая ступень");
                    communicationModel.onKM47();
                    is1AState = false;
                    is10AState = false;
                    is50AState = true;
                }
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                communicationModel.setObjectParams(200 * HZ, 5 * VOLT, 200 * HZ);
                appendOneMessageToLog(Constants.LogTag.BLUE, "Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Запускаем ЧП");
                sleep(3000);
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.ORANGE, "Поднимаем напряжение до " + (UHHTestItem * 2));
                regulation(5 * VOLT, 40, 8, UHHTestItem * 2, 0.1, 2, 100, 200);
            }

            int experimentTime = 30;
            while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
                sleep(1000);
                appendOneMessageToLog(Constants.LogTag.BLUE, "Ждем 30 секунд");
                experiment6ModelPhase3.setTime(String.valueOf(experimentTime));
            }

            finalizeExperiment();
        }).start();
    }


    @Override
    protected void finalizeExperiment() {
        experiment6ModelPhase3.setTime("30");
        isNeedToRefresh = false;
        sleep(100);

        communicationModel.stopObject();
        int time = 100;
        while (isExperimentRunning && (time-- > 0)) {
            sleep(10);
        }

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
            appendMessageToLog(Constants.LogTag.RED, String.format("Испытание прервано по причине: %s", cause));
            experiment6ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment6ModelPhase3.setResult("Неуспешно");
        } else {
            experiment6ModelPhase3.setResult("Успешно");
            appendMessageToLog(Constants.LogTag.GREEN, "Испытание завершено успешно");
        }
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
            appendOneMessageToLog(Constants.LogTag.BLUE, "Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringU < end - fineLimit) || (measuringU > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringU < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringU > end + fineLimit) {
                communicationModel.setObjectUMax(start -= fineStep);
            }
            sleep(fineSleep);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Выводим напряжение для получения заданного значения точно");
        }
        return start;
    }

    private void pickUpState() {
        if (is50AState) {
            if (measuringIAvr < 10.0 && measuringIAvr > 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 10A");
                communicationModel.onKM58();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = true;
                is1AState = false;
                communicationModel.offDO10();
                communicationModel.offDO12();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (measuringIAvr < 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 1A");
                communicationModel.onKM69();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = false;
                is1AState = true;
                communicationModel.offDO11();
                communicationModel.offDO12();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 50A");
            }
        } else if (is10AState) {
            if (measuringIAvr > 10) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 50A");
                communicationModel.onKM47();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = true;
                is10AState = false;
                is1AState = false;
                communicationModel.offDO10();
                communicationModel.offDO11();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (measuringIAvr < 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 1A");
                communicationModel.onKM69();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = false;
                is1AState = true;
                communicationModel.offDO11();
                communicationModel.offDO12();
                sleep(TIME_DELAY_CURRENT_STAGES);
            }
        } else if (is1AState) {
            if (measuringIAvr > 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 10A");
                communicationModel.onKM58();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = true;
                is1AState = false;
                communicationModel.offDO10();
                communicationModel.offDO12();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (measuringIAvr < 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 1A");
                communicationModel.onKM69();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = false;
                is1AState = true;
                communicationModel.offDO11();
                communicationModel.offDO12();
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
                        setDeviceState(deviceStateCirclePR200, (isOwenPRResponding) ? View.DeviceState.RESPONDING : View.DeviceState.NOT_RESPONDING);
                        break;
                    case OwenPRModel.PRI2_FIXED:
                        isCurrentOI = (boolean) value;
                        if (isCurrentOI) {
                            setCause("токовая защита ОИ\nВозможная причина: неисправность объекта испытания");
                        }
                        break;
                    case OwenPRModel.PRI3_FIXED:
                        isDoorSHSO = (boolean) value;
                        if (isDoorSHSO) {
                            setCause("открыты двери ШСО");
                        }
                        break;
                    case OwenPRModel.PRI5_FIXED:
                        isStopButton = (boolean) value;
                        if (isStopButton) {
                            setCause("Нажата кнопка СТОП");
                        }
                        break;
                    case OwenPRModel.PRI6_FIXED:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRI7_FIXED:
                        isDoorZone = (boolean) value;
                        if (isDoorZone) {
                            setCause("открыты двери зоны");
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
                    case PM130Model.I1_PARAM:
                        if (isNeedToRefresh) {
                            iA = (float) value;
                            if (is50AState) {
                                iA *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                iA *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                iA *= STATE_1_TO_5_MULTIPLIER;
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
                            if (is50AState) {
                                iB *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                iB *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                iB *= STATE_1_TO_5_MULTIPLIER;
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
                            if (is50AState) {
                                iC *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                iC *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                iC *= STATE_1_TO_5_MULTIPLIER;
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
                        if (isNeedToRefresh) {
                            experiment6ModelPhase3.setF(formatRealNumber(measuringF));
                        }
                        break;
                }
                break;
        }
    }
}