package tn.esprit.farmvision.integrations.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tn.esprit.farmvision.gestionstock.model.Stock;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Client pour l'API InvenTree - Système de gestion de stock open-source
 * Documentation: https://docs.inventree.org/en/stable/api/
 */
public class InvenTreeClient {

    private static final String BASE_URL = "http://localhost:8000/api"; // URL par défaut d'InvenTree
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String apiToken;

    public InvenTreeClient(String apiToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiToken = apiToken;
    }

    /**
     * Authentification et récupération du token
     */
    public boolean authenticate(String username, String password) throws Exception {
        String url = BASE_URL + "/auth/login/";

        ObjectNode credentials = objectMapper.createObjectNode();
        credentials.put("username", username);
        credentials.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(credentials.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            this.apiToken = root.get("token").asText();
            System.out.println("✅ Authentification InvenTree réussie");
            return true;
        } else {
            System.err.println("❌ Erreur authentification InvenTree: " + response.statusCode());
            return false;
        }
    }

    /**
     * Récupère toutes les pièces (stocks) d'InvenTree
     */
    public List<InvenTreePart> getAllParts() throws Exception {
        String url = BASE_URL + "/part/";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + apiToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parsePartsResponse(response.body());
        } else {
            System.err.println("❌ Erreur récupération pièces: " + response.statusCode());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère une pièce par son ID
     */
    public InvenTreePart getPartById(int partId) throws Exception {
        String url = BASE_URL + "/part/" + partId + "/";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + apiToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parsePartResponse(response.body());
        } else {
            System.err.println("❌ Pièce non trouvée: " + partId);
            return null;
        }
    }

    /**
     * Crée une nouvelle pièce dans InvenTree à partir d'un stock FarmVision
     */
    public InvenTreePart createPartFromStock(Stock stock) throws Exception {
        String url = BASE_URL + "/part/";

        ObjectNode partData = objectMapper.createObjectNode();
        partData.put("name", stock.getNomProduit());
        partData.put("description", "Produit agricole: " + stock.getTypeProduit());
        partData.put("category", getCategoryIdFromType(stock.getTypeProduit()));
        partData.put("units", stock.getUnite());
        partData.put("minimum_stock", 10);
        partData.put("active", true);
        partData.put("virtual", false);
        partData.put("trackable", true);

        if (stock.getDateExpiration() != null) {
            partData.put("expiry_date", stock.getDateExpiration().toString());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + apiToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(partData.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            System.out.println("✅ Pièce créée dans InvenTree");
            return parsePartResponse(response.body());
        } else {
            System.err.println("❌ Erreur création pièce: " + response.statusCode());
            System.err.println(response.body());
            return null;
        }
    }

    /**
     * Met à jour le stock d'une pièce
     */
    public boolean updatePartStock(int partId, double newQuantity) throws Exception {
        String url = BASE_URL + "/part/" + partId + "/";

        ObjectNode updateData = objectMapper.createObjectNode();
        updateData.put("quantity", newQuantity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + apiToken)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(updateData.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            System.out.println("✅ Stock mis à jour dans InvenTree");
            return true;
        } else {
            System.err.println("❌ Erreur mise à jour stock: " + response.statusCode());
            return false;
        }
    }

    /**
     * Récupère l'historique d'une pièce
     */
    public List<StockHistory> getPartHistory(int partId) throws Exception {
        String url = BASE_URL + "/part/" + partId + "/stock/";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + apiToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return parseHistoryResponse(response.body());
        } else {
            System.err.println("❌ Erreur récupération historique: " + response.statusCode());
            return new ArrayList<>();
        }
    }

