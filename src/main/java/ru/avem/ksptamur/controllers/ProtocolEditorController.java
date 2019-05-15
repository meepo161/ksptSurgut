package ru.avem.ksptamur.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ru.avem.ksptamur.db.ProtocolRepository;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.logging.Logging;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.utils.Toast;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static ru.avem.ksptamur.Main.setTheme;
import static ru.avem.ksptamur.utils.Utils.openFile;

public class ProtocolEditorController {
    @FXML
    private AnchorPane root;

    @FXML
    private TextField filterField;

    @FXML
    private TableView<Protocol> tableProtocols;
    @FXML
    private TableColumn<Protocol, String> columnProtocolID;
    @FXML
    private TableColumn<Protocol, Double> columnProtocolSerialNumber;
    @FXML
    private TableColumn<Protocol, String> columnProtocolDate;
    @FXML
    private TableColumn<Protocol, String> columnProtocolTime;
    @FXML
    private TableColumn<Protocol, Double> columnProtocolFullName1;
    @FXML
    private TableColumn<Protocol, Double> columnProtocolFullName2;

    @FXML
    private ComboBox<String> comboBoxExperiments;

    private MainModel mainModel;
    private ObservableList<Protocol> protocols;

    @FXML
    private void initialize() {
        setTheme(root);
        mainModel = MainModel.getInstance();
        initData();

        columnProtocolID.setCellValueFactory(new PropertyValueFactory<>("id"));
        columnProtocolSerialNumber.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        columnProtocolDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        columnProtocolTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        columnProtocolFullName1.setCellValueFactory(new PropertyValueFactory<>("position1FullName"));
        columnProtocolFullName2.setCellValueFactory(new PropertyValueFactory<>("position2FullName"));

        FilteredList<Protocol> filteredData = new FilteredList<>(protocols, p -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(protocol -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(protocol.getId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (protocol.getSerialNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (protocol.getDate().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (protocol.getTime().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (protocol.getPosition1FullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (protocol.getPosition2FullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Protocol> sortedData = new SortedList<>(filteredData);

        sortedData.comparatorProperty().bind(tableProtocols.comparatorProperty());

        tableProtocols.setItems(sortedData);
    }

    private void initData() {
        List<Protocol> allProtocols = ProtocolRepository.getAllProtocols();
        protocols = FXCollections.observableArrayList(allProtocols);
    }

    @FXML
    private void handleProtocolOpen() {
        int selectedIndex = tableProtocols.getSelectionModel().getSelectedIndex();
        Protocol protocol = tableProtocols.getSelectionModel().getSelectedItem();
        openFile(Logging.getTempWorkbook(protocol));

    }


    @FXML
    private void handleProtocolSaveAs() {
        if (tableProtocols.getSelectionModel().getSelectedIndex() >= 0) {
            FileChooser protocolFileChooser = new FileChooser();
            protocolFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            protocolFileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("AVEM Protocol (*.xlsx)", "*.xlsx"));

            File file = protocolFileChooser.showSaveDialog(root.getScene().getWindow());
            if (!Logging.writeWorkbookToFile(tableProtocols.getSelectionModel().getSelectedItem(), file)) {
                Toast.makeText("При попытке сохранения протокола произошла ошибка").show(Toast.ToastType.ERROR);
            } else {
                Toast.makeText("Протокол успешно сохранён").show(Toast.ToastType.INFORMATION);
            }
        } else {
            Toast.makeText("Выберите для какого испытания сохранить протокол").show(Toast.ToastType.WARNING);
        }
    }

    @FXML
    private void handleProtocolDelete() {
        int selectedIndex = tableProtocols.getSelectionModel().getSelectedIndex();
        Protocol protocol = tableProtocols.getSelectionModel().getSelectedItem();
        if (selectedIndex >= 0) {
            protocols.remove(selectedIndex);
            ProtocolRepository.deleteProtocol(protocol);
            mainModel.setNeedRefresh(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Не выбрано");
            alert.setHeaderText("Протокол не выбран");
            alert.setContentText("Пожалуйста выберите протокол в таблице");

            alert.showAndWait();
        }
    }
}
