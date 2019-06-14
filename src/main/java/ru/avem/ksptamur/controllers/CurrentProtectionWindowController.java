package ru.avem.ksptamur.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ru.avem.ksptamur.communication.CommunicationModel;

import static ru.avem.ksptamur.Main.setTheme;

public class CurrentProtectionWindowController extends CurrentProtection {

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        CommunicationModel model = CommunicationModel.getInstance();
        model.addObserver(this);
        model.initOwenPrController();
    }
}
