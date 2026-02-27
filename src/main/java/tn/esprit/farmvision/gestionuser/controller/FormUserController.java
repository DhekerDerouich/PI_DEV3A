package tn.esprit.farmvision.gestionuser.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.farmvision.gestionuser.model.*;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import javax.sound.sampled.*;
import javafx.application.Platform;

public class FormUserController {

    private final UtilisateurService service = new UtilisateurService();

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtPassword;
    @FXML private TextField txtTelephone, txtAdresse, txtMatricule;
    @FXML private ComboBox<String> cmbRole;
    @FXML private VBox paneAgriculteur, paneResponsable, paneRemarks;
    @FXML private TextArea txtRemarks;
    @FXML private Button btnVoiceRemarks;
    @FXML private Label lblRemarksStatus, lblError;

    private String mode = "AJOUT";
    private Utilisateur currentUser;

    // ✅ Vosk global statique - chargé une seule fois pour toute l'app
    private static Model voskModel;
    private static boolean voskInitialized = false;
    private static boolean voskInitializing = false;

    private Recognizer recognizer;
    private boolean isRecording = false;
    private static final String VOSK_MODEL_PATH = "src/main/resources/vosk-model-fr-0.22";

    @FXML
    private void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList(
                "Administrateur", "Agriculteur", "Responsable d'exploitation"));

        paneAgriculteur.setVisible(false);
        paneAgriculteur.setManaged(false);
        paneResponsable.setVisible(false);
        paneResponsable.setManaged(false);
        paneRemarks.setVisible(false);
        paneRemarks.setManaged(false);

        cmbRole.valueProperty().addListener((obs, old, newVal) -> {
            boolean isAgri = "Agriculteur".equals(newVal);
            boolean isResp = "Responsable d'exploitation".equals(newVal);
            paneAgriculteur.setVisible(isAgri);
            paneAgriculteur.setManaged(isAgri);
            paneResponsable.setVisible(isResp);
            paneResponsable.setManaged(isResp);
        });

        // ✅ Initialiser Vosk seulement si pas déjà fait
        if (!voskInitialized && !voskInitializing) {
            initializeVoskAsync();
        } else if (voskInitialized) {
            Platform.runLater(() -> {
                lblRemarksStatus.setText("✅ Reconnaissance vocale prête");
                lblRemarksStatus.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
            });
        }
    }

    /**
     * ✅ Initialiser Vosk une seule fois (mode asynchrone)
     */
    private void initializeVoskAsync() {
        voskInitializing = true;

        Platform.runLater(() -> {
            lblRemarksStatus.setText("⏳ Initialisation reconnaissance vocale...");
            lblRemarksStatus.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            btnVoiceRemarks.setDisable(true);
        });

        new Thread(() -> {
            try {
                if (voskModel == null) {
                    LibVosk.setLogLevel(LogLevel.WARNINGS);
                    voskModel = new Model(VOSK_MODEL_PATH);
                    voskInitialized = true;
                    System.out.println("✅ Vosk chargé avec succès");
                }

                recognizer = new Recognizer(voskModel, 16000.0f);

                Platform.runLater(() -> {
                    lblRemarksStatus.setText("✅ Reconnaissance vocale prête");
                    lblRemarksStatus.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");
                    btnVoiceRemarks.setDisable(false);
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur Vosk : " + e.getMessage());
                Platform.runLater(() -> {
                    lblRemarksStatus.setText("⚠️ Reconnaissance vocale indisponible");
                    lblRemarksStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px;");
                    btnVoiceRemarks.setDisable(true);

                    // ✅ Tooltip professionnel au lieu d'erreur rouge
                    Tooltip tooltip = new Tooltip("Le modèle vocal n'a pas été trouvé dans:\n" + VOSK_MODEL_PATH + "\n\nVeuillez vérifier le chemin ou télécharger le modèle.");
                    tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #fef3c7; -fx-text-fill: #92400e;");
                    Tooltip.install(btnVoiceRemarks, tooltip);
                });
            } finally {
                voskInitializing = false;
            }
        }).start();
    }

    public void setMode(String mode, Utilisateur user) {
        this.mode = mode;
        this.currentUser = user;

        if ("MODIFICATION".equals(mode) && user != null) {
            loadUser(user);
            paneRemarks.setVisible(true);
            paneRemarks.setManaged(true);
        } else {
            clearForm();
            paneRemarks.setVisible(false);
            paneRemarks.setManaged(false);
        }
    }

    private void loadUser(Utilisateur u) {
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());

        // ✅ Affichage amélioré des remarques
        String remarques = u.getRemarques();
        if (remarques != null && !remarques.isEmpty()) {
            txtRemarks.setText(remarques);
        } else {
            txtRemarks.setPromptText("Aucune remarque pour le moment...");
        }

        if (u instanceof Administrateur) {
            cmbRole.setValue("Administrateur");
        } else if (u instanceof Agriculteur a) {
            cmbRole.setValue("Agriculteur");
            txtTelephone.setText(a.getTelephone());
            txtAdresse.setText(a.getAdresse());
        } else if (u instanceof ResponsableExploitation r) {
            cmbRole.setValue("Responsable d'exploitation");
            txtMatricule.setText(r.getMatricule());
        }
    }

    @FXML
    private void handleSave() {
        lblError.setVisible(false);

        try {
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String password = txtPassword.getText();
            String role = cmbRole.getValue();

            if (nom.isEmpty()) throw new Exception("Le nom est obligatoire");
            if (prenom.isEmpty()) throw new Exception("Le prénom est obligatoire");
            if (email.isEmpty()) throw new Exception("L'email est obligatoire");
            if (!email.contains("@")) throw new Exception("Email invalide");
            if (role == null) throw new Exception("Veuillez sélectionner un rôle");

            if ("AJOUT".equals(mode)) {
                if (password.isEmpty()) throw new Exception("Le mot de passe est obligatoire");
                if (password.length() < 6) throw new Exception("Minimum 6 caractères");
            }

            Utilisateur u;

            if ("Agriculteur".equals(role)) {
                String tel = txtTelephone.getText().trim();
                String adr = txtAdresse.getText().trim();
                if (tel.isEmpty()) throw new Exception("Le téléphone est obligatoire");
                if (tel.length() != 8 || !tel.matches("\\d+"))
                    throw new Exception("Téléphone : 8 chiffres");
                if (adr.isEmpty()) throw new Exception("L'adresse est obligatoire");
                u = new Agriculteur(nom, prenom, email, password, tel, adr);
            } else if ("Responsable d'exploitation".equals(role)) {
                String mat = txtMatricule.getText().trim();
                if (mat.isEmpty()) throw new Exception("Le matricule est obligatoire");
                u = new ResponsableExploitation(nom, prenom, email, password, mat);
            } else {
                u = new Administrateur(nom, prenom, email, password, "ADM-" + System.currentTimeMillis());
            }

            if ("MODIFICATION".equals(mode)) {
                String remarques = txtRemarks.getText().trim();
                u.setRemarques(remarques.isEmpty() ? null : remarques);
            }

            if ("AJOUT".equals(mode)) {
                service.register(u);
                showSuccess("✅ Utilisateur ajouté avec succès !");
                closeAfterDelay(1500);
            } else {
                u.setId(currentUser.getId());
                if (password.isEmpty()) u.setPassword(currentUser.getPassword());
                service.update(u);
                showSuccess("✅ Utilisateur modifié avec succès !");
                closeAfterDelay(1000);
            }
        } catch (Exception e) {
            showError("❌ " + e.getMessage());
        }
    }

    /**
     * ✅ RECONNAISSANCE VOCALE OPTIMISÉE
     */
    @FXML
    private void handleVoiceRemarks() {
        if (isRecording) {
            stopRecording();
            return;
        }

        if (!voskInitialized || recognizer == null) {
            lblRemarksStatus.setText("⏳ Reconnaissance vocale en cours d'initialisation...");
            lblRemarksStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px; -fx-font-weight: bold;");
            return;
        }

        startRecording();
    }

    /**
     * ✅ Démarrer l'enregistrement
     */
    private void startRecording() {
        isRecording = true;
        btnVoiceRemarks.setText("⏹️ Arrêter");
        btnVoiceRemarks.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 130; -fx-pref-height: 120; -fx-background-radius: 10;");

        lblRemarksStatus.setText("🎤 En écoute... Parlez maintenant");
        lblRemarksStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-color: #fee2e2; -fx-background-radius: 6;");

        new Thread(() -> {
            TargetDataLine microphone = null;
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    throw new Exception("Microphone non disponible");
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();

                byte[] buffer = new byte[4096];
                StringBuilder fullText = new StringBuilder();

                String existingText = txtRemarks.getText().trim();
                if (!existingText.isEmpty() && !existingText.equals("Aucune remarque")) {
                    fullText.append(existingText).append(" ");
                }

                while (isRecording) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);

                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String result = recognizer.getResult();
                        String text = extractText(result);

                        if (!text.isEmpty()) {
                            fullText.append(text).append(" ");

                            String currentText = cleanText(fullText.toString());
                            Platform.runLater(() -> {
                                txtRemarks.setText(currentText);
                                txtRemarks.positionCaret(txtRemarks.getLength());

                                // ✅ Aperçu professionnel
                                String preview = currentText.length() > 50 ?
                                        currentText.substring(0, 50) + "..." : currentText;
                                lblRemarksStatus.setText("📝 " + preview);
                                lblRemarksStatus.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px; -fx-font-weight: normal; -fx-padding: 6; -fx-background-color: #eff6ff; -fx-background-radius: 6;");
                            });
                        }
                    }

                    Thread.sleep(50);
                }

                String finalResult = recognizer.getFinalResult();
                String finalText = extractText(finalResult);
                if (!finalText.isEmpty()) {
                    fullText.append(finalText);
                }

                String cleanedText = cleanText(fullText.toString());

                Platform.runLater(() -> {
                    txtRemarks.setText(cleanedText);
                    txtRemarks.positionCaret(txtRemarks.getLength());

                    lblRemarksStatus.setText("✅ Remarque enregistrée avec succès");
                    lblRemarksStatus.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8; -fx-background-color: #d1fae5; -fx-background-radius: 6;");
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblRemarksStatus.setText("⚠️ " + e.getMessage());
                    lblRemarksStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 12px; -fx-padding: 6;");
                });
            } finally {
                if (microphone != null && microphone.isOpen()) {
                    microphone.stop();
                    microphone.close();
                }
                isRecording = false;
                Platform.runLater(() -> {
                    btnVoiceRemarks.setText("🎤 Dictée vocale");
                    btnVoiceRemarks.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 130; -fx-pref-height: 120; -fx-background-radius: 10;");
                });
            }
        }).start();
    }

    /**
     * ✅ Arrêter l'enregistrement
     */
    private void stopRecording() {
        isRecording = false;
        lblRemarksStatus.setText("⏹️ Enregistrement arrêté");
        lblRemarksStatus.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
    }

    /**
     * ✅ Extraire le texte du JSON Vosk
     */
    private String extractText(String json) {
        try {
            if (json.contains("\"text\"")) {
                int start = json.indexOf("\"text\"") + 8;
                int end = json.indexOf("\"", start + 2);
                if (end > start) {
                    return json.substring(start, end).trim();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Parsing JSON échoué");
        }
        return "";
    }

    /**
     * ✅ Nettoyer le texte (amélioration professionnelle)
     */
    private String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Aucune remarque";
        }

        String cleaned = text
                .replaceAll("\\b(euh|hum|bah|ben|voilà|donc|alors|bon|si|test|texte|ok|merci|alt|dot|point)\\b", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\\s+([,.])", "$1")
                .trim();

        if (cleaned.isEmpty() || cleaned.length() < 3) {
            return "Aucune remarque";
        }

        // Capitaliser
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtMatricule.clear();
        txtRemarks.clear();
        cmbRole.setValue(null);
        lblError.setVisible(false);
        lblRemarksStatus.setText("");
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: #7f1d1d; -fx-background-color: #fef2f2; " +
                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; " +
                "-fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 8;");
        lblError.setVisible(true);
    }

    private void showSuccess(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: #065f46; -fx-background-color: #d1fae5; " +
                "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; " +
                "-fx-border-color: #a7f3d0; -fx-border-width: 1; -fx-border-radius: 8;");
        lblError.setVisible(true);
    }

    private void closeAfterDelay(int ms) {
        new Thread(() -> {
            try {
                Thread.sleep(ms);
                Platform.runLater(this::closeWindow);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void closeWindow() {
        if (txtNom != null && txtNom.getScene() != null && txtNom.getScene().getWindow() != null) {
            txtNom.getScene().getWindow().hide();
        }
    }
}