package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.communication.devices.ikas.IKASModel;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.communication.devices.trm.TRMModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.model.phase3.Experiment2ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;

import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;
import static ru.avem.ksptsurgut.utils.Utils.sleep;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;

public class Experiment2ControllerPhase3 extends AbstractExperiment {
    @FXML
    private TableView<Experiment2ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnResistanceAB;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnResistanceBC;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnResistanceAC;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnTemperature;
    @FXML
    private TableColumn<Experiment2ModelPhase3, String> tableColumnResultExperiment;
    @FXML
    private VBox vBoxLog;

    private Experiment2ModelPhase3 Experiment2ModelPhase3BH = experimentsValuesModel.getExperiment2ModelPhase3BH();
    private Experiment2ModelPhase3 Experiment2ModelPhase3HH = experimentsValuesModel.getExperiment2ModelPhase3HH();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment2Choice() & 0b1) > 0;
    private boolean isBHStarted;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment2Choice() & 0b10) > 0;
    private boolean isHHStarted;

    private volatile float ikasReadyParam;
    private volatile float measuringR;

    private volatile float temperature;

    @FXML
    public void initialize() {
        setTheme(root);

        tableColumnWinding.setCellValueFactory(cellData -> cellData.getValue().windingProperty());
        tableColumnResistanceAB.setCellValueFactory(cellData -> cellData.getValue().ABProperty());
        tableColumnResistanceBC.setCellValueFactory(cellData -> cellData.getValue().BCProperty());
        tableColumnResistanceAC.setCellValueFactory(cellData -> cellData.getValue().ACProperty());
        tableColumnTemperature.setCellValueFactory(cellData -> cellData.getValue().temperatureProperty());
        tableColumnResultExperiment.setCellValueFactory(cellData -> cellData.getValue().resultProperty());

        tableViewExperimentValues.setItems(FXCollections.observableArrayList(Experiment2ModelPhase3BH, Experiment2ModelPhase3HH));
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);
        scrollPaneLog.vvalueProperty().bind(vBoxLog.heightProperty());
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        isBHStarted = false;
        isHHStarted = false;

        Experiment2ModelPhase3BH.clearProperties();
        Experiment2ModelPhase3HH.clearProperties();

        isOwenPRResponding = true;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);

        isIkasResponding = true;
        setDeviceState(deviceStateCircleIKAS, View.DeviceState.UNDEFINED);

        isTrmResponding = true;
        setDeviceState(deviceStateCircleTrm, View.DeviceState.UNDEFINED);

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            if (isExperimentRunning) {
                appendOneMessageToLog(Constants.LogTag.BLUE, "Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment2Devices();
                sleep(2000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment2Devices();
            }

            if (isBHSelected) {
                startBHExperiment();
            }

            if (isHHSelected) {
                startHHExperiment();
            }

            if (!cause.isEmpty()) {
                if (isBHSelected) {
                    if (Experiment2ModelPhase3BH.getResult().isEmpty()) {
                        Experiment2ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment2ModelPhase3HH.getResult().isEmpty()) {
                        Experiment2ModelPhase3HH.setResult("Прервано");
                    }
                }
                appendMessageToLog(Constants.LogTag.RED, String.format("Испытание прервано по причине: %s", cause));
            } else if (!isStartButtonOn) {
                if (isBHSelected) {
                    if (Experiment2ModelPhase3BH.getResult().isEmpty()) {
                        Experiment2ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment2ModelPhase3HH.getResult().isEmpty()) {
                        Experiment2ModelPhase3HH.setResult("Прервано");
                    }
                }
                appendMessageToLog(Constants.LogTag.RED, "Испытание прервано по причине: нажали кнопку <Стоп>");
            } else if (!isDevicesResponding()) {
                if (isBHSelected) {
                    if (Experiment2ModelPhase3BH.getResult().isEmpty()) {
                        Experiment2ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment2ModelPhase3HH.getResult().isEmpty()) {
                        Experiment2ModelPhase3HH.setResult("Прервано");
                    }
                }
                appendMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            }

            finalizeExperiment();
        }).start();
    }

    private void startBHExperiment() {
        if (isExperimentRunning) {
            isBHStarted = true;
        }

        if (isExperimentRunning && isThereAreAccidents() && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.RED, getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация кнопочного поста...");
            isStartButtonOn = false;
            sleep(1000);
        }

        if (isExperimentRunning) {
            appendOneMessageToLog(Constants.LogTag.ORANGE, "Включите кнопочный пост");
            showInformDialogForButtonPost("Нажмите <ПУСК> кнопочного поста");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания обмотки BH...");
            communicationModel.onKM10();
            communicationModel.onKM16();
            communicationModel.onKM17();
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Начало измерения обмотки AB");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение обмотки AB завершено");
            Experiment2ModelPhase3BH.setAB(measuringR);

            appendOneMessageToLog(Constants.LogTag.BLUE, "Начало измерения обмотки BC");
            communicationModel.startMeasuringBC();
            sleep(2000);
        }
        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока 2 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение обмотки BC завершено");
            Experiment2ModelPhase3BH.setBC(measuringR);

            appendOneMessageToLog(Constants.LogTag.BLUE, "Начало измерения обмотки AC");
            communicationModel.startMeasuringAC();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока 3 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение обмотки AC завершено");
            Experiment2ModelPhase3BH.setAC(measuringR);
        }

        appendOneMessageToLog(Constants.LogTag.GREEN, "Конец испытания обмотки BH");
        communicationModel.offAllKms();

        if (isExperimentRunning && isDevicesResponding()) {
            try {
                float AB = Float.parseFloat(Experiment2ModelPhase3BH.getAB());
                float BC = Float.parseFloat(Experiment2ModelPhase3BH.getBC());
                float AC = Float.parseFloat(Experiment2ModelPhase3BH.getAC());

                if ((AB / BC >= 0.98) &&
                        (AB / AC >= 0.98) &&
                        (BC / AC >= 0.98) &&
                        (AB / BC <= 1.02) &&
                        (AB / AC <= 1.02) &&
                        (BC / AC <= 1.02)) {
                    Experiment2ModelPhase3BH.setResult("Успешно");
                } else {
                    Experiment2ModelPhase3BH.setResult("Расхождение");
                    appendOneMessageToLog(Constants.LogTag.RED, "Измеренные сопротивления отличаются между собой более чем на 2%");
                }
            } catch (NumberFormatException e) {
                Experiment2ModelPhase3BH.setResult("Обрыв");
            }
        }
        isBHStarted = false;
    }

    private void startHHExperiment() {
        if (isExperimentRunning) {
            isHHStarted = true;
        }

        if (isExperimentRunning && isThereAreAccidents() && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.RED, getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация кнопочного поста...");
//            isStartButtonOn = false;
            isStartButtonOn = true;
            sleep(1000);
        }

        if (isExperimentRunning) {
            appendOneMessageToLog(Constants.LogTag.ORANGE, "Включите кнопочный пост");
            showInformDialogForButtonPost("Нажмите <ПУСК> кнопочного поста");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания обмотки HH...");
            communicationModel.onKM11();
            communicationModel.onKM16();
            communicationModel.onKM18();
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Начало измерения обмотки AB");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение обмотки AB завершено");
            Experiment2ModelPhase3HH.setAB(measuringR);

            appendOneMessageToLog(Constants.LogTag.BLUE, "Начало измерения обмотки BC");
            communicationModel.startMeasuringBC();
            sleep(2000);
        }
        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока 2 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение обмотки BC завершено");
            Experiment2ModelPhase3HH.setBC(measuringR);

            appendOneMessageToLog(Constants.LogTag.BLUE, "Начало измерения обмотки AC");
            communicationModel.startMeasuringAC();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидаем, пока 3 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog(Constants.LogTag.GREEN, "Измерение обмотки AC завершено");
            Experiment2ModelPhase3HH.setAC(measuringR);
        }

        appendOneMessageToLog(Constants.LogTag.GREEN, "Конец испытания обмотки HH");
        communicationModel.offAllKms();

        if (isExperimentRunning && isDevicesResponding()) {
            try {
                float AB = Float.parseFloat(Experiment2ModelPhase3HH.getAB());
                float BC = Float.parseFloat(Experiment2ModelPhase3HH.getBC());
                float AC = Float.parseFloat(Experiment2ModelPhase3HH.getAC());

                if ((AB / BC >= 0.98) &&
                        (AB / AC >= 0.98) &&
                        (BC / AC >= 0.98) &&
                        (AB / BC <= 1.02) &&
                        (AB / AC <= 1.02) &&
                        (BC / AC <= 1.02)) {
                    Experiment2ModelPhase3HH.setResult("Успешно");
                } else {
                    Experiment2ModelPhase3HH.setResult("Расхождение");
                    appendOneMessageToLog(Constants.LogTag.RED, "Измеренные сопротивления отличаются между собой более чем на 2%");
                }
            } catch (NumberFormatException e) {
                Experiment2ModelPhase3HH.setResult("Обрыв");
            }
        }
        isHHStarted = false;
    }

    @Override
    protected void finalizeExperiment() {
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
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding && isIkasResponding && isTrmResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "ПР200 ",
                isIkasResponding ? "" : "ИКАС ",
                isTrmResponding ? "" : "ТРМ");
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE2WindingBH(Experiment2ModelPhase3BH.getWinding());
        currentProtocol.setE2ABBH(Experiment2ModelPhase3BH.getAB());
        currentProtocol.setE2BCBH(Experiment2ModelPhase3BH.getBC());
        currentProtocol.setE2CABH(Experiment2ModelPhase3BH.getAC());
        currentProtocol.setE2TBH(Experiment2ModelPhase3BH.getTemperature());
        currentProtocol.setE2ResultBH(Experiment2ModelPhase3BH.getResult());

        currentProtocol.setE2WindingHH(Experiment2ModelPhase3HH.getWinding());
        currentProtocol.setE2ABHH(Experiment2ModelPhase3HH.getAB());
        currentProtocol.setE2BCHH(Experiment2ModelPhase3HH.getBC());
        currentProtocol.setE2CAHH(Experiment2ModelPhase3HH.getAC());
        currentProtocol.setE2THH(Experiment2ModelPhase3HH.getTemperature());
        currentProtocol.setE2ResultHH(Experiment2ModelPhase3HH.getResult());
