package ru.avem.ksptamur.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import ru.avem.ksptamur.Main;

import java.util.Optional;

public class View {

    private View() {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface Actionable {
        void onAction();
    }

    public static void showConfirmDialog(String text, Actionable actionYes, Actionable actionNo) {
        showConfirmDialog("", text, actionYes, actionNo);
    }

    public static void showConfirmDialog(String title, String text, Actionable actionYes, Actionable actionNo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initStyle(StageStyle.UTILITY);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(text);
        Image image = new Image(Main.class.getResourceAsStream("icon/alert.gif"));
        ImageView imageView = new ImageView(image);
        alert.setGraphic(imageView);

        ButtonType buttonTypeYes = new ButtonType("Да", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType("Нет", ButtonBar.ButtonData.NO);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonTypeYes) {
            actionYes.onAction();
        } else if (result.isPresent() && result.get() == buttonTypeNo) {
            actionNo.onAction();
        }
    }
}
