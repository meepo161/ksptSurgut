package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.communication.devices.parmaT400.ParmaT400Model;
import ru.avem.ksptamur.communication.devices.phasemeter.PhaseMeterModel;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.model.phase3.Experiment3ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.formatRealNumber;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment3ControllerPhase3 extends AbstractExperiment {
    private static final int WIDDING400 = 400;

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
    private Experiment3ModelPhase3 experiment3ModelPhase3;
    private ObservableList<Experiment3ModelPhase3> experiment3Data = FXCollections.observableArrayList();

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

    @FXML
    public void initialize() {
        setTheme(root);
        experiment3ModelPhase3 = experimentsValuesModel.getExperiment3ModelPhase3();
        experiment3Data.add(experiment3ModelPhase3);
        tableViewExperimentValues.setItems(experiment3Data);
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);

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
        currentProtocol.setE3UInputAB(experiment3ModelPhase3.getuInputAB());
        currentProtocol.setE3UInputBC(experiment3ModelPhase3.getuInputBC());
        currentProtocol.setE3UInputCA(experiment3ModelPhase3.getuInputCA());
        currentProtocol.setE3UInputAvr(experiment3ModelPhase3.getuInputAvr());

        currentProtocol.setE3UOutputAB(experiment3ModelPhase3.getuOutputAB());
        currentProtocol.setE3UOutputBC(experiment3ModelPhase3.getuOutputBC());
        currentProtocol.setE3UOutputCA(experiment3ModelPhase3.getuOutputCA());
        currentProtocol.setE3UOutputAvr(experiment3ModelPhase3.getuOutputAvr());

        currentProtocol.setE3DiffU(experiment3ModelPhase3.getuDiff());
        currentProtocol.setE3WindingBH(experiment3ModelPhase3.getGroupBH());
        currentProtocol.setE3WindingHH(experiment3ModelPhase3.getGroupHH());
        currentProtocol.setE3F(experiment3ModelPhase3.getF());
        currentProtocol.setE3Result(experiment3ModelPhase3.getResult());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        experiment3ModelPhase3.clearProperties();

        isNeedToRefresh = true;
        isNeedToWaitDelta = false;
        isExperimentRunning = true;
        isExperimentEnded = false;
        isStartButtonOn = false;

        isOwenPRResponding = false;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);
        isParmaResponding = false;
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
            showRequestDialog("Отсоедините все провода и кабели от ОИ.\n" +
                    "Подключите кабели ОИ и крокодилы <A-B-C> к ВН, а <a-b-c> к НН.\n" +
                    "После нажмите <Да>", true);

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment3Devices();
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
                communicationModel.initExperiment3Devices();
                sleep(3000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment3Devices();
            }

            if (isExperimentRunning && isDevicesResponding()) {
                appendOneMessageToLog("Инициализация испытания");
                if (isExperimentRunning && UHHTestItem < WIDDING400) {
                    communicationModel.onKM11();
                    communicationModel.onKM5();
                    communicationModel.onKM13();
                    sleep(5000);
                    appendOneMessageToLog("Собрана схема для испытания трансформатора с HH до 418В");
                } else {
                    communicationModel.offAllKms();
                    appendOneMessageToLog("Схема разобрана. Введите корректный HH в объекте испытания.");
                    isExperimentRunning = false;
                }
            }

            if (isExperimentRunning && isDevicesResponding()) {
                communicationModel.startPhaseMeter();
                appendOneMessageToLog("Началось измерение");
                sleep(2000);
                isNeedToRefresh = false;
                experiment3ModelPhase3.setGroupBH(String.valueOf(windingGroup0));
                experiment3ModelPhase3.setGroupHH(String.valueOf(windingGroup1));
                experiment3ModelPhase3.setuDiff(formatRealNumber(measuringUInAvr / measuringUOutAvr));
                if (measuringUInAvr / measuringUOutAvr * 2 < coefTransf || (measuringUInAvr / measuringUOutAvr) * 0.5 > coefTransf) {
                    setCause("Расхождение коэффицента трансформации от заданного.\n" +
                            " Проверьте правильность соединения измерительных крокодилов");
                }
                appendOneMessageToLog("Измерение завершено");
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
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment3ModelPhase3.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment3ModelPhase3.setResult("Неуспешно");
        } else {
            experiment3ModelPhase3.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isParmaResponding && isPM130Responding && isPhaseMeterResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isPhaseMeterResponding ? "" : "Фазометр ",
                isParmaResponding ? "" : "Парма ",
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
                    case OwenPRModel.PRI1_FIXED:
                        isDoorZone = (boolean) value;
                        if (!isDoorZone) {
                            setCause("открыты двери зоны");
                        }
                        break;
                    case OwenPRModel.PRI2_FIXED:
                        isDoorSHSO = (boolean) value;
                        if (!isDoorSHSO) {
                            setCause("открыты двери ШСО");
                        }
                        break;
                    case OwenPRModel.PRI3_FIXED:
                        isCurrentOI = (boolean) value;
                        if (!isCurrentOI) {
                            setCause("токовая защита ОИ");
                        }
                        break;
                    case OwenPRModel.PRI4_FIXED:
                        isCurrentVIU = (boolean) value;
                        if (!isCurrentVIU) {
                            setCause("токовая защита ВИУ");
                        }
                        break;
                    case OwenPRModel.PRI5_FIXED:
                        isCurrentInput = (boolean) value;
                        if (!isCurrentInput) {
                            setCause("токовая защита по входу");
                        }
                        break;
                    case OwenPRModel.PRI6:
                        isStartButtonOn = (boolean) value;
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
                            experiment3ModelPhase3.setF(freq);
                        }
                        break;
                    case PM130Model.V1_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInAB = (float) value;
                            experiment3ModelPhase3.setuInputAB(formatRealNumber(measuringUInAB));
                        }
                        break;
                    case PM130Model.V2_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInBC = (float) value;
                            experiment3ModelPhase3.setuInputBC(formatRealNumber(measuringUInBC));

                        }
                        break;
                    case PM130Model.V3_PARAM:
                        if (isNeedToRefresh) {
                            measuringUInCA = (float) value;
                            experiment3ModelPhase3.setuInputCA(formatRealNumber(measuringUInCA));
                            measuringUInAvr = (measuringUInAB + measuringUInBC + measuringUInCA) / 3.0;
                            experiment3ModelPhase3.setuInputAvr(formatRealNumber(measuringUInAvr));
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
                    case ParmaT400Model.UAB_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutAB = (double) value;
                            String UOutAB = formatRealNumber(measuringUOutAB);
                            experiment3ModelPhase3.setuOutputAB(UOutAB);
                        }
                        break;
                    case ParmaT400Model.UBC_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutBC = (double) value;
                            String UOutBC = formatRealNumber(measuringUOutBC);
                            experiment3ModelPhase3.setuOutputBC(UOutBC);
                        }
                        break;
                    case ParmaT400Model.UCA_PARAM:
                        if (isNeedToRefresh) {
                            measuringUOutCA = (double) value;
                            String UOutCA = formatRealNumber(measuringUOutCA);
                            experiment3ModelPhase3.setuOutputCA(UOutCA);
                            measuringUOutAvr = (measuringUOutAB + measuringUOutBC + measuringUOutCA) / 3.0;
                            experiment3ModelPhase3.setuOutputAvr(formatRealNumber(measuringUOutAvr));
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
