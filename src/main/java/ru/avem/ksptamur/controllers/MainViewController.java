package ru.avem.ksptamur.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import ru.avem.ksptamur.utils.BuildConfig;
import ru.avem.ksptamur.utils.Toast;
import ru.avem.ksptamur.utils.Utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static ru.avem.ksptamur.Main.*;

@SuppressWarnings("ALL")
public class MainViewController implements Statable {


    @FXML
    private Button buttonProtocolCancel;
    @FXML
    private Button buttonProtocolNext;

    @FXML
    private TextField labelProtocolSerialNumber;
    @FXML
    private MenuItem menuBarProtocolSaveAs;
    @FXML
    private MenuItem menuBarDBTestItems;

    @FXML
    private Parent root;

    @FXML
    private CheckMenuItem checkMenuItemTheme;

    @FXML
    private RadioButton radioBH;
    @FXML
    private RadioButton radioHH;

    @FXML
    private CheckBox radioMegerBH;
    @FXML
    private CheckBox radioMegerHH;
    @FXML
    private CheckBox radioMegerBHHH;

    @FXML
    private RadioButton radioIKASBH;
    @FXML
    private RadioButton radioIKASHH;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabProtocol;
    @FXML
    private Tab tabExperiments;
    @FXML
    private Tab tabResults;

    @FXML
    private CheckBox checkBoxSelectAllItems;
    @FXML
    private CheckBox checkBoxExperiment0;
    @FXML
    private CheckBox checkBoxExperiment1;
    @FXML
    private CheckBox checkBoxExperiment2;
    @FXML
    private CheckBox checkBoxExperiment3;
    @FXML
    private CheckBox checkBoxExperiment4;
    @FXML
    private CheckBox checkBoxExperiment5;
    @FXML
    private CheckBox checkBoxExperiment6;
    @FXML
    private CheckBox checkBoxExperiment7;
    @FXML
    private CheckBox checkBoxExperiment8;

    @FXML
    private ComboBox<TestItem> comboBoxProtocolTestItem;
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
        css = "white";
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

