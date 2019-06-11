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
import ru.avem.ksptamur.communication.devices.ikas.IKASModel;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.communication.devices.trm.TRMModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment1ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.model.phase3.Experiment1ModelPhase3.BREAK_IKAS;
import static ru.avem.ksptamur.utils.Utils.sleep;


public class Experiment1ControllerPhase3 extends DeviceState implements ExperimentController {

    @FXML
    private TableView<Experiment1ModelPhase3> tableViewExperiment1;
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
    private Experiment1ModelPhase3 experiment1ModelPhase3BH;
    private Experiment1ModelPhase3 experiment1ModelPhase3HH;
    private ObservableList<Experiment1ModelPhase3> experiment1Data = FXCollections.observableArrayList();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isExperimentStart;
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
    private volatile boolean isPressedOk;


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
        experiment1ModelPhase3BH = mainModel.getExperiment1ModelPhase3BH();
        experiment1ModelPhase3HH = mainModel.getExperiment1ModelPhase3HH();
        experiment1Data.add(experiment1ModelPhase3BH);
        experiment1Data.add(experiment1ModelPhase3HH);
        tableViewExperiment1.setItems(experiment1Data);
        tableViewExperiment1.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnWinding.setCellValueFactory(cellData -> cellData.getValue().windingProperty());
        tableColumnResistanceAB.setCellValueFactory(cellData -> cellData.getValue().ABProperty());
        tableColumnResistanceBC.setCellValueFactory(cellData -> cellData.getValue().BCProperty());
        tableColumnResistanceAC.setCellValueFactory(cellData -> cellData.getValue().ACProperty());
        tableColumnTemperature.setCellValueFactory(cellData -> cellData.getValue().temperatureProperty());
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
        currentProtocol.setE1WindingBH(experiment1ModelPhase3BH.getWinding());
        currentProtocol.setE1ABBH(experiment1ModelPhase3BH.getAB());
        currentProtocol.setE1BCBH(experiment1ModelPhase3BH.getBC());
        currentProtocol.setE1CABH(experiment1ModelPhase3BH.getAC());
        currentProtocol.setE1TBH(experiment1ModelPhase3BH.getTemperature());
        currentProtocol.setE1ResultBH(experiment1ModelPhase3BH.getResult());

        currentProtocol.setE1WindingHH(experiment1ModelPhase3HH.getWinding());
        currentProtocol.setE1ABHH(experiment1ModelPhase3HH.getAB());
        currentProtocol.setE1BCHH(experiment1ModelPhase3HH.getBC());
        currentProtocol.setE1CAHH(experiment1ModelPhase3HH.getAC());
        currentProtocol.setE1THH(experiment1ModelPhase3HH.getTemperature());
        currentProtocol.setE1ResultHH(experiment1ModelPhase3HH.getResult());
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
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        experiment1ModelPhase3BH.clearProperties();
        experiment1ModelPhase3HH.clearProperties();
        isIkasResponding = false;
        cause = "";
        isPressedOk = false;
        isExperimentStart = true;

