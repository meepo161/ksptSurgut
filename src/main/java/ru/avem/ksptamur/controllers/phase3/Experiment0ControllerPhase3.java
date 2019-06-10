package ru.avem.ksptamur.controllers.phase3;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.communication.devices.cs02021.CS020201Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.communication.devices.trm.TRMModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment0ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;


public class Experiment0ControllerPhase3 extends DeviceState implements ExperimentController {

    @FXML
    private TableView<Experiment0ModelPhase3> tableViewExperiment0;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnR15;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnR60;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnCoef;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnTemperature;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnResultExperiment;
    @FXML
    private TextArea textAreaExperiment0Log;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;
    @FXML
    private Button buttonStartStop;

    private MainModel mainModel = MainModel.getInstance();
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment0ModelPhase3 experiment0ModelPhase3BH;
    private Experiment0ModelPhase3 experiment0ModelPhase3HH;
    private Experiment0ModelPhase3 experiment0ModelPhase3BHHH;
    private ObservableList<Experiment0ModelPhase3> experiment0Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isExperimentStart;
    private volatile boolean isExperimentEnd = true;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isMegaCSResponding;
    private volatile float ikasReadyParam;
    private volatile boolean isTrmResponding;

    private volatile boolean isCurrent1On;
    private volatile boolean isCurrent2On;
    private volatile boolean isDoorLockOn;
    private volatile boolean isInsulationOn;
    private volatile boolean isDoorZoneOn;
    private volatile boolean isStopButtonOn;
    private volatile boolean isPressedOk;


    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private float measuringR;
    private float temperature;
    private volatile String cause;


    @FXML
    private AnchorPane root;
    private int uMgr;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment0ModelPhase3BH = mainModel.getExperiment0ModelPhase3BH();
        experiment0ModelPhase3HH = mainModel.getExperiment0ModelPhase3HH();
        experiment0ModelPhase3BHHH = mainModel.getExperiment0ModelPhase3BHHH();
        experiment0Data.add(experiment0ModelPhase3BH);
        experiment0Data.add(experiment0ModelPhase3HH);
        experiment0Data.add(experiment0ModelPhase3BHHH);
        tableViewExperiment0.setItems(experiment0Data);
        tableViewExperiment0.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnWinding.setCellValueFactory(cellData -> cellData.getValue().windingProperty());
        tableColumnR15.setCellValueFactory(cellData -> cellData.getValue().r15Property());
        tableColumnR60.setCellValueFactory(cellData -> cellData.getValue().r60Property());
        tableColumnCoef.setCellValueFactory(cellData -> cellData.getValue().coefProperty());
        tableColumnTemperature.setCellValueFactory(cellData -> cellData.getValue().temperatureProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResultExperiment.setCellValueFactory(cellData -> cellData.getValue().resultProperty());

        uMgr = 600;
    }

