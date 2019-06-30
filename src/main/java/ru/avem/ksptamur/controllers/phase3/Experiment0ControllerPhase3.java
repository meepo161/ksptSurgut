package ru.avem.ksptamur.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.AbstractExperiment;
import ru.avem.ksptamur.model.phase3.Experiment0ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.PR200_ID;
import static ru.avem.ksptamur.utils.Utils.*;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment0ControllerPhase3 extends AbstractExperiment {

    @FXML
    private TableView<Experiment0ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnUr;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnR15;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnR60;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnCoef;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnResultExperiment;

    private Experiment0ModelPhase3 experiment0ModelPhase3BH = experimentsValuesModel.getExperiment0ModelPhase3BH();
    private Experiment0ModelPhase3 experiment0ModelPhase3HH = experimentsValuesModel.getExperiment0ModelPhase3HH();
    private Experiment0ModelPhase3 experiment0ModelPhase3BHHH = experimentsValuesModel.getExperiment0ModelPhase3BHHH();

    private int uMgr = (int) currentProtocol.getUmeger();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b1) > 0;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b10) > 0;
    private boolean isBHHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b100) > 0;

    @FXML
    public void initialize() {
        setTheme(root);

        tableColumnWinding.setCellValueFactory(cellData -> cellData.getValue().windingProperty());
        tableColumnUr.setCellValueFactory(cellData -> cellData.getValue().urProperty());
        tableColumnR15.setCellValueFactory(cellData -> cellData.getValue().r15Property());
        tableColumnR60.setCellValueFactory(cellData -> cellData.getValue().r60Property());
        tableColumnCoef.setCellValueFactory(cellData -> cellData.getValue().coefProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResultExperiment.setCellValueFactory(cellData -> cellData.getValue().resultProperty());

        tableViewExperimentValues.setItems(FXCollections.observableArrayList(experiment0ModelPhase3BH, experiment0ModelPhase3HH, experiment0ModelPhase3BHHH));
        tableViewExperimentValues.setSelectionModel(null);

        communicationModel.addObserver(this);
    }

    @Override
    protected void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setDisable(true);
        buttonNext.setDisable(true);

        experiment0ModelPhase3BH.clearProperties();
        experiment0ModelPhase3HH.clearProperties();
        experiment0ModelPhase3BHHH.clearProperties();

        isOwenPRResponding = true;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);

        setDeviceState(deviceStateCircleCS0202, View.DeviceState.UNDEFINED);

        setCause("");

        runExperiment();
    }

    @Override
    protected void runExperiment() {
        new Thread(() -> {
            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment0Devices();
            }

            while (isExperimentRunning && !isDevicesResponding()) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment0Devices();
            }

            if (isBHSelected && isExperimentRunning && isDevicesResponding()) {
                startBHExperiment();
            }

            if (isHHSelected && isExperimentRunning && isDevicesResponding()) {
                startHHExperiment();
            }

            if (isBHHHSelected && isExperimentRunning && isDevicesResponding()) {
                startBHHHExperiment();
            }

            if (!cause.isEmpty()) {
                if (isBHSelected) {
                    if (experiment0ModelPhase3BH.getResult().isEmpty()) {
                        experiment0ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (experiment0ModelPhase3HH.getResult().isEmpty()) {
                        experiment0ModelPhase3HH.setResult("Прервано");
                    }
                }
                if (isBHHHSelected) {
                    if (experiment0ModelPhase3BHHH.getResult().isEmpty()) {
                        experiment0ModelPhase3BHHH.setResult("Прервано");
                    }
                }
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            } else if (!isDevicesResponding()) {
                if (isBHSelected) {
                    if (experiment0ModelPhase3BH.getResult().isEmpty()) {
                        experiment0ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (experiment0ModelPhase3HH.getResult().isEmpty()) {
                        experiment0ModelPhase3HH.setResult("Прервано");
                    }
                }
                if (isBHHHSelected) {
                    if (experiment0ModelPhase3BHHH.getResult().isEmpty()) {
                        experiment0ModelPhase3BHHH.setResult("Прервано");
                    }
                }
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            } else if (!isStartButtonOn) {
                if (isBHSelected) {
                    if (experiment0ModelPhase3BH.getResult().isEmpty()) {
                        experiment0ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (experiment0ModelPhase3HH.getResult().isEmpty()) {
                        experiment0ModelPhase3HH.setResult("Прервано");
                    }
                }
                if (isBHHHSelected) {
                    if (experiment0ModelPhase3BHHH.getResult().isEmpty()) {
                        experiment0ModelPhase3BHHH.setResult("Прервано");
                    }
                }
                appendMessageToLog("Испытание прервано по причине: нажали кнопку <Стоп>");
            }
            appendMessageToLog("------------------------------------------------\n");

            finalizeExperiment();
        }).start();
    }

    private void startBHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмотке BH и корпусу. После нажмите <Да>");

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

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            if (!communicationModel.setUMgr(uMgr)) {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.NOT_RESPONDING);
                setCause("Мегер не отвечает на запросы");
            } else {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.RESPONDING);
            }
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Измерение началось");
            appendOneMessageToLog("Ожидайте 90 секунд");
            appendOneMessageToLog("Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            experiment0ModelPhase3BH.setUr(formatRealNumber(data[1]));
            experiment0ModelPhase3BH.setR15(formatRMrg(data[3]));
            experiment0ModelPhase3BH.setR60(formatRMrg(data[0]));
            experiment0ModelPhase3BH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки BH прервано по причине: Мегер не отвечает на запросы");
            experiment0ModelPhase3BH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            experiment0ModelPhase3BH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки BH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    private void startHHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмотке HH и корпусу. После нажмите <Да>");

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
            appendOneMessageToLog("Инициализация испытания обмотки HH...");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            if (!communicationModel.setUMgr(uMgr)) {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.NOT_RESPONDING);
                setCause("Мегер не отвечает на запросы");
            } else {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.RESPONDING);
            }
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Измерение началось");
            appendOneMessageToLog("Ожидайте 90 секунд.");
            appendOneMessageToLog("Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            experiment0ModelPhase3HH.setUr(formatRealNumber(data[1]));
            experiment0ModelPhase3HH.setR15(formatRMrg(data[3]));
            experiment0ModelPhase3HH.setR60(formatRMrg(data[0]));
            experiment0ModelPhase3HH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки HH прервано по причине: Мегер не отвечает на запросы");
            experiment0ModelPhase3HH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            experiment0ModelPhase3HH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки HH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    private void startBHHHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмоткам BH и HH. После нажмите <Да>");

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
            appendOneMessageToLog("Инициализация испытания обмоток BH и HH...");
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            if (!communicationModel.setUMgr(uMgr)) {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.NOT_RESPONDING);
                setCause("Мегер не отвечает на запросы");
            } else {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.RESPONDING);
            }
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog("Измерение началось");
            appendOneMessageToLog("Ожидайте 90 секунд.");
            appendOneMessageToLog("Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            experiment0ModelPhase3BHHH.setUr(formatRealNumber(data[1]));
            experiment0ModelPhase3BHHH.setR15(formatRMrg(data[3]));
            experiment0ModelPhase3BHHH.setR60(formatRMrg(data[0]));
            experiment0ModelPhase3BHHH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмоток BH и HH прервано по причине: Мегер не отвечает на запросы");
            experiment0ModelPhase3BHHH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            experiment0ModelPhase3BHHH.setResult("Успешно");
            appendMessageToLog("Испытание обмоток BH и HH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    protected void finalizeExperiment() {
        communicationModel.setCS02021ExperimentRun(false);
        communicationModel.finalizeMegaCS();
        Platform.runLater(() -> {
            isExperimentRunning = false;
            isExperimentEnded = true;
            buttonCancelAll.setDisable(false);
            buttonStartStop.setDisable(false);
            buttonNext.setDisable(false);
        });
    }

    @Override
    protected boolean isDevicesResponding() {
        return isOwenPRResponding;
    }

    @Override
    protected String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s",
                mainText,
                isOwenPRResponding ? "" : "ПР200 ");
    }

    @Override
    protected void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE0WindingBH(experiment0ModelPhase3BH.getWinding());
        currentProtocol.setE0UBH(experiment0ModelPhase3BH.getUr());
        currentProtocol.setE0R15BH(experiment0ModelPhase3BH.getR15());
        currentProtocol.setE0R60BH(experiment0ModelPhase3BH.getR60());
        currentProtocol.setE0CoefBH(experiment0ModelPhase3BH.getCoef());
        currentProtocol.setE0ResultBH(experiment0ModelPhase3BH.getResult());

        currentProtocol.setE0WindingHH(experiment0ModelPhase3HH.getWinding());
        currentProtocol.setE0UHH(experiment0ModelPhase3HH.getUr());
        currentProtocol.setE0R15HH(experiment0ModelPhase3HH.getR15());
        currentProtocol.setE0R60HH(experiment0ModelPhase3HH.getR60());
        currentProtocol.setE0CoefHH(experiment0ModelPhase3HH.getCoef());
        currentProtocol.setE0ResultHH(experiment0ModelPhase3HH.getResult());

        currentProtocol.setE0WindingBHHH(experiment0ModelPhase3BHHH.getWinding());
        currentProtocol.setE0UBHHH(experiment0ModelPhase3BHHH.getUr());
        currentProtocol.setE0R15BHHH(experiment0ModelPhase3BHHH.getR15());
        currentProtocol.setE0R60BHHH(experiment0ModelPhase3BHHH.getR60());
        currentProtocol.setE0CoefBHHH(experiment0ModelPhase3BHHH.getCoef());
        currentProtocol.setE0ResultBHHH(experiment0ModelPhase3BHHH.getResult());
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
        }
    }
}