package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.gestionuser.service.EmailService;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordController {

    @FXML private VBox rootPane;
    @FXML private TextField txtEmail;
    @FXML private Label lblMessage;
    @FXML private Button btnSend;
    @FXML private Button btnBack;

    private final UtilisateurService userService = new UtilisateurService();

    // Stockage temporaire des codes de réinitialisation (email -> code)
    private static final Map<String, String> resetCodes = new HashMap<>();
    private static final SecureRandom random = new SecureRandom();

    @FXML
    private void initialize() {
        AnimationManager.fadeInPage(rootPane);
        txtEmail.requestFocus();
    }

    @FXML
    private void handleSendCode() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez saisir votre email");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showError("Format d'email invalide");
            return;
        }

        btnSend.setDisable(true);
        btnSend.setText("⏳ Envoi en cours...");

        // Vérifier si l'email existe dans la base
        Utilisateur user = null;
        try {
            user = userService.findByEmail(email);
        } catch (Exception e) {
            // Ignorer
        }

        if (user == null) {
            showError("Aucun compte trouvé avec cet email");
            btnSend.setDisable(false);
            btnSend.setText("📧 Envoyer le code");
            return;
        }

        // Générer un code à 6 chiffres
        String code = generateSixDigitCode();

        // Stocker le code temporairement
        resetCodes.put(email, code);

        // Envoyer l'email
        boolean sent = EmailService.sendPasswordResetCode(user, code);

        if (sent) {
            showSuccess("✅ Code envoyé à " + email);

            // Attendre 1.5s puis aller à la page de vérification
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() ->
                            navigateToVerifyCode(email));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            showError("❌ Erreur d'envoi d'email");
            btnSend.setDisable(false);
            btnSend.setText("📧 Envoyer le code");
            resetCodes.remove(email);
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToVerifyCode(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VerifyResetCode.fxml"));
            Parent root = loader.load();

            VerifyResetCodeController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) btnSend.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateSixDigitCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
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

    // Méthode statique pour vérifier le code
    public static boolean verifyCode(String email, String code) {
        String storedCode = resetCodes.get(email);
        return storedCode != null && storedCode.equals(code);
    }
}