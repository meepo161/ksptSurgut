package ru.avem.ksptsurgut.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ru.avem.ksptsurgut.communication.devices.pr200.OwenPRModel;

import java.util.Observable;
import java.util.Observer;

import static ru.avem.ksptsurgut.communication.devices.DeviceController.PR200_ID;

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

    @Override
    public void update(Observable o, Object values) {
        int modelId = (int) (((Object[]) values)[0]);
        int param = (int) (((Object[]) values)[1]);
        Object value = (((Object[]) values)[2]);

        switch (modelId) {
            case PR200_ID:
                switch (param) {
                    case OwenPRModel.PRI2:
                        Platform.runLater(() -> deviceStateKM1.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI3:
                        Platform.runLater(() -> deviceStateKM2.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI5:
                        Platform.runLater(() -> deviceStateKM3.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI6:
                        Platform.runLater(() -> deviceStateKM4.setFill(((boolean) value) ? Color.LIME : Color.RED));
                        break;
                    case OwenPRModel.PRI7:
                        Platform.runLater(() -> deviceStateKM5.setFill(((boolean) value) ? Color.LIME : Color.RED));
                }
        }
    }
}
