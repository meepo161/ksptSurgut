package ru.avem.ksptamur.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import ru.avem.ksptamur.db.AccountRepository;
import ru.avem.ksptamur.db.model.Account;
import ru.avem.ksptamur.utils.Toast;

import java.net.URISyntaxException;
import java.util.List;

import static ru.avem.ksptamur.Main.PRIMARY_STAGE;
import static ru.avem.ksptamur.Main.setTheme;

public class RegisterWindowController {
    private ObservableList<Account> profilesData;

    @FXML
    private TableView<Account> tableProfiles;
    @FXML
    private TableColumn<Account, String> columnProfilesLogin;
    @FXML
    private TableColumn<Account, String> columnProfilesFullName;


    @FXML
    private TextField textProfilesLogin;
    @FXML
    private TextField textProfilesFullName;
    @FXML
    private TextField textProfilesPosition;
    @FXML
    private TextField textProfilesPositionNumber;
    @FXML
    private PasswordField textProfilesPassword1;
    @FXML
    private PasswordField textProfilesPassword2;
    //endregion
    @FXML
    private AnchorPane root;

    @FXML
    public void initialize() {
        setTheme(root);
        initData();

        columnProfilesLogin.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnProfilesFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        // заполняем таблицу данными
        tableProfiles.setItems(profilesData);
    }

    private void initData() {
        List<Account> allAccounts = AccountRepository.getAllAccounts();
        profilesData = FXCollections.observableArrayList(allAccounts);
    }

    @FXML
    private void handleProfilesAddProfile() {
        if (isInputValid()) {
            Account account = new Account(textProfilesLogin.getText(), textProfilesPassword1.getText(),
                    textProfilesPosition.getText(), textProfilesPositionNumber.getText(), textProfilesFullName.getText());
            AccountRepository.insertAccount(account);
            profilesData.add(account);
        }
    }

    @FXML
    private void handleProfilesDeleteProfile() {
        int selectedIndex = tableProfiles.getSelectionModel().getSelectedIndex();
        Account account = tableProfiles.getSelectionModel().getSelectedItem();
        if (selectedIndex >= 0) {
            tableProfiles.getItems().remove(selectedIndex);
            AccountRepository.deleteAccount(account);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(PRIMARY_STAGE);
            alert.setTitle("Не выбрано");
            alert.setHeaderText("Профиль не выбран");
            alert.setContentText("Пожалуйста выберите профиль в таблице");

            alert.showAndWait();
        }
    }

    private boolean isInputValid() {
        StringBuilder errorMessage = new StringBuilder();

        if (textProfilesLogin.getText() == null || textProfilesLogin.getText().length() == 0) {
            errorMessage.append("Неверный логин\n");
        }
        if (textProfilesPosition.getText() == null || textProfilesPosition.getText().length() == 0) {
            errorMessage.append("Неверная должность\n");
        }
        if (textProfilesPositionNumber.getText() == null || textProfilesPositionNumber.getText().length() == 0) {
            errorMessage.append("Неверный табельный номер\n");
        }
        if (textProfilesFullName.getText() == null || textProfilesFullName.getText().length() == 0) {
            errorMessage.append("Неверные ФИО\n");
        }
        if (textProfilesPassword1.getText() == null || textProfilesPassword1.getText().length() == 0) {
            errorMessage.append("Неверный пароль\n");
        }

        if (textProfilesPassword2.getText() == null || textProfilesPassword2.getText().length() == 0) {
            errorMessage.append("Неверный второй пароль\n");
        } else {
            if (!textProfilesPassword1.getText().equals(textProfilesPassword2.getText())) {
                errorMessage.append("Пароли не совпадают\n");
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Toast.makeText(errorMessage.toString()).show(Toast.ToastType.ERROR);
            return false;
        }
    }
}
