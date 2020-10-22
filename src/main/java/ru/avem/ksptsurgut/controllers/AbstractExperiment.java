package ru.avem.ksptsurgut.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.avem.ksptsurgut.Main;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.model.ExperimentValuesModel;
import ru.avem.ksptsurgut.utils.AlertController;
import ru.avem.ksptsurgut.utils.View;

import java.util.concurrent.atomic.AtomicBoolean;

import static ru.avem.ksptsurgut.Constants.Formatting.EXPERIMENT_FORMAT;
import static ru.avem.ksptsurgut.utils.Utils.sleep;

public abstract class AbstractExperiment extends DeviceState implements ExperimentController {

    @FXML
    protected AnchorPane root;
    @FXML
    protected Button buttonCancelAll;
    @FXML
    protected Button buttonStartStop;
    @FXML
    protected Button buttonNext;
    @FXML
    protected VBox vBoxLog;
    @FXML
    protected ScrollPane scrollPaneLog;

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

    protected void showInformDialogForButtonPost(String informMessage) {
        if (isExperimentRunning) {
            AtomicBoolean isReallyCanceled = new AtomicBoolean(false);
            AtomicBoolean isPressed = new AtomicBoolean(false);
            Platform.runLater(() -> AlertController.showConfirmDialog(
                    "ВНИМАНИЕ",
                    informMessage,
                    "",
                    null,
                    "Отмена",
                    () -> {
                        if (!isReallyCanceled.get()) {
                            setCause("Испытание остановлено оператором");
                            isPressed.set(true);
                        }
                    }));

            while (!isPressed.get() && !isStartButtonOn && isExperimentRunning) {
                sleep(10);
            }
            isReallyCanceled.set(true);
            Platform.runLater(() -> AlertController.getAlert().close());
        }
    }

    protected void clearLog() {
        Platform.runLater(() -> vBoxLog.getChildren().clear());
    }

    protected void appendMessageToLog(String tag, String message) {
        Text msg = new Text(EXPERIMENT_FORMAT.format(System.currentTimeMillis()) + " | " + message);
        msg.setStyle("-fx-fill:" + tag + ";" + "; -fx-font-size: 24;");/* + tag + ";");*/
//        msg.setStyle("-fx-font-size: 24;");
        Platform.runLater(() ->
                vBoxLog.getChildren().add(msg));
    }

    protected void appendOneMessageToLog(String tag, String message) {
        if (logBuffer == null || !logBuffer.equals(message)) {
            logBuffer = message;
            appendMessageToLog(tag, message);
        }
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
