package ru.avem.ksptamur.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.model.ExperimentValuesModel;
import ru.avem.ksptamur.utils.View;

import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptamur.Constants.Formatting.EXPERIMENT_FORMAT;
import static ru.avem.ksptamur.utils.Utils.sleep;

public abstract class AbstractExperiment extends DeviceState implements ExperimentController {

    @FXML
    protected AnchorPane root;
    @FXML
    protected JFXButton buttonCancelAll;
    @FXML
    protected JFXButton buttonStartStop;
    @FXML
    protected JFXButton buttonNext;
    @FXML
    protected JFXTextArea textAreaExperimentProcessLog;

    protected CommunicationModel communicationModel = CommunicationModel.getInstance();
    protected ExperimentValuesModel experimentsValuesModel = ExperimentValuesModel.getInstance();
    protected Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();

    private String logBuffer;

    private Stage dialogStage;
    private boolean isCanceled;

    protected volatile boolean isExperimentRunning;
    protected volatile boolean isExperimentEnded = true;
    protected volatile boolean isNeedToRefresh = true;
    protected volatile boolean isNeedToWaitDelta;

    protected volatile boolean isOwenPRResponding;
    protected volatile boolean isDeltaResponding;
    protected volatile boolean isParmaResponding;
    protected volatile boolean isPM130Responding;
    protected volatile boolean isIkasResponding;
    protected volatile boolean isTrmResponding;
    protected volatile boolean isPhaseMeterResponding;

    protected volatile boolean isStartButtonOn;

    protected volatile String cause;


    @FXML
    protected void handleExperimentCancel() {
        dialogStage.close();
        isCanceled = true;
    }


    @FXML
    protected void handleRunStopExperiment() {
        if (isExperimentEnded) {
            initExperiment();
        } else {
            setCause("Отменено оператором");
        }
    }

    protected void showRequestDialog(String request) {
        showRequestDialog(request, false);
    }

    protected void showRequestDialog(String request, boolean force) {
        if (isExperimentRunning && ((isDevicesResponding() && isStartButtonOn) || force)) {
            AtomicBoolean isPressed = new AtomicBoolean(false);
            Platform.runLater(() -> {
                View.showConfirmDialog(request,
                        () -> isPressed.set(true),
                        () -> {
                            setCause("Отменено оператором");
                            isPressed.set(true);
                        });
            });

            while (!isPressed.get()) {
                sleep(100);
            }
        }
    }

    protected void appendOneMessageToLog(String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(message);
        }
    }

    protected void appendMessageToLog(String message) {
        Platform.runLater(() -> textAreaExperimentProcessLog.appendText(String.format("%s | %s\n",
                EXPERIMENT_FORMAT.format(System.currentTimeMillis()), message)));
    }

    protected boolean isThereAreAccidents() {
        return false;
    }

    protected String getAccidentsString(String mainText) {
        return String.format("%s: ",
                mainText);
    }

    protected void setCause(String cause) {
        this.cause = cause;
        if (!cause.isEmpty()) {
            isExperimentRunning = false;
        }
    }

    @FXML
    protected void handleNextExperiment() {
        fillFieldsOfExperimentProtocol();
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

    protected abstract void initExperiment();

    protected abstract void runExperiment();

    protected abstract void finalizeExperiment();

    protected abstract boolean isDevicesResponding();

    protected abstract String getNotRespondingDevicesString(String mainText);

    protected abstract void fillFieldsOfExperimentProtocol();


}
