package tn.esprit.farmvision.gestionuser.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.farmvision.gestionuser.model.*;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;

/**
 * 🌾 Formulaire d'ajout/modification d'utilisateur FarmVision
 * ✅ Validation améliorée et feedback utilisateur
 */
public class FormUserController {

    private final UtilisateurService service = new UtilisateurService();

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtPassword;
    @FXML private TextField txtTelephone, txtAdresse, txtMatricule;
    @FXML private ComboBox<String> cmbRole;
    @FXML private VBox paneAgriculteur, paneResponsable;
    @FXML private Label lblError;
    @FXML private Button btnSave, btnCancel;

    private String mode = "AJOUT";
    private Utilisateur currentUser;

    @FXML
    private void initialize() {
        System.out.println("📋 Initialisation FormUserController");

        // Initialiser le combobox
        cmbRole.setItems(FXCollections.observableArrayList(
                "Administrateur",
                "Agriculteur",
                "Responsable d'exploitation"
        ));

        // Masquer les panneaux spécifiques au départ
        paneAgriculteur.setVisible(false);
        paneAgriculteur.setManaged(false);
        paneResponsable.setVisible(false);
        paneResponsable.setManaged(false);

        // Dynamique selon rôle sélectionné
        cmbRole.valueProperty().addListener((obs, old, newVal) -> {
            boolean isAgri = "Agriculteur".equals(newVal);
            boolean isResp = "Responsable d'exploitation".equals(newVal);

            paneAgriculteur.setVisible(isAgri);
            paneAgriculteur.setManaged(isAgri);

            paneResponsable.setVisible(isResp);
            paneResponsable.setManaged(isResp);

            System.out.println("✅ Rôle sélectionné : " + newVal);
        });

        System.out.println("✅ FormUserController initialisé avec succès");
    }

    public void setMode(String mode, Utilisateur user) {
        this.mode = mode;
        this.currentUser = user;

        System.out.println("🔧 Mode : " + mode);

        if ("MODIFICATION".equals(mode) && user != null) {
            loadUser(user);
        } else {
            clearForm();
        }
    }

    private void loadUser(Utilisateur u) {
        txtNom.setText(u.getNom());
        txtPrenom.setText(u.getPrenom());
        txtEmail.setText(u.getEmail());

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

        System.out.println("📝 Utilisateur chargé : " + u.getNomComplet());
    }

    @FXML
    private void handleSave() {
        lblError.setVisible(false);

        try {
            // Récupération et validation des champs
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String password = txtPassword.getText();
            String role = cmbRole.getValue();

            // Validations basiques
            if (nom.isEmpty()) {
                throw new Exception("Le nom est obligatoire");
            }
            if (prenom.isEmpty()) {
                throw new Exception("Le prénom est obligatoire");
            }
            if (email.isEmpty()) {
                throw new Exception("L'email est obligatoire");
            }
            if (!email.contains("@")) {
                throw new Exception("Email invalide (doit contenir @)");
            }
            if (role == null || role.isEmpty()) {
                throw new Exception("Veuillez sélectionner un rôle");
            }

            // Validation password selon le mode
            if ("AJOUT".equals(mode)) {
                if (password.isEmpty()) {
                    throw new Exception("Le mot de passe est obligatoire en mode ajout");
                }
                if (password.length() < 6) {
                    throw new Exception("Le mot de passe doit contenir au moins 6 caractères");
                }
            }

            // Création de l'utilisateur selon le rôle
            Utilisateur u;

            if ("Agriculteur".equals(role)) {
                String tel = txtTelephone.getText().trim();
                String adr = txtAdresse.getText().trim();

                if (tel.isEmpty()) {
                    throw new Exception("Le téléphone est obligatoire pour les agriculteurs");
                }
                if (tel.length() != 8 || !tel.matches("\\d+")) {
                    throw new Exception("Le téléphone doit contenir exactement 8 chiffres");
                }
                if (adr.isEmpty()) {
                    throw new Exception("L'adresse est obligatoire pour les agriculteurs");
                }

                u = new Agriculteur(nom, prenom, email, password, tel, adr);

            } else if ("Responsable d'exploitation".equals(role)) {
                String mat = txtMatricule.getText().trim();

                if (mat.isEmpty()) {
                    throw new Exception("Le matricule est obligatoire pour les responsables");
                }

                u = new ResponsableExploitation(nom, prenom, email, password, mat);

            } else { // Administrateur
                String matriculeAdmin = "ADM-" + System.currentTimeMillis();
                u = new Administrateur(nom, prenom, email, password, matriculeAdmin);
            }

            // Enregistrement selon le mode
            if ("AJOUT".equals(mode)) {
                System.out.println("💾 Ajout d'un nouvel utilisateur...");
                service.register(u);
                showSuccess("✅ Utilisateur ajouté avec succès !");

                closeAfterDelay(1500);

            } else { // MODIFICATION
                System.out.println("🔄 Modification de l'utilisateur...");
                u.setId(currentUser.getId());

                // Si le password est vide en modification, garder l'ancien
                if (password.isEmpty()) {
                    u.setPassword(currentUser.getPassword());
                    System.out.println("ℹ️ Mot de passe inchangé");
                }

                service.update(u);
                showSuccess("✅ Utilisateur modifié avec succès !");

                closeAfterDelay(1000);
            }

        } catch (Exception e) {
            showError("❌ " + e.getMessage());
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        System.out.println("🚫 Annulation");
        closeWindow();
    }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        txtMatricule.clear();
        cmbRole.setValue(null);
        lblError.setVisible(false);
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #dc3545; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        lblError.setVisible(true);
    }

    private void showSuccess(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #198754; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        lblError.setVisible(true);
    }

    private void closeAfterDelay(int milliseconds) {
        new Thread(() -> {
            try {
                Thread.sleep(milliseconds);
                javafx.application.Platform.runLater(this::closeWindow);
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