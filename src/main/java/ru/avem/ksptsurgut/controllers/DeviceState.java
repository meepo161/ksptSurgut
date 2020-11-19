package ru.avem.ksptsurgut.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ru.avem.ksptsurgut.communication.devices.ikas.IKASModel;
import ru.avem.ksptsurgut.communication.devices.phasemeter.PhaseMeterModel;
import ru.avem.ksptsurgut.communication.devices.pm130.PM130Model;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptsurgut.communication.devices.trm.TRMModel;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.ksptsurgut.communication.devices.DeviceController.*;

//import ru.avem.ksptamur.communication.devices.fr_a800.FRA800Model;

public class DeviceState implements Observer {

    @FXML
    protected Circle deviceStateCirclePM130;
    @FXML
    protected Circle deviceStateCirclePM130_2;
    @FXML
    protected Circle deviceStateCircleParma400;
    @FXML
    protected Circle deviceStateCircleAVEM;
    @FXML
    protected Circle deviceStateCircleCS0202;
    @FXML
    protected Circle deviceStateCirclePhaseMeter;
    @FXML
    protected Circle deviceStateCircleIKAS;
    @FXML
    protected Circle deviceStateCirclePR200;
    @FXML
    protected Circle deviceStateCircleDELTACP2000;
    @FXML
    protected Circle deviceStateCircleTrm;

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);


        switch (modelId) {
//            case MEGACS_ID:
//                if (param == CS020201Model.RESPONDING_PARAM) {
//                    Platform.runLater(() -> deviceStateCircleCS0202.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                }
//                break;
            case PR200_ID:
                if (param == OwenPRModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
//            case AVEM_ID:
//                if (param == AvemVoltmeterModel.RESPONDING_PARAM) {
//                    Platform.runLater(() -> deviceStateCircleAVEM.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                }
//                break;
            case PM130_ID:
                if (param == PM130Model.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case PM130_2_ID:
                if (param == PM130Model.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCirclePM130_2.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
            case IKAS_ID:
                if (param == IKASModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCircleIKAS.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
//            case PARMA400_ID:
//                if (param == ParmaT400Model.RESPONDING_PARAM) {
//                    Platform.runLater(() -> deviceStateCircleParma400.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                }
//                break;
            case PHASEMETER_ID:
                if (param == PhaseMeterModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCirclePhaseMeter.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
//            case DELTACP2000_ID:
//                if (param == DeltaCP2000Model.RESPONDING_PARAM) {
//                    Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
//                }
//                break;
            case TRM_ID:
                if (param == TRMModel.RESPONDING_PARAM) {
                    Platform.runLater(() -> deviceStateCircleTrm.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
                break;
        }
    }
}
