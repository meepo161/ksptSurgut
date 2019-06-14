package ru.avem.ksptamur.controllers.phase1;


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
import ru.avem.ksptamur.communication.devices.ikas.IKASModel;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.communication.devices.trm.TRMModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase1.Experiment1ModelPhase1;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.model.phase1.Experiment1ModelPhase1.BREAK_IKAS;
import static ru.avem.ksptamur.utils.Utils.sleep;


public class Experiment1ControllerPhase1 extends DeviceState implements ExperimentController {

    @FXML
    private TableView<Experiment1ModelPhase1> tableViewExperiment1;
    @FXML
    private TableColumn<Experiment1ModelPhase1, String> tableColumnWinding;
    @FXML
    private TableColumn<Experiment1ModelPhase1, String> tableColumnResistanceAB;
    @FXML
    private TableColumn<Experiment1ModelPhase1, String> tableColumnResistanceBC;
    @FXML
    private TableColumn<Experiment1ModelPhase1, String> tableColumnResistanceAC;
    @FXML
    private TableColumn<Experiment1ModelPhase1, String> tableColumnTemperature;
    @FXML
    private TableColumn<Experiment1ModelPhase1, String> tableColumnResultExperiment;
    @FXML
    private TextArea textAreaExperiment1Log;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;
    @FXML
    private Button buttonStartStop;

    private MainModel mainModel = MainModel.getInstance();
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment1ModelPhase1 experiment1ModelBHPhase1;
    private Experiment1ModelPhase1 experiment1ModelHHPhase1;
    private ObservableList<Experiment1ModelPhase1> experiment1Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isExperimentRunning;
    private volatile boolean isExperimentEnd = true;

    private volatile boolean isOwenPRResponding;
    private volatile boolean isIkasResponding;
    private volatile float ikasReadyParam;
    private volatile boolean isTrmResponding;

