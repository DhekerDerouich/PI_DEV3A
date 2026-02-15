package tn.esprit.farmvision.gestionuser.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.*;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class GestionUsersControllerFX {

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, String> colNom, colPrenom, colEmail, colRole, colDetails, colDateCreation, colPassword;
    @FXML private TableColumn<Utilisateur, Boolean> colActivated;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilterRole;
    @FXML private Button btnAdd, btnEdit, btnDelete, btnValidate;
    @FXML private Label lblMessage;
    @FXML private BorderPane rootPane;

    private final UtilisateurService service = new UtilisateurService();
    private ObservableList<Utilisateur> allData = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> filteredData = FXCollections.observableArrayList();
    private boolean showPendingOnly = false;

    @FXML
    private void initialize() {
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Administrateur)) {
            showMessage("⛔ Accès réservé aux administrateurs", "red");
            return;
        }

        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);
            rootPane.setOnKeyTyped(event ->
                    AnimationManager.handleSecretCode(event.getCharacter(), rootPane));
            rootPane.setFocusTraversable(true);
        }

        setupTableColumns();
        cbFilterRole.setItems(FXCollections.observableArrayList(
                "Tous", "Administrateur", "Agriculteur", "ResponsableExploitation"));
        cbFilterRole.setValue("Tous");

        loadUsers();

        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        cbFilterRole.valueProperty().addListener((obs, old, newVal) -> applyFilters());

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            boolean selected = newSel != null;
            btnEdit.setDisable(!selected);
            btnDelete.setDisable(!selected);
            btnValidate.setDisable(!selected || (selected && newSel.isActivated()));
        });
    }

    public void showPendingAccountsOnly() {
        this.showPendingOnly = true;
        lblMessage.setText("⏳ Affichage des comptes en attente de validation uniquement");
        lblMessage.setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
        applyFilters();
    }

    private void setupTableColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClass().getSimpleName()));

        colDetails.setCellValueFactory(cellData -> {
            Utilisateur u = cellData.getValue();
            String details = "";
            if (u instanceof Agriculteur a) {
                details = "📞 " + (a.getTelephone() != null ? a.getTelephone() : "N/A") +
                        " | 📍 " + (a.getAdresse() != null ? a.getAdresse() : "N/A");
            } else if (u instanceof ResponsableExploitation r) {
                details = "🎫 Matricule: " + (r.getMatricule() != null ? r.getMatricule() : "N/A");
            } else if (u instanceof Administrateur admin) {
                details = "🎫 Matricule: " + (admin.getMatricule() != null ? admin.getMatricule() : "N/A");
            }
            return new SimpleStringProperty(details);
        });

        // ✅ NOUVELLE COLONNE : Mot de passe avec bouton Réinitialiser
        colPassword.setCellFactory(column -> new TableCell<>() {
            private final Button btnReset = new Button("🔄 Réinitialiser");

            {
                btnReset.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; " +
                        "-fx-font-size: 11px; -fx-padding: 5 10; -fx-cursor: hand;");
                btnReset.setOnAction(event -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    handleResetPassword(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(btnReset);
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });

        colActivated.setCellValueFactory(new PropertyValueFactory<>("activated"));
        colActivated.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✅ Oui" : "⏳ Non");
                    setStyle(item ? "-fx-text-fill: #198754; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                }
            }
        });

        colDateCreation.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return new SimpleStringProperty(sdf.format(cellData.getValue().getDateCreation()));
        });
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Réinitialiser le mot de passe
     */
    private void handleResetPassword(Utilisateur user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Réinitialiser mot de passe");
        dialog.setHeaderText("Réinitialiser le mot de passe de " + user.getNomComplet());
        dialog.setContentText("Nouveau mot de passe (min. 6 caractères):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (newPassword.trim().length() < 6) {
                showMessage("❌ Le mot de passe doit contenir au moins 6 caractères", "red");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Confirmer la réinitialisation ?");
            confirm.setContentText("Voulez-vous vraiment réinitialiser le mot de passe de " +
                    user.getNomComplet() + " ?");

            Optional<ButtonType> confirmResult = confirm.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                if (service.resetPassword(user.getId(), newPassword.trim())) {
                    showMessage("✅ Mot de passe réinitialisé avec succès", "green");
                } else {
                    showMessage("❌ Erreur lors de la réinitialisation", "red");
                }
            }
        });
    }

    private void loadUsers() {
        allData.clear();
        List<Utilisateur> users = service.getAll();

        if (users.isEmpty()) {
            showMessage("⚠️ Aucun utilisateur dans la base de données", "orange");
        } else {
            allData.addAll(users);
            if (!showPendingOnly) {
                showMessage("✅ " + users.size() + " utilisateur(s) chargé(s)", "green");
            }
        }
        applyFilters();
    }

    private void applyFilters() {
        String search = txtSearch.getText().toLowerCase().trim();
        String roleFilter = cbFilterRole.getValue();

        filteredData.clear();

        for (Utilisateur u : allData) {
            if (showPendingOnly && u.isActivated()) continue;

            boolean matchSearch = search.isEmpty() ||
                    u.getNom().toLowerCase().contains(search) ||
                    u.getPrenom().toLowerCase().contains(search) ||
                    u.getEmail().toLowerCase().contains(search);

            String userRole = u.getClass().getSimpleName();
            boolean matchRole = "Tous".equals(roleFilter) || userRole.equals(roleFilter);

            if (matchSearch && matchRole) {
                filteredData.add(u);
            }
        }

        tableUsers.setItems(filteredData);

        if (showPendingOnly) {
            showMessage(filteredData.isEmpty() ?
                            "✅ Aucun compte en attente de validation" :
                            "⏳ " + filteredData.size() + " compte(s) en attente de validation",
                    filteredData.isEmpty() ? "green" : "orange");
        } else if (!search.isEmpty() || !"Tous".equals(roleFilter)) {
            showMessage("🔍 Résultats : " + filteredData.size() + " utilisateur(s)", "blue");
        }
    }

    @FXML private void handleSearch() { applyFilters(); }
    @FXML private void handleAddUser() { openForm("AJOUT", null); }

    @FXML
    private void handleEdit() {
        Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Veuillez sélectionner un utilisateur", "orange");
            return;
        }
        openForm("MODIFICATION", selected);
    }

    @FXML
    private void handleDelete() {
        Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Veuillez sélectionner un utilisateur", "orange");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cet utilisateur ?");
        alert.setContentText("Voulez-vous vraiment supprimer " + selected.getNomComplet() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.delete(selected.getId())) {
                showMessage("✅ Utilisateur supprimé avec succès", "green");
                loadUsers();
            } else {
                showMessage("❌ Erreur lors de la suppression", "red");
            }
        }
    }

    @FXML
    private void handleValidate() {
        Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("⚠️ Veuillez sélectionner un utilisateur", "orange");
            return;
        }

        if (selected.isActivated()) {
            showMessage("ℹ️ Cet utilisateur est déjà activé", "blue");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Validation");
        alert.setHeaderText("Valider ce compte ?");
        alert.setContentText("Voulez-vous activer le compte de " + selected.getNomComplet() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.validerUtilisateur(selected.getId())) {
                showMessage("✅ Compte validé avec succès", "green");
                loadUsers();
            } else {
                showMessage("❌ Erreur lors de la validation", "red");
            }
        }
    }

    @FXML
    private void handleRetour() {
        navigateToPage("/fxml/AdminDashboard.fxml", "FarmVision - Dashboard Admin");
    }

    private void openForm(String mode, Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FormUser.fxml"));
            Parent root = loader.load();

            FormUserController controller = loader.getController();
            controller.setMode(mode, user);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(mode.equals("AJOUT") ? "➕ Ajouter un utilisateur" : "✏️ Modifier un utilisateur");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadUsers();
        } catch (IOException e) {
            showMessage("❌ Erreur ouverture formulaire : " + e.getMessage(), "red");
            e.printStackTrace();
        }
    }

    private void navigateToPage(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) tableUsers.getScene().getWindow();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1200,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle(title);

            if (isFullScreen) {
                stage.setFullScreen(true);
            } else if (isMaximized) {
                stage.setMaximized(true);
            }

            root.setOpacity(0);
            AnimationManager.fadeInPage(root);
            stage.show();

        } catch (IOException e) {
            showMessage("❌ Erreur navigation: " + e.getMessage(), "red");
            e.printStackTrace();
        }
    }

    private void showMessage(String message, String color) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
        lblMessage.setVisible(true);
    }
}