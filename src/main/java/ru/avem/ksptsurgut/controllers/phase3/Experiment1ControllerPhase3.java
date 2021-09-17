package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.controllers.AbstractExperiment;
import ru.avem.ksptsurgut.model.phase3.Experiment1ModelPhase3;
import ru.avem.ksptsurgut.utils.View;

import java.util.Observable;

import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.PR200_ID;
import static ru.avem.ksptsurgut.utils.Utils.*;
import static ru.avem.ksptsurgut.utils.View.setDeviceState;

public class Experiment1ControllerPhase3 extends AbstractExperiment {

    @FXML
    private TableView<Experiment1ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnUr;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnR15;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnR60;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnCoef;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment1ModelPhase3, String> tableColumnResultExperiment;

    private Experiment1ModelPhase3 experiment1ModelPhase3BH = experimentsValuesModel.getExperiment1ModelPhase3BH();
    private Experiment1ModelPhase3 experiment1ModelPhase3HH = experimentsValuesModel.getExperiment1ModelPhase3HH();
    private Experiment1ModelPhase3 experiment1ModelPhase3BHHH = experimentsValuesModel.getExperiment1ModelPhase3BHHH();

    private int uMgr = (int) currentProtocol.getUmeger();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b1) > 0;
    private boolean isBHStarted;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b10) > 0;
    private boolean isHHStarted;
    private boolean isBHHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b100) > 0;
    private boolean isBHHHStarted;

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

        tableViewExperimentValues.setItems(FXCollections.observableArrayList(experiment1ModelPhase3BH, experiment1ModelPhase3HH, experiment1ModelPhase3BHHH));
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

        isBHStarted = false;
        isHHStarted = false;
        isBHHHStarted = false;

        experiment1ModelPhase3BH.clearProperties();
        experiment1ModelPhase3HH.clearProperties();
        experiment1ModelPhase3BHHH.clearProperties();

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
                communicationModel.initExperiment1Devices();
                sleep(2000);
            }

            int timeOut = 30;
            while (isExperimentRunning && !isDevicesResponding() && timeOut-- > 0) {
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
                sleep(100);
                communicationModel.initExperiment1Devices();
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

            finalizeExperiment();
        }).start();
    }

    private void startBHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмотке BH и корпусу. После нажмите <Да>", true);

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

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            if (currentProtocol.getUmeger() <= 2500) {
                if (!communicationModel.setUMgr(uMgr)) {
                    setDeviceState(deviceStateCircleCS0202, View.DeviceState.NOT_RESPONDING);
                    setCause("Мегер не отвечает на запросы");
                } else {
                    setDeviceState(deviceStateCircleCS0202, View.DeviceState.RESPONDING);
                }
            } else {
                setCause("Напряжение Мегаомметра выше допустимого. Измените объект испытания");
                isExperimentRunning = false;
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
            experiment1ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            experiment1ModelPhase3BH.setUr(formatRealNumber(data[1]));
            experiment1ModelPhase3BH.setR15(formatRMrg(data[3]));
            experiment1ModelPhase3BH.setR60(formatRMrg(data[0]));
            experiment1ModelPhase3BH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment1ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки BH прервано по причине: Мегер не отвечает на запросы");
            experiment1ModelPhase3BH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            experiment1ModelPhase3BH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки BH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
        isBHStarted = false;
    }

    private void startHHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмотке HH и корпусу. После нажмите <Да>", true);

        if (isExperimentRunning) {
            isHHStarted = true;
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
            experiment1ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            experiment1ModelPhase3HH.setUr(formatRealNumber(data[1]));
            experiment1ModelPhase3HH.setR15(formatRMrg(data[3]));
            experiment1ModelPhase3HH.setR60(formatRMrg(data[0]));
            experiment1ModelPhase3HH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment1ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки HH прервано по причине: Мегер не отвечает на запросы");
            experiment1ModelPhase3HH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            experiment1ModelPhase3HH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки HH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
        isHHStarted = false;
    }

    private void startBHHHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмоткам BH и HH. После нажмите <Да>", true);

        if (isExperimentRunning) {
            isBHHHStarted = true;
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
            experiment1ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            experiment1ModelPhase3BHHH.setUr(formatRealNumber(data[1]));
            experiment1ModelPhase3BHHH.setR15(formatRMrg(data[3]));
            experiment1ModelPhase3BHHH.setR60(formatRMrg(data[0]));
            experiment1ModelPhase3BHHH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            experiment1ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмоток BH и HH прервано по причине: Мегер не отвечает на запросы");
            experiment1ModelPhase3BHHH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            experiment1ModelPhase3BHHH.setResult("Успешно");
            appendMessageToLog("Испытание обмоток BH и HH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
        isBHHHStarted = false;
    }

    protected void finalizeExperiment() {
        communicationModel.setCS02021ExperimentRun(false);
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

        if (!cause.isEmpty()) {
            if (isBHSelected) {
                if (experiment1ModelPhase3BH.getResult().isEmpty()) {
                    experiment1ModelPhase3BH.setResult("Прервано");
                }
            }
            if (isHHSelected) {
                if (experiment1ModelPhase3HH.getResult().isEmpty()) {
                    experiment1ModelPhase3HH.setResult("Прервано");
                }
            }
            if (isBHHHSelected) {
                if (experiment1ModelPhase3BHHH.getResult().isEmpty()) {
                    experiment1ModelPhase3BHHH.setResult("Прервано");
                }
            }
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
        } else if (!isStartButtonOn) {
            if (isBHSelected) {
                if (experiment1ModelPhase3BH.getResult().isEmpty()) {
                    experiment1ModelPhase3BH.setResult("Прервано");
                }
            }
            if (isHHSelected) {
                if (experiment1ModelPhase3HH.getResult().isEmpty()) {
                    experiment1ModelPhase3HH.setResult("Прервано");
                }
            }
            if (isBHHHSelected) {
                if (experiment1ModelPhase3BHHH.getResult().isEmpty()) {
                    experiment1ModelPhase3BHHH.setResult("Прервано");
                }
            }
            appendMessageToLog("Испытание прервано по причине: нажали кнопку <Стоп>");
        } else if (!isDevicesResponding()) {
            if (isBHSelected) {
                if (experiment1ModelPhase3BH.getResult().isEmpty()) {
                    experiment1ModelPhase3BH.setResult("Прервано");
                }
            }
            if (isHHSelected) {
                if (experiment1ModelPhase3HH.getResult().isEmpty()) {
                    experiment1ModelPhase3HH.setResult("Прервано");
                }
            }
            if (isBHHHSelected) {
                if (experiment1ModelPhase3BHHH.getResult().isEmpty()) {
                    experiment1ModelPhase3BHHH.setResult("Прервано");
                }
            }
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
        }
        appendMessageToLog("------------------------------------------------\n");
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
        currentProtocol.setE1WindingBH(experiment1ModelPhase3BH.getWinding());
        currentProtocol.setE1UBH(experiment1ModelPhase3BH.getUr());
        currentProtocol.setE1R15BH(experiment1ModelPhase3BH.getR15());
        currentProtocol.setE1R60BH(experiment1ModelPhase3BH.getR60());
        currentProtocol.setE1CoefBH(experiment1ModelPhase3BH.getCoef());
        currentProtocol.setE1ResultBH(experiment1ModelPhase3BH.getResult());

        currentProtocol.setE1WindingHH(experiment1ModelPhase3HH.getWinding());
        currentProtocol.setE1UHH(experiment1ModelPhase3HH.getUr());
        currentProtocol.setE1R15HH(experiment1ModelPhase3HH.getR15());
        currentProtocol.setE1R60HH(experiment1ModelPhase3HH.getR60());
        currentProtocol.setE1CoefHH(experiment1ModelPhase3HH.getCoef());
        currentProtocol.setE1ResultHH(experiment1ModelPhase3HH.getResult());

        currentProtocol.setE1WindingBHHH(experiment1ModelPhase3BHHH.getWinding());
        currentProtocol.setE1UBHHH(experiment1ModelPhase3BHHH.getUr());
        currentProtocol.setE1R15BHHH(experiment1ModelPhase3BHHH.getR15());
        currentProtocol.setE1R60BHHH(experiment1ModelPhase3BHHH.getR60());
        currentProtocol.setE1CoefBHHH(experiment1ModelPhase3BHHH.getCoef());
        currentProtocol.setE1ResultBHHH(experiment1ModelPhase3BHHH.getResult());
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
        }
    }
}