    /**
     * Synchronise tous les stocks FarmVision vers InvenTree
     */
    public SyncResult synchronizeAllStocks(List<Stock> farmVisionStocks) {
        SyncResult result = new SyncResult();
        result.setTotalStocks(farmVisionStocks.size());

        try {
            // Récupérer toutes les pièces existantes dans InvenTree
            List<InvenTreePart> existingParts = getAllParts();

            for (Stock stock : farmVisionStocks) {
                boolean found = false;

                // Chercher si la pièce existe déjà
                for (InvenTreePart part : existingParts) {
                    if (part.getName().equalsIgnoreCase(stock.getNomProduit())) {
                        // Mise à jour du stock existant
                        updatePartStock(part.getId(), stock.getQuantite());
                        result.incrementUpdated();
                        found = true;
                        break;
                    }
                }

                // Si la pièce n'existe pas, la créer
                if (!found) {
                    InvenTreePart newPart = createPartFromStock(stock);
                    if (newPart != null) {
                        result.incrementCreated();
                    } else {
                        result.incrementFailed();
                    }
                }
            }

            result.setSuccess(true);
            result.setMessage("✅ Synchronisation terminée");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("❌ Erreur synchronisation: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Convertit le type de produit en ID de catégorie InvenTree
     */
    private int getCategoryIdFromType(String type) {
        // Mapping des types vers des catégories InvenTree
        switch (type) {
            case "Légumes": return 1;
            case "Fruits": return 2;
            case "Céréales": return 3;
            case "Légumineuses": return 4;
            case "Produits laitiers": return 5;
            case "Viandes": return 6;
            case "Volailles": return 7;
            case "Œufs": return 8;
            default: return 9; // Autre
        }
    }

    // ==================== PARSING METHODS ====================

    private List<InvenTreePart> parsePartsResponse(String json) throws Exception {
        List<InvenTreePart> parts = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        if (root.isArray()) {
            for (JsonNode node : root) {
                parts.add(parsePartFromNode(node));
            }
        }

        return parts;
    }

    private InvenTreePart parsePartResponse(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        return parsePartFromNode(node);
    }

    private InvenTreePart parsePartFromNode(JsonNode node) {
        InvenTreePart part = new InvenTreePart();

        part.setId(node.has("pk") ? node.get("pk").asInt() : node.get("id").asInt());
        part.setName(node.has("name") ? node.get("name").asText() : "");
        part.setDescription(node.has("description") ? node.get("description").asText() : "");
        part.setCategory(node.has("category") ? node.get("category").asInt() : 0);
        part.setCategoryName(node.has("category_name") ? node.get("category_name").asText() : "");
        part.setQuantity(node.has("quantity") ? node.get("quantity").asDouble() : 0);
        part.setUnits(node.has("units") ? node.get("units").asText() : "");
        part.setMinimumStock(node.has("minimum_stock") ? node.get("minimum_stock").asDouble() : 0);
        part.setActive(node.has("active") && node.get("active").asBoolean());
        part.setTrackable(node.has("trackable") && node.get("trackable").asBoolean());

        if (node.has("expiry_date") && !node.get("expiry_date").isNull()) {
            part.setExpiryDate(LocalDate.parse(node.get("expiry_date").asText()));
        }

        return part;
    }

    private List<StockHistory> parseHistoryResponse(String json) throws Exception {
        List<StockHistory> history = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        if (root.isArray()) {
            for (JsonNode node : root) {
                StockHistory entry = new StockHistory();
                entry.setId(node.has("pk") ? node.get("pk").asInt() : 0);
                entry.setPartId(node.has("part") ? node.get("part").asInt() : 0);
                entry.setQuantity(node.has("quantity") ? node.get("quantity").asDouble() : 0);
                entry.setDate(node.has("date") ? LocalDate.parse(node.get("date").asText()) : LocalDate.now());
                entry.setAction(node.has("action") ? node.get("action").asText() : "");
                entry.setNotes(node.has("notes") ? node.get("notes").asText() : "");
                history.add(entry);
            }
        }

        return history;
    }

    // ==================== CLASSES DE DONNÉES ====================

    public static class InvenTreePart {
        private int id;
        private String name;
        private String description;
        private int category;
        private String categoryName;
        private double quantity;
        private String units;
        private double minimumStock;
        private boolean active;
        private boolean trackable;
        private LocalDate expiryDate;

        // Getters et setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getCategory() { return category; }
        public void setCategory(int category) { this.category = category; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }

        public String getUnits() { return units; }
        public void setUnits(String units) { this.units = units; }

        public double getMinimumStock() { return minimumStock; }
        public void setMinimumStock(double minimumStock) { this.minimumStock = minimumStock; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public boolean isTrackable() { return trackable; }
        public void setTrackable(boolean trackable) { this.trackable = trackable; }

        public LocalDate getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

        @Override
        public String toString() {
            return name + " (ID: " + id + ", Stock: " + quantity + " " + units + ")";
        }
    }

    public static class StockHistory {
        private int id;
        private int partId;
        private double quantity;
        private LocalDate date;
        private String action;
        private String notes;

        // Getters et setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getPartId() { return partId; }
        public void setPartId(int partId) { this.partId = partId; }

        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class SyncResult {
        private boolean success;
        private int totalStocks;
        private int created;
        private int updated;
        private int failed;
        private String message;

        public void incrementCreated() { created++; }
        public void incrementUpdated() { updated++; }
        public void incrementFailed() { failed++; }

        // Getters et setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getTotalStocks() { return totalStocks; }
        public void setTotalStocks(int total) { this.totalStocks = total; }

        public int getCreated() { return created; }
        public void setCreated(int created) { this.created = created; }

        public int getUpdated() { return updated; }
        public void setUpdated(int updated) { this.updated = updated; }

        public int getFailed() { return failed; }
        public void setFailed(int failed) { this.failed = failed; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        @Override
        public String toString() {
            return String.format("SyncResult: %s, Total: %d, Créés: %d, Mis à jour: %d, Échoués: %d",
                    success ? "✅ SUCCÈS" : "❌ ÉCHEC", totalStocks, created, updated, failed);
        }
    }
}