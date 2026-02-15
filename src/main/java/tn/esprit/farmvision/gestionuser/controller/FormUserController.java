package tn.esprit.farmvision.gestionuser.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tn.esprit.farmvision.gestionuser.model.*;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;

public class FormUserController {

    private final UtilisateurService service = new UtilisateurService();

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtPassword;
    @FXML private TextField txtTelephone, txtAdresse, txtMatricule;
    @FXML private ComboBox<String> cmbRole;
    @FXML private VBox paneAgriculteur, paneResponsable;
    @FXML private Label lblError;

    private String mode = "AJOUT";
    private Utilisateur currentUser;

    @FXML
    private void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList(
                "Administrateur", "Agriculteur", "Responsable d'exploitation"));

        paneAgriculteur.setVisible(false);
        paneAgriculteur.setManaged(false);
        paneResponsable.setVisible(false);
        paneResponsable.setManaged(false);

        cmbRole.valueProperty().addListener((obs, old, newVal) -> {
            boolean isAgri = "Agriculteur".equals(newVal);
            boolean isResp = "Responsable d'exploitation".equals(newVal);
            paneAgriculteur.setVisible(isAgri);
            paneAgriculteur.setManaged(isAgri);
            paneResponsable.setVisible(isResp);
            paneResponsable.setManaged(isResp);
        });
    }

    public void setMode(String mode, Utilisateur user) {
        this.mode = mode;
        this.currentUser = user;
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

    @FXML private void handleCancel() { closeWindow(); }

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
        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #dc3545; " +
                "-fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        lblError.setVisible(true);
    }

    private void showSuccess(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: white; -fx-background-color: #198754; " +
                "-fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        lblError.setVisible(true);
    }

    private void closeAfterDelay(int ms) {
        new Thread(() -> {
            try {
                Thread.sleep(ms);
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