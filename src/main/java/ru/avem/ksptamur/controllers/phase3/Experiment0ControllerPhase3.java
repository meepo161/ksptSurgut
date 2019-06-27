package ru.avem.ksptamur.controllers.phase3;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment0ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Constants.Formatting.EXPERIMENT_FORMAT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.PR200_ID;
import static ru.avem.ksptamur.utils.Utils.formatRMrg;
import static ru.avem.ksptamur.utils.Utils.sleep;
import static ru.avem.ksptamur.utils.View.setDeviceState;

public class Experiment0ControllerPhase3 extends DeviceState implements ExperimentController {
    @FXML
    private AnchorPane root;

    @FXML
    private TableView<Experiment0ModelPhase3> tableViewExperimentValues;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment0ModelPhase3, String> tableColumnAmbientTemperature;
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

    @FXML
    private JFXButton buttonCancelAll;
    @FXML
    private JFXButton buttonStartStop;
    @FXML
    private JFXButton buttonNext;

    @FXML
    private JFXTextArea textAreaExperimentProcessLog;

    private CommunicationModel communicationModel = CommunicationModel.getInstance();

    private MainModel experimentsValuesModel = MainModel.getInstance();
    private Experiment0ModelPhase3 Experiment0ModelPhase3BH = experimentsValuesModel.getExperiment0ModelPhase3BH();
    private Experiment0ModelPhase3 Experiment0ModelPhase3HH = experimentsValuesModel.getExperiment0ModelPhase3HH();
    private Experiment0ModelPhase3 Experiment0ModelPhase3BHHH = experimentsValuesModel.getExperiment0ModelPhase3BHHH();

    private Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
    private int uMgr = (int) currentProtocol.getUmeger();

