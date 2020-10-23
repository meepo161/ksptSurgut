package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.devices.avem_voltmeter.AvemVoltmeterModel;
import ru.avem.ksptsurgut.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptsurgut.communication.devices.parmaT400.ParmaT400Model;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.model.phase3.Experiment4ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;

import static ru.avem.ksptsurgut.Constants.Measuring.HZ;
import static ru.avem.ksptsurgut.Constants.Measuring.VOLT;
import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;
import static ru.avem.ksptsurgut.utils.Utils.formatRealNumber;
import static ru.avem.ksptsurgut.utils.Utils.sleep;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;

public class Experiment4ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 400;
    private static final double STATE_1_TO_5_MULTIPLIER = 0.2;
    private static final double STATE_10_TO_5_MULTIPLIER = 2.0;
    private static final double STATE_50_TO_5_MULTIPLIER = 10.0;

    @FXML
    private TableView<Experiment4ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUBHKZ1;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUBHKZ2;
    @FXML
    private TableColumn<Experiment4ModelPhase3, String> tableColumnUBHKZ3;
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

    private double UBHTestItem = currentProtocol.getUbh();
    private double UHHTestItem = currentProtocol.getUhh();
    private double UKZTestItem = currentProtocol.getUkz();
    private double Ukz = UBHTestItem * (UKZTestItem / 100.0) / 4;
    private double PInKVA = currentProtocol.getP();
    private double P = PInKVA * 1000;
    private double Iном = P / (Math.sqrt(3) * UBHTestItem);
    private double Ikz = Iном / 4.0;
    private double coef = 1;
    private Experiment4ModelPhase3 experiment4ModelPhase3;
    private ObservableList<Experiment4ModelPhase3> experiment4Data = FXCollections.observableArrayList();

    private boolean is50AState;
    private boolean is10AState;
    private boolean is1AState;

    private volatile double measuringIAvemA;
    private volatile double measuringIAvemB;
    private volatile double measuringIAvemC;

    private volatile double measuringIA;
    private volatile double measuringIB;
    private volatile double measuringIC;

    private volatile double ptRatio;
    private volatile double measuringPKZ;
    private volatile double measuringP;
    private volatile double measuringF;
    private volatile double measuringPParma;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double measuringUkzPercent;
    private volatile double measuringUAvem1;
    private volatile double measuringUAvem2;
    private volatile double measuringUAvem3;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment4ModelPhase3 = experimentsValuesModel.getExperiment4ModelPhase3();
        experiment4Data.add(experiment4ModelPhase3);
        tableViewExperimentValues.setItems(experiment4Data);
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);
        scrollPaneLog.vvalueProperty().bind(vBoxLog.heightProperty());

        tableColumnUBHKZ1.setCellValueFactory(cellData -> cellData.getValue().UBH1Property());
        tableColumnUBHKZ2.setCellValueFactory(cellData -> cellData.getValue().UBH2Property());
        tableColumnUBHKZ3.setCellValueFactory(cellData -> cellData.getValue().UBH3Property());
        tableColumnUKZPercent.setCellValueFactory(cellData -> cellData.getValue().UKZPercentProperty());
        tableColumnIA.setCellValueFactory(cellData -> cellData.getValue().IAProperty());
        tableColumnIB.setCellValueFactory(cellData -> cellData.getValue().IBProperty());
        tableColumnIC.setCellValueFactory(cellData -> cellData.getValue().ICProperty());
        tableColumnPp.setCellValueFactory(cellData -> cellData.getValue().PPProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().fProperty());
        tableColumnResultExperiment4.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE4UKZVA(experiment4ModelPhase3.getUBH1());
        currentProtocol.setE4UKZVB(experiment4ModelPhase3.getUBH2());
        currentProtocol.setE4UKZVC(experiment4ModelPhase3.getUBH3());
        currentProtocol.setE4UKZPercent(experiment4ModelPhase3.getUKZPercent());
        currentProtocol.setE4IA(experiment4ModelPhase3.getIA());
        currentProtocol.setE4IB(experiment4ModelPhase3.getIB());
        currentProtocol.setE4IC(experiment4ModelPhase3.getIC());
        currentProtocol.setE4Pp(experiment4ModelPhase3.getPP());
        currentProtocol.setE4F(experiment4ModelPhase3.getF());
        currentProtocol.setE4Result(experiment4ModelPhase3.getResult());

