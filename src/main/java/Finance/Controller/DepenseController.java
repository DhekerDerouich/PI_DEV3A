package Finance.Controller;

import Finance.model.Depense;
import Finance.service.depenseService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class DepenseController {

    @FXML private TextField txtMontant;
    @FXML private TextField txtType;
    @FXML private TextArea txtDescription;  // Changed from TextField to TextArea
    @FXML private DatePicker datePicker;

    private depenseService depenseService = new depenseService();
    private Depense depenseToEdit = null; // For edit mode

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
    }

    // Call this to set data for editing
    public void setDepense(Depense depense) {
        this.depenseToEdit = depense;
        txtMontant.setText(String.valueOf(depense.getMontant()));
        txtType.setText(depense.getTypeDepense());
        txtDescription.setText(depense.getDescription() != null ? depense.getDescription() : "");
        datePicker.setValue(((java.sql.Date) depense.getDateDepense()).toLocalDate());
    }

    @FXML
    private void handleAjouter() {
        try {
            // Validate montant
            String montantText = txtMontant.getText().trim();
            if (montantText.isEmpty()) {
                showError("Le montant est requis");
                return;
            }

            double montant = Double.parseDouble(montantText);
            if (montant <= 0) {
                showError("Le montant doit être positif");
                return;
            }

            // Validate type
            String type = txtType.getText().trim();
            if (type.isEmpty()) {
                showError("Le type de dépense est requis");
                return;
            }

            // Get description (optional)
            String description = txtDescription.getText().trim();

            // Validate date
            LocalDate localDate = datePicker.getValue();
            if (localDate == null) {
                showError("La date est requise");
                return;
            }

            Date date = Date.valueOf(localDate);

            if (depenseToEdit != null) {
                // Update existing
                depenseToEdit.setMontant(montant);
                depenseToEdit.setTypeDepense(type);
                depenseToEdit.setDescription(description);
                depenseToEdit.setDateDepense(date);
                depenseService.updateDepense(depenseToEdit);
            } else {
                // Create new
                Depense depense = new Depense(montant, type, description, date);
                depenseService.ajouterDepense(depense);
            }

            closeWindow();

        } catch (NumberFormatException e) {
            showError("Montant invalide. Utilisez un nombre (ex: 500.00)");
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
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}