package ru.avem.ksptsurgut.controllers;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.utils.Toast;

import java.util.Observable;

import static ru.avem.ksptsurgut.Constants.Measuring.HZ;
import static ru.avem.ksptsurgut.Constants.Measuring.VOLT;
import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.utils.Utils.sleep;

public class DebugWindowController extends DeviceState implements ExperimentController {

    @FXML
    public JFXCheckBox rbPRI1;
    @FXML
    public JFXCheckBox rbPRI2;
    @FXML
    public JFXCheckBox rbPRI3;
    @FXML
    public JFXCheckBox rbPRI4;
    @FXML
    public JFXCheckBox rbPRI5;
    @FXML
    public JFXCheckBox rbPRI6;
    @FXML
    public JFXCheckBox rbPRI7;
    @FXML
    public JFXCheckBox rbPRI8;
    @FXML
    public JFXCheckBox rbPRI1M1;
    @FXML
    public JFXCheckBox rbPRI2M1;
    @FXML
    public JFXCheckBox rbPRI3M1;
    @FXML
    public JFXCheckBox rbPRI4M1;
    @FXML
    public JFXCheckBox rbPRI5M1;
    @FXML
    public JFXCheckBox rbPRI6M1;
    @FXML
    public JFXCheckBox rbPRI7M1;
    @FXML
    public JFXCheckBox rbPRI8M1;

    @FXML
    public ToggleButton DO1;
    @FXML
    public ToggleButton DO2;
    @FXML
    public ToggleButton DO3;
    @FXML
    public ToggleButton DO4;
    @FXML
    public ToggleButton DO5;
    @FXML
    public ToggleButton DO6;
    @FXML
    public ToggleButton DO7;
    @FXML
    public ToggleButton DO8;
    @FXML
    public ToggleButton DO9;
    @FXML
    public ToggleButton DO10;
    @FXML
    public ToggleButton DO11;
    @FXML
    public ToggleButton DO12;
    @FXML
    public ToggleButton DO13;
    @FXML
    public ToggleButton DO14;
    @FXML
    public ToggleButton DO15;
    @FXML
    public ToggleButton DO16;
    @FXML
    public ToggleButton DOM1;
    @FXML
    public ToggleButton DOM2;
    @FXML
    public ToggleButton DOM3;
    @FXML
    public ToggleButton DOM4;
    @FXML
    public ToggleButton DOM5;
    @FXML
    public ToggleButton DOM6;
    @FXML
    public ToggleButton DOM7;
    @FXML
    public ToggleButton DOM8;


    @FXML
    public JFXTextField tfVoltage;
    @FXML
    public JFXTextField tfCurrentVoltage;


    private CommunicationModel communicationModel = CommunicationModel.getInstance();

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        communicationModel.addObserver(this);
//        communicationModel.setNeedToReadForDebug(true);
        communicationModel.setDeviceStateOn(true);
        communicationModel.initOwenPrController();
    }

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
//            case PR200_ID:
//                switch (param) {
//                    case OwenPRModel.RESPONDING_PARAM:
//                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                }
//                break;
//            case DELTACP2000_ID:
//                switch (param) {
//                    case DeltaCP2000Model.RESPONDING_PARAM:
//                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                        break;
//                }
//                break;
        }
    }

    public void handleBtnSetVoltage() {
        try {
            communicationModel.setObjectParams(50 * HZ, 5 * VOLT, 50 * HZ);
            sleep(1000);
            communicationModel.startObject();
            sleep(1000);
            communicationModel.setObjectUMax(Integer.parseInt(tfVoltage.getText()));
        } catch (NumberFormatException e) {
            Toast.makeText("Введите верное напряжение").show(Toast.ToastType.ERROR);
        }
    }

    @FXML
    private void handleBtnStopObject() {
        communicationModel.stopObject();
    }

    @FXML
    private void handleAllOn() {
        communicationModel.onAllKms();
    }

    @FXML
    private void handleAllOff() {
        communicationModel.offAllKms();
    }


    @Override
    public void setDialogStage(Stage dialogStage) {

    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @FXML
    private void handleBtnPR1() {
    }

    public void handleDO1() {
        new Thread(() -> {
            if (DO1.isSelected()) {
                communicationModel.onDO1();
                Platform.runLater(() -> DO1.setText("OFF"));
            } else {
                communicationModel.offDO1();
                Platform.runLater(() -> DO1.setText("ON"));
            }
        }).start();
    }

    public void handleDO2() {
        new Thread(() -> {
            if (DO2.isSelected()) {
                communicationModel.onDO2();
                DO2.setText("OFF");
            } else {
                communicationModel.offDO2();
                DO2.setText("ON");
            }
        }).start();
    }

    public void handleDO3() {
        if (DO3.isSelected()) {
            communicationModel.onDO3();
            DO3.setText("OFF");
        } else {
            communicationModel.offDO3();
            DO3.setText("ON");
        }
    }

    public void handleDO4() {
        if (DO4.isSelected()) {
            communicationModel.onDO4();
            DO4.setText("OFF");
        } else {
            communicationModel.offDO4();
            DO4.setText("ON");
        }
    }

    public void handleDO5() {
        if (DO5.isSelected()) {
            communicationModel.onDO5();
            DO4.setText("OFF");
        } else {
            communicationModel.offDO5();
            DO4.setText("ON");
        }
    }

    public void handleDO6() {
    }

    public void handleDO7() {
    }

    public void handleDO8() {
    }

    public void handleDO9() {
    }

    public void handleDO10() {
    }

    public void handleDO11() {
    }

    public void handleDO12() {
    }

    public void handleDO13() {
    }

    public void handleDO14() {
    }

    public void handleDO15() {
    }

    public void handleDO16() {
    }

    public void handleDOM1() {
    }

    public void handleDOM2() {
    }

    public void handleDOM3() {
    }

    public void handleDOM4() {
    }

    public void handleDOM5() {
    }

    public void handleDOM6() {
    }

    public void handleDOM7() {
    }

    public void handleDOM8() {
    }
}
