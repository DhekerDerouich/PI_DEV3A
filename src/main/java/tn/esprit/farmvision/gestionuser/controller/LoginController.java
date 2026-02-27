package tn.esprit.farmvision.gestionuser.controller;

import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.ResponsableExploitation;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.TwoFactorAuthService;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;
import tn.esprit.farmvision.gestionuser.util.GoogleAuthUtil;
import tn.esprit.farmvision.gestionuser.util.GoogleAuthUtil.GoogleUserInfo;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkSignup;
    @FXML private Hyperlink linkForgotPassword;  // ✅ Important !
    @FXML private Button btnGoogleLogin;
    @FXML private BorderPane rootPane;
    @FXML private ImageView logoImageView;

    private final UtilisateurService service = new UtilisateurService();

    @FXML
    private void initialize() {
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event -> {
                AnimationManager.handleSecretCode(event.getCharacter(), rootPane);
            });
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }

        if (logoImageView != null) {
            AnimationManager.animateLogoStart(logoImageView);
        }

        System.out.println("🔐 LoginController initialisé");
    }

    @FXML
    private void handleLogin() {
        lblError.setVisible(false);

        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            AnimationManager.showError(lblError, "Email et mot de passe obligatoires");
            return;
        }

        AnimationManager.startLoadingButton(btnLogin, "Se connecter", "Connexion...");

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(event -> {
            try {
                Utilisateur user = service.login(email, password);

                if (user == null) {
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                    AnimationManager.showError(lblError, "Email ou mot de passe incorrect");
                    return;
                }

                if (!user.isActivated() && !(user instanceof Administrateur)) {
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                    AnimationManager.showError(lblError, "Compte en attente de validation");
                    return;
                }

                if (user instanceof Administrateur) {
                    System.out.println("👨‍💼 Admin détecté");
                    SessionManager.getInstance().setCurrentUser(user);
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");

                    if (logoImageView != null && rootPane != null) {
                        AnimationManager.playLogoSuccessAnimation(
                                logoImageView,
                                rootPane,
                                () -> navigateToDashboard(user)
                        );
                    } else {
                        navigateToDashboard(user);
                    }
                } else if (user instanceof ResponsableExploitation) {
                    System.out.println("👔 Responsable détecté");
                    SessionManager.getInstance().setCurrentUser(user);
                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");

                    if (logoImageView != null && rootPane != null) {
                        AnimationManager.playLogoSuccessAnimation(
                                logoImageView,
                                rootPane,
                                () -> navigateToModuleCOM()
                        );
                    } else {
                        navigateToModuleCOM();
                    }
                } else {
                    System.out.println("🌾 Agriculteur détecté");
                    boolean codeSent = TwoFactorAuthService.sendVerificationCode(user);

                    if (!codeSent) {
                        AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                        AnimationManager.showError(lblError, "❌ Erreur envoi code 2FA");
                        return;
                    }

                    AnimationManager.stopLoadingButton(btnLogin, "Se connecter");

                    if (logoImageView != null && rootPane != null) {
                        AnimationManager.playLogoSuccessAnimation(
                                logoImageView,
                                rootPane,
                                () -> navigate2FAVerification(user)
                        );
                    } else {
                        navigate2FAVerification(user);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                AnimationManager.stopLoadingButton(btnLogin, "Se connecter");
                AnimationManager.showError(lblError, e.getMessage());
            }
        });
        pause.play();
    }

    // ✅ MÉTHODE MOT DE PASSE OUBLIÉ
    @FXML
    private void handleForgotPassword() {
        try {
            System.out.println("🔐 Redirection vers page mot de passe oublié");

            URL url = getClass().getResource("/fxml/ForgotPassword.fxml");
            if (url == null) {
                AnimationManager.showError(lblError, "❌ Page de récupération introuvable");
                return;
            }

            Parent root = FXMLLoader.load(url);
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur: " + e.getMessage());
        }
    }

    private void navigateToDashboard(Utilisateur user) {
        try {
            String fxmlPath = (user instanceof Administrateur)
                    ? "/fxml/AdminDashboard.fxml"
                    : "/fxml/UserDashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isFullScreen = stage.isFullScreen();

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1000,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle("FarmVision - Dashboard");

            if (isFullScreen) {
                stage.setFullScreen(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur navigation : " + e.getMessage());
        }
    }

    private void navigateToModuleCOM() {
        try {
            String[] chemins = {
                    "/com/pi/view/main.fxml",
                    "/tn/esprit/farmvision/com/pi/view/main.fxml",
                    "/main.fxml"
            };

            FXMLLoader loader = null;
            for (String chemin : chemins) {
                URL url = getClass().getResource(chemin);
                if (url != null) {
                    loader = new FXMLLoader(url);
                    break;
                }
            }

            if (loader == null) {
                throw new Exception("Fichier equipement.fxml introuvable");
            }

            Parent root = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("FarmVision - Gestion des Équipements");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur navigation COM: " + e.getMessage());
        }
    }

    private void navigate2FAVerification(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Verify2FA.fxml"));
            Parent root = loader.load();

            Verify2FAController controller = loader.getController();
            controller.setPendingUser(user);

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));
            stage.setTitle("FarmVision - Vérification 2FA");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur chargement page 2FA: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignUp() {
        try {
            if (linkSignup != null) {
                AnimationManager.slideToRight(linkSignup, "/fxml/Signup.fxml");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AnimationManager.showError(lblError, "Erreur inscription: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoogleLogin() {
        lblError.setVisible(false);

        if (btnGoogleLogin != null) {
            btnGoogleLogin.setDisable(true);
            btnGoogleLogin.setText("🔄 Connexion Google...");
        }

        new Thread(() -> {
            try {
                GoogleUserInfo userInfo = GoogleAuthUtil.authenticateAndGetUserInfo();

                if (userInfo == null || userInfo.email == null) {
                    javafx.application.Platform.runLater(() -> {
                        AnimationManager.showError(lblError, "❌ Échec authentification Google");
                        resetGoogleButton();
                    });
                    return;
                }

                Utilisateur user = service.loginWithGoogle(userInfo);

                javafx.application.Platform.runLater(() -> {
                    SessionManager.getInstance().setCurrentUser(user);
                    navigateToDashboard(user);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    String errorMessage = e.getMessage();

                    if (errorMessage.contains("en attente") || errorMessage.contains("créé avec succès")) {
                        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #ffc107; " +
                                "-fx-font-weight: bold; -fx-padding: 12 15; " +
                                "-fx-background-radius: 10; -fx-border-color: #ff9800; " +
                                "-fx-border-width: 1; -fx-border-radius: 10;");
                    } else {
                        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #dc3545; " +
                                "-fx-font-weight: bold; -fx-padding: 12 15; " +
                                "-fx-background-radius: 10; -fx-border-color: #c82333; " +
                                "-fx-border-width: 1; -fx-border-radius: 10;");
                    }

                    lblError.setText(errorMessage);
                    lblError.setVisible(true);
                    resetGoogleButton();
                });

                e.printStackTrace();
            }
        }).start();
    }

    private void resetGoogleButton() {
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setDisable(false);
            btnGoogleLogin.setText("Continuer avec Google");
        }
    }

    @FXML
    private void onButtonHover() {
        if (btnLogin != null) {
            AnimationManager.buttonHoverEffect(btnLogin);
        }
    }

    @FXML
    private void onButtonExit() {
        if (btnLogin != null) {
            AnimationManager.buttonExitEffect(btnLogin);
        }
    }
}