    @Override
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }

    @FXML
    private void handleExperimentCancel() {
        dialogStage.close();
        isCanceled = true;
    }

    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE0WindingBH(experiment0ModelPhase3BH.getWinding());
        currentProtocol.setE0R15BH(experiment0ModelPhase3BH.getR15());
        currentProtocol.setE0R60BH(experiment0ModelPhase3BH.getR60());
        currentProtocol.setE0CoefBH(experiment0ModelPhase3BH.getCoef());
        currentProtocol.setE0TBH(experiment0ModelPhase3BH.getTemperature());
        currentProtocol.setE0ResultBH(experiment0ModelPhase3BH.getResult());

        currentProtocol.setE0WindingHH(experiment0ModelPhase3HH.getWinding());
        currentProtocol.setE0R15HH(experiment0ModelPhase3HH.getR15());
        currentProtocol.setE0R60HH(experiment0ModelPhase3HH.getR60());
        currentProtocol.setE0CoefHH(experiment0ModelPhase3HH.getCoef());
        currentProtocol.setE0THH(experiment0ModelPhase3HH.getTemperature());
        currentProtocol.setE0ResultHH(experiment0ModelPhase3HH.getResult());

        currentProtocol.setE0WindingBHHH(experiment0ModelPhase3BHHH.getWinding());
        currentProtocol.setE0R15BHHH(experiment0ModelPhase3BHHH.getR15());
        currentProtocol.setE0R60BHHH(experiment0ModelPhase3BHHH.getR60());
        currentProtocol.setE0CoefBHHH(experiment0ModelPhase3BHHH.getCoef());
        currentProtocol.setE0TBHHH(experiment0ModelPhase3BHHH.getTemperature());
        currentProtocol.setE0ResultBHHH(experiment0ModelPhase3BHHH.getResult());
    }

    @FXML
    private void handleNextExperiment() {
        fillProtocolExperimentFields();
        dialogStage.close();
    }

    @FXML
    private void handleStartExperiment() {
        if (isExperimentEnd) {
            startExperiment();
        } else {
            stopExperiment();
        }
    }

    private void stopExperiment() {
        buttonStartStop.setDisable(false);
        cause = "Отменено оператором";
        isExperimentStart = false;
        isExperimentEnd = true;
        buttonStartStop.setText("Запустить");
        buttonNext.setDisable(false);
        buttonCancelAll.setDisable(false);
    }

    private void startExperiment() {
        isCurrent1On = true;
        isCurrent2On = true;
        isDoorLockOn = true;
        isInsulationOn = true;
        isDoorZoneOn = true;
        isExperimentStart = true;
        isExperimentEnd = false;
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        experiment0ModelPhase3BH.clearProperties();
        experiment0ModelPhase3HH.clearProperties();
        experiment0ModelPhase3BHHH.clearProperties();
        isMegaCSResponding = false;
        cause = "";
        isPressedOk = false;

        new Thread(() -> {

            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment0Devices();
                sleep(3000);
            }

            if (isExperimentStart && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
                isExperimentStart = false;
            }

            while (isExperimentStart && isThereAreAccidents()) { //если сработали защиты
                appendOneMessageToLog(getAccidentsString("Аварии")); //вывод в лог сообщение со списком сработавших защит
                sleep(100);
            }
            while (isExperimentStart && !isDevicesResponding()) {  //если устройства не отвечают
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));//вывод в лог сообщение со списком устройств без связи
                sleep(100);
            }

            if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_ALL && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к ВН обмотке и корпусу. После нажмите <Да>");
                startBH();
                showDialog("Подключите крокодилы Мегаомметра к HH обмотке и корпусу. После нажмите <Да>");
                startHH();
                showDialog("Подключите крокодилы Мегаомметра к ВН и к НН. После нажмите <Да>");
                startBHHH();
            } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_BH_HH && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к ВН обмотке и корпусу. После нажмите <Да>");
                startBH();
                showDialog("Подключите крокодилы Мегаомметра к HH обмотке и корпусу. После нажмите <Да>");
                startHH();
            } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_BHHH_BH && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к ВН обмотке и корпусу. После нажмите <Да>");
                startBH();
                showDialog("Подключите крокодилы Мегаомметра к ВН и к НН. После нажмите <Да>");
                startBHHH();
            } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_BHHH_HH && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к HH обмотке и корпусу. После нажмите <Да>");
                startHH();
                showDialog("Подключите крокодилы Мегаомметра к ВН и к НН. После нажмите <Да>");
                startBHHH();
            } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_BH && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к ВН обмотке и корпусу. После нажмите <Да>");
                startBH();
            } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_HH && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к HH обмотке и корпусу. После нажмите <Да>");
                startHH();
            } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT0_BHHH && isExperimentStart) {
                showDialog("Подключите крокодилы Мегаомметра к ВН и к НН. После нажмите <Да>");
                startBHHH();
            }

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment0ModelPhase3BH.setResult("Неуспешно"); //запуск испытния ВН обмотки
                    experiment0ModelPhase3HH.setResult("Неуспешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment0ModelPhase3BH.setResult("Неуспешно");
                } else { //если выбрано испытание НН обмоток
                    experiment0ModelPhase3HH.setResult("Неуспешно");
                }
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment0ModelPhase3BH.setResult("Неуспешно"); //запуск испытния ВН обмотки
                    experiment0ModelPhase3HH.setResult("Неуспешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment0ModelPhase3BH.setResult("Неуспешно");
                } else { //если выбрано испытание НН обмоток
                    experiment0ModelPhase3HH.setResult("Неуспешно");
                }
            } else {
                if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment0ModelPhase3BH.setResult("Успешно"); //запуск испытния ВН обмотки
                    experiment0ModelPhase3HH.setResult("Успешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment0ModelPhase3BH.setResult("Успешно");
                } else { //если выбрано испытание НН обмоток
                    experiment0ModelPhase3HH.setResult("Успешно");
                }
                appendMessageToLog("Испытание завершено успешно");
            }
            appendMessageToLog("\n------------------------------------------------\n");

            isExperimentStart = false;
            isExperimentEnd = true;
            communicationModel.offAllKms(); //разбираем все возможные схемы
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства


            Platform.runLater(() -> {
                buttonStartStop.setText("Запустить");
                buttonStartStop.setDisable(false);
                buttonNext.setDisable(false);
                buttonCancelAll.setDisable(false);
            });
        }).start();
    }

    private void showDialog(String s) {
        AtomicBoolean isPressed = new AtomicBoolean(false);
        if (isExperimentStart) {
            Platform.runLater(() -> {
                View.showConfirmDialog(s,
                        () -> {
                            isPressed.set(true);
                        },
                        () -> {
                            cause = "Отменено";
                            isExperimentStart = false;
                            isPressed.set(true);
                        });
            });
        }
        while (!isPressed.get()) {
            sleep(100);
        }
        isPressed.set(false);
    }

    private void startBH() {
        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания ВН...");
        }

        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Измерение началось");
            communicationModel.setUMgr(uMgr);
            appendOneMessageToLog("Ждём, пока измерение закончится");
        }

        int experimentTime = 90;
        while (isExperimentStart && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentStart && isDevicesResponding()) {
            float[] data = communicationModel.readDataMgr();
//            experiment0ModelPhase3BH.set(data[1]);
            experiment0ModelPhase3BH.setR15(String.valueOf(data[3]));
            experiment0ModelPhase3BH.setR60(String.valueOf(data[0]));
            experiment0ModelPhase3BH.setCoef(String.valueOf(data[2]));
        }

        experimentTime = 15;
        while (isExperimentStart && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            appendMessageToLog("Ждём 15 секунд пока разрядится.");
        }

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment0ModelPhase3BH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment0ModelPhase3BH.setResult("Неуспешно");
        } else {
            experiment0ModelPhase3BH.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("\n------------------------------------------------\n");
    }

    private void startHH() {
        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания HН...");
        }

        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Измерение началось");
            communicationModel.setUMgr(uMgr);
            appendOneMessageToLog("Ждём, пока измерение закончится");
        }

        int experimentTime = 90;
        while (isExperimentStart && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentStart && isDevicesResponding()) {
            float[] data = communicationModel.readDataMgr();
//            experiment0ModelPhase3BH.set(data[1]);
            experiment0ModelPhase3HH.setR15(String.valueOf(data[3]));
            experiment0ModelPhase3HH.setR60(String.valueOf(data[0]));
            experiment0ModelPhase3HH.setCoef(String.valueOf(data[2]));
        }

        experimentTime = 15;
        while (isExperimentStart && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            appendMessageToLog("Ждём 15 секунд пока разрядится.");
        }

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment0ModelPhase3HH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment0ModelPhase3HH.setResult("Неуспешно");
        } else {
            experiment0ModelPhase3HH.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("\n------------------------------------------------\n");
    }

    private void startBHHH() {
        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания HН...");
        }

        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Измерение началось");
            communicationModel.setUMgr(uMgr);
            appendOneMessageToLog("Ждём, пока измерение закончится");
        }

        int experimentTime = 90;
        while (isExperimentStart && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentStart && isDevicesResponding()) {
            float[] data = communicationModel.readDataMgr();
//            experiment0ModelPhase3BH.set(data[1]);
            experiment0ModelPhase3BHHH.setR15(String.valueOf(data[3]));
            experiment0ModelPhase3BHHH.setR60(String.valueOf(data[0]));
            experiment0ModelPhase3BHHH.setCoef(String.valueOf(data[2]));
        }

        experimentTime = 15;
        while (isExperimentStart && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            appendMessageToLog("Ждём 15 секунд пока разрядится.");
        }

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment0ModelPhase3BHHH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment0ModelPhase3BHHH.setResult("Неуспешно");
        } else {
            experiment0ModelPhase3BHHH.setResult("Успешно");
            appendMessageToLog("Испытание завершено успешно");
        }
        appendMessageToLog("\n------------------------------------------------\n");
    }

    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperiment0Log.appendText(String.format("%s | %s\n", sdf.format(System.currentTimeMillis()), message)));
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private boolean isThereAreAccidents() {
//        if (!isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn) {
//            isExperimentStart = false;
//            isExperimentEnd = true;
//        }
//        return !isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn;
        return false;
    }

    private String getAccidentsString(String mainText) {
        return String.format("%s: %s%s%s%s%s%s",
                mainText,
                isCurrent1On ? "" : "сработала токовая защита 1, ",
                isCurrent2On ? "" : "сработала токовая защита 2, ",
                isDoorLockOn ? "" : "открылась дверь, ",
                isInsulationOn ? "" : "обрыв изоляции, ",
                isCanceled ? "" : "нажата кнопка отмены, ",
                isDoorZoneOn ? "" : "открылась дверь зоны");
    }

    private boolean isDevicesResponding() {
        return isOwenPRResponding && isMegaCSResponding /*&& isTrmResponding*/;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isMegaCSResponding ? "" : "Мегаомметр ",
                isTrmResponding ? "" : "ТРМ ");
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
//                    case OwenPRModel.PRDI6:
//                        isStopButtonOn = (boolean) value;
//                        break;
//                    case OwenPRModel.PRDI6_FIXED:
//                        if ((boolean) value) {
//                            cause = "Нажата кнопка (СТОП)";
//                            isExperimentStart = false;
//                        }
//                        break;
//                    case OwenPRModel.PRDI1:
//                        isCurrent1On = (boolean) value;
//                        if (!isCurrent1On) {
//                            cause = "сработала токовая защита 1";
//                            isExperimentStart = false;
//                        }
//                        break;
//                    case OwenPRModel.PRDI2:
//                        isCurrent2On = (boolean) value;
//                        if (!isCurrent2On) {
//                            cause = "сработала токовая защита 2";
//                            isExperimentStart = false;
//                        }
//                        break;
//                    case OwenPRModel.PRDI3:
//                        isDoorLockOn = (boolean) value;
//                        if (!isDoorLockOn) {
//                            cause = "открыта дверь";
//                            isExperimentStart = false;
//                        }
//                        break;
//                    case OwenPRModel.PRDI4:
//                        isInsulationOn = (boolean) value;
//                        if (!isInsulationOn) {
//                            cause = "пробита изоляция";
//                            isExperimentStart = false;
//                        }
//                        break;
//                    case OwenPRModel.PRDI7:
//                        isDoorZoneOn = (boolean) value;
//                        if (!isDoorZoneOn) {
//                            cause = "открыта дверь зоны";
//                            isExperimentStart = false;
//                        }
//                        break;
                }
                break;
            case MEGACS_ID:
                switch (param) {
                    case CS020201Model.RESPONDING_PARAM:
                        isMegaCSResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleCS0202.setFill((isMegaCSResponding) ? Color.LIME : Color.RED));
                        break;
                }
                break;
            case TRM_ID:
                switch (param) {
                    case TRMModel.RESPONDING_PARAM:
//                        isTrmResponding = (boolean) value;
                        isTrmResponding = true;
                        Platform.runLater(() -> deviceStateCircleTrm.setFill((isTrmResponding) ? Color.LIME : Color.RED));
                        break;
                    case TRMModel.T_AMBIENT_PARAM:
                        temperature = (float) value;
                        if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BOTH) {
                            experiment0ModelPhase3BH.setTemperature(String.valueOf(temperature));
                            experiment0ModelPhase3HH.setTemperature(String.valueOf(temperature));
                        } else if (mainModel.getExperiment0Choise() == MainModel.EXPERIMENT1_BH) {
                            experiment0ModelPhase3BH.setTemperature(String.valueOf(temperature));
                        } else {
                            experiment0ModelPhase3HH.setTemperature(String.valueOf(temperature));
                        }
                        break;
                }
                break;
        }
    }
}

