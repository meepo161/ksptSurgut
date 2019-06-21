
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
import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.controllers.DeviceState;
import ru.avem.ksptamur.controllers.ExperimentController;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.phase3.Experiment7ModelPhase3;
import ru.avem.ksptamur.utils.View;

import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Constants.Measuring.HZ;
import static ru.avem.ksptamur.Constants.Measuring.VOLT;
import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.communication.devices.DeviceController.*;
import static ru.avem.ksptamur.utils.Utils.sleep;

public class Experiment7ControllerPhase3 extends DeviceState implements ExperimentController {
    private static final double STATE_5_TO_5_MULTIPLIER = 5.0 / 5.0;
    private static final double POWER = 100;

    @FXML
    private TableView<Experiment7ModelPhase3> tableViewExperiment7;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnType;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnU;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnI;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnTime;
    @FXML
    private TableColumn<Experiment7ModelPhase3, String> tableColumnResult;
    @FXML
    private TextArea textAreaExperiment7Log;

    @FXML
    private Button buttonStartStop;
    @FXML
    private Button buttonNext;
    @FXML
    private Button buttonCancelAll;

    private MainModel mainModel = MainModel.getInstance();
    private CommunicationModel communicationModel = CommunicationModel.getInstance();
    private Experiment7ModelPhase3 Experiment7ModelPhase3BH;
    private Experiment7ModelPhase3 Experiment7ModelPhase3HH;
    private ObservableList<Experiment7ModelPhase3> Experiment7Data = FXCollections.observableArrayList();
    private Protocol currentProtocol = mainModel.getCurrentProtocol();
    private double UInsulation = currentProtocol.getUinsulation();

    private Stage dialogStage;
    private boolean isCanceled;

    private volatile boolean isNeedToRefresh;
    private volatile boolean isStartButtonOn;
    private volatile boolean isStopButtonOn;
    private volatile boolean isExperimentRunning;
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
        Experiment7ModelPhase3BH = mainModel.getExperiment7ModelPhase3BH();
        Experiment7ModelPhase3HH = mainModel.getExperiment7ModelPhase3HH();
        Experiment7Data.add(Experiment7ModelPhase3BH);
        Experiment7Data.add(Experiment7ModelPhase3HH);
        tableViewExperiment7.setItems(Experiment7Data);
        tableViewExperiment7.setSelectionModel(null);
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


    private void fillProtocolExperimentFields() {
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setE8TypeBHandCorps(Experiment7ModelPhase3BH.getType());
        currentProtocol.setE8UBHandCorps(Experiment7ModelPhase3BH.getUIN());
        currentProtocol.setE8IBHandCorps(Experiment7ModelPhase3BH.getIBH());
        currentProtocol.setE8TimeBHandCorps(Experiment7ModelPhase3BH.getTime());
        currentProtocol.setE8ResultBHandCorps(Experiment7ModelPhase3BH.getResult());

        currentProtocol.setE8TypeHHandCorps(Experiment7ModelPhase3HH.getType());
        currentProtocol.setE8UHHandCorps(Experiment7ModelPhase3HH.getUIN());
        currentProtocol.setE8IHHandCorps(Experiment7ModelPhase3HH.getIBH());
        currentProtocol.setE8TimeHHandCorps(Experiment7ModelPhase3HH.getTime());
        currentProtocol.setE8ResultHHandCorps(Experiment7ModelPhase3HH.getResult());
    }

    @FXML
    private void handleExperimentCancel() {
        dialogStage.close();
        isCanceled = true;
    }

    @FXML
    private void handleNextExperiment() {
        fillProtocolExperimentFields();
        dialogStage.close();
    }

    @FXML
    private void handleStartExperiment() {
        if (isExperimentEnd || !isExperimentRunning) {
            startExperiment();
        } else {
            stopExperiment();
        }
    }

    private void stopExperiment() {
        isNeedToRefresh = false;
        buttonStartStop.setDisable(false);
        cause = "Отменено оператором";
        isExperimentRunning = false;
        isExperimentEnd = true;
        buttonStartStop.setText("Запустить");
        buttonNext.setDisable(false);
        buttonCancelAll.setDisable(false);
        isCanceled = true;
        communicationModel.stopObject();
        communicationModel.finalizeAllDevices();
        communicationModel.offAllKms();
    }