        new Thread(() -> {

            if (isExperimentStart) {
                appendOneMessageToLog("Начало испытания");
                communicationModel.initOwenPrController();
                communicationModel.initExperiment1Devices();
                sleep(2000);
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

            if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH && isExperimentStart) {
                AtomicBoolean isPressed = new AtomicBoolean(false);
                if (isExperimentStart) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Подключите крокодилы ИКАС к ВН",
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


                startBH();


                AtomicBoolean isPressed2 = new AtomicBoolean(false);
                if (isExperimentStart) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Отключите крокодилы ИКАС от ВН и подключите к НН",
                                () -> {
                                    isPressed2.set(true);
                                },
                                () -> {
                                    cause = "Отменено";
                                    isExperimentStart = false;
                                    isPressed2.set(true);
                                });
                    });
                }
                while (!isPressed2.get()) {
                    sleep(100);
                }
                isPressed.set(false);


                startHH();


            } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) {
                AtomicBoolean isPressed = new AtomicBoolean(false);
                if (isExperimentStart) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Поключите крокодилы ИКАС к ВН",
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


                startBH();


            } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_HH) {


                AtomicBoolean isPressed = new AtomicBoolean(false);
                if (isExperimentStart) {
                    Platform.runLater(() -> {
                        View.showConfirmDialog("Поключите крокодилы ИКАС к НН",
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


                startHH();


            }


            if (!cause.equals("")) {
                appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
                if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment1ModelPhase3BH.setResult("Неуспешно"); //запуск испытния ВН обмотки
                    experiment1ModelPhase3HH.setResult("Неуспешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment1ModelPhase3BH.setResult("Неуспешно");
                } else { //если выбрано испытание НН обмоток
                    experiment1ModelPhase3HH.setResult("Неуспешно");
                }
            } else if (!isDevicesResponding()) {
                appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
                if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment1ModelPhase3BH.setResult("Неуспешно"); //запуск испытния ВН обмотки
                    experiment1ModelPhase3HH.setResult("Неуспешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment1ModelPhase3BH.setResult("Неуспешно");
                } else { //если выбрано испытание НН обмоток
                    experiment1ModelPhase3HH.setResult("Неуспешно");
                }
            } else {
                if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) { //если выбрано испытание ВН и НН обмоток
                    experiment1ModelPhase3BH.setResult("Успешно"); //запуск испытния ВН обмотки
                    experiment1ModelPhase3HH.setResult("Успешно"); //запуск испытния НН обмотки
                } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) { //если выбрано испытание ВН
                    experiment1ModelPhase3BH.setResult("Успешно");
                } else { //если выбрано испытание НН обмоток
                    experiment1ModelPhase3HH.setResult("Успешно");
                }
                appendMessageToLog("------------------------------------------------\n");
                appendMessageToLog("Испытание завершено успешно");
            }
            appendMessageToLog("------------------------------------------------\n");

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

    private void startBH() {
        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания ВН...");
        }

        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) { //проверка готовности ИКАС
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Начало измерения обмотки AB");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AB завершено");
            experiment1ModelPhase3BH.setAB((double) ((int) (measuringR * 10000000)) / 10000000);

            appendOneMessageToLog("Начало измерения обмотки BC");
            communicationModel.startMeasuringBC();
            sleep(2000);
        }
        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 2 измерение закончится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки BC завершено");
            experiment1ModelPhase3BH.setBC((double) ((int) (measuringR * 10000000)) / 10000000);

            appendOneMessageToLog("Начало измерения обмотки AC");
            communicationModel.startMeasuringAC();
            sleep(2000);
        }

        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 3 измерение закончится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AC завершено");
            experiment1ModelPhase3BH.setAC((double) ((int) (measuringR * 10000000)) / 10000000);
        }

        appendOneMessageToLog("Конец испытания BH\n_______________________________________________________");

        try {
            double AB = Double.parseDouble(experiment1ModelPhase3BH.getAB());
            double BC = Double.parseDouble(experiment1ModelPhase3BH.getBC());
            double AC = Double.parseDouble(experiment1ModelPhase3BH.getAC());

            if (AB == BREAK_IKAS || BC == BREAK_IKAS
                    || AC == BREAK_IKAS) {
                experiment1ModelPhase3BH.setResult("Неуспешно");
                appendOneMessageToLog("Испытание ВН завершилось неуспешно\n" +
                        "_______________________________________________________");

            } else if ((AB / BC >= 0.95) &&
                    (AB / AC >= 0.95) &&
                    (BC / AC >= 0.95) &&
                    (BC / AC <= 1.05) &&
                    (BC / AC <= 1.05) &&
                    (BC / AC <= 1.05)) {
                experiment1ModelPhase3BH.setResult("Успешно");
            } else {
                experiment1ModelPhase3BH.setResult("Неуспешно");
                appendOneMessageToLog("Измеренные сопротивления отличаются между собой более чем на 5%\n" +
                        "_______________________________________________________");
            }
        } catch (NumberFormatException e) {
            experiment1ModelPhase3BH.setResult("Неуспешно");
        }
        appendOneMessageToLog("После завершения опыта не забудьте отсоединить провода от ИКАС");
    }

    private void startHH() {
        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания НН...");
        }

        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 1f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока ИКАС подготовится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            appendOneMessageToLog("Начало измерения обмотки AB");
            communicationModel.startMeasuringAB();
            sleep(2000);
        }

        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 1 измерение закончится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AB завершено");
            experiment1ModelPhase3HH.setAB((double) ((int) (measuringR * 10000000)) / 10000000);

            appendOneMessageToLog("Начало измерения обмотки BC");
            communicationModel.startMeasuringBC();
            sleep(2000);
        }
        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 2 измерение закончится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки BC завершено");
            experiment1ModelPhase3HH.setBC((double) ((int) (measuringR * 10000000)) / 10000000);

            appendOneMessageToLog("Начало измерения обмотки AC");
            communicationModel.startMeasuringAC();
            sleep(2000);
        }

        while (isExperimentStart && isDevicesResponding() && (ikasReadyParam != 0f) && (ikasReadyParam != 101f)) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока 3 измерение закончится");
        }

        if (isExperimentStart && isDevicesResponding()) {
            sleep(500);
            appendOneMessageToLog("Измерение обмотки AC завершено");
            experiment1ModelPhase3HH.setAC((double) ((int) (measuringR * 10000000)) / 10000000);
        }

        appendOneMessageToLog("Конец испытания HH\n_______________________________________________________");
        appendOneMessageToLog("После завершения опыта не забудьте отсоединить провода от ИКАС");

        try {
            double AB = Double.parseDouble(experiment1ModelPhase3BH.getAB());
            double BC = Double.parseDouble(experiment1ModelPhase3BH.getBC());
            double AC = Double.parseDouble(experiment1ModelPhase3BH.getAC());

            if (AB == BREAK_IKAS || BC == BREAK_IKAS
                    || AC == BREAK_IKAS) {
                experiment1ModelPhase3BH.setResult("Неуспешно");
                appendOneMessageToLog("Испытание ВН завершилось неуспешно\n" +
                        "_______________________________________________________");

            } else if ((AB / BC >= 0.95) &&
                    (AB / AC >= 0.95) &&
                    (BC / AC >= 0.95) &&
                    (BC / AC <= 1.05) &&
                    (BC / AC <= 1.05) &&
                    (BC / AC <= 1.05)) {
                experiment1ModelPhase3BH.setResult("Успешно");
            } else {
                experiment1ModelPhase3BH.setResult("Неуспешно");
                appendOneMessageToLog("Измеренные сопротивления отличаются между собой более чем на 5%\n" +
                        "_______________________________________________________");
            }
        } catch (NumberFormatException e) {
            experiment1ModelPhase3BH.setResult("Неуспешно");
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
        return isOwenPRResponding && isIkasResponding /*&& isTrmResponding*/;
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
//                        isTrmResponding = (boolean) value;
                        isTrmResponding = true;
                        Platform.runLater(() -> deviceStateCircleTrm.setFill((isTrmResponding) ? Color.LIME : Color.RED));
                        break;
                    case TRMModel.T_AMBIENT_PARAM:
                        temperature = (float) value;
                        if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BOTH) {
                            experiment1ModelPhase3BH.setTemperature(String.valueOf(temperature));
                            experiment1ModelPhase3HH.setTemperature(String.valueOf(temperature));
                        } else if (mainModel.getExperiment1Choise() == MainModel.EXPERIMENT1_BH) {
                            experiment1ModelPhase3BH.setTemperature(String.valueOf(temperature));
                        } else {
                            experiment1ModelPhase3HH.setTemperature(String.valueOf(temperature));
                        }
                        break;
                }
                break;
        }
    }
}

