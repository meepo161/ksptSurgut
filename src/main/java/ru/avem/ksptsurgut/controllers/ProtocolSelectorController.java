package ru.avem.ksptsurgut.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ru.avem.ksptsurgut.db.ProtocolRepository;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.model.ExperimentValuesModel;

import java.util.List;

import static ru.avem.ksptsurgut.Main.setTheme;

public class ProtocolSelectorController implements ExperimentController {

    private ExperimentValuesModel experimentsValuesModel;

    private ObservableList<Protocol> protocols;

    @FXML
    private TableView<Protocol> tableProtocols;

    @FXML
    private TableColumn<Protocol, String> columnProtocolID;

    @FXML
    private TableColumn<Protocol, Double> columnProtocolSerialNumber;

    @FXML
    private TableColumn<Protocol, String> columnProtocolDate;

    @FXML
    private TableColumn<Protocol, Double> columnProtocolFullName1;

    @FXML
    private TableColumn<Protocol, Double> columnProtocolFullName2;

    private Stage dialogStage;
    private boolean isCanceled = true;

    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        experimentsValuesModel = ExperimentValuesModel.getInstance();
        initData();

        columnProtocolID.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnProtocolSerialNumber.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        columnProtocolDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        columnProtocolFullName1.setCellValueFactory(new PropertyValueFactory<>("position1FullName"));
        columnProtocolFullName2.setCellValueFactory(new PropertyValueFactory<>("position2FullName"));

        // заполняем таблицу данными
        tableProtocols.setItems(protocols);
    }

    private void initData() {
        List<Protocol> allProtocols = ProtocolRepository.getAllProtocols();
        protocols = FXCollections.observableArrayList(allProtocols);
    }

    @FXML
    private void handleProtocolSelect() {
        int selectedIndex = tableProtocols.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            experimentsValuesModel.setIntermediateProtocol(tableProtocols.getSelectionModel().getSelectedItem());
            isCanceled = false;
            dialogStage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Не выбрано");
            alert.setHeaderText("Протокол не выбран");
            alert.setContentText("Пожалуйста выберите протокол в таблице");

            alert.showAndWait();
        }
    }

    @Override
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @Override
    public boolean isCanceled() {
        return isCanceled;
    }
}
