package ru.avem.ksptsurgut.controllers;

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
import ru.avem.ksptsurgut.db.TestItemRepository;
import ru.avem.ksptsurgut.db.model.TestItem;
import ru.avem.ksptsurgut.model.ExperimentValuesModel;
import ru.avem.ksptsurgut.utils.Toast;

import java.util.List;

import static ru.avem.ksptsurgut.Main.PRIMARY_STAGE;
import static ru.avem.ksptsurgut.Main.setTheme;

public class TestItemEditorController {

    private ExperimentValuesModel experimentsValuesModel;

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
    private TextField textTestItemIxx;

    @FXML
    private TextField textTestItemUkz;

    @FXML
    private TextField textTestItemXXTime;

    @FXML
    private TextField textTestItemUInsulton;

    @FXML
    private TextField textTestItemUMeger;


    @FXML
    private TableColumn<TestItem, String> columnTestItemType;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUBH;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUHH;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemP;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemIxx;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUkz;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemXXTime;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUinsulation;

    @FXML
    private TableColumn<TestItem, Double> columnTestItemUMeger;


    @FXML
    private void initialize() {
        setTheme(root);

        experimentsValuesModel = ExperimentValuesModel.getInstance();
        initData();

        columnTestItemType.setCellValueFactory(new PropertyValueFactory<>("type"));
        columnTestItemType.setCellFactory(TextFieldTableCell.forTableColumn());
        columnTestItemType.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setType(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );
        columnTestItemUBH.setCellValueFactory(new PropertyValueFactory<>("ubh"));
        columnTestItemUBH.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUBH.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUbh(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );
        columnTestItemUHH.setCellValueFactory(new PropertyValueFactory<>("uhh"));
        columnTestItemUHH.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUHH.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUhh(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );
        columnTestItemP.setCellValueFactory(new PropertyValueFactory<>("p"));
        columnTestItemP.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemP.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setP(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );
        columnTestItemIxx.setCellValueFactory(new PropertyValueFactory<>("ixx"));
        columnTestItemIxx.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemIxx.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setIxx(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );

        columnTestItemUkz.setCellValueFactory(new PropertyValueFactory<>("ukz"));
        columnTestItemUkz.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUkz.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUkz(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );

        columnTestItemXXTime.setCellValueFactory(new PropertyValueFactory<>("xxtime"));
        columnTestItemXXTime.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemXXTime.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setXxtime(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );

        columnTestItemUinsulation.setCellValueFactory(new PropertyValueFactory<>("uinsulation"));
        columnTestItemUinsulation.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUinsulation.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUinsulation(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
                }
        );

        columnTestItemUMeger.setCellValueFactory(new PropertyValueFactory<>("umeger"));
        columnTestItemUMeger.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        columnTestItemUMeger.setOnEditCommit(t -> {
                    TestItem editingTestItem = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    editingTestItem.setUmeger(t.getNewValue());
                    TestItemRepository.updateTestItem(editingTestItem);
                    experimentsValuesModel.setNeedRefresh(true);
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
                    Double.parseDouble(textTestItemIxx.getText()),
                    Double.parseDouble(textTestItemUkz.getText()),
                    Double.parseDouble(textTestItemXXTime.getText()),
                    Double.parseDouble(textTestItemUInsulton.getText()),
                    Double.parseDouble(textTestItemUMeger.getText()));
            TestItemRepository.insertTestItem(testItem);
            testItems.add(testItem);
            experimentsValuesModel.setNeedRefresh(true);
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
            experimentsValuesModel.setNeedRefresh(true);
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
        if (textTestItemUBH.getText() == null || textTestItemUBH.getText().length() == 0 || Integer.parseInt(textTestItemUBH.getText()) < 0) {
            errorMessage += "Неверное значение U BH. Проверьте, чтоб U BH было больше, чем U HH.\n";
        }
        if (textTestItemUHH.getText() == null || textTestItemUHH.getText().length() == 0 || Integer.parseInt(textTestItemUHH.getText()) > Integer.parseInt(textTestItemUBH.getText()) || Integer.parseInt(textTestItemUHH.getText()) < 0) {
            errorMessage += "Неверное значение U HH. Проверьте, чтоб U BH было больше, чем U HH.\n";
        }
        if (textTestItemP.getText() == null || textTestItemP.getText().length() == 0 || Integer.parseInt(textTestItemP.getText()) < 0) {
            errorMessage += "Неверное значение P\n";
        }
        if (textTestItemIxx.getText() == null || textTestItemIxx.getText().length() == 0 || Integer.parseInt(textTestItemIxx.getText()) < 0 || Integer.parseInt(textTestItemIxx.getText()) > 100) {
            errorMessage += "Неверное значение Ixx\n";
        }
        if (textTestItemUkz.getText() == null || textTestItemUkz.getText().length() == 0 || Integer.parseInt(textTestItemUkz.getText()) < 0 || Integer.parseInt(textTestItemUkz.getText()) > 100) {
            errorMessage += "Неверное значение Ukz\n";
        }
        if (textTestItemXXTime.getText() == null || textTestItemXXTime.getText().length() == 0 || Integer.parseInt(textTestItemXXTime.getText()) < 0) {
            errorMessage += "Неверное значение xxTime\n";
        }
        if (textTestItemUInsulton.getText() == null || textTestItemUInsulton.getText().length() == 0 || Integer.parseInt(textTestItemUInsulton.getText()) < 0 || Integer.parseInt(textTestItemUInsulton.getText()) > 50000) {
            errorMessage += "Неверное значение U пробоя. Допустимый диапозон 0-50000\n";
        }
        if (textTestItemUMeger.getText() == null || textTestItemUMeger.getText().length() == 0 || Integer.parseInt(textTestItemUMeger.getText()) < 1000 || Integer.parseInt(textTestItemUMeger.getText()) > 2500) {
            errorMessage += "Неверное значение U Мегаомметр. Допустимый диапозон 1000-2500\n";
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

