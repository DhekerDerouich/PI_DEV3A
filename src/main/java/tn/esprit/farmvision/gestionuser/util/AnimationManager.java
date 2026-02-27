package tn.esprit.farmvision.gestionuser.util;

import javafx.animation.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 🌾 Gestionnaire centralisé de toutes les animations FarmVision
 * ✅ VERSION CORRIGÉE - Maintient la taille plein écran lors des transitions
 * 🎮 EASTER EGG: Tapez "FARM" rapidement sur n'importe quelle page!
 */
public class AnimationManager {

    // ========================
    // 🎮 EASTER EGG KOALA/FARM CODE
    // ========================
    private static StringBuilder secretCode = new StringBuilder();
    private static long lastKeyTime = 0;
    private static final String SECRET = "FARM"; // Le code secret!

    /**
     * 🎮 EASTER EGG: Appelle cette méthode depuis n'importe quel controller avec:
     *
     * rootPane.setOnKeyTyped(event -> {
     *     AnimationManager.handleSecretCode(event.getCharacter(), rootPane);
     * });
     */
    public static void handleSecretCode(String character, Pane rootPane) {
        long currentTime = System.currentTimeMillis();

        // Reset si plus de 2 secondes entre les touches
        if (currentTime - lastKeyTime > 2000) {
            secretCode.setLength(0);
        }

        lastKeyTime = currentTime;
        secretCode.append(character.toUpperCase());

        // Garder seulement les 4 derniers caractères
        if (secretCode.length() > 4) {
            secretCode.deleteCharAt(0);
        }

        // Vérifier si le code secret est entré
        if (secretCode.toString().equals(SECRET)) {
            System.out.println("🎉 EASTER EGG ACTIVÉ!");
            playFarmEasterEgg(rootPane);
            secretCode.setLength(0); // Reset
        }
    }