    private void startExperiment() {
        isNeedToRefresh = true;
        isCurrent1On = true;
        isCurrent2On = true;
        isDoorLockOn = true;
        isInsulationOn = true;
        isDoorZoneOn = true;
        isCanceled = false;
        isExperimentRunning = true;
        isStartButtonOn = false;
        isExperimentEnd = false;
        buttonStartStop.setText("Остановить");
        buttonNext.setDisable(true);
        buttonCancelAll.setDisable(true);
        Experiment7ModelPhase3BH.clearProperties();
        Experiment7ModelPhase3HH.clearProperties();
        isAvemResponding = true;
        isDeltaResponding = true;
        isPM130Responding = true;
        isOwenPRResponding = true;
        isAvemResponding = false;
        isDeltaResponding = false;
        isPM130Responding = false;
        isOwenPRResponding = false;
        cause = "";
        iAOld = -1;

        new Thread(() -> {
            if (mainModel.getExperiment7Choice() == MainModel.EXPERIMENT7_BOTH && !isBHSuccess) { //если выбрано испытание ВН и НН обмоток
                startBH(); //запуск испытния ВН обмотки
                sleep(5000);
                startHH(); //запуск испытния НН обмотки
            } else if (mainModel.getExperiment7Choice() == MainModel.EXPERIMENT7_BH && !isBHSuccess) { //если выбрано испытание ВН
                startBH();
            } else { //если выбрано испытание НН обмоток
                startHH();
            }

            isExperimentRunning = false;
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
        isExperimentRunning = true;
        isExperimentEnd = false;

        currentStage = 1;
        AtomicBoolean isPressed = new AtomicBoolean(false);
        if (isExperimentRunning) {
            Platform.runLater(() -> {
                View.showConfirmDialog("Подключены крокодилы к BН и корпусу?",
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

        if (isExperimentRunning) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
            communicationModel.initExperiment7Devices();
            sleep(3000);
        }

        if (isExperimentRunning && !isOwenPRResponding) {
            appendOneMessageToLog("Нет связи с ПР");
            sleep(100);
            isExperimentRunning = false;
        }

        if (isExperimentRunning && isThereAreAccidents()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
            isExperimentRunning = false;
            isExperimentEnd = true;
        }

        if (isExperimentRunning && isOwenPRResponding) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            communicationModel.onKM2();
            isStartButtonOn = true;
            sleep(1000);
        }

        while (isExperimentRunning && !isStartButtonOn) {
            sleep(100);
            appendOneMessageToLog("Включите кнопочный пост");
        }

        if (isExperimentRunning && isStartButtonOn) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(9000);
            communicationModel.initExperiment7Devices();
        }

        while (isExperimentRunning && !isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(100);
        }


        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onKM5();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * HZ, 50 * VOLT, 50 * HZ);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            regulation(50 * VOLT, 40, 15, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            Experiment7ModelPhase3BH.setTime(String.valueOf(experimentTime));
        }
        experimentTime = 60;
        Experiment7ModelPhase3BH.setTime(String.valueOf(experimentTime));
        currentStage = 3;

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            Experiment7ModelPhase3BH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            Experiment7ModelPhase3BH.setResult("Неуспешно");
        } else {
            isBHSuccess = true;
            Experiment7ModelPhase3BH.setResult("Успешно");
            appendMessageToLog("Испытание BH завершено успешно");
        }
        appendMessageToLog("------------------------------------------------\n");

        communicationModel.stopObject();
        sleep(500);
        while (isExperimentRunning && !isDeltaReady0 && isDeltaResponding && isStartButtonOn) {
            sleep(100);
            appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
        }

        isExperimentRunning = false;
        isExperimentEnd = true;
        communicationModel.offAllKms(); //разбираем все возможные схемы
        communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

    }

    private void startHH() {

        isExperimentRunning = true;
        isExperimentEnd = false;

        currentStage = 2;
        AtomicBoolean isPressed = new AtomicBoolean(false);
        if (isExperimentRunning) {
            Platform.runLater(() -> {
                View.showConfirmDialog("Подключены крокодилы к HН и корпусу?",
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

        if (isExperimentRunning) {
            appendOneMessageToLog("Начало испытания");
            communicationModel.initOwenPrController();
            communicationModel.initExperiment7Devices();
            sleep(3000);
        }

        if (isExperimentRunning && !isOwenPRResponding) {
            appendOneMessageToLog("Нет связи с ПР");
            sleep(100);
            isExperimentRunning = false;
        }

        if (isExperimentRunning && isThereAreAccidents()) {
            appendOneMessageToLog(getAccidentsString("Аварии"));
            isExperimentRunning = false;
        }

        if (isExperimentRunning && isOwenPRResponding) {
            appendOneMessageToLog("Инициализация кнопочного поста...");
            isStartButtonOn = true;
            sleep(1000);
        }

        while (isExperimentRunning && !isStartButtonOn) {
            sleep(100);
            appendOneMessageToLog("Включите кнопочный пост");
        }

        if (isExperimentRunning && isStartButtonOn) {
            appendOneMessageToLog("Идет загрузка ЧП");
            sleep(9000);
            communicationModel.initExperiment7Devices();
        }

        while (isExperimentRunning && !isDevicesResponding() && isStartButtonOn) {
            appendOneMessageToLog(getNotRespondingDevicesString("Нет связи с устройствами "));
            sleep(100);
        }


        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Инициализация испытания");
            communicationModel.onPR8M1();
            communicationModel.onKM5();

        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            communicationModel.setObjectParams(50 * HZ, 50 * VOLT, 50 * HZ);
            appendOneMessageToLog("Устанавливаем начальные точки для ЧП");
            communicationModel.startObject();
        }

        if (isExperimentRunning && isStartButtonOn && isDevicesResponding()) {
            appendOneMessageToLog("Поднимаем напряжение до " + (int) UInsulation + "B");
            regulation(50 * VOLT, 40, 15, (int) UInsulation, 0.1, 30, 100, 200);
        }

        int experimentTime = 60;
        while (isExperimentRunning && isStartButtonOn && isDevicesResponding() && (experimentTime-- > 0)) {
            sleep(1000);
            appendOneMessageToLog("Ждем 60 секунд");
            Experiment7ModelPhase3HH.setTime(String.valueOf(experimentTime));
        }
        experimentTime = 60;
        Experiment7ModelPhase3HH.setTime(String.valueOf(experimentTime));
        currentStage = 3;

        if (!cause.equals("")) {
            appendMessageToLog(String.format("Испытание прервано по причине: %s", cause));
            Experiment7ModelPhase3HH.setResult("Неуспешно");
        } else if (!isDevicesResponding()) {
            appendMessageToLog(getNotRespondingDevicesString("Испытание прервано по причине: потеряна связь с устройствами"));
            Experiment7ModelPhase3HH.setResult("Неуспешно");
        } else {
            Experiment7ModelPhase3HH.setResult("Успешно");
            appendMessageToLog("Испытание HH завершено успешно");
            isBHSuccess = false;
        }
        appendMessageToLog("------------------------------------------------\n");
        if (isExperimentRunning && isStartButtonOn) {
            communicationModel.stopObject();
            sleep(500);
            while (isExperimentRunning && !isDeltaReady0 && isDeltaResponding && isStartButtonOn) {
                sleep(100);
                appendOneMessageToLog("Ожидаем, пока частотный преобразователь остановится");
            }
        }

        isExperimentRunning = false;
        isExperimentEnd = true;
        communicationModel.offAllKms(); //разбираем все возможные схемы
        communicationModel.finalizeAllDevices(); //прекращаем опрашивать устройства

    }


    private void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperiment7Log.appendText(String.format("%s \t| %s\n", sdf.format(System.currentTimeMillis()), message)));
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
        while (isExperimentRunning && ((measuringUIn < end * coarseMinLimit) || (measuringUIn > end * coarseMaxLimit)) && isStartButtonOn && isDevicesResponding()) {
            if (measuringUIn < end * coarseMinLimit) {
                communicationModel.setObjectUMax(start += coarseStep);
            } else if (measuringUIn > end * coarseMaxLimit) {
                communicationModel.setObjectUMax(start -= coarseStep);
            }
            sleep(coarseSleep);
            appendOneMessageToLog("Выводим напряжение для получения заданного значения грубо");
        }
        while (isExperimentRunning && ((measuringUIn < end - fineLimit) || (measuringUIn > end + fineLimit)) && isStartButtonOn && isDevicesResponding()) {
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
                Experiment7ModelPhase3BH.setUIN(String.valueOf(measuringUIn));
                break;
            case 2:
                Experiment7ModelPhase3HH.setUIN(String.valueOf(measuringUIn));
                break;
        }
    }

    private void setI(double value) {
        iA = (int) (value * STATE_5_TO_5_MULTIPLIER * 1000 * POWER) / POWER;
        switch (currentStage) {
            case 1:
                Experiment7ModelPhase3BH.setIBH(String.valueOf(iA));
                break;
            case 2:
                Experiment7ModelPhase3HH.setIBH(String.valueOf(iA));
                break;
        }
        if (iA > 1000.0) {
            cause = "ток превысил";
            isExperimentRunning = false;
        } else {
            iAOld = iA;
        }
    }

    private void setCurrentFrequencyObject(short value) {
        isDeltaReady50 = value == 50 * HZ;
        isDeltaReady0 = value == 0;
    }
}