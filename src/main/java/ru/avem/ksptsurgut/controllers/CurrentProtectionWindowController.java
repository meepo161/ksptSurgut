package ru.avem.ksptsurgut.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ru.avem.ksptsurgut.communication.CommunicationModel;

import static ru.avem.ksptsurgut.Main.setTheme;

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
