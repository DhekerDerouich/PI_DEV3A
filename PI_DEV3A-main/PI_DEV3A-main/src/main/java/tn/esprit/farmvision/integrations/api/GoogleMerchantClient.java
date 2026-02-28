package tn.esprit.farmvision.integrations.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import tn.esprit.farmvision.gestionstock.model.Marketplace;
import tn.esprit.farmvision.gestionstock.model.Stock;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GoogleMerchantClient {

    private static final String BASE_URL = "https://merchantapi.googleapis.com/inventories/v1beta";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String accessToken;
    private String merchantId;
    private boolean useMockMode = true; // Mode test sans vraie API

    public GoogleMerchantClient(String accessToken, String merchantId) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.accessToken = accessToken;
        this.merchantId = merchantId;
    }

    /**
     * Mode test (sans appel API réel)
     */
    public void setUseMockMode(boolean useMockMode) {
        this.useMockMode = useMockMode;
    }

    public GoogleInventoryResponse insertLocalInventory(Marketplace annonce, Stock stock, String storeCode) throws Exception {
        if (useMockMode) {
            System.out.println("🔧 [MOCK] Insertion inventaire local:");
            System.out.println("   - Produit: " + stock.getNomProduit());
            System.out.println("   - Prix: " + annonce.getPrixUnitaire() + " DT");
            System.out.println("   - Quantité: " + annonce.getQuantiteEnVente());

            GoogleInventoryResponse response = new GoogleInventoryResponse();
            response.setSuccess(true);
            response.setStatusCode(200);
            response.setMessage("✅ [MOCK] Inventaire ajouté avec succès");
            return response;
        }

        String url = BASE_URL + "/accounts/" + merchantId + "/localInventories:insert";

        ObjectNode inventoryData = objectMapper.createObjectNode();
        inventoryData.put("storeCode", storeCode);
        inventoryData.put("productId", generateProductId(annonce, stock));

        // Disponibilité
        ObjectNode availability = objectMapper.createObjectNode();
        availability.put("availability", mapStatutToGoogle(annonce.getStatut()));
        if ("En vente".equals(annonce.getStatut())) {
            availability.put("availabilityCount", (int) annonce.getQuantiteEnVente());
        }
        inventoryData.set("availability", availability);

        // Prix
        ObjectNode price = objectMapper.createObjectNode();
        price.put("amountMicros", (long) (annonce.getPrixUnitaire() * 1000000));
        price.put("currencyCode", "TND");
        inventoryData.set("price", price);

        // Informations produit
        ObjectNode product = objectMapper.createObjectNode();
        product.put("title", stock.getNomProduit());
        product.put("description", annonce.getDescription() != null ? annonce.getDescription() : "");
        product.put("brand", "FarmVision");
        product.put("gtin", generateGTIN(annonce.getIdMarketplace()));
        product.put("mpn", "FV-" + annonce.getIdMarketplace());

        ArrayNode categories = objectMapper.createArrayNode();
        categories.add(getGoogleCategoryId(stock.getTypeProduit()));
        product.set("productTypes", categories);

        inventoryData.set("product", product);

        // Date d'expiration
        if (stock.getDateExpiration() != null) {
            inventoryData.put("expirationDate", stock.getDateExpiration().toString());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(inventoryData.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        GoogleInventoryResponse result = new GoogleInventoryResponse();
        result.setSuccess(response.statusCode() == 200 || response.statusCode() == 201);
        result.setStatusCode(response.statusCode());

        if (result.isSuccess()) {
            result.setMessage("✅ Inventaire ajouté avec succès sur Google Merchant");
            result.setResponseBody(response.body());
        } else {
            result.setMessage("❌ Erreur Google Merchant: " + response.statusCode());
            result.setErrorBody(response.body());
            System.err.println("Erreur Google Merchant: " + response.body());
        }

        return result;
    }

    public GoogleInventoryResponse insertRegionalInventory(Marketplace annonce, Stock stock, List<String> regions) throws Exception {
        if (useMockMode) {
            System.out.println("🔧 [MOCK] Insertion inventaire régional:");
            System.out.println("   - Produit: " + stock.getNomProduit());
            System.out.println("   - Régions: " + regions);

            GoogleInventoryResponse response = new GoogleInventoryResponse();
            response.setSuccess(true);
            response.setStatusCode(200);
            response.setMessage("✅ [MOCK] Inventaire régional ajouté");
            return response;
        }

        String url = BASE_URL + "/accounts/" + merchantId + "/regionalInventories:insert";

        ObjectNode inventoryData = objectMapper.createObjectNode();
        inventoryData.put("productId", generateProductId(annonce, stock));

        // Régions
        ArrayNode regionsArray = objectMapper.createArrayNode();
        for (String region : regions) {
            regionsArray.add(region);
        }
        inventoryData.set("regions", regionsArray);

        // Disponibilité
        ObjectNode availability = objectMapper.createObjectNode();
        availability.put("availability", mapStatutToGoogle(annonce.getStatut()));
        if ("En vente".equals(annonce.getStatut())) {
            availability.put("availabilityCount", (int) annonce.getQuantiteEnVente());
        }
        inventoryData.set("availability", availability);

        // Prix
        ObjectNode price = objectMapper.createObjectNode();
        price.put("amountMicros", (long) (annonce.getPrixUnitaire() * 1000000));
        price.put("currencyCode", "TND");
        inventoryData.set("price", price);

        // Dates de validité
        ObjectNode validity = objectMapper.createObjectNode();
        validity.put("startDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        if (stock.getDateExpiration() != null) {
            validity.put("endDateTime", stock.getDateExpiration().atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        inventoryData.set("validity", validity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(inventoryData.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        GoogleInventoryResponse result = new GoogleInventoryResponse();
        result.setSuccess(response.statusCode() == 200 || response.statusCode() == 201);
        result.setStatusCode(response.statusCode());

        if (result.isSuccess()) {
            result.setMessage("✅ Inventaire régional ajouté avec succès");
        } else {
            result.setMessage("❌ Erreur: " + response.statusCode());
            System.err.println(response.body());
        }

        return result;
    }

    public SyncResult synchronizeAllMarketplaces(List<Marketplace> annonces, List<Stock> stocks) {
        SyncResult result = new SyncResult();
        result.setTotalItems(annonces.size());

        for (Marketplace annonce : annonces) {
            try {
                // Trouver le stock associé
                Stock stockAssocie = stocks.stream()
                        .filter(s -> s.getIdStock() == annonce.getIdStock())
                        .findFirst()
                        .orElse(null);

                if (stockAssocie == null) {
                    result.incrementFailed();
                    result.addError("Stock non trouvé pour annonce ID: " + annonce.getIdMarketplace());
                    continue;
                }

                // Ajouter à Google Merchant
                GoogleInventoryResponse response = insertLocalInventory(
                        annonce,
                        stockAssocie,
                        "ferme-principale"
                );

                if (response.isSuccess()) {
                    result.incrementSuccess();
                } else {
                    result.incrementFailed();
                    result.addError("Échec annonce " + annonce.getIdMarketplace() + ": " + response.getMessage());
                }

            } catch (Exception e) {
                result.incrementFailed();
                result.addError("Exception: " + e.getMessage());
            }
        }

        result.complete();
        result.setMessage(String.format(
                "Synchronisation terminée: %d succès, %d échecs",
                result.getSuccessCount(), result.getFailedCount()
        ));

        return result;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String generateProductId(Marketplace annonce, Stock stock) {
        return String.format("fv-marketplace-%d-stock-%d",
                annonce.getIdMarketplace(),
                stock.getIdStock()
        );
    }

    private String mapStatutToGoogle(String statut) {
        switch (statut) {
            case "En vente": return "IN_STOCK";
            case "Réservé": return "RESERVED";
            case "Vendu": return "OUT_OF_STOCK";
            default: return "OUT_OF_STOCK";
        }
    }

    private String generateGTIN(int id) {
        return String.format("0%011d", id);
    }

    private int getGoogleCategoryId(String type) {
        switch (type != null ? type : "") {
            case "Légumes": return 487;
            case "Fruits": return 486;
            case "Céréales": return 495;
            case "Produits laitiers": return 496;
            case "Viandes": return 497;
            case "Volailles": return 498;
            case "Œufs": return 499;
            default: return 500;
        }
    }

    // ==================== CLASSES DE DONNÉES ====================

    public static class GoogleInventoryResponse {
        private boolean success;
        private int statusCode;
        private String message;
        private String responseBody;
        private String errorBody;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getResponseBody() { return responseBody; }
        public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
        public String getErrorBody() { return errorBody; }
        public void setErrorBody(String errorBody) { this.errorBody = errorBody; }
    }

    public static class SyncResult {
        private int totalItems;
        private int successCount;
        private int failedCount;
        private List<String> errors = new ArrayList<>();
        private String message;

        public void incrementSuccess() { successCount++; }
        public void incrementFailed() { failedCount++; }
        public void addError(String error) { errors.add(error); }
        public void complete() { this.totalItems = successCount + failedCount; }

        public int getTotalItems() { return totalItems; }
        public void setTotalItems(int total) { this.totalItems = total; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int count) { this.successCount = count; }
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int count) { this.failedCount = count; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public double getSuccessRate() {
            return totalItems > 0 ? (successCount * 100.0 / totalItems) : 0;
        }
    }
}