package ru.avem.ksptsurgut.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.model.ExperimentValuesModel;
import ru.avem.ksptsurgut.utils.View;

import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptsurgut.Constants.Formatting.EXPERIMENT_FORMAT;
import static ru.avem.ksptsurgut.utils.Utils.sleep;

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

    protected volatile boolean isOwenPRResponding;
    protected volatile boolean isDeltaResponding;
    protected volatile boolean isParmaResponding;
    protected volatile boolean isPM130Responding;
    protected volatile boolean isPM130_2_Responding;
    protected volatile boolean isAvem1Responding;
    protected volatile boolean isAvem2Responding;
    protected volatile boolean isAvem3Responding;
    protected volatile boolean isIkasResponding;
    protected volatile boolean isTrmResponding;
    protected volatile boolean isPhaseMeterResponding;

    protected volatile boolean isDoorZone;
    protected volatile boolean isDoorSHSO;
    protected volatile boolean isStopButton;
    protected volatile boolean isCurrentOI;
    protected volatile boolean isCurrentVIU;
    protected volatile boolean isCurrentInput;
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

    protected void showStartDialog(String request) {
        if (isExperimentRunning) {
            AtomicBoolean isPressed = new AtomicBoolean(false);
            Platform.runLater(() -> {
                View.showConfirmDialog(request,
                        () -> isPressed.set(true),
                        () -> setCause("Отменено оператором"));
            });

            while (!isStartButtonOn) {
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
        return isDoorZone || isDoorSHSO || isCurrentOI;
    }

    protected String getAccidentsString(String mainText) {
        return String.format("%s: %s%s%s",
                mainText,
                isDoorZone ? "открыта дверь зоны, " : "",
                isDoorSHSO ? "открыты двери ШСО, " : "",
                isCurrentOI ? "сработала токовая защита объекта испытания, " : "");
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
