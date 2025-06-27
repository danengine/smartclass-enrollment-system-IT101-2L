package com.example.smartclass;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheckBox;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private VBox loginForm;
    @FXML private Button loginButton;
    @FXML private StackPane loadingOverlay;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (Database.validateCredentials(username, password)) {
            errorLabel.setVisible(false);
            showLoading(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("admin-dashboard.fxml"));
                    Scene dashboardScene = new Scene(loader.load(), 1200, 700);
                    dashboardScene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(dashboardScene);
                    stage.setTitle("SmartClass - Admin Dashboard");
                    stage.centerOnScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            pause.play();
        } else {
            StudentsController.showLogoAlert("The username or password you entered is incorrect. Please try again.", Alert.AlertType.WARNING);
        }
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        loadingSpinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }


    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckBox.isSelected()) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
        }
    }
}