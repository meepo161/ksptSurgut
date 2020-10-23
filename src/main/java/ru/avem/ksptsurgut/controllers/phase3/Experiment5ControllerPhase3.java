package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.model.phase3.Experiment5ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;

import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.PM130_ID;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.PR200_ID;
import static ru.avem.ksptsurgut.utils.Utils.formatRealNumber;
import static ru.avem.ksptsurgut.utils.Utils.sleep;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;

public class Experiment5ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING380 = 401;
    private static final double STATE_1_TO_5_MULTIPLIER = 0.2;
    private static final double STATE_10_TO_5_MULTIPLIER = 2.0;
    private static final double STATE_50_TO_5_MULTIPLIER = 10.0;
    private static final int TIME_DELAY_CURRENT_STAGES = 1000;

    @FXML
    private TableView<Experiment5ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableView<Experiment5ModelPhase3> tableViewExperimentValuesI;
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
    private TableColumn<Experiment5ModelPhase3, String> tableColumnPP;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnCOS;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment5ModelPhase3, String> tableColumnResultExperiment5;

    private double UBHTestItem = currentProtocol.getUbh();
    private double Pkva = currentProtocol.getP();
    private double UHHTestItem = currentProtocol.getUhh();
    private double Inom = Pkva * 1000 / (UHHTestItem * Math.sqrt(3));
    private double Time = currentProtocol.getXxtime();
    private int XXTime = (int) Time;

    private Experiment5ModelPhase3 experiment5ModelPhase3;
    private ObservableList<Experiment5ModelPhase3> experiment5Data = FXCollections.observableArrayList();

    private boolean is50AState;
    private boolean is10AState;
    private boolean is1AState;


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
    private volatile double cosParma;
    private volatile double IAvr;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment5ModelPhase3 = experimentsValuesModel.getExperiment5ModelPhase3();
        experiment5Data.add(experiment5ModelPhase3);
        tableViewExperimentValues.setItems(experiment5Data);
        tableViewExperimentValuesI.setItems(experiment5Data);
        tableViewExperimentValues.setSelectionModel(null);
        tableViewExperimentValuesI.setSelectionModel(null);
        communicationModel.addObserver(this);
        scrollPaneLog.vvalueProperty().bind(vBoxLog.heightProperty());

        tableColumnUBH.setCellValueFactory(cellData -> cellData.getValue().UBHProperty());
        tableColumnIA.setCellValueFactory(cellData -> cellData.getValue().IAProperty());
        tableColumnIB.setCellValueFactory(cellData -> cellData.getValue().IBProperty());
        tableColumnIC.setCellValueFactory(cellData -> cellData.getValue().ICProperty());
        tableColumnIAPercent.setCellValueFactory(cellData -> cellData.getValue().IAPercentProperty());
        tableColumnIBPercent.setCellValueFactory(cellData -> cellData.getValue().IBPercentProperty());
        tableColumnICPercent.setCellValueFactory(cellData -> cellData.getValue().ICPercentProperty());
        tableColumnPP.setCellValueFactory(cellData -> cellData.getValue().PProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().FProperty());
        tableColumnCOS.setCellValueFactory(cellData -> cellData.getValue().COSProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResultExperiment5.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        new Thread(() -> showRequestDialog("Отсоедините все провода и кабели от ВН объекта испытания.\n" +
                "После нажмите <Да>", true)).start();
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
        currentProtocol.setE5UBH(experiment5ModelPhase3.getUBH());
        currentProtocol.setE5IA(experiment5ModelPhase3.getIA());
        currentProtocol.setE5IB(experiment5ModelPhase3.getIB());
        currentProtocol.setE5IC(experiment5ModelPhase3.getIC());
        currentProtocol.setE5IAPercent(experiment5ModelPhase3.getIAPercent());
        currentProtocol.setE5IBPercent(experiment5ModelPhase3.getIBPercent());
        currentProtocol.setE5ICPercent(experiment5ModelPhase3.getICPercent());
        currentProtocol.setE5Pp(experiment5ModelPhase3.getPP());
        currentProtocol.setE5F(experiment5ModelPhase3.getF());
        currentProtocol.setE5Result(experiment5ModelPhase3.getResult());

//       Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
//       currentProtocol.setE5UBH("1");
//       currentProtocol.setE5IA("2");
//       currentProtocol.setE5IB("3");
//       currentProtocol.setE5IC("4");
//       currentProtocol.setE5IAPercent("5");
//       currentProtocol.setE5IBPercent("6");
//       currentProtocol.setE5ICPercent("7");
//       currentProtocol.setE5Pp("8");
//       currentProtocol.setE5F("9");
//       currentProtocol.setE5Result("10");
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment5ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isExperimentRunning = true;
        isExperimentEnded = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isPM130Responding = false;
        setDeviceState(deviceStateCirclePM130, View.DeviceState.UNDEFINED);

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
                communicationModel.initExperiment5Devices();
                experiment5ModelPhase3.setTime(String.valueOf(XXTime));
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
                communicationModel.initExperiment5Devices();
            }

            if (isExperimentRunning && !isDevicesResponding()) {
                setCause(getNotRespondingDevicesString("Нет связи с устройствами "));
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
                if (isExperimentRunning && UHHTestItem < WIDDING380) {
                    communicationModel.onKM1();
                    sleep(3000);
                    communicationModel.onKM15();
                    sleep(300);
                    communicationModel.onKM47();
                    sleep(300);
                    communicationModel.onKM11();
                    sleep(300);
                    communicationModel.onDOM6();
                    sleep(300);
                    communicationModel.onKM3();
                    sleep(500);
                    communicationModel.offDOM6();
                    appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                    isExperimentRunning = false;
                }
                is1AState = false;
                is10AState = false;
                is50AState = true;
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Идет подбор токовой ступени");
                sleep(5000);
                pickUpState();
                sleep(100);
                appendOneMessageToLog(Constants.LogTag.GREEN, "Токовая ступень подобрана");
                appendOneMessageToLog(Constants.LogTag.BLUE, "Измерение тока первичной обмотки и мощности потерь");
            }

            if (isExperimentRunning && isDevicesResponding()) {
                XXTime = (int) currentProtocol.getXxtime();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Ждем " + XXTime + " секунд");
            }

            while (isExperimentRunning && isDevicesResponding() && (XXTime-- > 0)) {
                sleep(1000);
                experiment5ModelPhase3.setTime(String.valueOf(XXTime));
            }

            if (isExperimentRunning && isDevicesResponding()) {
                experiment5ModelPhase3.setTime(String.valueOf((int) currentProtocol.getXxtime()));
            }

            finalizeExperiment();
        }).start();
    }

    @Override
    protected void finalizeExperiment() {
        isNeedToRefresh = false;
        sleep(100);

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
            experiment5ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment5ModelPhase3.setResult("Неуспешно");
        } else {
            experiment5ModelPhase3.setResult("Успешно");
            appendMessageToLog(Constants.LogTag.GREEN, "Испытание завершено успешно");
        }
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isPM130Responding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isPM130Responding ? "" : "ПМ130 ");
    }

    private void pickUpState() {
        if (is50AState) {
            if (IAvr < 10.0 && IAvr > 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 10A");
                communicationModel.onKM58();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = true;
                is1AState = false;
                communicationModel.offDO10();
                communicationModel.offDO12();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 1) {
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
            if (IAvr > 10) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 50A");
                communicationModel.onKM47();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = true;
                is10AState = false;
                is1AState = false;
                communicationModel.offDO10();
                communicationModel.offDO11();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 1) {
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
            if (IAvr > 1) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Выставляем токовую ступень 10A");
                communicationModel.onKM58();
                sleep(TIME_DELAY_CURRENT_STAGES);
                is50AState = false;
                is10AState = true;
                is1AState = false;
                communicationModel.offDO10();
                communicationModel.offDO12();
                sleep(TIME_DELAY_CURRENT_STAGES);
            } else if (IAvr < 1) {
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
                            if (iA > 0.001) {
                                String iAString = formatRealNumber(iA);
                                experiment5ModelPhase3.setIA(iAString);
                                iAPercentD = (iA / Inom) * 100;
                                String iAPercent = formatRealNumber(iAPercentD);
                                experiment5ModelPhase3.setIAPercent(iAPercent);
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
                            if (iB > 0.001) {
                                String iBString = formatRealNumber(iB);
                                experiment5ModelPhase3.setIB(iBString);
                                iBPercentD = (iB / Inom) * 100;
                                String iBPercent = formatRealNumber(iBPercentD);
                                experiment5ModelPhase3.setIBPercent(iBPercent);
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
                            if (iC > 0.001) {
                                String iCString = formatRealNumber(iC);
                                experiment5ModelPhase3.setIC(iCString);
                                IAvr = (iA + iB + iC) / 3;
                                iCPercentD = (iC / Inom) * 100;
                                String iCPercent = formatRealNumber(iCPercentD);
                                experiment5ModelPhase3.setICPercent(iCPercent);
                            }
                        }
                        break;
                    case PM130Model.P_PARAM:
                        if (isNeedToRefresh) {
                            measuringP = (float) value * 1000;
                            if (is50AState) {
                                measuringP *= STATE_50_TO_5_MULTIPLIER;
                            } else if (is10AState) {
                                measuringP *= STATE_10_TO_5_MULTIPLIER;
                            } else if (is1AState) {
                                measuringP *= STATE_1_TO_5_MULTIPLIER;
                            }
                            experiment5ModelPhase3.setPP(formatRealNumber(measuringP));
                        }
                        break;
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            fParma = (float) value;
                            experiment5ModelPhase3.setF(formatRealNumber(fParma));
                        }
                        break;
                    case PM130Model.COS_PARAM:
                        if (isNeedToRefresh) {
                            cosParma = (float) value;
                            if (cosParma > 0.001) {
                                experiment5ModelPhase3.setCOS(formatRealNumber(cosParma));
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
                            String UInAvr = formatRealNumber(measuringUInAvr);
                            if (measuringUInAvr > 0.001) {
                                experiment5ModelPhase3.setUBH(UInAvr);
                            }
                        }
                        break;
                }
                break;
        }
    }
}