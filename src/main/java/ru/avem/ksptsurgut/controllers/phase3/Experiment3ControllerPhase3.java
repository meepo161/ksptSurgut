package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.communication.devices.phasemeter.PhaseMeterModel;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.model.phase3.Experiment3ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;
import static ru.avem.ksptsurgut.utils.Utils.formatRealNumber;
import static ru.avem.ksptsurgut.utils.Utils.sleep;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;
import static ru.avem.ksptsurgut.utils.View.showConfirmDialog;

public class Experiment3ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING380 = 401;

    @FXML
    private TableView<Experiment3ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUInputAB;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUInputBC;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUInputCA;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUInputAvr;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUOutputAB;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUOutputBC;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUOutputCA;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUOutputAvr;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnUDiff;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnGroupBH;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnGroupHH;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnF;
    @FXML
    private TableColumn<Experiment3ModelPhase3, String> tableColumnResultExperiment;

    private double UHHTestItem = currentProtocol.getUhh();
    private double UBHTestItem = currentProtocol.getUbh();
    private double coefTransf = UBHTestItem / UHHTestItem;


    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private ObservableList<Experiment3ModelPhase3> experiment3Data = FXCollections.observableArrayList();


    private Experiment3ModelPhase3 Experiment3ModelPhase3BH = experimentsValuesModel.getExperiment3ModelPhase3();
    private Experiment3ModelPhase3 Experiment3ModelPhase3BH2 = experimentsValuesModel.getExperiment3ModelPhase3BH2();
    private Experiment3ModelPhase3 Experiment3ModelPhase3BH3 = experimentsValuesModel.getExperiment3ModelPhase3BH3();
    private Experiment3ModelPhase3 Experiment3ModelPhase3BH4 = experimentsValuesModel.getExperiment3ModelPhase3BH4();
    private Experiment3ModelPhase3 Experiment3ModelPhase3BH5 = experimentsValuesModel.getExperiment3ModelPhase3BH5();

    private boolean isBHStarted;
    private boolean isBH2Started;
    private boolean isBH3Started;
    private boolean isBH4Started;
    private boolean isBH5Started;

    private volatile boolean isNeedToWaitDelta;

    private volatile double measuringUOutAB;
    private volatile double measuringUOutBC;
    private volatile double measuringUOutCA;
    private volatile double measuringUOutAvr;
    private volatile double measuringUInAB;
    private volatile double measuringUInBC;
    private volatile double measuringUInCA;
    private volatile double measuringUInAvr;
    private volatile double windingGroup0;
    private volatile double windingGroup1;
    private volatile double measuringF;

    int timeOut = 120;

    @FXML
    public void initialize() {
        setTheme(root);
        Experiment3ModelPhase3BH = experimentsValuesModel.getExperiment3ModelPhase3();
        Experiment3ModelPhase3BH2 = experimentsValuesModel.getExperiment3ModelPhase3BH2();
        Experiment3ModelPhase3BH3 = experimentsValuesModel.getExperiment3ModelPhase3BH3();
        Experiment3ModelPhase3BH4 = experimentsValuesModel.getExperiment3ModelPhase3BH4();
        Experiment3ModelPhase3BH5 = experimentsValuesModel.getExperiment3ModelPhase3BH5();
        experiment3Data.add(Experiment3ModelPhase3BH);
        experiment3Data.add(Experiment3ModelPhase3BH2);
        experiment3Data.add(Experiment3ModelPhase3BH3);
        experiment3Data.add(Experiment3ModelPhase3BH4);
        experiment3Data.add(Experiment3ModelPhase3BH5);
        tableViewExperimentValues.setItems(FXCollections.observableArrayList(
                Experiment3ModelPhase3BH,
                Experiment3ModelPhase3BH2,
                Experiment3ModelPhase3BH3,
                Experiment3ModelPhase3BH4,
                Experiment3ModelPhase3BH5));

        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);
        scrollPaneLog.vvalueProperty().bind(vBoxLog.heightProperty());

        tableColumnUOutputAB.setCellValueFactory(cellData -> cellData.getValue().uOutputABProperty());
        tableColumnUOutputBC.setCellValueFactory(cellData -> cellData.getValue().uOutputBCProperty());
        tableColumnUOutputCA.setCellValueFactory(cellData -> cellData.getValue().uOutputCAProperty());
        tableColumnUOutputAvr.setCellValueFactory(cellData -> cellData.getValue().uOutputAvrProperty());
        tableColumnUInputAB.setCellValueFactory(cellData -> cellData.getValue().uInputABProperty());
        tableColumnUInputBC.setCellValueFactory(cellData -> cellData.getValue().uInputBCProperty());
        tableColumnUInputCA.setCellValueFactory(cellData -> cellData.getValue().uInputCAProperty());
        tableColumnUInputAvr.setCellValueFactory(cellData -> cellData.getValue().uInputAvrProperty());
        tableColumnUDiff.setCellValueFactory(cellData -> cellData.getValue().uDiffProperty());
        tableColumnGroupBH.setCellValueFactory(cellData -> cellData.getValue().groupBHProperty());
        tableColumnGroupHH.setCellValueFactory(cellData -> cellData.getValue().groupHHProperty());
        tableColumnF.setCellValueFactory(cellData -> cellData.getValue().fProperty());
        tableColumnResultExperiment.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE3UInputAB(Experiment3ModelPhase3BH.getuInputAB());
        currentProtocol.setE3UInputBC(Experiment3ModelPhase3BH.getuInputBC());
        currentProtocol.setE3UInputCA(Experiment3ModelPhase3BH.getuInputCA());
        currentProtocol.setE3UInputAvr(Experiment3ModelPhase3BH.getuInputAvr());

        currentProtocol.setE3UOutputAB(Experiment3ModelPhase3BH.getuOutputAB());
        currentProtocol.setE3UOutputBC(Experiment3ModelPhase3BH.getuOutputBC());
        currentProtocol.setE3UOutputCA(Experiment3ModelPhase3BH.getuOutputCA());
        currentProtocol.setE3UOutputAvr(Experiment3ModelPhase3BH.getuOutputAvr());

        currentProtocol.setE3DiffU(Experiment3ModelPhase3BH.getuDiff());
        currentProtocol.setE3WindingBH(Experiment3ModelPhase3BH.getGroupBH());
        currentProtocol.setE3WindingHH(Experiment3ModelPhase3BH.getGroupHH());
        currentProtocol.setE3F(Experiment3ModelPhase3BH.getF());
        currentProtocol.setE3Result(Experiment3ModelPhase3BH.getResult());

        currentProtocol.setE3UInputAB2(Experiment3ModelPhase3BH2.getuInputAB());
        currentProtocol.setE3UInputBC2(Experiment3ModelPhase3BH2.getuInputBC());
        currentProtocol.setE3UInputCA2(Experiment3ModelPhase3BH2.getuInputCA());
        currentProtocol.setE3UInputAvr2(Experiment3ModelPhase3BH2.getuInputAvr());

        currentProtocol.setE3UOutputAB2(Experiment3ModelPhase3BH2.getuOutputAB());
        currentProtocol.setE3UOutputBC2(Experiment3ModelPhase3BH2.getuOutputBC());
        currentProtocol.setE3UOutputCA2(Experiment3ModelPhase3BH2.getuOutputCA());
        currentProtocol.setE3UOutputAvr2(Experiment3ModelPhase3BH2.getuOutputAvr());

        currentProtocol.setE3DiffU2(Experiment3ModelPhase3BH2.getuDiff());
        currentProtocol.setE3WindingBH2(Experiment3ModelPhase3BH2.getGroupBH());
        currentProtocol.setE3WindingHH2(Experiment3ModelPhase3BH2.getGroupHH());
        currentProtocol.setE3F2(Experiment3ModelPhase3BH2.getF());
        currentProtocol.setE3Result2(Experiment3ModelPhase3BH2.getResult());

        currentProtocol.setE3UInputAB3(Experiment3ModelPhase3BH3.getuInputAB());
        currentProtocol.setE3UInputBC3(Experiment3ModelPhase3BH3.getuInputBC());
        currentProtocol.setE3UInputCA3(Experiment3ModelPhase3BH3.getuInputCA());
        currentProtocol.setE3UInputAvr3(Experiment3ModelPhase3BH3.getuInputAvr());

        currentProtocol.setE3UOutputAB3(Experiment3ModelPhase3BH3.getuOutputAB());
        currentProtocol.setE3UOutputBC3(Experiment3ModelPhase3BH3.getuOutputBC());
        currentProtocol.setE3UOutputCA3(Experiment3ModelPhase3BH3.getuOutputCA());
        currentProtocol.setE3UOutputAvr3(Experiment3ModelPhase3BH3.getuOutputAvr());

        currentProtocol.setE3DiffU3(Experiment3ModelPhase3BH3.getuDiff());
        currentProtocol.setE3WindingBH3(Experiment3ModelPhase3BH3.getGroupBH());
        currentProtocol.setE3WindingHH3(Experiment3ModelPhase3BH3.getGroupHH());
        currentProtocol.setE3F3(Experiment3ModelPhase3BH3.getF());
        currentProtocol.setE3Result3(Experiment3ModelPhase3BH3.getResult());

        currentProtocol.setE3UInputAB4(Experiment3ModelPhase3BH4.getuInputAB());
        currentProtocol.setE3UInputBC4(Experiment3ModelPhase3BH4.getuInputBC());
        currentProtocol.setE3UInputCA4(Experiment3ModelPhase3BH4.getuInputCA());
        currentProtocol.setE3UInputAvr4(Experiment3ModelPhase3BH4.getuInputAvr());

        currentProtocol.setE3UOutputAB4(Experiment3ModelPhase3BH4.getuOutputAB());
        currentProtocol.setE3UOutputBC4(Experiment3ModelPhase3BH4.getuOutputBC());
        currentProtocol.setE3UOutputCA4(Experiment3ModelPhase3BH4.getuOutputCA());
        currentProtocol.setE3UOutputAvr4(Experiment3ModelPhase3BH4.getuOutputAvr());

        currentProtocol.setE3DiffU4(Experiment3ModelPhase3BH4.getuDiff());
        currentProtocol.setE3WindingBH4(Experiment3ModelPhase3BH4.getGroupBH());
        currentProtocol.setE3WindingHH4(Experiment3ModelPhase3BH4.getGroupHH());
        currentProtocol.setE3F4(Experiment3ModelPhase3BH4.getF());
        currentProtocol.setE3Result4(Experiment3ModelPhase3BH4.getResult());

        currentProtocol.setE3UInputAB5(Experiment3ModelPhase3BH5.getuInputAB());
        currentProtocol.setE3UInputBC5(Experiment3ModelPhase3BH5.getuInputBC());
        currentProtocol.setE3UInputCA5(Experiment3ModelPhase3BH5.getuInputCA());
        currentProtocol.setE3UInputAvr5(Experiment3ModelPhase3BH5.getuInputAvr());

        currentProtocol.setE3UOutputAB5(Experiment3ModelPhase3BH5.getuOutputAB());
        currentProtocol.setE3UOutputBC5(Experiment3ModelPhase3BH5.getuOutputBC());
        currentProtocol.setE3UOutputCA5(Experiment3ModelPhase3BH5.getuOutputCA());
        currentProtocol.setE3UOutputAvr5(Experiment3ModelPhase3BH5.getuOutputAvr());

        currentProtocol.setE3DiffU5(Experiment3ModelPhase3BH5.getuDiff());
        currentProtocol.setE3WindingBH5(Experiment3ModelPhase3BH5.getGroupBH());
        currentProtocol.setE3WindingHH5(Experiment3ModelPhase3BH5.getGroupHH());
        currentProtocol.setE3F5(Experiment3ModelPhase3BH5.getF());
        currentProtocol.setE3Result5(Experiment3ModelPhase3BH5.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        Experiment3ModelPhase3BH.clearProperties();
        Experiment3ModelPhase3BH2.clearProperties();
        Experiment3ModelPhase3BH3.clearProperties();
        Experiment3ModelPhase3BH4.clearProperties();
        Experiment3ModelPhase3BH5.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = true;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        setDeviceState(deviceStateCirclePM130, View.DeviceState.UNDEFINED);
        isPhaseMeterResponding = false;
        setDeviceState(deviceStateCirclePhaseMeter, View.DeviceState.UNDEFINED);


        isNeedToRefresh = true;

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            if (isExperimentRunning) {
                Experiment3ModelPhase3BH.setGroupBH(String.valueOf(0));
                Experiment3ModelPhase3BH.setGroupHH(String.valueOf(0));
                appendOneMessageToLog(Constants.LogTag.BLUE, "Начало испытания");
                communicationModel.initOwenPrController();
                sleep(2000);
                communicationModel.initExperiment3Devices();
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

            if (isExperimentRunning && !isDevicesResponding()) {
                setCause(getNotRespondingDevicesString("Нет связи с устройствами "));
            }

            isNeedCheckDoor = true;
            startKTR1();

            isNeedCheckDoor = false;

            if (isExperimentRunning) {

                AtomicInteger nextOrEnd = showWindowConfirm();

                if (nextOrEnd.get() == 1) {
                    if (isExperimentRunning && isDevicesResponding()) {
                        isNeedCheckDoor = true;
                        startKTR2();
                    }

                    isNeedCheckDoor = false;

                    if (isExperimentRunning) {
                        AtomicInteger nextOrEnd2 = showWindowConfirm();
                        if (nextOrEnd2.get() == 1) {
                            if (isExperimentRunning && isDevicesResponding()) {
                                isNeedCheckDoor = true;
                                startKTR3();
                            }

                            isNeedCheckDoor = false;

                            if (isExperimentRunning) {
                                AtomicInteger nextOrEnd3 = showWindowConfirm();
                                if (nextOrEnd3.get() == 1) {
                                    if (isExperimentRunning && isDevicesResponding()) {
                                        isNeedCheckDoor = true;
                                        startKTR4();
                                    }

                                    isNeedCheckDoor = false;

                                    if (isExperimentRunning) {
                                        AtomicInteger nextOrEnd4 = showWindowConfirm();
                                        if (nextOrEnd4.get() == 1) {
                                            if (isExperimentRunning && isDevicesResponding()) {
                                                isNeedCheckDoor = true;
                                                startKTR5();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            finalizeExperiment();
        }).start();
    }

    private AtomicInteger showWindowConfirm() {
        AtomicInteger nextOrEnd = new AtomicInteger(0);
        Platform.runLater(() -> showConfirmDialog("Переключите трансформатор в следующее положение или нажмите (Нет)",
                () -> nextOrEnd.set(1),
                () -> nextOrEnd.set(2)));
        timeOut = 120;
        while (nextOrEnd.get() == 0 && timeOut-- > 0) {
            sleep(1000);
        }
        return nextOrEnd;
    }

    private void startKTR1() {
        isBHStarted = true;
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");

            if (isExperimentRunning && UHHTestItem < WIDDING380) {
                communicationModel.onKM1();
                communicationModel.onKM10();
                communicationModel.onKM15();
                communicationModel.onKM1213();
                communicationModel.onKM47();
                communicationModel.onKM3();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
            } else {
                communicationModel.offAllKms();
                appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            communicationModel.startPhaseMeter();
            appendOneMessageToLog(Constants.LogTag.BLUE, "Началось измерение");

            int timeToWait = 500;
            while ((timeToWait-- > 0) && isDevicesResponding() && isExperimentRunning) {
                sleep(10);
            }

            Experiment3ModelPhase3BH.setGroupBH(String.valueOf(windingGroup0));
            Experiment3ModelPhase3BH.setGroupHH(String.valueOf(windingGroup1));
            Experiment3ModelPhase3BH.setuDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
            if (measuringUInAvr / measuringUOutAvr * 2 < coefTransf || (measuringUInAvr / measuringUOutAvr) * 0.5 > coefTransf) {
                setCause("Расхождение коэффицента трансформации от заданного.\n" +
                        " Проверьте правильность соединения измерительных крокодилов");
            }
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение завершено");
        }
        isBHStarted = false;
        sleep(2000);
        communicationModel.offAllKms();
    }

    private void startKTR2() {
        isBH2Started = true;
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
            communicationModel.deinitPR();
            sleep(2000);
            communicationModel.initOwenPrController();
            sleep(2000);
            communicationModel.initExperiment3Devices();

            if (isExperimentRunning && UHHTestItem < WIDDING380) {
                communicationModel.onKM1();
                communicationModel.onKM10();
                communicationModel.onKM15();
                communicationModel.onKM1213();
                communicationModel.onKM47();
                communicationModel.onKM3();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
            } else {
                communicationModel.offAllKms();
                appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            communicationModel.startPhaseMeter();
            appendOneMessageToLog(Constants.LogTag.BLUE, "Началось измерение");

            int timeToWait = 500;
            while ((timeToWait-- > 0) && isDevicesResponding() && isExperimentRunning) {
                sleep(10);
            }

            Experiment3ModelPhase3BH2.setGroupBH(String.valueOf(windingGroup0));
            Experiment3ModelPhase3BH2.setGroupHH(String.valueOf(windingGroup1));
            Experiment3ModelPhase3BH2.setuDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
            if (measuringUInAvr / measuringUOutAvr * 2 < coefTransf || (measuringUInAvr / measuringUOutAvr) * 0.5 > coefTransf) {
                setCause("Расхождение коэффицента трансформации от заданного.\n" +
                        " Проверьте правильность соединения измерительных крокодилов");
            }
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение завершено");
        }

        isBH2Started = false;
        sleep(2000);
        communicationModel.offAllKms();
    }

    private void startKTR3() {
        isBH3Started = true;
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
            communicationModel.deinitPR();
            sleep(2000);
            communicationModel.initOwenPrController();
            sleep(2000);
            communicationModel.initExperiment3Devices();

            if (isExperimentRunning && UHHTestItem < WIDDING380) {
                communicationModel.onKM1();
                communicationModel.onKM10();
                communicationModel.onKM15();
                communicationModel.onKM1213();
                communicationModel.onKM47();
                communicationModel.onKM3();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
            } else {
                communicationModel.offAllKms();
                appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            communicationModel.startPhaseMeter();
            appendOneMessageToLog(Constants.LogTag.BLUE, "Началось измерение");

            int timeToWait = 500;
            while ((timeToWait-- > 0) && isDevicesResponding() && isExperimentRunning) {
                sleep(10);
            }

            Experiment3ModelPhase3BH3.setGroupBH(String.valueOf(windingGroup0));
            Experiment3ModelPhase3BH3.setGroupHH(String.valueOf(windingGroup1));
            Experiment3ModelPhase3BH3.setuDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
            if (measuringUInAvr / measuringUOutAvr * 2 < coefTransf || (measuringUInAvr / measuringUOutAvr) * 0.5 > coefTransf) {
                setCause("Расхождение коэффицента трансформации от заданного.\n" +
                        " Проверьте правильность соединения измерительных крокодилов");
            }
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение завершено");
        }

        isBH3Started = false;
        sleep(2000);
        communicationModel.offAllKms();
    }

    private void startKTR4() {
        isBH4Started = true;
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
            communicationModel.deinitPR();
            sleep(2000);
            communicationModel.initOwenPrController();
            sleep(2000);
            communicationModel.initExperiment3Devices();

            if (isExperimentRunning && UHHTestItem < WIDDING380) {
                communicationModel.onKM1();
                communicationModel.onKM10();
                communicationModel.onKM15();
                communicationModel.onKM1213();
                communicationModel.onKM47();
                communicationModel.onKM3();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
            } else {
                communicationModel.offAllKms();
                appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            communicationModel.startPhaseMeter();
            appendOneMessageToLog(Constants.LogTag.BLUE, "Началось измерение");

            int timeToWait = 500;
            while ((timeToWait-- > 0) && isDevicesResponding() && isExperimentRunning) {
                sleep(10);
            }

            Experiment3ModelPhase3BH4.setGroupBH(String.valueOf(windingGroup0));
            Experiment3ModelPhase3BH4.setGroupHH(String.valueOf(windingGroup1));
            Experiment3ModelPhase3BH4.setuDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
            if (measuringUInAvr / measuringUOutAvr * 2 < coefTransf || (measuringUInAvr / measuringUOutAvr) * 0.5 > coefTransf) {
                setCause("Расхождение коэффицента трансформации от заданного.\n" +
                        " Проверьте правильность соединения измерительных крокодилов");
            }
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение завершено");
        }

        isBH4Started = false;
        sleep(2000);
        communicationModel.offAllKms();
    }

    private void startKTR5() {
        isBH5Started = true;
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания");
            communicationModel.deinitPR();
            sleep(2000);
            communicationModel.initOwenPrController();
            sleep(2000);
            communicationModel.initExperiment3Devices();

            if (isExperimentRunning && UHHTestItem < WIDDING380) {
                communicationModel.onKM1();
                communicationModel.onKM10();
                communicationModel.onKM15();
                communicationModel.onKM1213();
                communicationModel.onKM47();
                communicationModel.onKM3();
                appendOneMessageToLog(Constants.LogTag.BLUE, "Собрана схема для испытания трансформатора с HH до 400В");
            } else {
                communicationModel.offAllKms();
                appendOneMessageToLog(Constants.LogTag.RED, "Схема разобрана. Введите корректный HH в объекте испытания.");
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            communicationModel.startPhaseMeter();
            appendOneMessageToLog(Constants.LogTag.BLUE, "Началось измерение");

            int timeToWait = 500;
            while ((timeToWait-- > 0) && isDevicesResponding() && isExperimentRunning) {
                sleep(10);
            }

            Experiment3ModelPhase3BH5.setGroupBH(String.valueOf(windingGroup0));
            Experiment3ModelPhase3BH5.setGroupHH(String.valueOf(windingGroup1));
            Experiment3ModelPhase3BH5.setuDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
            if (measuringUInAvr / measuringUOutAvr * 2 < coefTransf || (measuringUInAvr / measuringUOutAvr) * 0.5 > coefTransf) {
                setCause("Расхождение коэффицента трансформации от заданного.\n" +
                        " Проверьте правильность соединения измерительных крокодилов");
            }
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение завершено");
        }

        isBH5Started = false;
        sleep(2000);
        communicationModel.offAllKms();
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
    protected void finalizeExperiment() {
        isNeedToRefresh = false;
        sleep(100);

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
            Experiment3ModelPhase3BH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            Experiment3ModelPhase3BH.setResult("Неуспешно");
        } else {
            Experiment3ModelPhase3BH.setResult("Успешно");
            appendMessageToLog(Constants.LogTag.GREEN, "Испытание завершено успешно");
        }
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isPM130_2_Responding && isPM130Responding && isPhaseMeterResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isPhaseMeterResponding ? "" : "Фазометр ",
                isPM130_2_Responding ? "" : "ПМ130(2) ",
                isPM130Responding ? "" : "ПМ130 ");
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
                        if (isStopButton) {
                            setCause("Нажата кнопка СТОП");
                        }
                        break;
                    case OwenPRModel.PRI6_FIXED:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRI7:
                        isDoorZone = (boolean) value;
                        if (isDoorZone && isNeedCheckDoor) {
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
                    case PM130Model.F_PARAM:
                        if (isNeedToRefresh) {
                            measuringF = (float) value;
                            String freq = formatRealNumber(measuringF);
                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setF(freq);
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setF(freq);
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setF(freq);
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setF(freq);
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setF(freq);
                            }
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setuInputAB(formatRealNumber(measuringUInAB));
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setuInputAB(formatRealNumber(measuringUInAB));
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setuInputAB(formatRealNumber(measuringUInAB));
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setuInputAB(formatRealNumber(measuringUInAB));
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setuInputAB(formatRealNumber(measuringUInAB));
                            }
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value;
                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setuInputBC(formatRealNumber(measuringUInBC));
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setuInputBC(formatRealNumber(measuringUInBC));
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setuInputBC(formatRealNumber(measuringUInBC));
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setuInputBC(formatRealNumber(measuringUInBC));
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setuInputBC(formatRealNumber(measuringUInBC));
                            }

                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value;
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setuInputCA(formatRealNumber(measuringUInCA));
                                Experiment3ModelPhase3BH.setuInputAvr(formatRealNumber(measuringUInAvr));
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setuInputCA(formatRealNumber(measuringUInCA));
                                Experiment3ModelPhase3BH2.setuInputAvr(formatRealNumber(measuringUInAvr));
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setuInputCA(formatRealNumber(measuringUInCA));
                                Experiment3ModelPhase3BH3.setuInputAvr(formatRealNumber(measuringUInAvr));
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setuInputCA(formatRealNumber(measuringUInCA));
                                Experiment3ModelPhase3BH4.setuInputAvr(formatRealNumber(measuringUInAvr));
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setuInputCA(formatRealNumber(measuringUInCA));
                                Experiment3ModelPhase3BH5.setuInputAvr(formatRealNumber(measuringUInAvr));
                            }
                        }
                        break;
                }
                break;
            case PM130_2_ID:
                switch (param) {
                    case PM130Model.RESPONDING_PARAM:
                        isPM130_2_Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePM130_2.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutAB = (float) value;
                            String UOutAB = formatRealNumber(measuringUOutAB);
                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setuOutputAB(UOutAB);
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setuOutputAB(UOutAB);
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setuOutputAB(UOutAB);
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setuOutputAB(UOutAB);
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setuOutputAB(UOutAB);
                            }
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutBC = (float) value;
                            String UOutBC = formatRealNumber(measuringUOutBC);
                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setuOutputBC(UOutBC);
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setuOutputBC(UOutBC);
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setuOutputBC(UOutBC);
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setuOutputBC(UOutBC);
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setuOutputBC(UOutBC);
                            }

                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutCA = (float) value;
                            measuringUOutAvr = (measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0;
                            String UOutCA = formatRealNumber(measuringUOutCA);

                            measuringUOutAvr = (measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0;

                            if (isBHStarted) {
                                Experiment3ModelPhase3BH.setuOutputCA(UOutCA);
                                Experiment3ModelPhase3BH.setuOutputAvr(formatRealNumber(measuringUOutAvr));
                            } else if (isBH2Started) {
                                Experiment3ModelPhase3BH2.setuOutputCA(UOutCA);
                                Experiment3ModelPhase3BH2.setuOutputAvr(formatRealNumber(measuringUOutAvr));
                            } else if (isBH3Started) {
                                Experiment3ModelPhase3BH3.setuOutputCA(UOutCA);
                                Experiment3ModelPhase3BH3.setuOutputAvr(formatRealNumber(measuringUOutAvr));
                            } else if (isBH4Started) {
                                Experiment3ModelPhase3BH4.setuOutputCA(UOutCA);
                                Experiment3ModelPhase3BH4.setuOutputAvr(formatRealNumber(measuringUOutAvr));
                            } else if (isBH5Started) {
                                Experiment3ModelPhase3BH5.setuOutputCA(UOutCA);
                                Experiment3ModelPhase3BH5.setuOutputAvr(formatRealNumber(measuringUOutAvr));
                            }

                            if (measuringUOutAvr > 380) {
                                setCause("Напряжение поднялось больше 400В. Возможно, неправильно подключен ОИ");
                            }
                        }
                        break;
                }
                break;
            case PHASEMETER_ID:
                switch (param) {
                    case PhaseMeterModel.RESPONDING_PARAM:
                        isPhaseMeterResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePhaseMeter.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PhaseMeterModel.WINDING_GROUP0_PARAM:
                        windingGroup0 = (short) value;
                        break;
                    case PhaseMeterModel.WINDING_GROUP1_PARAM:
                        windingGroup1 = (short) value;
                        break;
                }
                break;
        }
    }
}
