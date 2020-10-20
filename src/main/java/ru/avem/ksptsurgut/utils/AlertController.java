package ru.avem.ksptsurgut.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import ru.avem.ksptsurgut.Main;

import java.util.Optional;

public class AlertController {
    private AlertController() {
        throw new IllegalAccessError("Non-instantiable class.");
    }

    public static void showConfirmDialog(String title, String text, Runnable actionYes) {
        showConfirmDialog(title, text, actionYes, () -> {
        });
    }

    public static void showConfirmDialog(String title, String text, Runnable actionYes, Runnable actionNo) {
        showConfirmDialog(title, text, "Да", actionYes, "Нет", actionNo);
    }

    private static Alert alert;

    public static Alert getAlert() {
        return alert;
    }

    public static void showConfirmDialog(String title, String text, String nameYes, Runnable actionYes, String nameNo, Runnable actionNo) {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(text);
        Image image = new Image(Main.class.getResourceAsStream("icon/start.gif"));
        ImageView imageView = new ImageView(image);
        alert.setGraphic(imageView);
        imageView.setFitWidth(70);
        imageView.setFitHeight(70);
        alert.setGraphic(imageView);

        alert.getButtonTypes().clear();

        ButtonType buttonTypeYes = null;
        if (actionYes != null) {
            buttonTypeYes = new ButtonType(nameYes, ButtonBar.ButtonData.YES);
            alert.getButtonTypes().add(buttonTypeYes);

        }

        ButtonType buttonTypeNo = null;
        if (actionNo != null) {
            buttonTypeNo = new ButtonType(nameNo, ButtonBar.ButtonData.NO);
            alert.getButtonTypes().add(buttonTypeNo);
        }

        alert.getDialogPane().getStylesheets().add(
                Main.class.getResource("styles/main_css.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("myDialog");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            actionYes.run();
        } else if (result.isPresent() && result.get() == buttonTypeNo) {
            actionNo.run();
        }
    }
}
