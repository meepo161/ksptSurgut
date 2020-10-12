package ru.avem.ksptsurgut.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.avem.ksptsurgut.Main;
import ru.avem.ksptsurgut.db.AccountRepository;
import ru.avem.ksptsurgut.db.model.Account;
import ru.avem.ksptsurgut.model.ExperimentValuesModel;
import ru.avem.ksptsurgut.utils.Toast;

import java.util.List;

public class LoginController {
    @FXML
    private TextField textLogin;
    @FXML
    private PasswordField textPassword;
    @FXML
    private ComboBox<Account> secondTester;

    private Main main;
    private ExperimentValuesModel experimentsValuesModel = ExperimentValuesModel.getInstance();

    @FXML
    private void handleLogIn() {
        List<Account> allAccounts = AccountRepository.getAllAccounts();
        String login = textLogin.getText();
        String password = textPassword.getText();

        if (allAccounts.size() == 0) {
            if (login.equals("Administrator") && password.equals("Administrator")) {
                Toast.makeText("Вы зашли в DEBUG режим").show(Toast.ToastType.INFORMATION);
                main.showMainView();
                return;
            } else {
                Toast.makeText("В базе данных нет пользователей, обратитесь к поставщику").show(Toast.ToastType.ERROR);
                return;
            }
        }

        Account foundAccount = null;
        for (Account account : allAccounts) {
            if (login.equals(account.getName())) {
                foundAccount = account;
                if (password.equals((account.getPassword()))) {
                    Account secondTesterAccount = secondTester.getSelectionModel().getSelectedItem();
                    if (secondTesterAccount == null) {
                        Toast.makeText("Выберите второго испытателя").show(Toast.ToastType.WARNING);
                        return;
                    }
                    if (account.equals(secondTesterAccount) && allAccounts.size() > 1) {
                        Toast.makeText("Первый и второй испытатель не могут быть одним и тем же лицом").show(Toast.ToastType.WARNING);
                        return;
                    }
                    experimentsValuesModel.setTesters(account, secondTesterAccount);
                    main.showMainView();
                    break;
                } else {
                    Toast.makeText("Введенные вами данные неверные").show(Toast.ToastType.ERROR);
                }
            }
        }

        if (foundAccount == null) {
            Toast.makeText("Введенные вами данные неверные").show(Toast.ToastType.ERROR);
        }
    }

    public void setMainApp(Main main) {
        this.main = main;
    }

    public void clearFields() {
        secondTester.getSelectionModel().clearSelection();
        List<Account> allAccounts = AccountRepository.getAllAccounts();
        secondTester.getItems().setAll(allAccounts);

        textLogin.clear();
        textPassword.clear();
        textLogin.requestFocus();
    }
}
