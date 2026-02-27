package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VerifyResetCodeController {

    @FXML private VBox rootPane;
    @FXML private TextField txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6;
    @FXML private Label lblMessage, lblEmail;
    @FXML private Button btnVerify, btnResend, btnBack;

    private String email;

    @FXML
    private void initialize() {
        AnimationManager.fadeInPage(rootPane);
        setupAutoAdvance();
        txtCode1.requestFocus();
    }

    public void setEmail(String email) {
        this.email = email;
        String maskedEmail = maskEmail(email);
        lblEmail.setText("📧 Code envoyé à: " + maskedEmail);
    }

    private void setupAutoAdvance() {
        TextField[] fields = {txtCode1, txtCode2, txtCode3, txtCode4, txtCode5, txtCode6};

        for (int i = 0; i < fields.length; i++) {
            final int index = i;

            fields[i].textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    fields[index].setText(oldVal);
                    return;
                }

                if (newVal.length() > 1) {
                    fields[index].setText(newVal.substring(0, 1));
                    return;
                }

                if (newVal.length() == 1 && index < fields.length - 1) {
                    fields[index + 1].requestFocus();
                }

                if (index == fields.length - 1 && newVal.length() == 1) {
                    checkAllFieldsFilled();
                }
            });

            fields[i].setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("BACK_SPACE")) {
                    if (fields[index].getText().isEmpty() && index > 0) {
                        fields[index - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void checkAllFieldsFilled() {
        String code = getEnteredCode();
        if (code.length() == 6) {
            new Thread(() -> {
                try {
                    Thread.sleep(300);
                    javafx.application.Platform.runLater(this::handleVerify);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private String getEnteredCode() {
        return txtCode1.getText() + txtCode2.getText() + txtCode3.getText() +
                txtCode4.getText() + txtCode5.getText() + txtCode6.getText();
    }

    @FXML
    private void handleVerify() {
        String code = getEnteredCode();

        if (code.length() != 6) {
            showError("Veuillez saisir les 6 chiffres");
            return;
        }

        boolean isValid = ForgotPasswordController.verifyCode(email, code);

        if (isValid) {
            showSuccess("✅ Code valide!");

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(this::navigateToResetPassword);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("❌ Code invalide ou expiré");
            clearFields();
            txtCode1.requestFocus();
        }
    }

    @FXML
    private void handleResend() {
        // Retourner à la page ForgotPassword
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnResend.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToResetPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ResetPassword.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) btnVerify.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtCode1.clear();
        txtCode2.clear();
        txtCode3.clear();
        txtCode4.clear();
        txtCode5.clear();
        txtCode6.clear();
    }

    private void showError(String message) {
        lblMessage.setText("❌ " + message);
        lblMessage.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }

    private void showSuccess(String message) {
        lblMessage.setText("✅ " + message);
        lblMessage.setStyle("-fx-text-fill: #198754; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) return email;
        return username.substring(0, 2) + "***@" + domain;
    }
}