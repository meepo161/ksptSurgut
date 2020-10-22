package ru.avem.ksptsurgut.controllers;

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
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.avem.ksptsurgut.Constants;
import ru.avem.ksptsurgut.Exitappable;
import ru.avem.ksptsurgut.Main;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.db.DataBaseRepository;
import ru.avem.ksptsurgut.db.ProtocolRepository;
import ru.avem.ksptsurgut.db.TestItemRepository;
import ru.avem.ksptsurgut.db.model.Protocol;
import ru.avem.ksptsurgut.db.model.TestItem;
import ru.avem.ksptsurgut.model.ExperimentValuesModel;
import ru.avem.ksptsurgut.model.ExperimentsHolder;
import ru.avem.ksptsurgut.model.ResultModel;
import ru.avem.ksptsurgut.states.main.*;
import ru.avem.ksptsurgut.utils.Toast;
import ru.avem.ksptsurgut.utils.Utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static ru.avem.ksptsurgut.Constants.Info.*;
import static ru.avem.ksptsurgut.Main.*;

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
    private BorderPane root;
    @FXML
    private CheckMenuItem checkMenuItemTheme;
    @FXML
    private JFXCheckBox rCheckBoxMegerBH;
    @FXML
    private JFXCheckBox rCheckBoxMegerHH;
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
    private ComboBox<String> comboBoxResult;

    private ExperimentValuesModel experimentsValuesModel;

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
        experimentsValuesModel = ExperimentValuesModel.getInstance();
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

        toInitIdleState();
    }

    private void refreshTestItems() {
        List<TestItem> allTestItems = TestItemRepository.getAllTestItems();
        comboBoxTestItem.getItems().setAll(allTestItems);
    }

    private void initializeComboBoxResult() {
        comboBoxResult.getItems().setAll(ExperimentsHolder.getNamesOfExperiments());
        comboBoxResult.setOnAction(event -> {
            Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
            switch (comboBoxResult.getSelectionModel().getSelectedItem()) {
                case Constants.Experiments.EXPERIMENT1_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("ВН и корпус", currentProtocol.getE1WindingBH()));
                    resultData.add(new ResultModel("R15", currentProtocol.getE1R15BH()));
                    resultData.add(new ResultModel("R60", currentProtocol.getE1R60BH()));
                    resultData.add(new ResultModel("Коэф", currentProtocol.getE1CoefBH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE1ResultBH()));

                    resultData.add(new ResultModel("ВН и корпус", currentProtocol.getE1WindingHH()));
                    resultData.add(new ResultModel("R15", currentProtocol.getE1R15HH()));
                    resultData.add(new ResultModel("R60", currentProtocol.getE1R60HH()));
                    resultData.add(new ResultModel("Коэф", currentProtocol.getE1CoefHH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE1ResultHH()));

                    break;
                case Constants.Experiments.EXPERIMENT2_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Обмотка", currentProtocol.getE2WindingBH()));
                    resultData.add(new ResultModel("AB, Ом", currentProtocol.getE2ABBH()));
                    resultData.add(new ResultModel("BC, Ом", currentProtocol.getE2BCBH()));
                    resultData.add(new ResultModel("CA, Ом", currentProtocol.getE2CABH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE2TBH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE2ResultBH()));

                    resultData.add(new ResultModel("Обмотка", currentProtocol.getE2WindingHH()));
                    resultData.add(new ResultModel("AB, Ом", currentProtocol.getE2ABHH()));
                    resultData.add(new ResultModel("BC, Ом", currentProtocol.getE2BCHH()));
                    resultData.add(new ResultModel("CA, Ом", currentProtocol.getE2CAHH()));
                    resultData.add(new ResultModel("t, °С", currentProtocol.getE2THH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE2ResultHH()));
                    break;
                case Constants.Experiments.EXPERIMENT3_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Uвх AB, В", currentProtocol.getE3UInputAB()));
                    resultData.add(new ResultModel("Uвх BC, В", currentProtocol.getE3UInputBC()));
                    resultData.add(new ResultModel("Uвх CA, В", currentProtocol.getE3UInputCA()));
                    resultData.add(new ResultModel("Uвх среднее, В", currentProtocol.getE3UInputAvr()));
                    resultData.add(new ResultModel("Uвых AB, В", currentProtocol.getE3UOutputAB()));
                    resultData.add(new ResultModel("Uвых BC, В", currentProtocol.getE3UOutputBC()));
                    resultData.add(new ResultModel("Uвых CA, В", currentProtocol.getE3UOutputCA()));
                    resultData.add(new ResultModel("Uвых среднее, В", currentProtocol.getE3UOutputAvr()));
                    resultData.add(new ResultModel("Uвых/Uвх, В", currentProtocol.getE3DiffU()));
                    resultData.add(new ResultModel("Группа соединений BH", currentProtocol.getE3WindingBH()));
                    resultData.add(new ResultModel("Группа соединений HH", currentProtocol.getE3WindingHH()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE3F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE3Result()));
                    break;
                case Constants.Experiments.EXPERIMENT4_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("U A КЗ, В", currentProtocol.getE4UKZVA()));
                    resultData.add(new ResultModel("U B КЗ, В", currentProtocol.getE4UKZVB()));
                    resultData.add(new ResultModel("U C КЗ, В", currentProtocol.getE4UKZVC()));
                    resultData.add(new ResultModel("U КЗ, %", currentProtocol.getE4UKZPercent()));
                    resultData.add(new ResultModel("I A, A", currentProtocol.getE4IA()));
                    resultData.add(new ResultModel("I B, A", currentProtocol.getE4IB()));
                    resultData.add(new ResultModel("I C, A", currentProtocol.getE4IC()));
                    resultData.add(new ResultModel("Pп, Вт", currentProtocol.getE4Pp()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE4F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE4Result()));
                    break;
                case Constants.Experiments.EXPERIMENT5_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("UВН, В", currentProtocol.getE5UBH()));
                    resultData.add(new ResultModel("I A, A", currentProtocol.getE5IA()));
                    resultData.add(new ResultModel("I B, A", currentProtocol.getE5IB()));
                    resultData.add(new ResultModel("I C, A", currentProtocol.getE5IC()));
                    resultData.add(new ResultModel("Iхх A, %", currentProtocol.getE5IAPercent()));
                    resultData.add(new ResultModel("Iхх B, %", currentProtocol.getE5IBPercent()));
                    resultData.add(new ResultModel("Iхх C, %", currentProtocol.getE5ICPercent()));
                    resultData.add(new ResultModel("Pп, Вт", currentProtocol.getE5Pp()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE5F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE5Result()));
                    break;
                case Constants.Experiments.EXPERIMENT6_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("UВх, В", currentProtocol.getE6UInput()));
                    resultData.add(new ResultModel("IВН, A", currentProtocol.getE6IBH()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE6F()));
                    resultData.add(new ResultModel("t, сек", currentProtocol.getE6Time()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE6Result()));
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
        experimentsValuesModel.setCurrentProtocol(null);
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
        Protocol currentProtocol = experimentsValuesModel.getCurrentProtocol();
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
            experimentsValuesModel.setCurrentProtocol(protocol);
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
                experimentsValuesModel.applyIntermediateProtocol();
                textFieldSerialNumber.setText(experimentsValuesModel.getCurrentProtocol().getSerialNumber());
                comboBoxTestItem.getSelectionModel().select(experimentsValuesModel.getCurrentProtocol().getObject());
                currentState.toWaitState();
            }
        }
        textFieldSerialNumber.setText(experimentsValuesModel.getCurrentProtocol().getSerialNumber());
        comboBoxTestItem.getSelectionModel().select(experimentsValuesModel.getCurrentProtocol().getObject());
        initializeComboBoxResult();
        currentState.toResultState();
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
            m.marshal(experimentsValuesModel.getCurrentProtocol(), file);
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
        Dialog dialog = new TextInputDialog("");
        dialog.setTitle("Авторизация");
        dialog.setHeaderText("Вход закрыт");
        dialog.setContentText("Введите пароль: ");
        dialog.showAndWait();
        if (dialog.getResult().equals("4444")) {
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
    }

    @FXML
    private void handleSelectTestItem() {
        if (experimentsValuesModel.isNeedRefresh()) {
            refreshTestItems();
            experimentsValuesModel.setNeedRefresh(false);
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
            checkBoxExperiment1.setIndeterminate(false);
            checkBoxExperiment1.setSelected(true);
            checkBoxExperiment2.setSelected(true);
            checkBoxExperiment3.setSelected(true);
            checkBoxExperiment4.setSelected(true);
            checkBoxExperiment5.setSelected(true);
            checkBoxExperiment6.setSelected(true);
            rCheckBoxMegerBH.setSelected(true);
            rCheckBoxMegerHH.setSelected(true);
            rCheckBoxIKASBH.setSelected(true);
            rCheckBoxIKASHH.setSelected(true);
        } else {
            checkBoxExperiment1.setIndeterminate(false);
            checkBoxExperiment1.setSelected(false);
            checkBoxExperiment2.setIndeterminate(false);
            checkBoxExperiment2.setSelected(false);
            checkBoxExperiment3.setSelected(false);
            checkBoxExperiment4.setSelected(false);
            checkBoxExperiment5.setSelected(false);
            checkBoxExperiment6.setSelected(false);
            rCheckBoxMegerBH.setSelected(false);
            rCheckBoxMegerHH.setSelected(false);
            rCheckBoxIKASBH.setSelected(false);
            rCheckBoxIKASHH.setSelected(false);
        }
    }


    @FXML
    private void handleCheckBox1(ActionEvent e) {
        CheckBox checkBox0 = (CheckBox) e.getSource();
        if (checkBox0.isSelected()) {
            checkBoxSelectAllItems.setSelected(isAllSelected());
            rCheckBoxMegerBH.setSelected(true);
            rCheckBoxMegerHH.setSelected(true);
        } else {
            rCheckBoxMegerBH.setSelected(false);
            rCheckBoxMegerHH.setSelected(false);
        }
    }

    @FXML
    private void handleRCheckBoxExperiment1BH() {
        setStateCheckBox1();
    }

    @FXML
    private void handleRCheckBoxExperiment1HH() {
        setStateCheckBox1();
    }

    @FXML
    private void handleRCheckBoxExperiment1BHHH() {
        setStateCheckBox1();
    }

    private void setStateCheckBox1() {
        checkBoxExperiment1.setIndeterminate(true);
        if (!rCheckBoxMegerBH.isSelected() && !rCheckBoxMegerHH.isSelected()) {
            checkBoxExperiment1.setSelected(false);
            checkBoxExperiment1.setIndeterminate(false);
        } else if (rCheckBoxMegerBH.isSelected() && rCheckBoxMegerHH.isSelected()) {
            checkBoxExperiment1.setIndeterminate(false);
            checkBoxExperiment1.setSelected(true);
        }
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox2(ActionEvent e) {
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
        setStateCheckBox2();
    }

    @FXML
    private void handleRCheckBoxIkasHH() {
        setStateCheckBox2();
    }

    private void setStateCheckBox2() {
        checkBoxExperiment2.setIndeterminate(true);
        if (!rCheckBoxIKASBH.isSelected() && !rCheckBoxIKASHH.isSelected()) {
            checkBoxExperiment2.setSelected(false);
            checkBoxExperiment2.setIndeterminate(false);
        } else if (rCheckBoxIKASBH.isSelected() && rCheckBoxIKASHH.isSelected()) {
            checkBoxExperiment2.setIndeterminate(false);
            checkBoxExperiment2.setSelected(true);
        }
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

    private boolean isAllSelected() {
        return checkBoxExperiment1.isSelected() &&
                !checkBoxExperiment1.isIndeterminate() &&
                checkBoxExperiment2.isSelected() &&
                checkBoxExperiment3.isSelected() &&
                checkBoxExperiment4.isSelected() &&
                checkBoxExperiment5.isSelected() &&
                checkBoxExperiment6.isSelected();

    }

    @FXML
    private void handleStartExperiments() {

//        showJFXDialog(root, "11111", "222222", 800, 600);

        if (textFieldSerialNumber.getText().isEmpty() || comboBoxTestItem.getSelectionModel().isEmpty()) {
            Toast.makeText("Введите заводской номер и выберите объект испытания").show(Toast.ToastType.WARNING);
        } else if (!isAtLeastOneIsSelected()) {
            Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING);
        } else {
            if (currentState instanceof IdleState) {
                experimentsValuesModel.createNewProtocol(textFieldSerialNumber.getText(), comboBoxTestItem.getSelectionModel().getSelectedItem());
                currentState.toWaitState();
            }

            boolean isCanceled = false;
            if ((checkBoxExperiment1.isSelected() || checkBoxExperiment1.isIndeterminate()) && !isCanceled) {

                int mask = 0;
                mask |= rCheckBoxMegerBH.isSelected() ? 0b1 : 0;
                mask |= rCheckBoxMegerHH.isSelected() ? 0b10 : 0;
                experimentsValuesModel.setExperiment1Choice(mask);

                isCanceled = start1Experiment();
            }
            if ((checkBoxExperiment2.isSelected() || checkBoxExperiment2.isIndeterminate()) && !isCanceled) {
                int experiment2ChoiceMask = 0;
                experiment2ChoiceMask |= rCheckBoxIKASBH.isSelected() ? 0b1 : 0;
                experiment2ChoiceMask |= rCheckBoxIKASHH.isSelected() ? 0b10 : 0;
                experimentsValuesModel.setExperiment2Choice(experiment2ChoiceMask);

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
            if (!isCanceled) {
                currentState.toResultState();
            } else {
                Toast.makeText("Отменено").show(Toast.ToastType.INFORMATION);
            }
        }
    }

    private boolean isAtLeastOneIsSelected() {
        return checkBoxExperiment1.isSelected() ||
                rCheckBoxMegerBH.isSelected() ||
                rCheckBoxMegerHH.isSelected() ||
                checkBoxExperiment2.isSelected() ||
                rCheckBoxIKASBH.isSelected() ||
                rCheckBoxIKASHH.isSelected() ||
                checkBoxExperiment3.isSelected() ||
                checkBoxExperiment4.isSelected() ||
                checkBoxExperiment5.isSelected() ||
                checkBoxExperiment6.isSelected();
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
        ProtocolRepository.insertProtocol(experimentsValuesModel.getCurrentProtocol());
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