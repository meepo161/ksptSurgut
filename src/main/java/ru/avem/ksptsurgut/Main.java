package ru.avem.ksptsurgut;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.StatusBar;
import ru.avem.ksptsurgut.communication.CommunicationModel;
import ru.avem.ksptsurgut.controllers.LoginController;
import ru.avem.ksptsurgut.controllers.MainViewController;
import ru.avem.ksptsurgut.db.DataBaseRepository;

import java.io.IOException;
import java.net.URISyntaxException;

import static ru.avem.ksptsurgut.Constants.Info.TITLE;
import static ru.avem.ksptsurgut.utils.View.showConfirmDialog;

public class Main extends Application implements ru.avem.ksptsurgut.Exitappable {
    private static Label statusBarLeft;
    private static Label statusBarRight;

    public static Stage PRIMARY_STAGE;
    public static String css = "white";

    private Scene loginScene;
    private LoginController loginWindowController;
    private Scene mainViewScene;

    private MainViewController mainViewController;
    private CommunicationModel communicationModel;

    @Override
    public void init() throws IOException {
        communicationModel = CommunicationModel.getInstance();
        DataBaseRepository.init(false);

        createLoginScene();
        createMainViewScene();
    }


    @Override
    public void start(Stage primaryStage) {
        PRIMARY_STAGE = primaryStage;
//        if (BuildConfig.DEBUG) {
        showMainView();
//        } else {
//            showLoginView();
//        }
        PRIMARY_STAGE.initStyle(StageStyle.TRANSPARENT);
        PRIMARY_STAGE.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        communicationModel.setFinished(true);
    }

    private void createLoginScene() throws IOException {
        FXMLLoader loginWindowLoader = new FXMLLoader();
        loginWindowLoader.setLocation(getClass().getResource("layouts/loginWindow.fxml"));
        Parent loginWindowParent = loginWindowLoader.load();
        loginWindowController = loginWindowLoader.getController();
        loginWindowController.setMainApp(this);

        loginScene = new Scene(loginWindowParent, ru.avem.ksptsurgut.Constants.Display.WIDTH, ru.avem.ksptsurgut.Constants.Display.HEIGHT);
        loginScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    if (!(event.getTarget() instanceof TextField)) {
                        Platform.exit();
                    }
                    break;
            }
        });
    }

    private void createMainViewScene() throws IOException {
        FXMLLoader mainViewLoader = new FXMLLoader();
        mainViewLoader.setLocation(getClass().getResource("layouts/mainView.fxml"));
        BorderPane mainViewParent = mainViewLoader.load();
        mainViewController = mainViewLoader.getController();
        mainViewController.setMain(this);

        StatusBar statusBar = new StatusBar();
        statusBar.setText("");
        statusBarLeft = new Label();
        statusBar.getLeftItems().add(0, statusBarLeft);
        statusBarRight = new Label();
        statusBar.getRightItems().add(0, statusBarRight);
        mainViewParent.setBottom(statusBar);
        mainViewScene = new Scene(mainViewParent, ru.avem.ksptsurgut.Constants.Display.WIDTH, ru.avem.ksptsurgut.Constants.Display.HEIGHT);
        mainViewScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    if (!(event.getTarget() instanceof TextField)) {
                        exitApp();
                    }
                    break;
            }
        });
    }

    private void showLoginView() {
        PRIMARY_STAGE.setTitle("Авторизация");
        loginWindowController.clearFields();
        PRIMARY_STAGE.setScene(loginScene);
    }

    public void showMainView() {
        PRIMARY_STAGE.setTitle(TITLE);
        PRIMARY_STAGE.setScene(mainViewScene);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setLeftStatus(String text) {
        statusBarLeft.setText(text);
    }

    public static void setRightStatus(String text) {
        statusBarRight.setText(text);
    }

    public static void setTheme(Parent root) {
        try {
            if (css.equals("white")) {
                root.getStylesheets().set(0, Main.class.getResource("styles/main_css.css").toURI().toString());

            } else {
                root.getStylesheets().set(0, Main.class.getResource("styles/main_css_black.css").toURI().toString());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exitApp() {
        showConfirmDialog("Вы действительно хотите выйти?", this::showLoginView, () -> {
        });
    }
}