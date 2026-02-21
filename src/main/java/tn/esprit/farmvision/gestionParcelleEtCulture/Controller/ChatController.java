package tn.esprit.farmvision.gestionParcelleEtCulture.Controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import tn.esprit.farmvision.gestionParcelleEtCulture.model.Parcelle;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.AIChatService;
import tn.esprit.farmvision.gestionParcelleEtCulture.service.ParcelleService;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatController {

    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private Label weatherSummaryLabel;
    @FXML
    private ComboBox<String> quickQuestionsCombo;
    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox weatherCard; // Add this if you use it in FXML

    private AIChatService aiChatService;
    private ParcelleService parcelleService;
    private List<Parcelle> currentParcelles;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        aiChatService = new AIChatService();
        parcelleService = new ParcelleService();

        setupQuickQuestions();
        loadParcellesData();
        addWelcomeMessage();

        // Configure send button and input field
        sendButton.setOnAction(e -> sendMessage());
        messageInput.setOnAction(e -> sendMessage());

        // Add loading indicator for waiting
        loadingIndicator.setVisible(false);
    }

    private void setupQuickQuestions() {
        if (quickQuestionsCombo != null) {
            quickQuestionsCombo.getItems().addAll(
                    "Quelles cultures recommandez-vous pour la saison actuelle?",
                    "Quand dois-je irriguer mes parcelles?",
                    "Comment protéger mes cultures du gel?",
                    "Quels engrais utiliser avec ces températures?",
                    "Y a-t-il un risque de maladies avec cette météo?",
                    "Comment optimiser ma récolte cette semaine?"
            );

            quickQuestionsCombo.setOnAction(e -> {
                if (quickQuestionsCombo.getValue() != null && !quickQuestionsCombo.getValue().isEmpty()) {
                    messageInput.setText(quickQuestionsCombo.getValue());
                    sendMessage();
                    Platform.runLater(() -> {
                        quickQuestionsCombo.getSelectionModel().clearSelection();
                        quickQuestionsCombo.setValue(null);
                    });
                }
            });

            quickQuestionsCombo.setVisibleRowCount(5);
        }
    }

    private void loadParcellesData() {
        try {
            currentParcelles = parcelleService.afficher();
            updateWeatherSummary();
        } catch (Exception e) {
            showError("Erreur de chargement des données", e.getMessage());
        }
    }

    private void updateWeatherSummary() {
        if (currentParcelles == null || currentParcelles.isEmpty()) {
            weatherSummaryLabel.setText("🌾 Aucune parcelle disponible");
            return;
        }

        double avgTemp = currentParcelles.stream()
                .mapToDouble(Parcelle::getTemperature)
                .average()
                .orElse(0);

        long rainCount = currentParcelles.stream()
                .filter(p -> p.getWeather() != null && p.getWeather().toLowerCase().contains("rain"))
                .count();

        String weatherEmoji = rainCount > 0 ? "🌧️" : "☀️";
        weatherSummaryLabel.setText(String.format(
                "%s %d parcelles | %.1f°C moyenne | %d avec pluie",
                weatherEmoji, currentParcelles.size(), avgTemp, rainCount
        ));
    }

    private void addWelcomeMessage() {
        String welcome = "👋 Bonjour! Je suis votre assistant agricole. " +
                "Je peux vous conseiller sur vos cultures en fonction de la météo actuelle " +
                "et des données de vos parcelles. Comment puis-je vous aider aujourd'hui?";

        addMessage(welcome, false, LocalTime.now());
    }

    @FXML
    private void sendMessage() {
        String userMessage = messageInput.getText().trim();
        if (userMessage.isEmpty()) return;

        LocalTime time = LocalTime.now();
        addMessage(userMessage, true, time);
        messageInput.clear();

        loadingIndicator.setVisible(true);
        sendButton.setDisable(true);

        new Thread(() -> {
            try {
                loadParcellesData();
                String response = aiChatService.getAgriculturalAdvice(userMessage, currentParcelles);
                Platform.runLater(() -> {
                    addMessage(response, false, LocalTime.now());
                    loadingIndicator.setVisible(false);
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addMessage("❌ Désolé, une erreur s'est produite: " + e.getMessage(), false, LocalTime.now());
                    loadingIndicator.setVisible(false);
                    sendButton.setDisable(false);
                });
            }
        }).start();
    }

    private void addMessage(String content, boolean isUser, LocalTime time) {
        HBox messageBox = new HBox(15);
        messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        VBox messageContent = new VBox(8);
        messageContent.setMaxWidth(600);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(isUser ? "👤" : "🤖");
        iconLabel.setStyle(isUser ?
                "-fx-font-size: 16px; -fx-background-color: #dbeafe; -fx-background-radius: 15; -fx-padding: 5 8;" :
                "-fx-font-size: 16px; -fx-background-color: #dcfce7; -fx-background-radius: 15; -fx-padding: 5 8;");

        Label senderLabel = new Label(isUser ? "Vous" : "Assistant Agricole");
        senderLabel.setStyle(isUser ?
                "-fx-font-weight: bold; -fx-text-fill: #2563eb; -fx-font-size: 15px;" :
                "-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-font-size: 15px;");

        Label timeLabel = new Label(time.format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(iconLabel, senderLabel, spacer, timeLabel);

        Text text = new Text(content);
        text.setStyle("-fx-fill: #1e293b; -fx-font-size: 15px;");

        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(15, 20, 15, 20));
        textFlow.setMaxWidth(550);

        if (isUser) {
            textFlow.setStyle("-fx-background-color: #dbeafe; -fx-background-radius: 20 20 5 20; -fx-effect: dropshadow(gaussian, rgba(37,99,235,0.1), 5, 0, 0, 2);");
        } else {
            textFlow.setStyle("-fx-background-color: white; -fx-background-radius: 20 20 20 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 20 20 20 5; -fx-border-width: 1;");
        }

        text.wrappingWidthProperty().bind(textFlow.widthProperty());

        messageContent.getChildren().addAll(header, textFlow);
        messageBox.getChildren().add(messageContent);

        Platform.runLater(() -> {
            messagesContainer.getChildren().add(messageBox);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            if (scrollPane != null) {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(scrollPane.vvalueProperty(), scrollPane.getVvalue())),
                        new KeyFrame(Duration.millis(200), new KeyValue(scrollPane.vvalueProperty(), 1.0, Interpolator.EASE_BOTH))
                );
                timeline.play();
            }
        });
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void clearChat() {
        messagesContainer.getChildren().clear();
        addWelcomeMessage();
    }

    @FXML
    private void refreshWeatherData() {
        loadParcellesData();
        addMessage("🔄 Données météo actualisées", false, LocalTime.now());
    }
}