    private volatile boolean isCurrent1On;
    private volatile boolean isCurrent2On;
    private volatile boolean isDoorLockOn;
    private volatile boolean isInsulationOn;
    private volatile boolean isDoorZoneOn;
    private volatile boolean isStopButtonOn;


    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private float measuringR;
    private float temperature;
    private volatile String cause;


    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment1ModelBHPhase1 = mainModel.getExperiment1ModelPhase1BH();
        experiment1ModelHHPhase1 = mainModel.getExperiment1ModelPhase1HH();
        experiment1Data.add(experiment1ModelBHPhase1);
        experiment1Data.add(experiment1ModelHHPhase1);
        tableViewExperiment1.setItems(experiment1Data);
        tableViewExperiment1.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnWinding.setCellValueFactory(cellData -> cellData.getValue().windingProperty());
        tableColumnResistanceAB.setCellValueFactory(cellData -> cellData.getValue().RProperty());
        tableColumnTemperature.setCellValueFactory(cellData -> cellData.getValue().temperatureProperty());
        tableColumnResultExperiment.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    }

    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE1WindingBH(experiment1ModelBHPhase1.getWinding());
        currentProtocol.setE1ABBH(experiment1ModelBHPhase1.getR());
        currentProtocol.setE1TBH(experiment1ModelBHPhase1.getTemperature());
        currentProtocol.setE1ResultBH(experiment1ModelBHPhase1.getResult());

        currentProtocol.setE1WindingHH(experiment1ModelHHPhase1.getWinding());
        currentProtocol.setE1ABHH(experiment1ModelHHPhase1.getR());
        currentProtocol.setE1THH(experiment1ModelHHPhase1.getTemperature());
        currentProtocol.setE1ResultHH(experiment1ModelHHPhase1.getResult());
    }

    @FXML
    private void handleNextExperiment() {
        fillProtocolExperimentFields();
        dialogStage.close();
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
        isExperimentRunning = false;
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
        isExperimentRunning = true;
        isExperimentEnd = false;
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        experiment1ModelBHPhase1.clearProperties();
        experiment1ModelHHPhase1.clearProperties();
        isIkasResponding = false;
        cause = "";


        new Thread(() -> {
            appendOneMessageToLog("Начало испытания");
            if (isExperimentRunning) {
                communicationModel.initExperiment1Devices();  //инициализация устройств для 1 испытания
                sleep(1000);
            }

            if (isExperimentRunning && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
                isExperimentRunning = false;
            }

            while (isExperimentRunning && isThereAreAccidents()) { //если сработали защиты
                appendOneMessageToLog(getAccidentsString("Аварии")); //вывод в лог сообщение со списком сработавших защит
                sleep(100);
            }

            while (isExperimentRunning && !isDevicesResponding()) {  //если устройства не отвечают
                appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));//вывод в лог сообщение со списком устройств без связи
                sleep(100);
            }

            if (isExperimentRunning && mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) {
                startBH();
                startHH();
                AtomicBoolean isPressed = new AtomicBoolean(false);
                if (isExperimentRunning) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Отключите крокодилы ИКАС",
                                () -> {
                                    isPressed.set(true);
                                },
                                () -> {
                                    cause = "Отменено";
                                    isExperimentRunning = false;
                                    isPressed.set(true);
                                });
                    });
                }
                while (!isPressed.get()) {
                    sleep(100);
                }
            } else if (isExperimentRunning && mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) {
                startBH();
                AtomicBoolean isPressed = new AtomicBoolean(false);
                if (isExperimentRunning && isExperimentRunning) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Отключите крокодилы ИКАС",
                                () -> {
                                    isPressed.set(true);
                                },
                                () -> {
                                    cause = "Отменено";
                                    isExperimentRunning = false;
                                    isPressed.set(true);
                                });
                    });
                }
                while (!isPressed.get()) {
                    sleep(100);
                }
            } else {
                startHH();
                AtomicBoolean isPressed = new AtomicBoolean(false);
                if (isExperimentRunning) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Отключите крокодилы ИКАС",
                                () -> {
                                    isPressed.set(true);
                                },
                                () -> {
                                    cause = "Отменено";
                                    isExperimentRunning = false;
                                    isPressed.set(true);
                                });
                    });
                }
                while (!isPressed.get()) {
                    sleep(100);
                }
            }

            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment1ModelBHPhase1.setResult("Неуспешно"); //запуск испытния ВН обмотки
                    experiment1ModelHHPhase1.setResult("Неуспешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment1ModelBHPhase1.setResult("Неуспешно");
                } else { //если выбрано испытание НН обмоток
                    experiment1ModelHHPhase1.setResult("Неуспешно");
                }
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment1ModelBHPhase1.setResult("Неуспешно"); //запуск испытния ВН обмотки
                    experiment1ModelHHPhase1.setResult("Неуспешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment1ModelBHPhase1.setResult("Неуспешно");
                } else { //если выбрано испытание НН обмоток
                    experiment1ModelHHPhase1.setResult("Неуспешно");
                }
            } else {
                if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment1ModelBHPhase1.setResult("Успешно"); //запуск испытния ВН обмотки
                    experiment1ModelHHPhase1.setResult("Успешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment1ModelBHPhase1.setResult("Успешно");
                } else { //если выбрано испытание НН обмоток
                    experiment1ModelHHPhase1.setResult("Успешно");
                }
                appendMessageToLog("Испытание завершено успешно");
            }
            appendMessageToLog("------------------------------------------------\n");

            isExperimentRunning = false;
            isExperimentEnd = true;
            communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства


            Platform.runLater(() -> {
                buttonStartStop.setText("Запустить");
                buttonStartStop.setDisable(false);
                buttonNext.setDisable(false);
                buttonCancelAll.setDisable(false);
            });
        }).start();
    }

    private void startBH() {
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания ВН...");
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) { //проверка готовности ИКАС
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
//                mFirstTime = System.currentTimeMillis();
            appendOneMessageToLog("Начало измерения обмотки BH");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки BH завершено");
            experiment1ModelBHPhase1.setR((double) ((int) (measuringR * 10000000)) / 10000000);
        }

        appendOneMessageToLog("Конец испытания BH\n_______________________________________________________");

        try {
            double R = Double.parseDouble(experiment1ModelBHPhase1.getR());

            if (R == BREAK_IKAS) {
                    experiment1ModelBHPhase1.setResult("Неуспешно");
                appendOneMessageToLog("Испытание ВН завершилось неуспешно\n" +
                        "_______________________________________________________");

            }
            experiment1ModelBHPhase1.setResult("Успешно");
        } catch (NumberFormatException e) {
            experiment1ModelBHPhase1.setResult("Неуспешно");
        }
    }

    private void startHH() {
        if (isExperimentRunning && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания НН...");
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
//                mFirstTime = System.currentTimeMillis();
            appendOneMessageToLog("Начало измерения обмотки HH");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentRunning && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentRunning && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки HH завершено");
            experiment1ModelHHPhase1.setR((double) ((int) (measuringR * 10000000)) / 10000000);
        }

        appendOneMessageToLog("Конец испытания HH\n_______________________________________________________");

        try {
            double R = Double.parseDouble(experiment1ModelHHPhase1.getR());

            if (R == BREAK_IKAS) {
                experiment1ModelHHPhase1.setResult("Неуспешно");
                appendOneMessageToLog("Испытание HН завершилось неуспешно\n" +
                        "_______________________________________________________");

            }
            experiment1ModelBHPhase1.setResult("Успешно");
        } catch (NumberFormatException e) {
            experiment1ModelBHPhase1.setResult("Неуспешно");
        }
    }

    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperiment1Log.appendText(String.format("%s | %s\n", sdf.format(System.currentTimeMillis()), message)));
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private boolean isThereAreAccidents() {
        if (!isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn) {
            isExperimentRunning = false;
            isExperimentEnd = true;
        }
        return !isCurrent1On || !isCurrent2On || !isDoorLockOn || !isInsulationOn || isCanceled || !isDoorZoneOn;
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
        return isOwenPRResponding && isIkasResponding && isTrmResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isIkasResponding ? "" : "ИКАС ",
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
                    case OwenPRModel.PRDI6:
                        isStopButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRDI6_FIXED:
                        if ((boolean) value) {
                            cause = "Нажата кнопка (СТОП)";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI1:
                        isCurrent1On = (boolean) value;
                        if (!isCurrent1On) {
                            cause = "сработала токовая защита 1";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI2:
                        isCurrent2On = (boolean) value;
                        if (!isCurrent2On) {
                            cause = "сработала токовая защита 2";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI3:
                        isDoorLockOn = (boolean) value;
                        if (!isDoorLockOn) {
                            cause = "открыта дверь";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI4:
                        isInsulationOn = (boolean) value;
                        if (!isInsulationOn) {
                            cause = "пробита изоляция";
                            isExperimentRunning = false;
                        }
                        break;
                    case OwenPRModel.PRDI7:
                        isDoorZoneOn = (boolean) value;
                        if (!isDoorZoneOn) {
                            cause = "открыта дверь зоны";
                            isExperimentRunning = false;
                        }
                        break;
                }
                break;
            case IKAS_ID:
                switch (param) {
                    case IKASModel.RESPONDING_PARAM:
                        isIkasResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleIKAS.setFill((isIkasResponding) ? Color.LIME : Color.RED));
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
                        Platform.runLater(() -> deviceStateCircleTrm.setFill((isTrmResponding) ? Color.LIME : Color.RED));
                        break;
                    case TRMModel.T_AMBIENT_PARAM:
                        temperature = (float) value;
                        if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) {
                            experiment1ModelBHPhase1.setTemperature(String.valueOf(temperature));
                            experiment1ModelHHPhase1.setTemperature(String.valueOf(temperature));
                        } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) {
                            experiment1ModelBHPhase1.setTemperature(String.valueOf(temperature));
                        } else {
                            experiment1ModelHHPhase1.setTemperature(String.valueOf(temperature));
                        }
                        break;
                }
                break;
        }
    }
}

