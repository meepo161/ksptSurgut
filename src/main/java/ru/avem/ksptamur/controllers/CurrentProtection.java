package ru.avem.ksptamur.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ru.avem.ksptamur.communication.devices.pr200.OwenPRModel;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.ksptamur.communication.devices.DeviceController.PR200_ID;

public class CurrentProtection implements Observer {
    @FXML
    private Circle deviceStateKM1;
    @FXML
    private Circle deviceStateKM2;
    @FXML
    private Circle deviceStateKM3;
    @FXML
    private Circle deviceStateKM4;
    @FXML
    private Circle deviceStateKM5;
    @FXML
    private Circle deviceStateKM6;
    @FXML
    private Circle deviceStateKM7;

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.PRDI1_FIXED:
                        Platform.runLater(() -> deviceStateKM1.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI2_FIXED:
                        Platform.runLater(() -> deviceStateKM2.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI3_FIXED:
                        Platform.runLater(() -> deviceStateKM3.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI4_FIXED:
                        Platform.runLater(() -> deviceStateKM4.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI5_FIXED:
                        Platform.runLater(() -> deviceStateKM5.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI6_FIXED:
                        Platform.runLater(() -> deviceStateKM6.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRDI7_FIXED:
                        Platform.runLater(() -> deviceStateKM7.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                }
        }
    }
}
