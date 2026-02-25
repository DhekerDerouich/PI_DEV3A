package Finance.Controller;

import Finance.model.Revenu;
import Finance.service.revenuService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class RevenuController {

    @FXML private TextField txtMontant;
    @FXML private TextField txtSource;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker datePicker;

    private revenuService revenuService = new revenuService();
    private Revenu revenuToEdit = null;

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());

        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });

        // (lettres uniquement)
        txtSource.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s]*")) {  // Accepte lettres avec accents
                txtSource.setText(oldValue);
            }
        });

        //  (lettres uniquement)
        txtDescription.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-ZÀ-ÿ\\s]*")) {  // Accepte lettres avec accents
                txtDescription.setText(oldValue);
            }
        });
    }

    public void setRevenu(Revenu revenu) {
        this.revenuToEdit = revenu;
        txtMontant.setText(String.valueOf(revenu.getMontant()));
        txtSource.setText(revenu.getSource());
        txtDescription.setText(revenu.getDescription() != null ? revenu.getDescription() : "");
        datePicker.setValue(((java.sql.Date) revenu.getDateRevenu()).toLocalDate());
    }

    @FXML
    private void handleAjouter() {
        try {
            // ✅ VALIDATION DU MONTANT
            String montantText = txtMontant.getText().trim();
            if (montantText.isEmpty()) {
                showError("Le montant est requis");
                txtMontant.requestFocus();
                return;
            }

            double montant = Double.parseDouble(montantText);
            if (montant <= 0) {
                showError("Le montant doit être positif");
                txtMontant.requestFocus();
                return;
            }

            // ✅ VALIDATION DE LA SOURCE
            String source = txtSource.getText().trim();
            if (source.isEmpty()) {
                showError("La source est requise");
                txtSource.requestFocus();
                return;
            }

            // ✅ VÉRIFICATION - SOURCE (lettres uniquement)
            if (!source.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                showError("La source ne doit contenir que des lettres");
                txtSource.requestFocus();
                return;
            }

            // ✅ VALIDATION DE LA DESCRIPTION (optionnelle mais si remplie, doit être valide)
            String description = txtDescription.getText().trim();
            if (!description.isEmpty() && !description.matches("[a-zA-ZÀ-ÿ\\s]+")) {
                showError("La description ne doit contenir que des lettres");
                txtDescription.requestFocus();
                return;
            }

            // ✅ VALIDATION DE LA DATE
            LocalDate localDate = datePicker.getValue();
            if (localDate == null) {
                showError("La date est requise");
                datePicker.requestFocus();
                return;
            }

            Date date = Date.valueOf(localDate);

            if (revenuToEdit != null) {
                // Update existing
                revenuToEdit.setMontant(montant);
                revenuToEdit.setSource(source);
                revenuToEdit.setDescription(description);
                revenuToEdit.setDateRevenu(date);
                revenuService.updateRevenu(revenuToEdit);
            } else {
                // Create new
                Revenu revenu = new Revenu(montant, source, description, date);
                revenuService.ajouterRevenu(revenu);
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showError("Montant invalide. Utilisez un nombre (ex: 1500.00)");
        } catch (SQLException e) {
            showError("Erreur base de données: " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtMontant.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}