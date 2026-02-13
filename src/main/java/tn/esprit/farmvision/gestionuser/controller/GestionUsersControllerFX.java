package tn.esprit.farmvision.gestionuser.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.farmvision.SessionManager;
import tn.esprit.farmvision.gestionuser.model.Administrateur;
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.model.ResponsableExploitation;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import tn.esprit.farmvision.gestionuser.service.UtilisateurService;
import tn.esprit.farmvision.gestionuser.util.AnimationManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * 🌾 Gestion des utilisateurs FarmVision
 * ✅ Maintien du plein écran lors de la navigation
 * 🎮 Easter egg activé!
 */
public class GestionUsersControllerFX {

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, Boolean> colActivated;
    @FXML private TableColumn<Utilisateur, String> colDateCreation;
    @FXML private TableColumn<Utilisateur, String> colDetails;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilterRole;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnValidate;
    @FXML private Button btnSearch;
    @FXML private Button btnRetour;
    @FXML private Label lblMessage;
    @FXML private BorderPane rootPane;

    private final UtilisateurService service = new UtilisateurService();
    private ObservableList<Utilisateur> allData = FXCollections.observableArrayList();
    private ObservableList<Utilisateur> filteredData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        System.out.println("🚀 Initialisation de GestionUsersControllerFX...");

        // Vérification admin
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Administrateur)) {
            showMessage("⛔ Accès réservé aux administrateurs", "red");
            return;
        }

        // Animation d'entrée
        if (rootPane != null) {
            AnimationManager.fadeInPage(rootPane);

            // 🎮 ACTIVER L'EASTER EGG
            rootPane.setOnKeyTyped(event -> {
                AnimationManager.handleSecretCode(event.getCharacter(), rootPane);
            });
            rootPane.setFocusTraversable(true);
        }

        // Configuration des colonnes
        setupTableColumns();

        // Initialiser le filtre rôle
        cbFilterRole.setItems(FXCollections.observableArrayList(
                "Tous", "Administrateur", "Agriculteur", "ResponsableExploitation"
        ));
        cbFilterRole.setValue("Tous");

        // Charger les utilisateurs
        loadUsers();

        // Recherche dynamique
        txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        cbFilterRole.valueProperty().addListener((obs, old, newVal) -> applyFilters());

        // Gérer la sélection dans la table
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            boolean selected = newSel != null;
            btnEdit.setDisable(!selected);
            btnDelete.setDisable(!selected);
            btnValidate.setDisable(!selected || (selected && newSel.isActivated()));
        });

        System.out.println("✅ Initialisation terminée");
        System.out.println("🎮 Easter Egg activé! Tapez 'FARM' rapidement!");
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Colonne Rôle
        colRole.setCellValueFactory(cellData -> {
            String role = cellData.getValue().getClass().getSimpleName();
            return new SimpleStringProperty(role);
        });

        // Colonne Détails
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

        // Colonne Activé
        colActivated.setCellValueFactory(new PropertyValueFactory<>("activated"));
        colActivated.setCellFactory(column -> new TableCell<Utilisateur, Boolean>() {
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

        // Colonne Date
        colDateCreation.setCellValueFactory(cellData -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String dateStr = sdf.format(cellData.getValue().getDateCreation());
            return new SimpleStringProperty(dateStr);
        });
    }

    private void loadUsers() {
        System.out.println("📥 Chargement des utilisateurs...");

        allData.clear();
        List<Utilisateur> users = service.getAll();

        System.out.println("📊 Nombre d'utilisateurs récupérés : " + users.size());

        if (users.isEmpty()) {
            showMessage("⚠️ Aucun utilisateur dans la base de données", "orange");
        } else {
            allData.addAll(users);
            showMessage("✅ " + users.size() + " utilisateur(s) chargé(s)", "green");
        }

        applyFilters();
    }

    private void applyFilters() {
        String search = txtSearch.getText().toLowerCase().trim();
        String roleFilter = cbFilterRole.getValue();

        filteredData.clear();

        for (Utilisateur u : allData) {
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

        if (!search.isEmpty() || !"Tous".equals(roleFilter)) {
            showMessage("🔍 Résultats : " + filteredData.size() + " utilisateur(s)", "blue");
        }
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleAddUser() {
        openForm("AJOUT", null);
    }

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

    /**
     * ✅ Retour au dashboard avec maintien du plein écran
     */
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

    /**
     * ✅ Navigation avec maintien du plein écran
     */
    private void navigateToPage(String fxmlPath, String title) {
        try {
            System.out.println("🔄 Navigation vers: " + fxmlPath);

            Stage stage = (Stage) tableUsers.getScene().getWindow();

            // ✅ SAUVEGARDER L'ÉTAT ACTUEL
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            boolean isMaximized = stage.isMaximized();
            boolean isFullScreen = stage.isFullScreen();

            System.out.println("📐 État actuel: Width=" + currentWidth +
                    ", Height=" + currentHeight +
                    ", Maximized=" + isMaximized +
                    ", FullScreen=" + isFullScreen);

            // Charger la nouvelle page
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer la scène avec les bonnes dimensions
            Scene newScene = new Scene(root,
                    currentWidth > 0 ? currentWidth : 1200,
                    currentHeight > 0 ? currentHeight : 700);

            stage.setScene(newScene);
            stage.setTitle(title);

            // ✅ RESTAURER L'ÉTAT
            if (isFullScreen) {
                stage.setFullScreen(true);
                System.out.println("✅ Plein écran restauré");
            } else if (isMaximized) {
                stage.setMaximized(true);
                System.out.println("✅ Maximisation restaurée");
            }

            // Animation d'entrée
            root.setOpacity(0);
            AnimationManager.fadeInPage(root);

            stage.show();

            System.out.println("✅ Navigation réussie");

        } catch (IOException e) {
            showMessage("❌ Erreur navigation: " + e.getMessage(), "red");
            e.printStackTrace();
        }
    }

    private void showMessage(String message, String color) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
        lblMessage.setVisible(true);

        System.out.println("📢 Message : " + message);
    }
}