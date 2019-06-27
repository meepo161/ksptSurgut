package ru.avem.ksptamur.controllers;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.avem.ksptamur.Constants;
import ru.avem.ksptamur.Exitappable;
import ru.avem.ksptamur.Main;
import ru.avem.ksptamur.communication.CommunicationModel;
import ru.avem.ksptamur.db.DataBaseRepository;
import ru.avem.ksptamur.db.ProtocolRepository;
import ru.avem.ksptamur.db.TestItemRepository;
import ru.avem.ksptamur.db.model.Protocol;
import ru.avem.ksptamur.db.model.TestItem;
import ru.avem.ksptamur.model.ExperimentsHolder;
import ru.avem.ksptamur.model.MainModel;
import ru.avem.ksptamur.model.ResultModel;
import ru.avem.ksptamur.states.main.*;
import ru.avem.ksptamur.utils.Toast;
import ru.avem.ksptamur.utils.Utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static ru.avem.ksptamur.Constants.Info.*;
import static ru.avem.ksptamur.Main.*;

public class MainViewController implements Statable {


    @FXML
    private ImageView imgProtocolNew;
    @FXML
    private ImageView imgProtocolOpen;
    @FXML
    private ImageView imgProtocolOpenFromDB;
    @FXML
    private ImageView imgProtocolSaveAs;
    @FXML
    private ImageView imgProtocolExit;
    @FXML
    private ImageView imgTestItem;
    @FXML
    private ImageView imgSerialNumber;
    @FXML
    private ImageView imgDBTestItem;
    @FXML
    private ImageView imgDBProtocols;
    @FXML
    private ImageView imgDBProfiles;
    @FXML
    private ImageView imgDBImport;
    @FXML
    private ImageView imgDBExport;
    @FXML
    private ImageView imgInstrumentsDeviceStates;
    @FXML
    private ImageView imgInstrumentsCurrentProtection;
    @FXML
    private ImageView imgInstrumentsInfo;
    @FXML
    private ImageView imgInstrumentsTheme;


    @FXML
    private Button buttonCancel;

    @FXML
    private JFXTextField textFieldSerialNumber;
    @FXML
    private JFXComboBox<TestItem> comboBoxTestItem;

    @FXML
    private MenuItem menuBarProtocolSaveAs;
    @FXML
    private MenuItem menuBarDBTestItems;

    @FXML
    private Parent root;

    @FXML
    private CheckMenuItem checkMenuItemTheme;

    @FXML
    private JFXCheckBox rCheckBoxExp7BH;
    @FXML
    private JFXCheckBox rCheckBoxExp7HH;

    @FXML
    private JFXCheckBox rCheckBoxMegerBH;
    @FXML
    private JFXCheckBox rCheckBoxMegerHH;
    @FXML
    private JFXCheckBox rCheckBoxMegerBHHH;

    @FXML
    private JFXCheckBox rCheckBoxIKASBH;
    @FXML
    private JFXCheckBox rCheckBoxIKASHH;

    @FXML
    private JFXTabPane tabPane;
    @FXML
    private Tab tabSourceData;
    @FXML
    private Tab tabResults;

    @FXML
    private JFXCheckBox checkBoxSelectAllItems;
    @FXML
    private JFXCheckBox checkBoxExperiment0;
    @FXML
    private JFXCheckBox checkBoxExperiment1;
    @FXML
    private JFXCheckBox checkBoxExperiment2;
    @FXML
    private JFXCheckBox checkBoxExperiment3;
    @FXML
    private JFXCheckBox checkBoxExperiment4;
    @FXML
    private JFXCheckBox checkBoxExperiment5;
    @FXML
    private JFXCheckBox checkBoxExperiment6;
    @FXML
    private JFXCheckBox checkBoxExperiment7;

    @FXML
    private ComboBox<String> comboBoxResult;

    private MainModel mainModel;

    private CommunicationModel communicationModel;

    private Exitappable exitappable;

    private FileChooser protocolFileChooser;

    private FileChooser DBFileChooser;

    @FXML
    private TableView<ResultModel> tableViewResults;
    @FXML
    private TableColumn<ResultModel, String> columnTableDimension;
    @FXML
    private TableColumn<ResultModel, String> columnTableValue;
    private ObservableList<ResultModel> resultData = FXCollections.observableArrayList();