    private boolean isBHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b1) > 0;
    private boolean isHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b10) > 0;
    private boolean isBHHHSelected = (experimentsValuesModel.getExperiment1Choice() & 0b100) > 0;

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isExperimentRunning;
    private volatile boolean isExperimentEnded = true;

    private String logBuffer;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isStartButtonOn;

    private volatile String cause;

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

        tableViewExperimentValues.setItems(FXCollections.observableArrayList(Experiment0ModelPhase3BH, Experiment0ModelPhase3HH, Experiment0ModelPhase3BHHH));
        tableViewExperimentValues.setSelectionModel(null);
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

    @FXML
    private void handleRunStopExperiment() {
        if (isExperimentEnded) {
            initExperiment();
        } else {
            setCause("Отменено оператором");
        }
    }

    private void initExperiment() {
        isExperimentEnded = false;
        isExperimentRunning = true;

        buttonCancelAll.setDisable(true);
        buttonStartStop.setDisable(true);
        buttonNext.setDisable(true);

        Experiment0ModelPhase3BH.clearProperties();
        Experiment0ModelPhase3HH.clearProperties();
        Experiment0ModelPhase3BHHH.clearProperties();

        isOwenPRResponding = true;
        setDeviceState(deviceStateCirclePR200, View.DeviceState.UNDEFINED);

        setDeviceState(deviceStateCircleCS0202, View.DeviceState.UNDEFINED);

        setCause("");

        runExperiment();
    }

    private void runExperiment() {
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
                    if (Experiment0ModelPhase3BH.getResult().isEmpty()) {
                        Experiment0ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment0ModelPhase3HH.getResult().isEmpty()) {
                        Experiment0ModelPhase3HH.setResult("Прервано");
                    }
                }
                if (isBHHHSelected) {
                    if (Experiment0ModelPhase3BHHH.getResult().isEmpty()) {
                        Experiment0ModelPhase3BHHH.setResult("Прервано");
                    }
                }
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            } else if (!isStartButtonOn) {
                if (isBHSelected) {
                    if (Experiment0ModelPhase3BH.getResult().isEmpty()) {
                        Experiment0ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment0ModelPhase3HH.getResult().isEmpty()) {
                        Experiment0ModelPhase3HH.setResult("Прервано");
                    }
                }
                if (isBHHHSelected) {
                    if (Experiment0ModelPhase3BHHH.getResult().isEmpty()) {
                        Experiment0ModelPhase3BHHH.setResult("Прервано");
                    }
                }
                appendMessageToLog("Испытание прервано по причине: нажали кнопку <Стоп>");
            } else if (!isDevicesResponding()) {
                if (isBHSelected) {
                    if (Experiment0ModelPhase3BH.getResult().isEmpty()) {
                        Experiment0ModelPhase3BH.setResult("Прервано");
                    }
                }
                if (isHHSelected) {
                    if (Experiment0ModelPhase3HH.getResult().isEmpty()) {
                        Experiment0ModelPhase3HH.setResult("Прервано");
                    }
                }
                if (isBHHHSelected) {
                    if (Experiment0ModelPhase3BHHH.getResult().isEmpty()) {
                        Experiment0ModelPhase3BHHH.setResult("Прервано");
                    }
                }
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
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
            Experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();// TODO: 24.06.2019 проверять получили ли что-либо
            Experiment0ModelPhase3BH.setUr(String.format("%.2f", data[1]));
            Experiment0ModelPhase3BH.setR15(formatRMrg(data[3]));
            Experiment0ModelPhase3BH.setR60(formatRMrg(data[0]));
            Experiment0ModelPhase3BH.setCoef(String.format("%.2f", data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            Experiment0ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки BH прервано по причине: Мегер не отвечает на запросы");
            Experiment0ModelPhase3BH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            Experiment0ModelPhase3BH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки BH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    private void showRequestDialog(String request) {
        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            AtomicBoolean isPressed = new AtomicBoolean(false);
            Platform.runLater(() -> {
                View.showConfirmDialog(request,
                        () -> isPressed.set(true),
                        () -> {
                            setCause("Отменено оператором");
                            isPressed.set(true);
                        });
            });

            while (!isPressed.get() && isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
                sleep(100);
            }
        }
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
            Experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            Experiment0ModelPhase3HH.setUr(String.format("%.2f", data[1]));
            Experiment0ModelPhase3HH.setR15(formatRMrg(data[3]));
            Experiment0ModelPhase3HH.setR60(formatRMrg(data[0]));
            Experiment0ModelPhase3HH.setCoef(String.format("%.2f", data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            Experiment0ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки HH прервано по причине: Мегер не отвечает на запросы");
            Experiment0ModelPhase3HH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            Experiment0ModelPhase3HH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки HH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    private void startBHHHExperiment() {
        showRequestDialog("Подключите крокодилы мегаомметра к обмотке BHHH и корпусу. После нажмите <Да>");

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
            appendOneMessageToLog("Инициализация испытания обмотки BHHH...");
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
            Experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            float[] data = communicationModel.readDataMgr();
            Experiment0ModelPhase3BHHH.setUr(String.format("%.2f", data[1]));
            Experiment0ModelPhase3BHHH.setR15(formatRMrg(data[3]));
            Experiment0ModelPhase3BHHH.setR60(formatRMrg(data[0]));
            Experiment0ModelPhase3BHHH.setCoef(String.format("%.2f", data[2]));
        }

        communicationModel.setCS02021ExperimentRun(false);

        if (isExperimentRunning && isDevicesResponding() && isStartButtonOn) {
            appendMessageToLog("Ожидание разряда.");
        }

        experimentTime = 15;
        while (isExperimentRunning && (experimentTime-- > 0) && isDevicesResponding() && isStartButtonOn) {
            sleep(1000);
            Experiment0ModelPhase3BHHH.setTime(String.valueOf(experimentTime));
        }

        if (cause.equals("Мегер не отвечает на запросы")) {
            appendMessageToLog("Испытание обмотки BHHH прервано по причине: Мегер не отвечает на запросы");
            Experiment0ModelPhase3BHHH.setResult("Прервано");
            setCause("");
        } else if (cause.isEmpty()) {
            Experiment0ModelPhase3BHHH.setResult("Успешно");
            appendMessageToLog("Испытание обмотки BHHH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");
    }

    private void finalizeExperiment() {
        communicationModel.setCS02021ExperimentRun(false);

        Platform.runLater(() -> {
            isExperimentRunning = false;
            isExperimentEnded = true;
            buttonCancelAll.setDisable(false);
            buttonStartStop.setDisable(false);// TODO: 24.06.2019 научиться перегружать ус-во
            buttonNext.setDisable(false);
        });
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperimentProcessLog.appendText(String.format("%s | %s\n",
                EXPERIMENT_FORMAT.format(System.currentTimeMillis()), message)));
    }

    private boolean isThereAreAccidents() {
        return false;
    }

    private String getAccidentsString(String mainText) {
        return String.format("%s: ",
                mainText);
    }

    private void setCause(String cause) {
        this.cause = cause;
        if (!cause.isEmpty()) {
            isExperimentRunning = false;
        }
    }

    private boolean isDevicesResponding() {
        return isOwenPRResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s",
                mainText,
                isOwenPRResponding ? "" : "БСУ ");
    }

    @FXML
    private void handleNextExperiment() {
        fillFieldsOfExperimentProtocol();
        dialogStage.close();
    }

    private void fillFieldsOfExperimentProtocol() {
        currentProtocol.setE0WindingBH(Experiment0ModelPhase3BH.getWinding());
        currentProtocol.setE0UBH(Experiment0ModelPhase3BH.getUr());
        currentProtocol.setE0R15BH(Experiment0ModelPhase3BH.getR15());
        currentProtocol.setE0R60BH(Experiment0ModelPhase3BH.getR60());
        currentProtocol.setE0CoefBH(Experiment0ModelPhase3BH.getCoef());
        currentProtocol.setE0ResultBH(Experiment0ModelPhase3BH.getResult());

        currentProtocol.setE0WindingHH(Experiment0ModelPhase3HH.getWinding());
        currentProtocol.setE0UHH(Experiment0ModelPhase3HH.getUr());
        currentProtocol.setE0R15HH(Experiment0ModelPhase3HH.getR15());
        currentProtocol.setE0R60HH(Experiment0ModelPhase3HH.getR60());
        currentProtocol.setE0CoefHH(Experiment0ModelPhase3HH.getCoef());
        currentProtocol.setE0ResultHH(Experiment0ModelPhase3HH.getResult());

        currentProtocol.setE0WindingBHHH(Experiment0ModelPhase3BHHH.getWinding());
        currentProtocol.setE0UBHHH(Experiment0ModelPhase3BHHH.getUr());
        currentProtocol.setE0R15BHHH(Experiment0ModelPhase3BHHH.getR15());
        currentProtocol.setE0R60BHHH(Experiment0ModelPhase3BHHH.getR60());
        currentProtocol.setE0CoefBHHH(Experiment0ModelPhase3BHHH.getCoef());
        currentProtocol.setE0ResultBHHH(Experiment0ModelPhase3BHHH.getResult());


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