    /**
     * 🎊 Animation Easter Egg FarmVision!
     */
    private static void playFarmEasterEgg(Pane rootPane) {
        // Créer des emojis de ferme qui tombent
        String[] farmEmojis = {"🌾", "🚜", "🐄", "🌻", "🥕", "🌽", "🐓", "🐷", "🐑"};

        for (int i = 0; i < 20; i++) {
            Label emoji = new Label(farmEmojis[(int)(Math.random() * farmEmojis.length)]);
            emoji.setStyle("-fx-font-size: 40px;");
            emoji.setLayoutX(Math.random() * rootPane.getWidth());
            emoji.setLayoutY(-50);

            rootPane.getChildren().add(emoji);

            // Animation de chute
            TranslateTransition fall = new TranslateTransition(
                    Duration.seconds(2 + Math.random() * 2), emoji);
            fall.setByY(rootPane.getHeight() + 100);

            // Rotation pendant la chute
            RotateTransition rotate = new RotateTransition(
                    Duration.seconds(2 + Math.random() * 2), emoji);
            rotate.setByAngle(360 * (Math.random() > 0.5 ? 1 : -1));

            ParallelTransition animation = new ParallelTransition(fall, rotate);
            animation.setDelay(Duration.millis(i * 100));
            animation.setOnFinished(e -> rootPane.getChildren().remove(emoji));
            animation.play();
        }

        // Message de célébration
        Label message = new Label("🎉 Bienvenue chez FarmVision! 🌾");
        message.setStyle(
                "-fx-font-size: 32px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: #4CAF50; " +
                        "-fx-background-color: rgba(255,255,255,0.9); " +
                        "-fx-padding: 20; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);"
        );
        message.setLayoutX(rootPane.getWidth() / 2 - 250);
        message.setLayoutY(rootPane.getHeight() / 2 - 25);
        message.setOpacity(0);

        rootPane.getChildren().add(message);

        // Animation du message
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), message);
        fadeIn.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(0.5), message);
        scale.setFromX(0.5);
        scale.setFromY(0.5);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition appear = new ParallelTransition(fadeIn, scale);
        appear.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(3));
            pause.setOnFinished(ev -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), message);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(eve -> rootPane.getChildren().remove(message));
                fadeOut.play();
            });
            pause.play();
        });
        appear.play();
    }

    // ========================
    // ANIMATIONS DE PAGE
    // ========================

    public static void fadeInPage(Node root) {
        root.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public static void animateLogoStart(ImageView logoImageView) {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), logoImageView);
        pulse.setFromX(0.95);
        pulse.setFromY(0.95);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        pulse.play();
    }

    // ========================
    // 🔧 NAVIGATION ENTRE PAGES - MAINTIENT PLEIN ÉCRAN
    // ========================

    /**
     * ✅ Navigation avec maintien automatique du plein écran
     */
    public static void slideToRight(Node currentNode, String fxmlPath) {
        try {
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // ✅ SAUVEGARDER L'ÉTAT ACTUEL
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            System.out.println("📐 Navigation - État actuel: Width=" + currentWidth +
                    ", Height=" + currentHeight +
                    ", Maximized=" + isMaximized +
                    ", FullScreen=" + isFullScreen);

            // Charger la nouvelle page
            FXMLLoader loader = new FXMLLoader(AnimationManager.class.getResource(fxmlPath));
            Parent newPage = loader.load();

            // Animation de sortie
            TranslateTransition slideOut = new TranslateTransition(Duration.seconds(0.5), currentScene.getRoot());
            slideOut.setFromX(0);
            slideOut.setToX(-currentWidth);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), currentScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            ParallelTransition exitAnimation = new ParallelTransition(slideOut, fadeOut);

            exitAnimation.setOnFinished(event -> {
                // ✅ CRÉER LA NOUVELLE SCÈNE AVEC LA TAILLE CORRECTE
                Scene newScene = new Scene(newPage,
                        currentWidth > 0 ? currentWidth : 1000,
                        currentHeight > 0 ? currentHeight : 700);
                stage.setScene(newScene);

                // ✅ RESTAURER L'ÉTAT DE FENÊTRE
                if (isFullScreen) {
                    stage.setFullScreen(true);
                } else if (isMaximized) {
                    stage.setMaximized(true);
                }

                System.out.println("✅ Navigation terminée - Nouvel état restauré");

                // Animation d'entrée
                newPage.setTranslateX(currentWidth);
                newPage.setOpacity(0);

                TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), newPage);
                slideIn.setFromX(currentWidth);
                slideIn.setToX(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), newPage);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ParallelTransition enterAnimation = new ParallelTransition(slideIn, fadeIn);
                enterAnimation.play();
            });

            exitAnimation.play();

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Navigation inverse avec maintien du plein écran
     */
    public static void slideToLeft(Node currentNode, String fxmlPath) {
        try {
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene currentScene = stage.getScene();

            // ✅ SAUVEGARDER L'ÉTAT
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            // Charger la nouvelle page
            FXMLLoader loader = new FXMLLoader(AnimationManager.class.getResource(fxmlPath));
            Parent newPage = loader.load();

            // Animation de sortie (vers la droite)
            TranslateTransition slideOut = new TranslateTransition(Duration.seconds(0.5), currentScene.getRoot());
            slideOut.setFromX(0);
            slideOut.setToX(currentWidth);
            slideOut.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), currentScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            ParallelTransition exitAnimation = new ParallelTransition(slideOut, fadeOut);

            exitAnimation.setOnFinished(event -> {
                // ✅ CRÉER LA NOUVELLE SCÈNE
                Scene newScene = new Scene(newPage,
                        currentWidth > 0 ? currentWidth : 900,
                        currentHeight > 0 ? currentHeight : 700);
                stage.setScene(newScene);

                // ✅ RESTAURER L'ÉTAT
                if (isFullScreen) {
                    stage.setFullScreen(true);
                } else if (isMaximized) {
                    stage.setMaximized(true);
                }

                // Animation d'entrée (depuis la gauche)
                newPage.setTranslateX(-currentWidth);
                newPage.setOpacity(0);

                TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), newPage);
                slideIn.setFromX(-currentWidth);
                slideIn.setToX(0);
                slideIn.setInterpolator(Interpolator.EASE_OUT);

                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), newPage);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ParallelTransition enterAnimation = new ParallelTransition(slideIn, fadeIn);
                enterAnimation.play();
            });

            exitAnimation.play();

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void fadeTransition(Node currentNode, String fxmlPath) {
        try {
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Parent currentRoot = currentNode.getScene().getRoot();

            // ✅ SAUVEGARDER LA TAILLE
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), currentRoot);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            fadeOut.setOnFinished(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(AnimationManager.class.getResource(fxmlPath));
                    Parent newRoot = loader.load();

                    // ✅ MAINTENIR LA TAILLE
                    Scene scene = new Scene(newRoot,
                            currentWidth > 0 ? currentWidth : 1000,
                            currentHeight > 0 ? currentHeight : 700);
                    stage.setScene(scene);

                    if (isFullScreen) {
                        stage.setFullScreen(true);
                    } else if (isMaximized) {
                        stage.setMaximized(true);
                    }

                    newRoot.setOpacity(0);
                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), newRoot);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            fadeOut.play();

        } catch (Exception e) {
            System.err.println("❌ Erreur transition: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================
    // 🔧 ANIMATION LOGO SUCCÈS - SEULEMENT AGRANDISSEMENT (PAS DE ROTATION)
    // ========================

    /**
     * 🎆 Animation de succès du logo - AGRANDISSEMENT UNIQUEMENT
     */
    public static void playLogoSuccessAnimation(ImageView logoImageView, Pane rootPane, Runnable onFinished) {

        // 1. ✅ Le logo GROSSIT BEAUCOUP (5x) - SANS ROTATION
        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.7), logoImageView);
        scaleUp.setToX(5.0);
        scaleUp.setToY(5.0);
        scaleUp.setInterpolator(Interpolator.EASE_IN);

        // 2. Effet de BRILLANCE (Glow vert FarmVision)
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(76, 175, 80, 0.9));
        glow.setRadius(80);
        glow.setSpread(0.8);

        Timeline glowAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.radiusProperty(), 30)),
                new KeyFrame(Duration.seconds(0.35),
                        new KeyValue(glow.radiusProperty(), 120)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(glow.radiusProperty(), 150))
        );
        logoImageView.setEffect(glow);

        // 3. Le reste devient FLOU
        GaussianBlur blur = new GaussianBlur(0);
        rootPane.setEffect(blur);

        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
                new KeyFrame(Duration.seconds(0.7), new KeyValue(blur.radiusProperty(), 35))
        );

        // 4. Fade out du reste
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.7), rootPane);
        fadeOut.setToValue(0.2);

        // Jouer l'animation (SANS rotation)
        scaleUp.setOnFinished(event -> {
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(e -> {
                if (onFinished != null) {
                    onFinished.run();
                }
            });
            pause.play();
        });

        // LANCER L'ANIMATION
        scaleUp.play();
        glowAnimation.play();
        blurTimeline.play();
        fadeOut.play();
    }

    // ========================
    // ANIMATIONS D'ERREUR
    // ========================

    public static void shakeError(Label errorLabel) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    public static void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        shakeError(errorLabel);
    }

    // ========================
    // ANIMATIONS DE CHAMPS
    // ========================

    public static void switchPanels(Node showPane, Node hidePane) {
        if (hidePane.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), hidePane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                hidePane.setVisible(false);
                hidePane.setManaged(false);
            });
            fadeOut.play();
        }

        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> {
            showPane.setVisible(true);
            showPane.setManaged(true);
            showPane.setOpacity(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), showPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        pause.play();
    }

    // ========================
    // ANIMATIONS DE BOUTON
    // ========================

    public static void buttonHoverEffect(Node button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
        st.setToX(1.05);
        st.setToY(1.05);
        st.play();
    }

    public static void buttonExitEffect(Node button) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    // ========================
    // ANIMATIONS DE CHARGEMENT
    // ========================

    public static void startLoadingButton(javafx.scene.control.Button button, String originalText, String loadingText) {
        button.setText(loadingText);
        button.setDisable(true);

        FadeTransition pulse = new FadeTransition(Duration.seconds(0.5), button);
        pulse.setFromValue(1);
        pulse.setToValue(0.6);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();

        button.getProperties().put("loadingAnimation", pulse);
    }

    public static void stopLoadingButton(javafx.scene.control.Button button, String text) {
        Animation anim = (Animation) button.getProperties().get("loadingAnimation");
        if (anim != null) {
            anim.stop();
        }

        button.setText(text);
        button.setDisable(false);
        button.setOpacity(1);
    }
}