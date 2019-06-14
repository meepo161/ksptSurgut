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
import javafx.stage.Stage;
import ru.avem.ksptamur.Constants;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.communication.devices.cs02021.CS020201Model;
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
import static ru.avem.ksptamur.communication.devices.DeviceController.MEGACS_ID;
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
    private TableColumn<Experiment0ModelPhase3, String> tableColumnUr;
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

    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private int uMgr = (int) currentProtocol.getUmeger();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isExperimentRunning;
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
    private String units;
    private volatile String cause;


    @FXML
    private AnchorPane root;

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
        tableColumnUr.setCellValueFactory(cellData -> cellData.getValue().urProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResultExperiment.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
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
        currentProtocol.setE0UBH(experiment0ModelPhase3BH.getUr());
        currentProtocol.setE0ResultBH(experiment0ModelPhase3BH.getResult());

        currentProtocol.setE0WindingHH(experiment0ModelPhase3HH.getWinding());
        currentProtocol.setE0R15HH(experiment0ModelPhase3HH.getR15());
        currentProtocol.setE0R60HH(experiment0ModelPhase3HH.getR60());
        currentProtocol.setE0CoefHH(experiment0ModelPhase3HH.getCoef());
        currentProtocol.setE0UHH(experiment0ModelPhase3HH.getUr());
        currentProtocol.setE0ResultHH(experiment0ModelPhase3HH.getResult());

        currentProtocol.setE0WindingBHHH(experiment0ModelPhase3BHHH.getWinding());
        currentProtocol.setE0R15BHHH(experiment0ModelPhase3BHHH.getR15());
        currentProtocol.setE0R60BHHH(experiment0ModelPhase3BHHH.getR60());
        currentProtocol.setE0CoefBHHH(experiment0ModelPhase3BHHH.getCoef());
        currentProtocol.setE0UBHHH(experiment0ModelPhase3BHHH.getUr());
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
        cause = "Отменено оператором";
        isExperimentRunning = false;
        isExperimentEnd = true;
        buttonStartStop.setText("Запустить");
        buttonNext.setDisable(false);
        buttonCancelAll.setDisable(false);
        communicationModel.finalizeMegaCS();
    }

    private void startExperiment() {
        buttonStartStop.setDisable(true);
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        experiment0ModelPhase3BH.clearProperties();
        experiment0ModelPhase3HH.clearProperties();
        experiment0ModelPhase3BHHH.clearProperties();
        isMegaCSResponding = false;
        cause = "";
        isPressedOk = false;
        isExperimentRunning = true;
        isExperimentEnd = false;

        new Thread(() -> {

            communicationModel.setConnectionBaudrate(Constants.Communication.BAUDRATE_MEGACS);

            if (isExperimentRunning) {
                appendOneMessageToLog("Начало испытания");
//                communicationModel.initOwenPrController();
                communicationModel.initExperiment0Devices();
            }

//            if (isExperimentRunning && !isOwenPRResponding) {
//                appendOneMessageToLog("Нет связи с ПР");
//                sleep(100);
//                isExperimentRunning = false;
//            }

            while (isExperimentRunning && isThereAreAccidents()) { //если сработали защиты
                appendOneMessageToLog(getAccidentsString("Аварии")); //вывод в лог сообщение со списком сработавших защит
                sleep(100);
            }
            while (isExperimentRunning && !isDevicesResponding()) {  //если устройства не отвечают
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));//вывод в лог сообщение со списком устройств без связи
                sleep(100);
            }

            if ((mainModel.getExperiment0Choise() & 0b1) > 0) {
                showDialog("Подключите крокодилы Мегаомметра к ВН обмотке и корпусу. После нажмите <Да>");
                startBH();
            }

            if ((mainModel.getExperiment0Choise() & 0b10) > 0) {
                showDialog("Подключите крокодилы Мегаомметра к HH обмотке и корпусу. После нажмите <Да>");
                startHH();
            }

            if ((mainModel.getExperiment0Choise() & 0b100) > 0) {
                showDialog("Подключите крокодилы Мегаомметра к ВН и к НН. После нажмите <Да>");
                startBHHH();
            }

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                experiment0ModelPhase3BH.setResult("Неуспешно");
                experiment0ModelPhase3HH.setResult("Неуспешно");
                experiment0ModelPhase3BHHH.setResult("Неуспешно");
            } else {
                experiment0ModelPhase3BH.setResult("Успешно");
                experiment0ModelPhase3HH.setResult("Успешно");
                experiment0ModelPhase3BHHH.setResult("Успешно");
                appendMessageToLog("Испытание завершено успешно");
            }
            appendMessageToLog("------------------------------------------------\n");

            isExperimentRunning = false;
            isExperimentEnd = true;
            communicationModel.finalizeMegaCS();
            communicationModel.setConnectionBaudrate(Constants.Communication.BAUDRATE_MAIN);

            Platform.runLater(() -> {
                buttonStartStop.setDisable(false);
                buttonNext.setDisable(false);
                buttonCancelAll.setDisable(false);
            });
        }).start();
    }

    private void showDialog(String s) {
        AtomicBoolean isPressed = new AtomicBoolean(false);
        if (isExperimentRunning) {
            Platform.runLater(() -> {
                View.showConfirmDialog(s,
                        () -> {
                            isPressed.set(true);
                        },
                        () -> {
                            stopExperiment();
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
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания ВН...");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            if (!communicationModel.setUMgr(uMgr)) {
                cause = "Мегер не отвечает";
                isExperimentRunning = false;
            }
        }

        if (isExperimentRunning) {
            appendOneMessageToLog("Измерение началось");
            appendOneMessageToLog("Ожидайте 90 секунд");
            appendOneMessageToLog("Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding()) {
            float[] data = communicationModel.readDataMgr();
            float r15 = formatR(data[3]);
            float r60 = formatR(data[0]);
            experiment0ModelPhase3BH.setUr(String.format("%.2f", data[1]));
            experiment0ModelPhase3BH.setR15(String.format("%.2f", r15) + ", " + units);
            experiment0ModelPhase3BH.setR60(String.format("%.2f", r60) + ", " + units);
            experiment0ModelPhase3BH.setCoef(String.format("%.2f", data[2]));
            appendMessageToLog("Ждём 15 секунд пока разрядится.");
        }

        communicationModel.setCS02021ExperimentRun(false);

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
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
        appendMessageToLog("------------------------------------------------\n");
    }

    private void startHH() {
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания HН...");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Измерение началось");
            appendOneMessageToLog("Ожидайте 90 секунд.");
            communicationModel.setUMgr(uMgr);
            appendOneMessageToLog("Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding()) {
            float[] data = communicationModel.readDataMgr();
            float r15 = formatR(data[3]);
            float r60 = formatR(data[0]);
            experiment0ModelPhase3HH.setUr(String.format("%.2f", data[1]));
            experiment0ModelPhase3HH.setR15(String.format("%.2f", r15) + ", " + units);
            experiment0ModelPhase3HH.setR60(String.format("%.2f", r60) + ", " + units);
            experiment0ModelPhase3HH.setCoef(String.format("%.2f", data[2]));
            appendMessageToLog("Ждём 15 секунд пока разрядится.");
        }

        communicationModel.setCS02021ExperimentRun(false);


        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
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
        appendMessageToLog("------------------------------------------------\n");
    }

    private void startBHHH() {
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания ВН и HН...");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Измерение началось");
            appendOneMessageToLog("Ожидайте 90 секунд.");
            communicationModel.setUMgr(uMgr);
            appendOneMessageToLog("Формирование напряжения");
        }

        int experimentTime = 90;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding()) {
            float[] data = communicationModel.readDataMgr();
            float r15 = formatR(data[3]);
            float r60 = formatR(data[0]);
            experiment0ModelPhase3BHHH.setUr(String.format("%.2f", data[1]));
            experiment0ModelPhase3BHHH.setR15(String.format("%.2f", r15) + ", " + units);
            experiment0ModelPhase3BHHH.setR60(String.format("%.2f", r60) + ", " + units);
            experiment0ModelPhase3BHHH.setCoef(String.format("%.2f", data[2]));
            appendMessageToLog("Ждём 15 секунд пока разрядится.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding()) {
            sleep(1000);
            experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        communicationModel.setCS02021ExperimentRun(false);

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
        appendMessageToLog("------------------------------------------------\n");
    }

    private float formatR(float datum) {
        float measuringR = datum;
        if (measuringR > 1000000000) {
            measuringR = measuringR / 1000000000f;
            units = "GΩ";
        } else if (measuringR > 1000000) {
            measuringR = measuringR / 1000000f;
            units = "MΩ";
        }
        return measuringR;
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
//            isExperimentRunning = false;
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
//        return isOwenPRResponding && isMegaCSResponding /*&& isTrmResponding*/;
        return true;
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
            case MEGACS_ID:
                switch (param) {
                    case CS020201Model.RESPONDING_PARAM:
                        isMegaCSResponding = (boolean) value;
//                        Platform.runLater(() -> deviceStateCircleCS0202.setFill((isMegaCSResponding) ? Color.LIME : Color.RED));
                        break;
                }
                break;
        }
    }
}

