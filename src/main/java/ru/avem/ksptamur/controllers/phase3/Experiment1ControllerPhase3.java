package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.avem.ksptamur.communication.devices.ikas.IKASModel;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.communication.devices.trm.TRMModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.model.phase3.Experiment1ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment1ControllerPhase3 extends AbstractExperiment {
    @FXML
    private TableView<Experiment1ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnResistanceAB;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnResistanceBC;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnResistanceAC;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnTemperature;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnResultExperiment;

    private Experiment1ModelPhase3 Experiment1ModelPhase3BH = experimentsValuesModel.getExperiment1ModelPhase3BH();
    private Experiment1ModelPhase3 Experiment1ModelPhase3HH = experimentsValuesModel.getExperiment1ModelPhase3HH();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b1) > 0;
    private boolean isBHStarted;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b10) > 0;
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

        tableViewExperimentValues.setItems(FXCollections.observableArrayList(Experiment1ModelPhase3BH, Experiment1ModelPhase3HH));
        tableViewExperimentValues.setSelectionModel(null);

        communicationModel.addObserver(this);
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);

        Experiment1ModelPhase3BH.clearProperties();
        Experiment1ModelPhase3HH.clearProperties();

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
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment1Devices();
                sleep(2000);
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment1Devices();
            }

            if (isExperimentRunning && isBHSelected && isDevicesResponding()) {
                startBHExperiment();
            }

            if (isExperimentRunning && isHHSelected && isDevicesResponding()) {
                startHHExperiment();
            }

            if (!cause.isEmpty()) {
                if (isBHSelected) {
                    if (Experiment1ModelPhase3BH.getResult().isEmpty()) {
                        Experiment1ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment1ModelPhase3HH.getResult().isEmpty()) {
                        Experiment1ModelPhase3HH.setResult("Прервано");
                    }
                }
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            } else if (!isStartButtonOn) {
                if (isBHSelected) {
                    if (Experiment1ModelPhase3BH.getResult().isEmpty()) {
                        Experiment1ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment1ModelPhase3HH.getResult().isEmpty()) {
                        Experiment1ModelPhase3HH.setResult("Прервано");
                    }
                }
                appendMessageToLog("Испытание прервано по причине: нажали кнопку <Стоп>");
            } else if (!isDevicesResponding()) {
                if (isBHSelected) {
                    if (Experiment1ModelPhase3BH.getResult().isEmpty()) {
                        Experiment1ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment1ModelPhase3HH.getResult().isEmpty()) {
                        Experiment1ModelPhase3HH.setResult("Прервано");
                    }
                }
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            }
            appendMessageToLog("------------------------------------------------\n");

            finalizeExperiment();
        }).start();
    }

    private void startBHExperiment() {
        showRequestDialog("Подключите крокодилы ИКАС к обмотке BH. После нажмите <Да>");

        if (isExperimentRunning) {
            isBHStarted = true;
        }

        if (isExperimentRunning && isThereAreAccidents() && isDevicesResponding()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            isStartButtonOn = false;
            sleep(1000);
        }

        while (isExperimentRunning && !isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Включите кнопочный пост");
            sleep(1);
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Инициализация испытания обмотки BH...");
        }

        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Начало измерения обмотки AB");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AB завершено");
            Experiment1ModelPhase3BH.setAB(measuringR);

            appendOneMessageToLog("Начало измерения обмотки BC");
            communicationModel.startMeasuringBC();
            sleep(2000);
        }
        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 2 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки BC завершено");
            Experiment1ModelPhase3BH.setBC(measuringR);

            appendOneMessageToLog("Начало измерения обмотки AC");
            communicationModel.startMeasuringAC();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 3 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AC завершено");
            Experiment1ModelPhase3BH.setAC(measuringR);
        }

        appendOneMessageToLog("Конец испытания обмотки BH\n_______________________________________________________");

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            try {
                float AB = Float.parseFloat(Experiment1ModelPhase3BH.getAB());
                float BC = Float.parseFloat(Experiment1ModelPhase3BH.getBC());
                float AC = Float.parseFloat(Experiment1ModelPhase3BH.getAC());

                if ((AB / BC >= 0.98) &&
                        (AB / AC >= 0.98) &&
                        (BC / AC >= 0.98) &&
                        (AB / BC <= 1.02) &&
                        (AB / AC <= 1.02) &&
                        (BC / AC <= 1.02)) {
                    Experiment1ModelPhase3BH.setResult("Успешно");
                } else {
                    Experiment1ModelPhase3BH.setResult("Расхождение");
                    appendOneMessageToLog("Измеренные сопротивления отличаются между собой более чем на 2%\n" +
                            "_______________________________________________________");
                }
            } catch (NumberFormatException e) {
                Experiment1ModelPhase3BH.setResult("Обрыв");
            }
        }
        isBHStarted = false;
    }

    private void startHHExperiment() {
        showRequestDialog("Подключите крокодилы ИКАС к обмотке HH. После нажмите <Да>");

        if (isExperimentRunning) {
            isHHStarted = true;
        }

        if (isExperimentRunning && isThereAreAccidents() && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            isStartButtonOn = false;
            sleep(1000);
        }

        while (isExperimentRunning && !isStartButtonOn && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Включите кнопочный пост");
            sleep(1);
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Инициализация испытания обмотки HH...");
        }

        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Начало измерения обмотки AB");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AB завершено");
            Experiment1ModelPhase3HH.setAB(measuringR);

            appendOneMessageToLog("Начало измерения обмотки BC");
            communicationModel.startMeasuringBC();
            sleep(2000);
        }
        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 2 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки BC завершено");
            Experiment1ModelPhase3HH.setBC(measuringR);

            appendOneMessageToLog("Начало измерения обмотки AC");
            communicationModel.startMeasuringAC();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && isStartButtonOn && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 3 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AC завершено");
            Experiment1ModelPhase3HH.setAC(measuringR);
        }

        appendOneMessageToLog("Конец испытания обмотки HH\n_______________________________________________________");

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            try {
                float AB = Float.parseFloat(Experiment1ModelPhase3HH.getAB());
                float BC = Float.parseFloat(Experiment1ModelPhase3HH.getBC());
                float AC = Float.parseFloat(Experiment1ModelPhase3HH.getAC());

                if ((AB / BC >= 0.98) &&
                        (AB / AC >= 0.98) &&
                        (BC / AC >= 0.98) &&
                        (AB / BC <= 1.02) &&
                        (AB / AC <= 1.02) &&
                        (BC / AC <= 1.02)) {
                    Experiment1ModelPhase3HH.setResult("Успешно");
                } else {
                    Experiment1ModelPhase3HH.setResult("Расхождение");
                    appendOneMessageToLog("Измеренные сопротивления отличаются между собой более чем на 2%\n" +
                            "_______________________________________________________");
                }
            } catch (NumberFormatException e) {
                Experiment1ModelPhase3HH.setResult("Обрыв");
            }
        }
        isHHStarted = false;
    }

    @Override
    protected void finalizeExperiment() {
        appendOneMessageToLog("После завершения опыта не забудьте отсоединить провода от ИКАС");
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
        currentProtocol.setE1WindingBH(Experiment1ModelPhase3BH.getWinding());
        currentProtocol.setE1ABBH(Experiment1ModelPhase3BH.getAB());
        currentProtocol.setE1BCBH(Experiment1ModelPhase3BH.getBC());
        currentProtocol.setE1CABH(Experiment1ModelPhase3BH.getAC());
        currentProtocol.setE1TBH(Experiment1ModelPhase3BH.getTemperature());
        currentProtocol.setE1ResultBH(Experiment1ModelPhase3BH.getResult());

        currentProtocol.setE1WindingHH(Experiment1ModelPhase3HH.getWinding());
        currentProtocol.setE1ABHH(Experiment1ModelPhase3HH.getAB());
        currentProtocol.setE1BCHH(Experiment1ModelPhase3HH.getBC());
        currentProtocol.setE1CAHH(Experiment1ModelPhase3HH.getAC());
        currentProtocol.setE1THH(Experiment1ModelPhase3HH.getTemperature());
        currentProtocol.setE1ResultHH(Experiment1ModelPhase3HH.getResult());
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
                    case OwenPRModel.PRI6:
                        isStartButtonOn = (boolean) value;
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
                        setDeviceState(deviceStateCircleIKAS, (isTrmResponding) ? View.DeviceState.RESPONDING : View.DeviceState.NOT_RESPONDING);
                        break;
                    case TRMModel.T_AMBIENT_PARAM:
                        setTemperatureInTableView((float) value);
                        break;
                }
                break;
        }
    }

    private void setTemperatureInTableView(float value) {
        temperature = value;
        if (isBHStarted) {
            Experiment1ModelPhase3BH.setTemperature(String.valueOf(temperature));
        }
        if (isHHStarted) {
            Experiment1ModelPhase3HH.setTemperature(String.valueOf(temperature));
        }
    }
}

