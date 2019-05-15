package ru.avem.ksptamur.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.DoubleStringConverter;
import org.controlsfx.control.Notifications;
import ru.avem.ksptamur.db.TestItemRepository;
import ru.avem.ksptamur.db.model.TestItem;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.utils.Toast;

import java.net.URISyntaxException;
import java.util.List;

import static ru.avem.ksptamur.Main.PRIMARY_STAGE;
import static ru.avem.ksptamur.Main.setTheme;

public class TestItemEditorController {

    private MainModel mainModel;

    private ObservableList<TestItem> testItems;

    @FXML
    private AnchorPane root;

    @FXML
    private TableView<TestItem> tableTestItems;

    @FXML
    private TextField textTestItemType;

    @FXML
    private TextField textTestItemUBH;

    @FXML
    private TextField textTestItemUHH;

    @FXML
    private TextField textTestItemP;

    @FXML
    private TextField textTestItemPhase;

    @FXML
    private TextField textTestItemIxx;

    @FXML
    private TextField textTestItemUkz;

    @FXML
    private TextField textTestItemXXTime;

    @FXML
    private TextField textTestItemUInsulton;


    @FXML
    private TableColumn<TestItem, String> columnTestItemType;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUBH;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUHH;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemP;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemPhase;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemIxx;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUkz;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemXXTime;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUinsulation;


    @FXML
    private void initialize() {
        setTheme(root);

        mainModel = MainModel.getInstance();
        initData();

        columnTestItemType.setCellValueFactory(new PropertyValueFactory<>("type"));
        columnTestItemType.setCellFactory(TextFieldTableCell.forTableColumn());
        columnTestItemType.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setType(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );
        columnTestItemUBH.setCellValueFactory(new PropertyValueFactory<>("ubh"));
        columnTestItemUBH.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUBH.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUbh(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );
        columnTestItemUHH.setCellValueFactory(new PropertyValueFactory<>("uhh"));
        columnTestItemUHH.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUHH.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUhh(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );
        columnTestItemP.setCellValueFactory(new PropertyValueFactory<>("p"));
        columnTestItemP.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemP.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setP(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );
        columnTestItemPhase.setCellValueFactory(new PropertyValueFactory<>("phase"));
        columnTestItemPhase.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemPhase.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setPhase(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );
        columnTestItemIxx.setCellValueFactory(new PropertyValueFactory<>("ixx"));
        columnTestItemIxx.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemIxx.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setIxx(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );

        columnTestItemUkz.setCellValueFactory(new PropertyValueFactory<>("ukz"));
        columnTestItemUkz.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUkz.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUkz(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );

        columnTestItemXXTime.setCellValueFactory(new PropertyValueFactory<>("xxtime"));
        columnTestItemXXTime.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemXXTime.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setXxtime(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );

        columnTestItemUinsulation.setCellValueFactory(new PropertyValueFactory<>("uinsulation"));
        columnTestItemUinsulation.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUinsulation.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUinsulation(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    mainModel.setNeedRefresh(true);
                }
        );

        // заполняем таблицу данными
        tableTestItems.setItems(testItems);
    }

    private void initData() {
        List<TestItem> allTestItems = TestItemRepository.getAllTestItems();
        testItems = FXCollections.observableArrayList(allTestItems);
    }

    @FXML
    private void handleTestItemAdd() {
        if (isInputValid()) {
            TestItem testItem = new TestItem(textTestItemType.getText(),
                    Double.parseDouble(textTestItemUBH.getText()),
                    Double.parseDouble(textTestItemUHH.getText()),
                    Double.parseDouble(textTestItemP.getText()),
                    Double.parseDouble(textTestItemPhase.getText()),
                    Double.parseDouble(textTestItemIxx.getText()),
                    Double.parseDouble(textTestItemUkz.getText()),
                    Double.parseDouble(textTestItemXXTime.getText()),
                    Double.parseDouble(textTestItemUInsulton.getText()),
                    1.0);
            TestItemRepository.insertTestItem(testItem);
            testItems.add(testItem);
            mainModel.setNeedRefresh(true);
        } else {
            Toast.makeText("Проверьте правильность ввода");
        }
    }

    @FXML
    private void handleTestItemDelete() {
        int selectedIndex = tableTestItems.getSelectionModel().getSelectedIndex();
        TestItem testItem = tableTestItems.getSelectionModel().getSelectedItem();
        if (selectedIndex >= 0) {
            tableTestItems.getItems().remove(selectedIndex);
            TestItemRepository.deleteTestItem(testItem);
            mainModel.setNeedRefresh(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(PRIMARY_STAGE);
            alert.setTitle("Не выбрано");
            alert.setHeaderText("Объект не выбран");
            alert.setContentText("Пожалуйста выберите объект в таблице");

            alert.showAndWait();
        }

    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (textTestItemType.getText() == null || textTestItemType.getText().length() == 0) {
            errorMessage += "Неверный тип\n";
        }
        if (textTestItemUBH.getText() == null || textTestItemUBH.getText().length() == 0) {
            errorMessage += "Неверное значение U BH\n";
        }
        if (textTestItemUHH.getText() == null || textTestItemUHH.getText().length() == 0) {
            errorMessage += "Неверное значение U HH\n";
        }
        if (textTestItemP.getText() == null || textTestItemP.getText().length() == 0) {
            errorMessage += "Неверное значение P\n";
        }
        if (textTestItemPhase.getText() == null || textTestItemPhase.getText().length() == 0 || textTestItemPhase.getText().length() == 0 || textTestItemPhase.getText().length() == 0) {
            errorMessage += "Неверное значение P\n";
        }
        if (textTestItemIxx.getText() == null || textTestItemIxx.getText().length() == 0) {
            errorMessage += "Неверное значение Ixx\n";
        }
        if (textTestItemUkz.getText() == null || textTestItemUkz.getText().length() == 0) {
            errorMessage += "Неверное значение Ukz\n";
        }
        if (textTestItemXXTime.getText() == null || textTestItemXXTime.getText().length() == 0) {
            errorMessage += "Неверное значение xxTime\n";
        }
        if (textTestItemUInsulton.getText() == null || textTestItemUInsulton.getText().length() == 0) {
            errorMessage += "Неверное значение Uinsulation\n";
        }
        if (errorMessage.length() == 0) {
            return true;
        } else {
            Notifications.create()
                    .title("Ошибка")
                    .text(errorMessage)
                    .showError();
            return false;
        }
    }
}