//
//        currentProtocol.setE2WindingBH("1");
//        currentProtocol.setE2ABBH("2");
//        currentProtocol.setE2BCBH("3");
//        currentProtocol.setE2CABH("4");
//        currentProtocol.setE2TBH("5");
//        currentProtocol.setE2ResultBH("6");
//
//        currentProtocol.setE2WindingHH("7");
//        currentProtocol.setE2ABHH("8");
//        currentProtocol.setE2BCHH("9");
//        currentProtocol.setE2CAHH("10");
//        currentProtocol.setE2THH("11");
//        currentProtocol.setE2ResultHH("12");
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
            case IKAS_ID:
                switch (param) {
                    case IKASModel.RESPONDING_PARAM:
                        isIkasResponding = (boolean) value;
                        setDeviceState(deviceStateCircleIKAS, (isIkasResponding) ? View.DeviceState.RESPONDING : View.DeviceState.NOT_RESPONDING);
                        break;
                    case IKASModel.READY_PARAM:
                        ikasReadyParam = (float) value;
                        break;
                    case IKASModel.MEASURABLE_PARAM:
                        measuringR = (float) value;
                        break;
                }
                break;
            case TRM_ID:
                switch (param) {
                    case TRMModel.RESPONDING_PARAM:
                        isTrmResponding = (boolean) value;
                        setDeviceState(deviceStateCircleTrm, (isTrmResponding) ? View.DeviceState.RESPONDING : View.DeviceState.NOT_RESPONDING);
                        break;
                    case TRMModel.T_AMBIENT_PARAM:
                        setTemperatureInTableView((float) value);
                        break;
                }
                break;
        }
    }

    private void setTemperatureInTableView(float value) {
        if (isNeedToRefresh) {
            temperature = value;
            if (isBHStarted) {
                Experiment2ModelPhase3BH.setTemperature(String.valueOf(temperature));
            }
            if (isHHStarted) {
                Experiment2ModelPhase3HH.setTemperature(String.valueOf(temperature));
            }
        }
    }
}

