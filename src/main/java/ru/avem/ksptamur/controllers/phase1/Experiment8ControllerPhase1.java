
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

import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase1.Experiment8ModelPhase1;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment8ControllerPhase1 extends DeviceState implements ExperimentController {
    private static final float STATE_1_TO_5_MULTIPLIER = 1f / 5f;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment8ModelPhase1> tableViewExperiment8;
    @FXML
    private TableColumn<Experiment8ModelPhase1, String> tableColumnType;
    @FXML
    private TableColumn<Experiment8ModelPhase1, String> tableColumnU;
    @FXML
    private TableColumn<Experiment8ModelPhase1, String> tableColumnI;
    @FXML
    private TableColumn<Experiment8ModelPhase1, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment8ModelPhase1, String> tableColumnTemperature;
    @FXML
    private TableColumn<Experiment8ModelPhase1, String> tableColumnResult;
    @FXML
    private TextArea textAreaExperiment8Log;

    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;

    private MainModel mainModel = MainModel.getInstance();
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment8ModelPhase1 experiment8ModelPhase1BH;
    private Experiment8ModelPhase1 experiment8ModelPhase1HH;
    private ObservableList<Experiment8ModelPhase1> experiment8Data = FXCollections.observableArrayList();
    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private double UInsulation = currentProtocol.getUinsulation();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentStart;
    private volatile boolean isExperimentEnd = true;
    private volatile boolean isBHSuccess = false;

    private volatile boolean isPM130Responding;
    private volatile boolean isOwenPRResponding;
    private volatile boolean isDeltaResponding;
    private volatile boolean isAvemResponding;
    private volatile boolean isDeltaReady50;
    private volatile boolean isDeltaReady0;


    private volatile boolean isCurrent1On;
    private volatile boolean isCurrent2On;
    private volatile boolean isDoorLockOn;
    private volatile boolean isInsulationOn;
    private volatile boolean isDoorZoneOn;

    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss-SSS");
    private String logBuffer;
    private volatile String cause;
    private float temperature;
    private double iA;
    private double iAOld;
    private double measuringUIn;
    private int currentStage;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experiment8ModelPhase1BH = mainModel.getExperiment8ModelPhase1BH();
        experiment8ModelPhase1HH = mainModel.getExperiment8ModelPhase1HH();
        experiment8Data.add(experiment8ModelPhase1BH);
        experiment8Data.add(experiment8ModelPhase1HH);
        tableViewExperiment8.setItems(experiment8Data);
        tableViewExperiment8.setSelectionModel(null);
        communicationModel.addObserver(this);

        tableColumnType.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        tableColumnU.setCellValueFactory(cellData -> cellData.getValue().UINProperty());
        tableColumnI.setCellValueFactory(cellData -> cellData.getValue().IBHProperty());
        tableColumnTime.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
        tableColumnResult.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
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
        currentProtocol.setE8TypeBHandCorps(experiment8ModelPhase1BH.getType());
        currentProtocol.setE8UBHandCorps(experiment8ModelPhase1BH.getUIN());
        currentProtocol.setE8IBHandCorps(experiment8ModelPhase1BH.getIBH());
        currentProtocol.setE8TimeBHandCorps(experiment8ModelPhase1BH.getTime());
        currentProtocol.setE8ResultBHandCorps(experiment8ModelPhase1BH.getTime());

        currentProtocol.setE8TypeHHandCorps(experiment8ModelPhase1HH.getType());
        currentProtocol.setE8UHHandCorps(experiment8ModelPhase1HH.getUIN());
        currentProtocol.setE8IHHandCorps(experiment8ModelPhase1HH.getIBH());
        currentProtocol.setE8TimeHHandCorps(experiment8ModelPhase1HH.getTime());
        currentProtocol.setE8ResultHHandCorps(experiment8ModelPhase1HH.getTime());
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
        experiment8ModelPhase1BH.clearProperties();
        experiment8ModelPhase1HH.clearProperties();
        isDeltaResponding = false;
        isPM130Responding = false;
        isOwenPRResponding = false;
        cause = "";
        iAOld = -1;

        new Thread(() -> {
            if (mainModel.getExperiment8Choise() == MainModel.EXPERIMENT8_BOTH && !isBHSuccess) { //если выбрано испытание ВН и НН обмоток
                startBH(); //запуск испытния ВН обмотки
                sleep(5000);
                startHH(); //запуск испытния НН обмотки
            } else if (mainModel.getExperiment8Choise() == MainModel.EXPERIMENT8_BH && !isBHSuccess) { //если выбрано испытание ВН
                startBH();
            } else { //если выбрано испытание НН обмоток
                startHH();
            }

            isExperimentStart = false;
            isExperimentEnd = true;

            Platform.runLater(() -> {
                buttonStartStop.setText("Запустить");
                buttonStartStop.setDisable(false);
                buttonNext.setDisable(false);
                buttonCancelAll.setDisable(false);
            });
        }).start();
    }

    private void startBH() {
        isExperimentStart = true;
        isExperimentEnd = false;

        currentStage = 1;
        AtomicBoolean isPressed = new AtomicBoolean(false);
        if (isExperimentStart) {
            Platform.runLater(() -> {
                View.showConfirmDialog("Подключены крокодилы к BН и корпусу?",
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

        if (isExperimentStart) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
            communicationModel.initExperiment8Devices();
            sleep(3000);
        }

            if (isExperimentStart && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
                isExperimentStart = false;
            }

        if (isExperimentStart && isThereAreAccidents()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
            isExperimentStart = false;
        }

        if (isExperimentStart && isOwenPRResponding) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            communicationModel.onKM1();
            isStartButtonOn = true;
            sleep(1000);
        }

        while (isExperimentStart && !isStartButtonOn) {
            sleep(100);
            appendOneMessageToLog("Включите кнопочный пост");
        }

        if (isExperimentStart) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(9000);
            communicationModel.initExperiment8Devices();
        }

        while (isExperimentStart && !isDevicesResponding()) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(100);
        }


        if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onKM8M1();
            communicationModel.onKM4();
        }

        if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * 100, 50 * 10, 50 * 100);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
        }

        if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            regulation(50 * 10, 40, 15, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentStart && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            experiment8ModelPhase1BH.setTime(String.valueOf(experimentTime));
        }

        isNeedToRefresh = false;
        experimentTime = 60;
        experiment8ModelPhase1BH.setTime(String.valueOf(experimentTime));

        currentStage = 3;

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment8ModelPhase1BH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment8ModelPhase1BH.setResult("Неуспешно");
        } else {
            isBHSuccess = true;
            experiment8ModelPhase1BH.setResult("Успешно");
            appendMessageToLog("Испытание BH завершено успешно");
        }
        appendMessageToLog("\n------------------------------------------------\n");

        communicationModel.stopObject();
        sleep(500);
        while (isExperimentStart && !isDeltaReady0 && isDeltaResponding) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        }

        isExperimentStart = false;
        isExperimentEnd = true;
        communicationModel.offAllKms(); //разбираем все возможные схемы
        communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

    }

    private void startHH() {

        isExperimentStart = true;
        isExperimentEnd = false;

        currentStage = 2;
        AtomicBoolean isPressed = new AtomicBoolean(false);
        if (isExperimentStart) {
            Platform.runLater(() -> {
                View.showConfirmDialog("Подключены крокодилы к HН и корпусу?",
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

        if (isExperimentStart) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
            communicationModel.initExperiment8Devices();
            sleep(3000);
        }

            if (isExperimentStart && !isOwenPRResponding) {
                appendOneMessageToLog("Нет связи с ПР");
                sleep(100);
                isExperimentStart = false;
            }

        if (isExperimentStart && isThereAreAccidents()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
            isExperimentStart = false;
        }

        if (isExperimentStart && isOwenPRResponding) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            communicationModel.onKM1();
            isStartButtonOn = true;
            sleep(1000);
        }

        while (isExperimentStart && !isStartButtonOn) {
            sleep(100);
            appendOneMessageToLog("Включите кнопочный пост");
        }

        if (isExperimentStart) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(9000);
            communicationModel.initExperiment8Devices();
        }

        while (isExperimentStart && !isDevicesResponding()) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(100);
        }


        if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onKM8M1();
            communicationModel.onKM4();

        }

        if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * 100, 50 * 10, 50 * 100);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
        }

        if (isExperimentStart && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            regulation(50 * 10, 40, 15, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentStart && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            experiment8ModelPhase1HH.setTime(String.valueOf(experimentTime));
        }

        isNeedToRefresh = false;
        experimentTime = 60;
        experiment8ModelPhase1HH.setTime(String.valueOf(experimentTime));

        currentStage = 3;

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            experiment8ModelPhase1HH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            experiment8ModelPhase1HH.setResult("Неуспешно");
        } else {
            experiment8ModelPhase1HH.setResult("Успешно");
            appendMessageToLog("Испытание HH завершено успешно");
            isBHSuccess = false;
        }
        appendMessageToLog("\n------------------------------------------------\n");

        communicationModel.stopObject();
        sleep(500);
        while (isExperimentStart && !isDeltaReady0 && isDeltaResponding) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        }

        isExperimentStart = false;
        isExperimentEnd = true;
        communicationModel.offAllKms(); //разбираем все возможные схемы
        communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

    }


    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperiment8Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
    }

    private void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    private boolean isThereAreAccidents() {
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
        for (int attempts = 5; !(isOwenPRResponding && isPM130Responding && isDeltaResponding && isAvemResponding) && attempts >= 0; attempts--) {
            sleep(50);
        }
        return isOwenPRResponding && isPM130Responding && isDeltaResponding && isAvemResponding;
    }

    private String getNotRespondingDevicesString(String mainText) {
        return String.format("%s %s%s%s%s",
                mainText,
                isOwenPRResponding ? "" : "Овен ПР ",
                isDeltaResponding ? "" : "Дельта ",
                isPM130Responding ? "" : "Парма ",
                isAvemResponding ? "" : "АВЭМ");
    }

    private int regulation(int start, int coarseStep, int fineStep, int end, double coarseLimit, double fineLimit, int coarseSleep, int fineSleep) {
        double coarseMinLimit = 1 - coarseLimit;
        double coarseMaxLimit = 1 + coarseLimit;
        while (isExperimentStart && ((measuringUIn < end * coarseMinLimit) || (measuringUIn > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUIn < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUIn > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentStart && ((measuringUIn < end - fineLimit) || (measuringUIn > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUIn < end - fineLimit) {
                communicationModel.setObjectUMax(start += fineStep);
            } else if (measuringUIn > end + fineLimit) {
                communicationModel.setObjectUMax(start -= fineStep);
            }
            sleep(fineSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения точно");
        }
        return start;
    }


    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
//            case AVEM_ID:
//                switch (param) {
//                    case AvemVoltmeterModel.RESPONDING_PARAM:
//                        isAvemResponding = (boolean) value;
//                        Platform.runLater(() -> deviceStateCircleAvem.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                    case AvemVoltmeterModel.U_PARAM:
//                        if (isNeedToRefresh) {
//                            setU((float) value);
//                            sleep(25);
//                        }
//                        break;
//                }
//                break;
            case PM130_ID:
                switch (param) {
                    case PM130Model.RESPONDING_PARAM:
                        isPM130Responding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case PM130Model.I1_PARAM:
                        setI((float) value);
                        sleep(25);
                        break;
                }
                break;
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.RESPONDING_PARAM:
                        isOwenPRResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI5:
                        isStartButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRDI6:
                        isStopButtonOn = (boolean) value;
                        break;
                    case OwenPRModel.PRDI6_FIXED:
                        if ((boolean) value) {
                            cause = "Нажата кнопка (СТОП)";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI1:
                        isCurrent1On = (boolean) value;
                        if (!isCurrent1On) {
                            cause = "сработала токовая защита 1";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI2:
                        isCurrent2On = (boolean) value;
                        if (!isCurrent2On) {
                            cause = "сработала токовая защита 2";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI3:
                        isDoorLockOn = (boolean) value;
                        if (!isDoorLockOn) {
                            cause = "открыта дверь";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI4:
                        isInsulationOn = (boolean) value;
                        if (!isInsulationOn) {
                            cause = "пробита изоляция";
                            isExperimentStart = false;
                        }
                        break;
                    case OwenPRModel.PRDI7:
                        isDoorZoneOn = (boolean) value;
                        if (!isDoorZoneOn) {
                            cause = "открыта дверь зоны";
                            isExperimentStart = false;
                        }
                        break;
                }
                break;
            case DELTACP2000_ID:
                switch (param) {
                    case DeltaCP2000Model.RESPONDING_PARAM:
                        isDeltaResponding = (boolean) value;
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));

                        break;
                    case DeltaCP2000Model.CURRENT_FREQUENCY_PARAM:
                        setCurrentFrequencyObject((short) value);
                        break;
                }
                break;
        }
    }

    private void setU(double value) {
        measuringUIn = (int) (value * POWER) / POWER;
        switch (currentStage) {
            case 1:
                experiment8ModelPhase1BH.setUIN(String.valueOf(measuringUIn));
                break;
            case 2:
                experiment8ModelPhase1HH.setUIN(String.valueOf(measuringUIn));
                break;
        }
    }

    private void setI(double value) {
        iA = (int) (value * STATE_1_TO_5_MULTIPLIER * 1000 * POWER) / POWER;
        switch (currentStage) {
            case 1:
                experiment8ModelPhase1BH.setIBH(String.valueOf(iA));
                break;
            case 2:
                experiment8ModelPhase1HH.setIBH(String.valueOf(iA));
                break;
        }
        if (iA > 1000.0) {
            cause = "ток превысил";
            isExperimentStart = false;
        } else {
            iAOld = iA;
        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 5000;
        isDeltaReady0 = value == 0;
    }
}