package ru.avem.ksptsurgut.controllers.phase3;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.avem.ksptsurgut.Constants;
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

    private int uMgr = (int) currentProtocol.getUmeger();
    private float[] data;

    private boolean isBHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b1) > 0;
    private boolean isBHStarted;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b10) > 0;
    private boolean isHHStarted;

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

        tableViewExperimentValues.setItems(FXCollections.observableArrayList(experiment1ModelPhase3BH, experiment1ModelPhase3HH));
        tableViewExperimentValues.setSelectionModel(null);
        communicationModel.addObserver(this);
        scrollPaneLog.vvalueProperty().bind(vBoxLog.heightProperty());
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

        experiment1ModelPhase3BH.clearProperties();
        experiment1ModelPhase3HH.clearProperties();

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
                appendOneMessageToLog(Constants.LogTag.BLUE, "Начало испытания");
                communicationModel.initOwenPrController();
                sleep(2000);
                communicationModel.initExperiment1Devices();
            }

            if (isExperimentRunning && !isDevicesResponding()) {
                setCause(getNotRespondingDevicesString("Нет связи с устройствами "));
            }

            if (isBHSelected && isExperimentRunning && isDevicesResponding()) {
                startBHExperiment();
            }

            if (isHHSelected && isExperimentRunning && isDevicesResponding()) {
                startHHExperiment();
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

        if (isExperimentRunning && isOwenPRResponding) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация кнопочного поста...");
        }

        if (isExperimentRunning) {
            appendOneMessageToLog(Constants.LogTag.ORANGE, "Включите кнопочный пост");
            showInformDialogForButtonPost("Нажмите <ПУСК> кнопочного поста");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            communicationModel.offAllKms();
            communicationModel.onKM20();
            communicationModel.onKM21();
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания обмотки BH...");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            if (currentProtocol.getUmeger() < 2500) {
                if (!communicationModel.setUMgr(uMgr)) {
                    setDeviceState(deviceStateCircleCS0202, View.DeviceState.NOT_RESPONDING);
                    setCause("Мегер не отвечает на запросы");
                } else {
                    setDeviceState(deviceStateCircleCS0202, View.DeviceState.RESPONDING);
                }
            } else {
                appendOneMessageToLog(Constants.LogTag.RED, "Напряжение Мегаомметра выше допустимого. Измените объект испытания");
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Измерение началось");
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидайте 90 секунд");
            appendOneMessageToLog(Constants.LogTag.ORANGE, "Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment1ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding()) {
            data = communicationModel.readDataMgr();
            experiment1ModelPhase3BH.setUr(formatRealNumber(data[1]));
            experiment1ModelPhase3BH.setR15(formatRMrg(data[3]));
            experiment1ModelPhase3BH.setR60(formatRMrg(data[0]));
            experiment1ModelPhase3BH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(Constants.LogTag.ORANGE, "Ожидание разряда");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment1ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog(Constants.LogTag.RED, "Испытание обмотки BH прервано по причине: Мегер не отвечает на запросы");
            experiment1ModelPhase3BH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            if (data[3] > 0.0f && data[3] < 500_000_000f) {
                appendMessageToLog(Constants.LogTag.RED, "Сопротивление изоляции обмотки ВН меньше 500 МОм");
                experiment1ModelPhase3BH.setResult("Неуспешно");
            } else {
                experiment1ModelPhase3BH.setResult("Успешно");
                appendMessageToLog(Constants.LogTag.GREEN, "Испытание обмотки BH завершено успешно");
            }
        }
        isBHStarted = false;
        communicationModel.offAllKms();
    }

    private void startHHExperiment() {

        if (isExperimentRunning) {
            isHHStarted = true;
        }

        if (isExperimentRunning && isThereAreAccidents() && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.RED, getAccidentsString("Аварии"));
        }

        if (isExperimentRunning && isOwenPRResponding) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация кнопочного поста...");
        }

        if (isExperimentRunning) {
            appendOneMessageToLog(Constants.LogTag.ORANGE, "Включите кнопочный пост");
            showInformDialogForButtonPost("Нажмите <ПУСК> кнопочного поста");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Инициализация испытания обмотки HH...");
            communicationModel.offAllKms();
            communicationModel.onKM19();
            communicationModel.onKM22();
        }

        if (isExperimentRunning && isDevicesResponding()) {
            if (!communicationModel.setUMgr(uMgr)) {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.NOT_RESPONDING);
                setCause("Мегер не отвечает на запросы");
            } else {
                setDeviceState(deviceStateCircleCS0202, View.DeviceState.RESPONDING);
            }
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog(Constants.LogTag.BLUE, "Измерение началось");
            appendOneMessageToLog(Constants.LogTag.BLUE, "Ожидайте 90 секунд.");
            appendOneMessageToLog(Constants.LogTag.ORANGE, "Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment1ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding()) {
            data = communicationModel.readDataMgr();
            experiment1ModelPhase3HH.setUr(formatRealNumber(data[1]));
            experiment1ModelPhase3HH.setR15(formatRMrg(data[3]));
            experiment1ModelPhase3HH.setR60(formatRMrg(data[0]));
            experiment1ModelPhase3HH.setCoef(formatRealNumber(data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding()) {
            appendMessageToLog(Constants.LogTag.ORANGE, "Ожидание разряда");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment1ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog(Constants.LogTag.RED, "Испытание обмотки HH прервано по причине: Мегер не отвечает на запросы");
            experiment1ModelPhase3HH.setResult("Прервано");
            setCause("");
        }  else if (cause.isEmpty()) {
            if (data[3] > 0.0f && data[3] < 500_000_000f) {
                appendMessageToLog(Constants.LogTag.RED, "Сопротивление изоляции обмотки ВН меньше 500 МОм");
                experiment1ModelPhase3HH.setResult("Неуспешно");
            } else {
                experiment1ModelPhase3HH.setResult("Успешно");
                appendMessageToLog(Constants.LogTag.GREEN, "Испытание обмотки BH завершено успешно");
            }
        }
        isHHStarted = false;
        communicationModel.offAllKms();
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
            appendMessageToLog(Constants.LogTag.RED, String.format("Испытание прервано по причине: %s", cause));
        } else if (isStopButton) {
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
            appendMessageToLog(Constants.LogTag.RED, "Испытание прервано по причине: нажали кнопку <Стоп>");
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
            appendMessageToLog(Constants.LogTag.RED, getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
        }
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

//        currentProtocol.setE1WindingBH("1");
//        currentProtocol.setE1UBH("2");
//        currentProtocol.setE1R15BH("3");
//        currentProtocol.setE1R60BH("4");
//        currentProtocol.setE1CoefBH("5");
//        currentProtocol.setE1ResultBH("6");
//
//        currentProtocol.setE1WindingHH("7");
//        currentProtocol.setE1UHH("8");
//        currentProtocol.setE1R15HH("9");
//        currentProtocol.setE1R60HH("10");
//        currentProtocol.setE1CoefHH("11");
//        currentProtocol.setE1ResultHH("12");

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
                            setCause("открыты двери ШСО или отключено питание");
                        }
                        break;
                    case OwenPRModel.PRI5_FIXED:
                        isStopButton = (boolean) value;
                        if (isStopButton) {
                            setCause("Нажата кнопка СТОП или отключено питание");
                        }
                        break;
                    case OwenPRModel.PRI6_FIXED:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRI7_FIXED:
                        isDoorZone = (boolean) value;
                        if (isDoorZone) {
                            setCause("открыты двери зоны или отключено питание");
                        }
                        break;
                }
                break;
        }
    }
}