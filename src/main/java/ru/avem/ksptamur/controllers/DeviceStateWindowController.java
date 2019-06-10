package ru.avem.ksptamur.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ru.avem.ksptamur.communication.CommunicationModel;

import static ru.avem.ksptamur.Main.setTheme;

public class DeviceStateWindowController extends DeviceState {
    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        CommunicationModel model = CommunicationModel.getInstance();
        model.addObserver(this);
        model.setDeviceStateOn(true);
        model.setNeedToReadAllDevices(true);
    }
}