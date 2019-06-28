package ru.avem.ksptamur.utils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.controlsfx.control.Notifications;

public class Toast {
    private Notifications notifications;

    private Toast(Notifications notifications) {
        this.notifications = notifications;
    }

    public void show(ToastType type) {
        switch (type) {
            case INFORMATION:
                notifications.title("Информация");
                notifications.showInformation();
                break;
            case CONFIRM:
                notifications.title("Подтверждение");
                notifications.showConfirm();
                break;
            case ERROR:
                notifications.title("Ошибка");
                notifications.showError();
                break;
            case WARNING:
                notifications.title("Внимание");
                notifications.showWarning();
                break;
            case NONE:
            default:
                notifications.show();
                break;
        }
    }

    public enum ToastType {
        INFORMATION,
        CONFIRM,
        ERROR,
        WARNING,
        NONE
    }

    public static Toast makeText(String text) {
        return new Toast(Notifications.create().text(text).position(Pos.BOTTOM_CENTER));

    }
    public static void showJFXDialog(Pane pane, String headingText, String bodyText, double dialogWidth, double dialogHeight) {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text(headingText));
        content.setBody(new Text(bodyText));
        content.setPrefSize(dialogWidth, dialogHeight);
        StackPane stackPane = new StackPane();
        stackPane.autosize();
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.LEFT, true);
        JFXButton button = new JFXButton("Okay");
        button.setOnAction(event -> dialog.close());
        button.setButtonType(com.jfoenix.controls.JFXButton.ButtonType.RAISED);
        button.setPrefHeight(32);
//        button.setStyle(dialogBtnStyle);
        content.setActions(button);
        pane.getChildren().add(stackPane);
        AnchorPane.setTopAnchor(stackPane, (pane.getHeight() - content.getPrefHeight()) / 2);
        AnchorPane.setLeftAnchor(stackPane, (pane.getWidth() - content.getPrefWidth()) / 2);
        dialog.show();
    }

}