//        currentProtocol.setE4UKZVA("1");
//        currentProtocol.setE4UKZVB("2");
//        currentProtocol.setE4UKZVC("3");
//        currentProtocol.setE4UKZPercent("4");
//        currentProtocol.setE4IA("5");
//        currentProtocol.setE4IB("6");
//        currentProtocol.setE4IC("7");
//        currentProtocol.setE4Pp("8");
//        currentProtocol.setE4F("9");
//        currentProtocol.setE4Result("10");
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment4ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isDeltaResponding = false;
        setDeviceState(deviceStateCircleDELTACP2000, View.DeviceState.UNDEFINED);


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
                communicationModel.initExperiment4Devices();
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

            if (isExperimentRunning) {
                communicationModel.onKM1();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Идет загрузка ЧП");
            }

            int timeToSleep = 600;
            while (isExperimentRunning && (timeToSleep-- > 0)) {
                sleep(10);
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
                if (Ikz < 1) {
                    appendOneMessageToLog(Constants.LogTag.BLUE, "1А токовая ступень");
                    communicationModel.onKM69();
                    is1AState = true;
                    is10AState = false;
                    is50AState = false;
                } else if (Ikz > 1 && Ikz < 10) {
                    appendOneMessageToLog(Constants.LogTag.BLUE, "10А токовая ступень");
                    communicationModel.onKM58();
                    is1AState = false;
                    is10AState = true;
                    is50AState = false;
                } else if (Ikz >= 10) {
                    appendOneMessageToLog(Constants.LogTag.BLUE, "50А токовая ступень");
                    communicationModel.onKM47();
                    is1AState = false;
                    is10AState = false;
                    is50AState = true;
                } else {
                    cause = "Ток короткого замыкания не входит в пределы";
                }
            }

            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.onKM27();
            }
            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.onKM24();
            }
            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.onKM15();
            }
            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.onKM10();
            }

            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.setObjectParams(50 * HZ, 1 * VOLT, 50 * HZ);
                appendOneMessageToLog(Constants.LogTag.BLUE, "Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Запускаем ЧП");
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.ORANGE, "Поднимаем напряжение");
                regulation(1 * VOLT, 10, 4, UHHTestItem, 0.1, 2, 50, 100);
            }

            finalizeExperiment();

        }).start();
    }

    @Override
    protected void finalizeExperiment() {
        if (measuringIA > Ikz) {
            appendOneMessageToLog(Constants.LogTag.GREEN, "Достигли I короткого замыкания на фазе A.");
        }
        if (measuringIB > Ikz) {
            appendOneMessageToLog(Constants.LogTag.GREEN, "Достигли I короткого замыкания на фазе B.");
        }
        if (measuringIC > Ikz) {
            appendOneMessageToLog(Constants.LogTag.GREEN, "Достигли I короткого замыкания на фазе C.");
        }

        int timeToWait = 500;
        while ((timeToWait-- > 0) && isDevicesResponding() && !isStopButton) {
            sleep(10);
        }

        isNeedToRefresh = false;

        communicationModel.stopObject();
        sleep(3000);

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
            experiment4ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment4ModelPhase3.setResult("Неуспешно");
        } else {
            experiment4ModelPhase3.setResult("Успешно");
            appendMessageToLog(Constants.LogTag.GREEN, "Испытание завершено успешно");
        }
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isPM130Responding &&
                isDeltaResponding && isAvem1Responding &&
                isAvem2Responding && isAvem3Responding &&
                isParmaResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isPM130Responding ? "" : "ПМ130 ");
    }

    private int regulation(int start, int coarseStep, int fineStep, double end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentRunning && ((measuringUInAvr < end * coarseMinLimit) || (measuringUInAvr > end * coarseMaxLimit)) && isDevicesResponding()) {
            if (measuringUInAvr < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUInAvr > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringUInAvr < end - fineLimit) || (measuringUInAvr > end + fineLimit)) && isDevicesResponding()) {
            if (measuringUInAvr < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringUInAvr > end + fineLimit) {
                communicationModel.setObjectUMax(start -= fineStep);
            }
            sleep(fineSleep);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Выводим напряжение для получения заданного значения точно");
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
            case AVEM_A_ID:
                switch (param) {
                    case AvemVoltmeterModel.RESPONDING_PARAM:
                        isAvem1Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleAVEM1.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case AvemVoltmeterModel.U_PARAM:
                        if (isNeedToRefresh) {
                            measuringUAvem1 = (float) value;
                        }
                        break;
                }
                break;
            case AVEM_B_ID:
                switch (param) {
                    case AvemVoltmeterModel.RESPONDING_PARAM:
                        isAvem2Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleAVEM2.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case AvemVoltmeterModel.U_PARAM:
                        if (isNeedToRefresh) {
                            measuringUAvem2 = (float) value;
                        }
                        break;
                }
                break;
            case AVEM_C_ID:
                switch (param) {
                    case AvemVoltmeterModel.RESPONDING_PARAM:
                        isAvem3Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleAVEM3.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case AvemVoltmeterModel.U_PARAM:
                        if (isNeedToRefresh) {
                            measuringUAvem3 = (float) value;
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
                            measuringIAvemA = (double) value * 400 / 5;
                        }
                        break;
                    case ParmaT400Model.IB_PARAM:
                        if (isNeedToRefresh) {
                            measuringIAvemB = (double) value * 400 / 5;
                        }
                        break;
                    case ParmaT400Model.IC_PARAM:
                        if (isNeedToRefresh) {
                            measuringIAvemC = (double) value * 400 / 5;
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
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                            experiment4ModelPhase3.setUBH1(formatRealNumber(measuringUInAB));
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value;
                            experiment4ModelPhase3.setUBH2(formatRealNumber(measuringUInBC));
                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value;
                            experiment4ModelPhase3.setUBH3(formatRealNumber(measuringUInCA));
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3;
                            measuringUkzPercent = (((measuringUInAvr * 100.0) / UBHTestItem) * (Iном / ((measuringIA + measuringIB + measuringIC) / 3)));
                            if (measuringUkzPercent > 0) {
                                experiment4ModelPhase3.setUKZPercent(formatRealNumber(measuringUkzPercent));
                            }
                        }
                        break;
                    case PM130Model.I1_PARAM:
                        if (isNeedToRefresh) {
                            measuringIA = (float) value;
                            if (is50AState) {
                                measuringIA *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                measuringIA *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                measuringIA *= STATE_1_TO_5_MULTIPLIER;
                            }
                            if (measuringIA > 0.001) {
                                experiment4ModelPhase3.setIA(String.format("%.3f", measuringIA));
                            }
                            if (measuringIA > Ikz) {
                                isExperimentRunning = false;
                            }
                        }
                        break;
                    case PM130Model.I2_PARAM:
                        if (isNeedToRefresh) {
                            measuringIB = (float) value;
                            if (is50AState) {
                                measuringIB *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                measuringIB *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                measuringIB *= STATE_1_TO_5_MULTIPLIER;
                            }
                            if (measuringIB > 0.001) {
                                experiment4ModelPhase3.setIB(String.format("%.3f", measuringIB));
                            }
                            if (measuringIB > Ikz) {
                                isExperimentRunning = false;
                            }
                        }
                        break;
                    case PM130Model.I3_PARAM:
                        if (isNeedToRefresh) {
                            measuringIC = (float) value;
                            if (is50AState) {
                                measuringIC *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                measuringIC *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                measuringIC *= STATE_1_TO_5_MULTIPLIER;
                            }
                            if (measuringIC > 0.001) {
                                experiment4ModelPhase3.setIC(String.format("%.3f", measuringIC));
                            }
                            if (measuringIC > Ikz) {
                                isExperimentRunning = false;
                            }
                        }
                        break;
                    case PM130Model.P_PARAM:
                        if (isNeedToRefresh) {
                            measuringP = (float) value * Math.pow(Iном / ((measuringIA + measuringIB + measuringIC) / 3), 2) * 1000;
                            if (is50AState) {
                                measuringP *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                measuringP *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                measuringP *= STATE_1_TO_5_MULTIPLIER;
                            }
                            measuringPKZ = (measuringUAvem1 * measuringIAvemA + measuringUAvem2 * measuringIAvemB + measuringUAvem3 * measuringIAvemC) / 3;
                            if ((measuringIAvemA + measuringIAvemB + measuringIAvemC) / 3 > 50) {
                                measuringP -= measuringPKZ;
                            }
                            if (measuringP > 0.001) {
                                experiment4ModelPhase3.setPP(formatRealNumber(measuringP));
                            }
                        }
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            measuringF = (float) value;
                            experiment4ModelPhase3.setF(formatRealNumber(measuringF));
                        }
                        break;
                    case PM130Model.PT_PARAM:
                        if (isNeedToRefresh) {
                            ptRatio = (float) value;
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
                }
                break;
        }
    }
}