    private State idleState = new IdleState(this);
    private State waitState = new WaitState(this);
    private State resultState = new ResultState(this);
    private State currentState = idleState;

    @FXML
    private void initialize() {
        setTheme(root);
        mainModel = MainModel.getInstance();
        communicationModel = CommunicationModel.getInstance();
        refreshTestItems();

        tableViewResults.setItems(resultData);
        columnTableDimension.setCellValueFactory(cellData -> cellData.getValue().dimensionProperty());
        columnTableValue.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        initializeComboBoxResult();

        protocolFileChooser = new FileChooser();
        protocolFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        protocolFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("AVEM Protocol (*.axml)", "*.axml"));

        DBFileChooser = new FileChooser();
        DBFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        DBFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("AVEM Database (*.adb)", "*.adb"));
    }

    private void refreshTestItems() {
        List<TestItem> allTestItems = TestItemRepository.getAllTestItems();
        comboBoxTestItem.getItems().setAll(allTestItems);
    }

    private void initializeComboBoxResult() {
        comboBoxResult.getItems().setAll(ExperimentsHolder.getNamesOfExperiments());
        comboBoxResult.setOnAction(event -> {
            Protocol currentProtocol = mainModel.getCurrentProtocol();
            switch (comboBoxResult.getSelectionModel().getSelectedItem()) {
                case Constants.Experiments.EXPERIMENT0_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("ВН и корпус", currentProtocol.getE0WindingBH()));
                    resultData.add(new ResultModel("R15", currentProtocol.getE0R15BH()));
                    resultData.add(new ResultModel("R60", currentProtocol.getE0R60BH()));
                    resultData.add(new ResultModel("Коэф", currentProtocol.getE0CoefBH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE0TBH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE0ResultBH()));

                    resultData.add(new ResultModel("ВН и корпус", currentProtocol.getE0WindingHH()));
                    resultData.add(new ResultModel("R15", currentProtocol.getE0R15HH()));
                    resultData.add(new ResultModel("R60", currentProtocol.getE0R60HH()));
                    resultData.add(new ResultModel("Коэф", currentProtocol.getE0CoefHH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE0THH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE0ResultHH()));

                    resultData.add(new ResultModel("ВН и корпус", currentProtocol.getE0WindingBHHH()));
                    resultData.add(new ResultModel("R15", currentProtocol.getE0R15BHHH()));
                    resultData.add(new ResultModel("R60", currentProtocol.getE0R60BHHH()));
                    resultData.add(new ResultModel("Коэф", currentProtocol.getE0CoefBHHH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE0TBHHH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE0ResultBHHH()));

                    break;
                case Constants.Experiments.EXPERIMENT1_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Обмотка", currentProtocol.getE1WindingBH()));
                    resultData.add(new ResultModel("AB, Ом", currentProtocol.getE1ABBH()));
                    resultData.add(new ResultModel("BC, Ом", currentProtocol.getE1BCBH()));
                    resultData.add(new ResultModel("CA, Ом", currentProtocol.getE1CABH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE1TBH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE1ResultBH()));

                    resultData.add(new ResultModel("Обмотка", currentProtocol.getE1WindingHH()));
                    resultData.add(new ResultModel("AB, Ом", currentProtocol.getE1ABHH()));
                    resultData.add(new ResultModel("BC, Ом", currentProtocol.getE1BCHH()));
                    resultData.add(new ResultModel("CA, Ом", currentProtocol.getE1CAHH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE1THH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE1ResultHH()));
                    break;
                case Constants.Experiments.EXPERIMENT2_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Uвых AB, В", currentProtocol.getE2UOutputAB()));
                    resultData.add(new ResultModel("Uвых BC, В", currentProtocol.getE2UOutputBC()));
                    resultData.add(new ResultModel("Uвых CA, В", currentProtocol.getE2UOutputCA()));
                    resultData.add(new ResultModel("Uвых среднее, В", currentProtocol.getE2UOutputAvr()));
                    resultData.add(new ResultModel("Uвх AB, В", currentProtocol.getE2UInputAB()));
                    resultData.add(new ResultModel("Uвх BC, В", currentProtocol.getE2UInputBC()));
                    resultData.add(new ResultModel("Uвх CA, В", currentProtocol.getE2UInputCA()));
                    resultData.add(new ResultModel("Uвх среднее, В", currentProtocol.getE2UInputAvr()));
                    resultData.add(new ResultModel("Uвых/Uвх, В", currentProtocol.getE2DiffU()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE2F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE2Result()));
                    break;
                case Constants.Experiments.EXPERIMENT3_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Группа соединений BH", currentProtocol.getE4WindingBH()));
                    resultData.add(new ResultModel("Группа соединений HH", currentProtocol.getE4WindingHH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE4Result()));
                    break;
                case Constants.Experiments.EXPERIMENT4_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("U КЗ, В", currentProtocol.getE5UKZV()));
                    resultData.add(new ResultModel("U КЗ, %", currentProtocol.getE5UKZPercent()));
                    resultData.add(new ResultModel("I A, A", currentProtocol.getE5IA()));
                    resultData.add(new ResultModel("I B, A", currentProtocol.getE5IB()));
                    resultData.add(new ResultModel("I C, A", currentProtocol.getE5IC()));
                    resultData.add(new ResultModel("Pп, Вт", currentProtocol.getE5Pp()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE5F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE5Result()));
                    break;
                case Constants.Experiments.EXPERIMENT5_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("UВН, В", currentProtocol.getE6UBH()));
                    resultData.add(new ResultModel("I A, A", currentProtocol.getE6IA()));
                    resultData.add(new ResultModel("I B, A", currentProtocol.getE6IB()));
                    resultData.add(new ResultModel("I C, A", currentProtocol.getE6IC()));
                    resultData.add(new ResultModel("Pп, Вт", currentProtocol.getE6Pp()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE6F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE6Result()));
                    break;
                case Constants.Experiments.EXPERIMENT6_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("UВх, В", currentProtocol.getE7UInput()));
                    resultData.add(new ResultModel("IВН, A", currentProtocol.getE7IBH()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE7F()));
                    resultData.add(new ResultModel("t, сек", currentProtocol.getE7Time()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE7Result()));
                    break;
                case Constants.Experiments.EXPERIMENT7_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Тип", currentProtocol.getE8TypeBHandCorps()));
                    resultData.add(new ResultModel("I, A", currentProtocol.getE8IBHandCorps()));
                    resultData.add(new ResultModel("U, В", currentProtocol.getE8UBHandCorps()));
                    resultData.add(new ResultModel("t,сек", currentProtocol.getE8TimeBHandCorps()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE8ResultBHandCorps()));
                    resultData.add(new ResultModel("Тип", currentProtocol.getE8TypeHHandCorps()));
                    resultData.add(new ResultModel("I, A", currentProtocol.getE8IHHandCorps()));
                    resultData.add(new ResultModel("U, В", currentProtocol.getE8UHHandCorps()));
                    resultData.add(new ResultModel("t,сек", currentProtocol.getE8TimeHHandCorps()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE8ResultHHandCorps()));
                    break;
            }
        });
    }


    private void toInitIdleState() {
        menuBarProtocolSaveAs.setDisable(true);
        tabResults.setDisable(true);
    }

    @Override
    public void toIdleState() {
        toInitIdleState();

        textFieldSerialNumber.clear();
        textFieldSerialNumber.setDisable(false);
        comboBoxTestItem.getSelectionModel().clearSelection();
        comboBoxTestItem.setDisable(false);
        buttonCancel.setText("Очистить");
        setLeftStatus("");
        setRightStatus("");

        tabSourceData.setDisable(false);
        tabPane.getSelectionModel().select(tabSourceData);
        mainModel.setCurrentProtocol(null);
        menuBarDBTestItems.setDisable(false);
        currentState = idleState;
    }

    @Override
    public void toWaitState() {
        textFieldSerialNumber.setDisable(true);
        comboBoxTestItem.setDisable(true);
        buttonCancel.setText("Новый");
        setLeftStatus("Заводской номер: " + textFieldSerialNumber.getText());
        setRightStatus("Объект испытания: " + comboBoxTestItem.getSelectionModel().getSelectedItem());
        menuBarProtocolSaveAs.setDisable(false);

        tabSourceData.setDisable(false);
        tabPane.getSelectionModel().select(tabSourceData);
        menuBarDBTestItems.setDisable(true);
        currentState = waitState;
    }

    @Override
    public void toResultState() {
        tabSourceData.setDisable(true);
        tabResults.setDisable(false);
        tabPane.getSelectionModel().select(tabResults);
        currentState = resultState;
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setMillis(System.currentTimeMillis());
        ProtocolRepository.updateProtocol(currentProtocol);
        Toast.makeText("Результаты проведенных испытаний сохранены").show(Toast.ToastType.INFORMATION);
    }

    @FXML
    private void handleContinueProtocol() {
        currentState.toWaitState();
    }

    @FXML
    private void handleCreateNewProtocol() {
        currentState.toIdleState();
    }

    @FXML
    private void handleMenuBarProtocolOpen() {
        currentState.toIdleState();
        if (currentState instanceof IdleState) {
            protocolFileChooser.setTitle("Выберите файл протокола");
            File file = protocolFileChooser.showOpenDialog(PRIMARY_STAGE);
            if (file != null) {
                openProtocolFromFile(file);
            }
        }
    }

    private void openProtocolFromFile(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(Protocol.class);
            Unmarshaller um = context.createUnmarshaller();
            Protocol protocol = (Protocol) um.unmarshal(file);
            textFieldSerialNumber.setText(protocol.getSerialNumber());
            comboBoxTestItem.getSelectionModel().select(protocol.getObject());
            mainModel.setCurrentProtocol(protocol);
            currentState.toWaitState();
            Toast.makeText(String.format("Протокол %s успешно загружен", file.getName())).show(Toast.ToastType.INFORMATION);
        } catch (Exception e) {
            Toast.makeText("Ошибка загрузки протокола").show(Toast.ToastType.ERROR);
        }
    }

    @FXML
    private void handleMenuBarProtocolOpenFromDB() throws IOException {
        currentState.toIdleState();
        if (currentState instanceof IdleState) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("layouts/protocolSelector.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Выберите протокол из списка");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(PRIMARY_STAGE);
            dialogStage.setResizable(false);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ProtocolSelectorController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (!controller.isCanceled()) {
                mainModel.applyIntermediateProtocol();
                textFieldSerialNumber.setText(mainModel.getCurrentProtocol().getSerialNumber());
                comboBoxTestItem.getSelectionModel().select(mainModel.getCurrentProtocol().getObject());
                currentState.toWaitState();
            }
        }
    }

    @FXML
    private void handleMenuBarProtocolSaveAs() {
        protocolFileChooser.setTitle("Сохраните файл протокола");
        File file = protocolFileChooser.showSaveDialog(PRIMARY_STAGE);
        if (file != null) {
            if (!file.getPath().endsWith(".axml")) {
                file = new File(file.getPath() + ".axml");
            }
            saveProtocolToFile(file);
        }
    }

    private void saveProtocolToFile(File file) {
        try {
            JAXBContext context = JAXBContext.newInstance(Protocol.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(mainModel.getCurrentProtocol(), file);
            Toast.makeText(String.format("Протокол %s успешно сохранён", file.getName())).show(Toast.ToastType.INFORMATION);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(String.format("Ошибка при сохранении протокола %s", file.getName())).show(Toast.ToastType.ERROR);
        }
    }

    @FXML
    public void handleExit() {
        currentState.toIdleState();
        if (currentState instanceof IdleState) {
            exitappable.exitApp();
        }
    }

    @FXML
    private void handleTestItems() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("layouts/testItemEditor.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Объекты испытания");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(PRIMARY_STAGE);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.setOnCloseRequest(event -> {
        });
        dialogStage.showAndWait();
        toIdleState();
    }


    @FXML
    private void handleProtocols() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("layouts/protocolEditor.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Протоколы");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(PRIMARY_STAGE);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        dialogStage.showAndWait();
    }

    @FXML
    private void handleMenuProfiles() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("layouts/registerWindow.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Редактировать профиль");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(PRIMARY_STAGE);
        dialogStage.setResizable(false);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    @FXML
    private void handleImportDB() {
        DBFileChooser.setTitle("Выберите файл базы данных для импорта");
        File file = DBFileChooser.showOpenDialog(PRIMARY_STAGE);
        if (file != null) {
            importDBFromFile(file);
        }
    }

    @FXML
    private void importDBFromFile(File file) {
        try {
            Utils.copyFileFromFile(file, new File(DataBaseRepository.DATABASE_NAME));
            Toast.makeText(String.format("База успешно импортирована из файла %s", file.getAbsolutePath())).show(Toast.ToastType.INFORMATION);
        } catch (IOException e) {
            Toast.makeText("Ошибка при импорте базы данных").show(Toast.ToastType.ERROR);
        }
    }

    @FXML
    private void handleExportDB() {
        DBFileChooser.setTitle("Сохраните базу данных в файл");
        File file = DBFileChooser.showSaveDialog(PRIMARY_STAGE);
        if (file != null) {
            if (!file.getPath().endsWith(".adb")) {
                file = new File(file.getPath() + ".adb");
            }
            exportDBToFile(file);
        }
    }

    private void exportDBToFile(File file) {
        try {
            Utils.copyFileFromFile(new File(DataBaseRepository.DATABASE_NAME), file);
            Toast.makeText(String.format("База успешно экспортирована в файл %s", file.getAbsolutePath())).show(Toast.ToastType.INFORMATION);
        } catch (IOException e) {
            Toast.makeText("Ошибка при экспорте базы данных").show(Toast.ToastType.ERROR);
        }
    }

    @FXML
    private void handleDeviceState() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("layouts/deviceStateWindow.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Состояние устройств");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(PRIMARY_STAGE);
        Scene scene = new Scene(page);
        dialogStage.setResizable(false);
        dialogStage.setScene(scene);

        dialogStage.setOnCloseRequest(event -> {
            CommunicationModel communicationModel = CommunicationModel.getInstance();
            communicationModel.finalizeAllDevices();
            communicationModel.deleteObservers();
            communicationModel.setDeviceStateOn(false);
        });
        dialogStage.showAndWait();
    }

    @FXML
    private void handleCurrentProtection() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("layouts/currentProtectionWindow.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Состояние защит");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);
        dialogStage.setScene(new Scene(page));

        dialogStage.setOnCloseRequest(event -> {
            communicationModel.finalizeAllDevices();
            communicationModel.deleteObservers();
            communicationModel.setDeviceStateOn(false);
        });
        dialogStage.showAndWait();
    }

    public void handleDebug() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("layouts/debugWindow.fxml"));
        Parent page = loader.load();
        DebugWindowController controller = loader.getController();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Отладка");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(PRIMARY_STAGE);
        Scene scene = new Scene(page);
        dialogStage.setResizable(false);
        dialogStage.setScene(scene);

        dialogStage.setOnCloseRequest(event -> {
            CommunicationModel communicationModel = CommunicationModel.getInstance();
            communicationModel.finalizeAllDevices();
            communicationModel.deleteObservers();
            communicationModel.setDeviceStateOn(false);
        });
        dialogStage.showAndWait();
    }

    @FXML
    private void handleSelectTestItem() {
        if (mainModel.isNeedRefresh()) {
            refreshTestItems();
            mainModel.setNeedRefresh(false);
        }
    }

    @FXML
    private void handleButtonProtocolCancel() {
        currentState.toIdleState();
    }

    @FXML
    public void handleMenuBarProtocolNew() {
        currentState.toIdleState();
    }

    @FXML
    private void handleSelectAllTests() {
        if (checkBoxSelectAllItems.isSelected()) {
            checkBoxExperiment0.setIndeterminate(false);
            checkBoxExperiment0.setSelected(true);
            checkBoxExperiment1.setSelected(true);
            checkBoxExperiment2.setSelected(true);
            checkBoxExperiment3.setSelected(true);
            checkBoxExperiment4.setSelected(true);
            checkBoxExperiment5.setSelected(true);
            checkBoxExperiment6.setSelected(true);
            checkBoxExperiment7.setSelected(true);
            rCheckBoxExp7BH.setSelected(true);
            rCheckBoxExp7HH.setSelected(true);
            rCheckBoxMegerBH.setSelected(true);
            rCheckBoxMegerHH.setSelected(true);
            rCheckBoxMegerBHHH.setSelected(true);
            rCheckBoxIKASBH.setSelected(true);
            rCheckBoxIKASHH.setSelected(true);
        } else {
            checkBoxExperiment0.setIndeterminate(false);
            checkBoxExperiment0.setSelected(false);
            checkBoxExperiment1.setIndeterminate(false);
            checkBoxExperiment1.setSelected(false);
            checkBoxExperiment2.setSelected(false);
            checkBoxExperiment3.setSelected(false);
            checkBoxExperiment4.setSelected(false);
            checkBoxExperiment5.setSelected(false);
            checkBoxExperiment6.setSelected(false);
            checkBoxExperiment7.setIndeterminate(false);
            checkBoxExperiment7.setSelected(false);
            rCheckBoxExp7BH.setSelected(false);
            rCheckBoxExp7HH.setSelected(false);
            rCheckBoxMegerBH.setSelected(false);
            rCheckBoxMegerHH.setSelected(false);
            rCheckBoxMegerBHHH.setSelected(false);
            rCheckBoxIKASBH.setSelected(false);
            rCheckBoxIKASHH.setSelected(false);
        }
    }


    @FXML
    private void handleCheckBox0(ActionEvent e) {
        CheckBox checkBox0 = (CheckBox) e.getSource();
        if (checkBox0.isSelected()) {
            checkBoxSelectAllItems.setSelected(isAllSelected());
            rCheckBoxMegerBH.setSelected(true);
            rCheckBoxMegerHH.setSelected(true);
            rCheckBoxMegerBHHH.setSelected(true);
        } else {
            rCheckBoxMegerBH.setSelected(false);
            rCheckBoxMegerHH.setSelected(false);
            rCheckBoxMegerBHHH.setSelected(false);
        }
    }

    @FXML
    private void handleRCheckBoxExperiment0BH() {
        setStateCheckBox0();
    }

    @FXML
    private void handleRCheckBoxExperiment0HH() {
        setStateCheckBox0();
    }

    @FXML
    private void handleRCheckBoxExperiment0BHHH() {
        setStateCheckBox0();
    }

    private void setStateCheckBox0() {
        checkBoxExperiment0.setIndeterminate(true);
        if (!rCheckBoxMegerBH.isSelected() && !rCheckBoxMegerHH.isSelected() && !rCheckBoxMegerBHHH.isSelected()) {
            checkBoxExperiment0.setSelected(false);
            checkBoxExperiment0.setIndeterminate(false);
        } else if (rCheckBoxMegerBH.isSelected() && rCheckBoxMegerHH.isSelected() && rCheckBoxMegerBHHH.isSelected()) {
            checkBoxExperiment0.setIndeterminate(false);
            checkBoxExperiment0.setSelected(true);
        }
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox1(ActionEvent e) {
        CheckBox checkBox1 = (CheckBox) e.getSource();
        if (checkBox1.isSelected()) {
            checkBoxSelectAllItems.setSelected(isAllSelected());
            rCheckBoxIKASBH.setSelected(true);
            rCheckBoxIKASHH.setSelected(true);
        } else {
            rCheckBoxIKASBH.setSelected(false);
            rCheckBoxIKASHH.setSelected(false);
        }
    }

    @FXML
    private void handleRCheckBoxIkasBH() {
        setStateCheckBox1();
    }

    @FXML
    private void handleRCheckBoxIkasHH() {
        setStateCheckBox1();
    }

    private void setStateCheckBox1() {
        checkBoxExperiment1.setIndeterminate(true);
        if (!rCheckBoxIKASBH.isSelected() && !rCheckBoxIKASHH.isSelected()) {
            checkBoxExperiment1.setSelected(false);
            checkBoxExperiment1.setIndeterminate(false);
        } else if (rCheckBoxIKASBH.isSelected() && rCheckBoxIKASHH.isSelected()) {
            checkBoxExperiment1.setIndeterminate(false);
            checkBoxExperiment1.setSelected(true);
        }
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }


    @FXML
    private void handleRadioExperiment1BH() {
        checkBoxExperiment1.setIndeterminate(true);
        mainModel.setExperiment1Choice(MainModel.EXPERIMENT1_BH);
    }

    @FXML
    private void handleRadioExperiment1HH() {
        checkBoxExperiment1.setIndeterminate(true);
        mainModel.setExperiment1Choice(MainModel.EXPERIMENT1_HH);
    }


    @FXML
    private void handleCheckBox2() {
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox3() {
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox4() {
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox5() {
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox6() {
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox7(ActionEvent e) {
        CheckBox checkBox7 = (CheckBox) e.getSource();
        if (checkBox7.isSelected()) {
            checkBoxSelectAllItems.setSelected(isAllSelected());
            rCheckBoxExp7BH.setSelected(true);
            rCheckBoxExp7HH.setSelected(true);
        } else {
            rCheckBoxExp7BH.setSelected(false);
            rCheckBoxExp7HH.setSelected(false);
        }
    }

    @FXML
    private void handleRCheckBox7BH() {
        setStateCheckBox7();
    }

    @FXML
    private void handleRCheckBox7HH() {
        setStateCheckBox7();
    }

    private void setStateCheckBox7() {
        checkBoxExperiment7.setIndeterminate(true);
        if (!rCheckBoxExp7BH.isSelected() && !rCheckBoxExp7HH.isSelected()) {
            checkBoxExperiment7.setSelected(false);
            checkBoxExperiment7.setIndeterminate(false);
        } else if (rCheckBoxExp7BH.isSelected() && rCheckBoxExp7HH.isSelected()) {
            checkBoxExperiment7.setIndeterminate(false);
            checkBoxExperiment7.setSelected(true);
        }
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleRadioExperiment7BH() {
        mainModel.setExperiment7Choice(MainModel.EXPERIMENT7_BH);
        checkBoxExperiment7.setIndeterminate(true);
    }

    @FXML
    private void handleRadioExperiment7HH() {
        mainModel.setExperiment7Choice(MainModel.EXPERIMENT7_BH);
        checkBoxExperiment7.setIndeterminate(true);
    }

    private boolean isAllSelected() {
        return checkBoxExperiment0.isSelected() &&
                !checkBoxExperiment0.isIndeterminate() &&
                checkBoxExperiment1.isSelected() &&
                checkBoxExperiment2.isSelected() &&
                checkBoxExperiment3.isSelected() &&
                checkBoxExperiment4.isSelected() &&
                checkBoxExperiment5.isSelected() &&
                checkBoxExperiment6.isSelected() &&
                checkBoxExperiment7.isSelected();

    }

    @FXML
    private void handleStartExperiments() {
        if (!textFieldSerialNumber.getText().isEmpty() && !comboBoxTestItem.getSelectionModel().isEmpty()) {
            mainModel.createNewProtocol(textFieldSerialNumber.getText(), comboBoxTestItem.getSelectionModel().getSelectedItem());
            currentState.toWaitState();
        } else {
            Toast.makeText("Введите заводской номер и выберите объект испытания").show(Toast.ToastType.INFORMATION);
        }
        if (!rCheckBoxIKASBH.isSelected() &&
                !rCheckBoxIKASHH.isSelected() &&
                !rCheckBoxMegerBH.isSelected() &&
                !rCheckBoxMegerHH.isSelected() &&
                !rCheckBoxMegerBHHH.isSelected() &&
                !checkBoxExperiment0.isSelected() &&
                !checkBoxExperiment1.isSelected() &&
                !checkBoxExperiment2.isSelected() &&
                !checkBoxExperiment3.isSelected() &&
                !checkBoxExperiment4.isSelected() &&
                !checkBoxExperiment5.isSelected() &&
                !checkBoxExperiment6.isSelected() &&
                !checkBoxExperiment7.isSelected() &&
                !rCheckBoxExp7BH.isSelected() &&
                !rCheckBoxExp7HH.isSelected()) {
            Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING);
        } else {
            boolean isCanceled = false;
            if ((checkBoxExperiment0.isSelected() || checkBoxExperiment0.isIndeterminate()) && !isCanceled) {

                int mask = 0;
                mask |= rCheckBoxMegerBH.isSelected() ? 0b1 : 0;
                mask |= rCheckBoxMegerHH.isSelected() ? 0b10 : 0;
                mask |= rCheckBoxMegerBHHH.isSelected() ? 0b100 : 0;
                mainModel.setExperiment0Choice(mask);

                isCanceled = start0Experiment();
            }
            if ((checkBoxExperiment1.isSelected() || checkBoxExperiment1.isIndeterminate()) && !isCanceled) {
                int experiment1ChoiceMask = 0;
                experiment1ChoiceMask |= rCheckBoxIKASBH.isSelected() ? 0b1 : 0;
                experiment1ChoiceMask |= rCheckBoxIKASHH.isSelected() ? 0b10 : 0;
                mainModel.setExperiment1Choice(experiment1ChoiceMask);

                isCanceled = start1Experiment();
            }
            if (checkBoxExperiment2.isSelected() && !isCanceled) {
                isCanceled = start2Experiment();
            }
            if (checkBoxExperiment3.isSelected() && !isCanceled) {
                isCanceled = start3Experiment();
            }
            if (checkBoxExperiment4.isSelected() && !isCanceled) {
                isCanceled = start4Experiment();
            }
            if (checkBoxExperiment5.isSelected() && !isCanceled) {
                isCanceled = start5Experiment();
            }
            if (checkBoxExperiment6.isSelected() && !isCanceled) {
                isCanceled = start6Experiment();
            }
            if ((checkBoxExperiment7.isSelected() || checkBoxExperiment7.isIndeterminate()) && !isCanceled) {
                isCanceled = start7Experiment();
            }
            if (!isCanceled) {
                currentState.toResultState();
            }
        }
    }

    private boolean start0Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment0ViewPhase3.fxml"));
    }

    private boolean start1Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment1ViewPhase3.fxml"));
    }

    private boolean start2Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment2ViewPhase3.fxml"));
    }

    private boolean start3Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment3ViewPhase3.fxml"));
    }

    private boolean start4Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment4ViewPhase3.fxml"));
    }

    private boolean start5Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment5ViewPhase3.fxml"));
    }

    private boolean start6Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment6ViewPhase3.fxml"));
    }

    private boolean start7Experiment() {
        return startExperiment(String.format("layouts/phase3/experiment7ViewPhase3.fxml"));
    }

    private boolean startExperiment(String layout) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource(layout));
        ExperimentController controller = null;
        try {
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Опыт");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(PRIMARY_STAGE);
            Scene scene = new Scene(page, Constants.Display.WIDTH, Constants.Display.HEIGHT);
            dialogStage.setScene(scene);

            controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        communicationModel.finalizeAllDevices();
        communicationModel.deleteObservers();

        return controller != null && controller.isCanceled();
    }

    public void setMain(Exitappable exitappable) {
        this.exitappable = exitappable;
    }

    @FXML
    public void handleSaveCurrentProtocol() {
        ProtocolRepository.insertProtocol(mainModel.getCurrentProtocol());
        Toast.makeText("Результаты проведенных испытаний сохранены").show(Toast.ToastType.INFORMATION);
    }

    @FXML
    public void handleCheckMenuItemTheme() {
        try {
            if (!checkMenuItemTheme.isSelected()) { //сначала галочка ставится, потом срабатывает handle
                root.getStylesheets().set(0, Main.class.getResource("styles/main_css.css").toURI().toString());
                css = "white";
                ColorAdjust blackout = new ColorAdjust();
                blackout.setBrightness(0);

                imgSerialNumber.setEffect(blackout);
                imgTestItem.setEffect(blackout);

                imgProtocolNew.setEffect(blackout);
                imgProtocolOpen.setEffect(blackout);
                imgProtocolOpenFromDB.setEffect(blackout);
                imgProtocolSaveAs.setEffect(blackout);
                imgProtocolExit.setEffect(blackout);

                imgDBTestItem.setEffect(blackout);
                imgDBProtocols.setEffect(blackout);
                imgDBProfiles.setEffect(blackout);
                imgDBExport.setEffect(blackout);
                imgDBImport.setEffect(blackout);

                imgInstrumentsDeviceStates.setEffect(blackout);
                imgInstrumentsCurrentProtection.setEffect(blackout);
                imgInstrumentsInfo.setEffect(blackout);
                imgInstrumentsTheme.setEffect(blackout);


            } else {
                root.getStylesheets().set(0, Main.class.getResource("styles/main_css_black.css").toURI().toString());
                css = "black";
                ColorAdjust blackout = new ColorAdjust();
                blackout.setBrightness(1);

                imgSerialNumber.setEffect(blackout);
                imgTestItem.setEffect(blackout);

                imgProtocolNew.setEffect(blackout);
                imgProtocolOpen.setEffect(blackout);
                imgProtocolOpenFromDB.setEffect(blackout);
                imgProtocolSaveAs.setEffect(blackout);
                imgProtocolExit.setEffect(blackout);

                imgDBTestItem.setEffect(blackout);
                imgDBProtocols.setEffect(blackout);
                imgDBProfiles.setEffect(blackout);
                imgDBExport.setEffect(blackout);
                imgDBImport.setEffect(blackout);

                imgInstrumentsDeviceStates.setEffect(blackout);
                imgInstrumentsCurrentProtection.setEffect(blackout);
                imgInstrumentsInfo.setEffect(blackout);
                imgInstrumentsTheme.setEffect(blackout);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(TITLE);
        alert.setHeaderText(VERSION);
        alert.setContentText(DATE);

        alert.showAndWait();
    }
}