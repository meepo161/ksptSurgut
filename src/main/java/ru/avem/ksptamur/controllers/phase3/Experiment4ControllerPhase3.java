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
import ru.avem.ksptamur.model.phase3.Experiment4ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.formatRealNumber;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment4ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 400;
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double STATE_40_TO_5_MULTIPLIER = 40.0 / 5.0;
    private static final double STATE_200_TO_5_MULTIPLIER = 200.0 / 5.0;

    @FXML
    private TableView<Experiment4ModelPhase3> tableViewExperimentValues;
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

    private double UBHTestItem = currentProtocol.getUbh();
    private double UHHTestItem = currentProtocol.getUhh();
    private double UKZTestItem = currentProtocol.getUkz();
    private double Ukz = UBHTestItem * (UKZTestItem / 100.0) / 4;
    private double PInKVA = currentProtocol.getP();
    private double P = PInKVA * 1000;
    private double Iном = P / (Math.sqrt(3) * UBHTestItem);
    private double Ikz = Iном / 4.0;
    private Experiment4ModelPhase3 experiment4ModelPhase3;
    private ObservableList<Experiment4ModelPhase3> experiment4Data = FXCollections.observableArrayList();

    private boolean is200to5State;
    private boolean is40to5State;
    private boolean is5to5State;

    private volatile double iA;
    private volatile double iB;
    private volatile double iC;

    private volatile double measuringP;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double measuringUkzPercent;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment4ModelPhase3 = experimentsValuesModel.getExperiment4ModelPhase3();
        experiment4Data.add(experiment4ModelPhase3);
        tableViewExperimentValues.setItems(experiment4Data);
        tableViewExperimentValues.setSelectionModel(null);
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
    protected void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE4UKZV(experiment4ModelPhase3.getUBH());
        currentProtocol.setE4UKZPercent(experiment4ModelPhase3.getUKZPercent());
        currentProtocol.setE4UKZDiff(experiment4ModelPhase3.getUKZDiff());
        currentProtocol.setE4IA(experiment4ModelPhase3.getIA());
        currentProtocol.setE4IB(experiment4ModelPhase3.getIB());
        currentProtocol.setE4IC(experiment4ModelPhase3.getIC());
        currentProtocol.setE4Pp(experiment4ModelPhase3.getPP());
        currentProtocol.setE4F(experiment4ModelPhase3.getF());
        currentProtocol.setE4Result(experiment4ModelPhase3.getResult());
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
        isNeedToWaitDelta = false;
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
            showRequestDialog("Отсоедините все провода и кабели от ОИ.\n" +
                    "Подключите кабели ОИ ВН, а сторону НН закоротите.\n" +
                    "После нажмите <Да>", true);

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment4Devices();
                sleep(2000);
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
                communicationModel.initExperiment4Devices();
                sleep(3000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment4Devices();
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
                if (Ukz < WIDDING400) {
                    communicationModel.onKM3();
                    communicationModel.onKM13();
                } else if (Ukz > WIDDING400) {
                    communicationModel.onKM2();
                    communicationModel.onKM13();
                }
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                communicationModel.setObjectParams(50 * HZ, 1 * VOLT, 50 * HZ);
                appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
                communicationModel.startObject();
                appendOneMessageToLog("Запускаем ЧП");
            }

            if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
                appendOneMessageToLog("Поднимаем напряжение до " + UHHTestItem);
                regulation(1 * VOLT, 10, 2, UHHTestItem, 0.1, 2, 100, 200);
            }

            finalizeExperiment();
        }).start();
    }

    @Override
    protected void finalizeExperiment() {
        sleep(100);
        isNeedToRefresh = false;
        sleep(3000);

        appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
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
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment4ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment4ModelPhase3.setResult("Неуспешно");
        } else {
            experiment4ModelPhase3.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isPM130Responding &&
                isDeltaResponding;
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
                    case OwenPRModel.PRI1:
                        break;
                    case OwenPRModel.PRI2:
                        break;
                    case OwenPRModel.PRI3:
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
                            if (iA > 0.001) {
                                experiment4ModelPhase3.setIA(String.format("%.3f", iA));
                            }
                            if (iA > Ikz) {
                                appendOneMessageToLog("Достигли I короткого замыкания на фазе A. Испытание остановлено");
                                isExperimentRunning = false;
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
                                experiment4ModelPhase3.setIB(String.format("%.3f", iB));
                            }
                            if (iB > Ikz) {
                                appendOneMessageToLog("Достигли I короткого замыкания на фазе B. Испытание остановлено");
                                isExperimentRunning = false;
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
                                experiment4ModelPhase3.setIC(String.format("%.3f", iC));
                            }
                            if (iC > Ikz) {
                                appendOneMessageToLog("Достигли I короткого замыкания на фазе C. Испытание остановлено");
                                isExperimentRunning = false;
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
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3;
                            experiment4ModelPhase3.setUBH(formatRealNumber(measuringUInAvr));
                            measuringUkzPercent = (measuringUInAvr * 100.0) / UBHTestItem;
                            experiment4ModelPhase3.setUKZPercent(formatRealNumber(measuringUkzPercent));
                            experiment4ModelPhase3.setUKZDiff(formatRealNumber(measuringUkzPercent - UKZTestItem));
                            if (measuringUInAvr > UHHTestItem) {
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
                            String PP = formatRealNumber(measuringP * 16);
                            experiment4ModelPhase3.setPP(PP);
                        }
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            String freq = formatRealNumber((float) value);
                            experiment4ModelPhase3.setF(freq);
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