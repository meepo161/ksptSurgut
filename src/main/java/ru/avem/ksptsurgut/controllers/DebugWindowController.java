package ru.avem.ksptsurgut.controllers;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.utils.Toast;

import java.util.Observable;

import static ru.avem.ksptsurgut.Constants.Measuring.HZ;
import static ru.avem.ksptsurgut.Constants.Measuring.VOLT;
import static ru.avem.ksptsurgut.Main.setTheme;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.DELTACP2000_ID;
import static ru.avem.ksptsurgut.communication.devices.DeviceController.PR200_ID;
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
    public JFXCheckBox rbPR1;
    @FXML
    public JFXCheckBox rbPR2;
    @FXML
    public JFXCheckBox rbPR3;
    @FXML
    public JFXCheckBox rbPR4;
    @FXML
    public JFXCheckBox rbPR5;
    @FXML
    public JFXCheckBox rbPR6;
    @FXML
    public JFXCheckBox rbPR7;
    @FXML
    public JFXCheckBox rbPR8;
    @FXML
    public JFXCheckBox rbPR1M1;
    @FXML
    public JFXCheckBox rbPR2M1;
    @FXML
    public JFXCheckBox rbPR3M1;
    @FXML
    public JFXCheckBox rbPR4M1;
    @FXML
    public JFXCheckBox rbPR5M1;
    @FXML
    public JFXCheckBox rbPR6M1;
    @FXML
    public JFXCheckBox rbPR7M1;
    @FXML
    public JFXCheckBox rbPR8M1;
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
        communicationModel.setNeedToReadForDebug(true);
        communicationModel.setDeviceStateOn(true);
        communicationModel.initOwenPrController();
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
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI1:
                        rbPRI1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI2:
                        rbPRI2.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI3:
                        rbPRI3.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI4:
                        rbPRI4.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI5:
                        rbPRI5.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI6:
                        rbPRI6.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI7:
                        rbPR7.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRI8:
                        rbPR8.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM1:
                        rbPR1M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM2:
                        rbPR2M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM3:
                        rbPR3M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM4:
                        rbPR4M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM5:
                        rbPR5M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM6:
                        rbPR6M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM7:
                        rbPR7M1.setSelected((boolean) value);
                        break;
                    case OwenPRModel.PRIM8:
                        rbPR8M1.setSelected((boolean) value);
                        break;
                }
                break;
            case DELTACP2000_ID:
                switch (param) {
                    case DeltaCP2000Model.RESPONDING_PARAM:
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                }
                break;
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
    private void handleRBPR1() {
        System.out.println("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        setRadioPR(rbPR1, 1, 1);
    }

    @FXML
    private void handleRBPR2() {
        setRadioPR(rbPR2, 2, 1);
    }

    @FXML
    private void handleRBPR3() {
        setRadioPR(rbPR3, 3, 1);
    }

    @FXML
    private void handleRBPR4() {
        setRadioPR(rbPR4, 4, 1);
    }

    @FXML
    private void handleRBPR5() {
        setRadioPR(rbPR5, 5, 1);
    }

    @FXML
    private void handleRBPR6() {
        setRadioPR(rbPR6, 6, 1);
    }

    @FXML
    private void handleRBPR7() {
        setRadioPR(rbPR7, 7, 1);
    }

    @FXML
    private void handleRBPR8() {
        setRadioPR(rbPR8, 8, 1);
    }

    @FXML
    private void handleRBPR1M1() {
        setRadioPR(rbPR1M1, 1, 2);
    }

    @FXML
    private void handleRBPR2M1() {
        setRadioPR(rbPR2M1, 2, 2);
    }

    @FXML
    private void handleRBPR3M1() {
        setRadioPR(rbPR3M1, 3, 2);
    }

    @FXML
    private void handleRBPR4M1() {
        setRadioPR(rbPR4M1, 4, 2);
    }

    @FXML
    private void handleRBPR5M1() {
        setRadioPR(rbPR5M1, 5, 2);
    }

    @FXML
    private void handleRBPR6M1() {
        setRadioPR(rbPR6M1, 6, 2);
    }

    @FXML
    private void handleRBPR7M1() {
        setRadioPR(rbPR7M1, 7, 2);
    }

    @FXML
    private void handleRBPR8M1() {
        setRadioPR(rbPR8M1, 8, 2);
    }

    private void setRadioPR(JFXCheckBox rb, int register, int kms) {
        System.out.println("setRadioPR" + rb.isSelected());
        if (!rb.isSelected()) {
            communicationModel.onRegisterInTheKms(register, kms);
        } else {
            communicationModel.offRegisterInTheKms(register, kms);
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
}