        if (!BuildConfig.DEBUG) {
            toInitIdleState();
        }
    }

    private void refreshTestItems() {
        List<TestItem> allTestItems = TestItemRepository.getAllTestItems();
        comboBoxProtocolTestItem.getItems().setAll(allTestItems);
    }

    private void initializeComboBoxResult() {
        comboBoxResult.getItems().setAll(ExperimentsHolder.getNamesOfExperiments());
        comboBoxResult.setOnAction(event -> {
            Protocol currentProtocol = mainModel.getCurrentProtocol();
            switch (comboBoxResult.getSelectionModel().getSelectedItem()) {
                case Constants.Experiments.EXPERIMENT1_NAME:
                    System.out.println(currentProtocol.getE1ResultBH());
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
                    resultData.add(new ResultModel("UВН, В", currentProtocol.getE3UBH()));
                    resultData.add(new ResultModel("UНН, В", currentProtocol.getE3UHH()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE3F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE3Result()));
                    break;
                case Constants.Experiments.EXPERIMENT4_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("Группа соединений BH", currentProtocol.getE4WindingBH()));
                    resultData.add(new ResultModel("Группа соединений HH", currentProtocol.getE4WindingHH()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE4Result()));
                    break;
                case Constants.Experiments.EXPERIMENT5_NAME:
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
                case Constants.Experiments.EXPERIMENT6_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("UВН, В", currentProtocol.getE6UBH()));
                    resultData.add(new ResultModel("I A, A", currentProtocol.getE6IA()));
                    resultData.add(new ResultModel("I B, A", currentProtocol.getE6IB()));
                    resultData.add(new ResultModel("I C, A", currentProtocol.getE6IC()));
                    resultData.add(new ResultModel("Pп, Вт", currentProtocol.getE6Pp()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE6F()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE6Result()));
                    break;
                case Constants.Experiments.EXPERIMENT7_NAME:
                    resultData.clear();
                    resultData.add(new ResultModel("UВх, В", currentProtocol.getE7UInput()));
                    resultData.add(new ResultModel("IВН, A", currentProtocol.getE7IBH()));
                    resultData.add(new ResultModel("f, Гц", currentProtocol.getE7F()));
                    resultData.add(new ResultModel("t, сек", currentProtocol.getE7Time()));
                    resultData.add(new ResultModel("Результат", currentProtocol.getE7Result()));

                    break;
                case Constants.Experiments.EXPERIMENT8_NAME:
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
        tabExperiments.setDisable(true);
        tabResults.setDisable(true);
    }

    @Override
    public void toIdleState() {
        toInitIdleState();
        labelProtocolSerialNumber.clear();
        labelProtocolSerialNumber.setDisable(false);
        comboBoxProtocolTestItem.getSelectionModel().clearSelection();
        comboBoxProtocolTestItem.setDisable(false);
        buttonProtocolCancel.setText("Очистить");
        buttonProtocolNext.setText("Создать");
        setLeftStatus("");
        setRightStatus("");
        tabPane.getSelectionModel().select(tabProtocol);
        mainModel.setCurrentProtocol(null);
        menuBarDBTestItems.setDisable(false);
        currentState = idleState;
    }

    @Override
    public void toWaitState() {
        tabExperiments.setDisable(false);
        tabPane.getSelectionModel().select(tabExperiments);
        labelProtocolSerialNumber.setDisable(true);
        comboBoxProtocolTestItem.setDisable(true);
        buttonProtocolCancel.setText("Новый");
        buttonProtocolNext.setText("Далее");
        setLeftStatus("Заводской номер: " + labelProtocolSerialNumber.getText());
        setRightStatus("Объект испытания: " + comboBoxProtocolTestItem.getSelectionModel().getSelectedItem());
        menuBarProtocolSaveAs.setDisable(false);
        menuBarDBTestItems.setDisable(true);
        currentState = waitState;
    }

    @Override
    public void toResultState() {
        tabResults.setDisable(false);
        tabPane.getSelectionModel().select(tabResults);
        currentState = resultState;
        Protocol currentProtocol = mainModel.getCurrentProtocol();
        currentProtocol.setMillis(System.currentTimeMillis());
        ProtocolRepository.insertProtocol(currentProtocol);
        Toast.makeText("Результаты проведенных испытаний сохранены").show(Toast.ToastType.INFORMATION);
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
            labelProtocolSerialNumber.setText(protocol.getSerialNumber());
            comboBoxProtocolTestItem.getSelectionModel().select(protocol.getObject());
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
                labelProtocolSerialNumber.setText(mainModel.getCurrentProtocol().getSerialNumber());
                comboBoxProtocolTestItem.getSelectionModel().select(mainModel.getCurrentProtocol().getObject());
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
        DeviceStateWindowController controller = loader.getController();

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
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setResizable(false);
        dialogStage.setScene(new Scene(page));

        dialogStage.setOnCloseRequest(event -> {
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
    private void handleButtonProtocolNext() {
        if (!labelProtocolSerialNumber.getText().isEmpty() && !comboBoxProtocolTestItem.getSelectionModel().isEmpty()) {
            mainModel.createNewProtocol(labelProtocolSerialNumber.getText(), comboBoxProtocolTestItem.getSelectionModel().getSelectedItem());
            currentState.toWaitState();
        } else {
            Toast.makeText("Введите заводской номер и выберите объект испытания").show(Toast.ToastType.INFORMATION);
        }
    }

    public void handleMenuBarProtocolNew() {
        currentState.toIdleState();
    }

    @FXML
    private void handleSelectAllTests() {
        if (checkBoxSelectAllItems.isSelected()) {
            checkBoxExperiment0.setIndeterminate(false);
            checkBoxExperiment0.setSelected(true);
            checkBoxExperiment1.setSelected(true);
            checkBoxExperiment1.setSelected(true);
            checkBoxExperiment2.setSelected(true);
            checkBoxExperiment3.setSelected(true);
            checkBoxExperiment4.setSelected(true);
            checkBoxExperiment5.setSelected(true);
            checkBoxExperiment6.setSelected(true);
            checkBoxExperiment7.setSelected(true);
            checkBoxExperiment8.setSelected(true);
            checkBoxExperiment8.setSelected(true);
            radioBH.setDisable(true);
            radioBH.setSelected(false);
            radioHH.setDisable(true);
            radioHH.setSelected(false);
            radioMegerBH.setSelected(true);
            radioMegerHH.setSelected(true);
            radioMegerBHHH.setSelected(true);
            radioIKASBH.setSelected(false);
            radioIKASBH.setDisable(true);
            radioIKASHH.setSelected(false);
            radioIKASHH.setDisable(true);
        } else {
            checkBoxExperiment0.setSelected(false);
            checkBoxExperiment1.setSelected(false);
            checkBoxExperiment1.setSelected(false);
            checkBoxExperiment2.setSelected(false);
            checkBoxExperiment3.setSelected(false);
            checkBoxExperiment4.setSelected(false);
            checkBoxExperiment5.setSelected(false);
            checkBoxExperiment6.setSelected(false);
            checkBoxExperiment7.setSelected(false);
            checkBoxExperiment8.setSelected(false);
            checkBoxExperiment8.setSelected(false);
            radioBH.setSelected(false);
            radioBH.setDisable(false);
            radioHH.setSelected(false);
            radioHH.setDisable(false);
            radioMegerBH.setSelected(false);
            radioMegerBH.setDisable(false);
            radioMegerHH.setSelected(false);
            radioMegerHH.setDisable(false);
            radioMegerBHHH.setSelected(false);
            radioMegerBHHH.setDisable(false);
            radioIKASBH.setSelected(false);
            radioIKASBH.setDisable(false);
            radioIKASHH.setSelected(false);
            radioIKASHH.setDisable(false);
        }
    }


    @FXML
    private void handleCheckBox0(ActionEvent e) {
        CheckBox checkBox0 = (CheckBox) e.getSource();
        if (checkBox0.isSelected()) {
            checkBoxSelectAllItems.setSelected(isAllSelected());
            radioMegerBH.setSelected(true);
            radioMegerHH.setSelected(true);
            radioMegerBHHH.setSelected(true);
        } else {
            radioMegerBH.setSelected(false);
            radioMegerHH.setSelected(false);
            radioMegerBHHH.setSelected(false);
        }
    }

    @FXML
    private void handleRadioExperiment0BH() {
        setStateCheckBox0();
    }

    @FXML
    private void handleRadioExperiment0HH() {
        setStateCheckBox0();
    }

    @FXML
    private void handleRadioExperiment0BHHH() {
        setStateCheckBox0();
    }

    private void setStateCheckBox0() {
        checkBoxExperiment0.setIndeterminate(true);
        if (!radioMegerBH.isSelected() && !radioMegerHH.isSelected() && !radioMegerBHHH.isSelected()) {
            checkBoxExperiment0.setSelected(false);
            checkBoxExperiment0.setIndeterminate(false);
        } else if (radioMegerBH.isSelected() && radioMegerHH.isSelected() && radioMegerBHHH.isSelected()) {
            checkBoxExperiment0.setIndeterminate(false);
            checkBoxExperiment0.setSelected(true);
        }
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox1(ActionEvent e) {
        checkBoxSelectAllItems.setSelected(isAllSelected());
        CheckBox checkBox1 = (CheckBox) e.getSource();
        if (checkBox1.isSelected()) {
            radioIKASBH.setSelected(false);
            radioIKASBH.setDisable(true);
            radioIKASHH.setSelected(false);
            radioIKASHH.setDisable(true);
            mainModel.setExperiment1Choise(MainModel.EXPERIMENT1_BOTH);
        } else {
            radioIKASBH.setSelected(false);
            radioIKASBH.setDisable(false);
            radioIKASHH.setSelected(false);
            radioIKASHH.setDisable(false);
        }
    }

    @FXML
    private void handleRadioExperiment1BH() {
        checkBoxExperiment1.setIndeterminate(true);
        mainModel.setExperiment1Choise(MainModel.EXPERIMENT1_BH);
    }

    @FXML
    private void handleRadioExperiment1HH() {
        checkBoxExperiment1.setIndeterminate(true);
        mainModel.setExperiment1Choise(MainModel.EXPERIMENT1_HH);
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
    private void handleCheckBox7() {
        checkBoxSelectAllItems.setSelected(isAllSelected());
    }

    @FXML
    private void handleCheckBox8(ActionEvent e) {
        checkBoxSelectAllItems.setSelected(isAllSelected());
        CheckBox checkBox8 = (CheckBox) e.getSource();
        if (checkBox8.isSelected()) {
            mainModel.setExperiment8Choise(MainModel.EXPERIMENT8_BOTH);
            radioBH.setSelected(false);
            radioBH.setDisable(true);
            radioHH.setSelected(false);
            radioHH.setDisable(true);
        } else {
            radioBH.setSelected(false);
            radioBH.setDisable(false);
            radioHH.setSelected(false);
            radioHH.setDisable(false);
        }
    }

    @FXML
    private void handleRadioExperiment8BH() {
        mainModel.setExperiment8Choise(MainModel.EXPERIMENT8_BH);
        checkBoxExperiment8.setIndeterminate(true);
    }

    @FXML
    private void handleRadioExperiment8HH() {
        mainModel.setExperiment8Choise(MainModel.EXPERIMENT8_HH);
        checkBoxExperiment8.setIndeterminate(true);
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
                checkBoxExperiment7.isSelected() &&
                checkBoxExperiment8.isSelected();

    }

    @FXML
    private void handleStartExperiments() {
        if (!radioIKASBH.isSelected() &&
                !radioIKASHH.isSelected() &&
                !radioMegerBH.isSelected() &&
                !radioMegerHH.isSelected() &&
                !radioMegerBHHH.isSelected() &&
                !checkBoxExperiment0.isSelected() &&
                !checkBoxExperiment1.isSelected() &&
                !checkBoxExperiment2.isSelected() &&
                !checkBoxExperiment3.isSelected() &&
                !checkBoxExperiment4.isSelected() &&
                !checkBoxExperiment5.isSelected() &&
                !checkBoxExperiment6.isSelected() &&
                !checkBoxExperiment7.isSelected() &&
                !checkBoxExperiment8.isSelected() &&
                !radioBH.isSelected() &&
                !radioHH.isSelected()) {
            Toast.makeText("Выберите хотя бы одно испытание из списка").show(Toast.ToastType.WARNING);
        } else {
            boolean isCanceled = false;
            if ((checkBoxExperiment0.isSelected() || checkBoxExperiment0.isIndeterminate()) && !isCanceled) {

                int mask = 0;
                mask |= radioMegerBH.isSelected() ? 0b1 : 0;
                mask |= radioMegerHH.isSelected() ? 0b10 : 0;
                mask |= radioMegerBHHH.isSelected() ? 0b100 : 0;
                mainModel.setExperiment0Choise(mask);

                isCanceled = start0Experiment();
            }
            if ((checkBoxExperiment1.isSelected() || checkBoxExperiment1.isIndeterminate()) && !isCanceled) {
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
            if (checkBoxExperiment7.isSelected() && !isCanceled) {
                isCanceled = start7Experiment();
            }
            if ((checkBoxExperiment8.isSelected() || checkBoxExperiment8.isIndeterminate()) && !isCanceled) {
                isCanceled = start8Experiment();
            }
            if (!isCanceled) {
                currentState.toResultState();
            }
        }
    }

    private boolean start0Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment0ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start1Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment1ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start2Experiment() {
        mainModel.setExperiment2Choise(MainModel.EXPERIMENT2_ONLY);
        if (checkBoxExperiment4.isSelected() && checkBoxExperiment6.isSelected()) {
            mainModel.setExperiment2Choise(MainModel.EXPERIMENT2_WITH_NOLOAD_AND_PHASEMETER);
        } else if (checkBoxExperiment4.isSelected()) {
            mainModel.setExperiment2Choise(MainModel.EXPERIMENT2_WITH_PHASEMETER);
        } else if (checkBoxExperiment6.isSelected()) {
            mainModel.setExperiment2Choise(MainModel.EXPERIMENT2_WITH_NOLOAD);
        }
        return startExperiment(String.format("layouts/phase%d/experiment2ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start3Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment3ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start4Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment4ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start5Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment5ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start6Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment6ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start7Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment7ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
    }

    private boolean start8Experiment() {
        return startExperiment(String.format("layouts/phase%d/experiment8ViewPhase%d.fxml", (int) mainModel.getCurrentProtocol().getPhase(), (int) mainModel.getCurrentProtocol().getPhase()));
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

    public void selectTabExperiment() {
        tabPane.getSelectionModel().select(tabExperiments);
    }

    public void handleEventLog() {

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
            } else {
                root.getStylesheets().set(0, Main.class.getResource("styles/main_css_black.css").toURI().toString());
                css = "black";
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Версия ПО");
        alert.setHeaderText("Версия: 1.3.3");
        alert.setContentText("Дата: 08.05.2019");

        alert.showAndWait();
    }
}
