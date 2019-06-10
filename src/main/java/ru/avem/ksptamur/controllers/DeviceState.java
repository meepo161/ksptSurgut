package ru.avem.ksptamur.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import ru.avem.ksptamur.communication.devices.cs02021.CS020201Model;
//import ru.avem.ksptamur.communication.devices.fr_a800.FRA800Model;
import ru.avem.ksptamur.communication.devices.deltaC2000.DeltaCP2000Model;
import ru.avem.ksptamur.communication.devices.ikas.IKASModel;
import ru.avem.ksptamur.communication.devices.parmaT400.ParmaT400Model;
import ru.avem.ksptamur.communication.devices.phasemeter.PhaseMeterModel;
import ru.avem.ksptamur.communication.devices.pm130.PM130Model;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;
import ru.avem.ksptamur.communication.devices.trm.TRMModel;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.ksptamur.communication.devices.DeviceController.*;

public class DeviceState implements Observer {

    @FXML
    protected Circle deviceStateCirclePM130;
    @FXML
    protected Circle deviceStateCircleParma400;
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

    private int isCS0202 = 0;
    private int isPR200 = 0;
    private int isPM130 = 0;
    private int isIKAS = 0;
    private int isPARMA400 = 0;
    private int isPhaseMeter = 0;
    private int isDelta = 0;
    private int isTRM = 0;

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);


        switch (modelId) {
            case MEGACS_ID:
                if (param == CS020201Model.RESPONDING_PARAM) {
                    if (isCS0202 < 2) {
                        Platform.runLater(() -> deviceStateCircleCS0202.setFill(Color.DODGERBLUE));
                        isCS0202++;
                    } else {
                        Platform.runLater(() -> deviceStateCircleCS0202.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case PR200_ID:
                if (param == OwenPRModel.RESPONDING_PARAM) {
                    System.out.println("isPR200 " + isPR200);
                    if (isPR200 < 2) {
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(Color.DODGERBLUE));
                        isPR200++;
                    } else {
                        Platform.runLater(() -> deviceStateCirclePR200.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case PM130_ID:
                if (param == PM130Model.RESPONDING_PARAM) {
                    if (isPM130 < 2) {
                        Platform.runLater(() -> deviceStateCirclePM130.setFill(Color.DODGERBLUE));
                        isPM130++;
                    } else {
                        Platform.runLater(() -> deviceStateCirclePM130.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case IKAS_ID:
                if (param == IKASModel.RESPONDING_PARAM) {
                    if (isIKAS < 2) {
                        Platform.runLater(() -> deviceStateCircleIKAS.setFill(Color.DODGERBLUE));
                        isIKAS++;
                    } else {
                        Platform.runLater(() -> deviceStateCircleIKAS.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case PARMA400_ID:
                if (param == ParmaT400Model.RESPONDING_PARAM) {
                    if (isPARMA400 < 2) {
                        Platform.runLater(() -> deviceStateCircleParma400.setFill(Color.DODGERBLUE));
                        isPARMA400++;
                    } else {
                        Platform.runLater(() -> deviceStateCircleParma400.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case PHASEMETER_ID:
                if (param == PhaseMeterModel.RESPONDING_PARAM) {
                    if (isPhaseMeter < 2) {
                        Platform.runLater(() -> deviceStateCirclePhaseMeter.setFill(Color.DODGERBLUE));
                        isPhaseMeter++;
                    } else {
                        Platform.runLater(() -> deviceStateCirclePhaseMeter.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case DELTACP2000_ID:
                if (param == DeltaCP2000Model.RESPONDING_PARAM) {
                    if (isDelta < 2) {
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(Color.DODGERBLUE));
                        isDelta++;
                    } else {
                        Platform.runLater(() -> deviceStateCircleDELTACP2000.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
            case TRM_ID:
                if (param == TRMModel.RESPONDING_PARAM) {
                    if (isTRM < 2) {
                        Platform.runLater(() -> deviceStateCircleTrm.setFill(Color.DODGERBLUE));
                        isTRM++;
                    } else {
                        Platform.runLater(() -> deviceStateCircleTrm.setFill(((boolean) value) ? Color.LIME : Color.RED));
                    }
                }
                break;
        }
    }
}
