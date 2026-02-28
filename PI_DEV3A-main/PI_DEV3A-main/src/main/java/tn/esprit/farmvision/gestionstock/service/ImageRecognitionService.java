package tn.esprit.farmvision.gestionstock.service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class ImageRecognitionService {

    private Map<String, String> productCategories;
    private List<String> keywords;
    private Map<String, String[]> productKeywords;

    public ImageRecognitionService() {
        initProductCategories();
        initKeywords();
        loadLabels();
    }

    private void initProductCategories() {
        productCategories = new HashMap<>();
        productCategories.put("tomate", "Légumes");
        productCategories.put("pomme", "Fruits");
        productCategories.put("carotte", "Légumes");
        productCategories.put("pomme_de_terre", "Légumes");
        productCategories.put("oignon", "Légumes");
        productCategories.put("salade", "Légumes");
        productCategories.put("concombre", "Légumes");
        productCategories.put("poivron", "Légumes");
        productCategories.put("aubergine", "Légumes");
        productCategories.put("courgette", "Légumes");
        productCategories.put("fraise", "Fruits");
        productCategories.put("raisin", "Fruits");
        productCategories.put("orange", "Fruits");
        productCategories.put("citron", "Fruits");
        productCategories.put("banane", "Fruits");
        productCategories.put("ble", "Céréales");
        productCategories.put("mais", "Céréales");
        productCategories.put("orge", "Céréales");
        productCategories.put("lait", "Produits laitiers");
        productCategories.put("fromage", "Produits laitiers");
        productCategories.put("oeuf", "Œufs");
        productCategories.put("poulet", "Volailles");
        productCategories.put("viande", "Viandes");
        productCategories.put("poisson", "Poissons");
    }

    private void initKeywords() {
        productKeywords = new HashMap<>();
        productKeywords.put("tomate", new String[]{"tomate", "tomatoes", "tomato"});
        productKeywords.put("pomme", new String[]{"pomme", "apple", "apples"});
        productKeywords.put("carotte", new String[]{"carotte", "carrot", "carrots"});
        productKeywords.put("pomme_de_terre", new String[]{"pomme de terre", "potato", "potatoes", "patate"});
        productKeywords.put("oignon", new String[]{"oignon", "onion", "onions"});
        productKeywords.put("salade", new String[]{"salade", "lettuce", "laitue"});
        productKeywords.put("concombre", new String[]{"concombre", "cucumber"});
        productKeywords.put("poivron", new String[]{"poivron", "pepper", "bell pepper"});
        productKeywords.put("aubergine", new String[]{"aubergine", "eggplant"});
        productKeywords.put("courgette", new String[]{"courgette", "zucchini"});
        productKeywords.put("fraise", new String[]{"fraise", "strawberry", "strawberries"});
        productKeywords.put("raisin", new String[]{"raisin", "grape", "grapes"});
        productKeywords.put("orange", new String[]{"orange", "oranges"});
        productKeywords.put("citron", new String[]{"citron", "lemon", "lemons"});
        productKeywords.put("banane", new String[]{"banane", "banana", "bananas"});
        productKeywords.put("ble", new String[]{"ble", "blé", "wheat"});
        productKeywords.put("mais", new String[]{"mais", "maïs", "corn", "maize"});
        productKeywords.put("lait", new String[]{"lait", "milk"});
        productKeywords.put("fromage", new String[]{"fromage", "cheese"});
        productKeywords.put("oeuf", new String[]{"oeuf", "oeufs", "egg", "eggs"});
        productKeywords.put("poulet", new String[]{"poulet", "chicken"});
        productKeywords.put("viande", new String[]{"viande", "meat", "beef", "veau"});
        productKeywords.put("poisson", new String[]{"poisson", "fish"});
    }

    private void loadLabels() {
        keywords = new ArrayList<>(productCategories.keySet());
    }

    public RecognitionResult recognizeProduct(File imageFile) {
        try {
            System.out.println("\n=== RECONNAISSANCE PRODUIT ===");
            System.out.println("📸 Analyse de l'image: " + imageFile.getName());

            // Lire l'image pour vérification
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                return fallbackRecognition(imageFile.getName());
            }

            System.out.println("✅ Image chargée: " + img.getWidth() + "x" + img.getHeight());

            // Mode fallback - reconnaissance basique par nom de fichier
            return fallbackRecognition(imageFile.getName());

        } catch (Exception e) {
            System.err.println("❌ Erreur reconnaissance: " + e.getMessage());
            e.printStackTrace();
            return fallbackRecognition(imageFile != null ? imageFile.getName() : "inconnu");
        }
    }

    private RecognitionResult fallbackRecognition(String fileName) {
        System.out.println("ℹ️ Analyse du nom de fichier: " + fileName);

        String fileNameLower = fileName.toLowerCase();

        // Chercher une correspondance
        for (Map.Entry<String, String[]> entry : productKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (fileNameLower.contains(keyword)) {
                    String productKey = entry.getKey();
                    String productName = formatProductName(productKey);
                    String category = productCategories.getOrDefault(productKey, "Autre");
                    float confidence = calculateConfidence(fileNameLower, keyword);

                    System.out.printf("✅ Produit détecté: %s (confiance: %.1f%%)%n", productName, confidence);
                    System.out.printf("🏷️ Catégorie: %s%n", category);

                    return new RecognitionResult(productKey, productName, category, confidence, false);
                }
            }
        }

        // Pas de correspondance
        System.out.println("⚠️ Aucun produit reconnu");
        return new RecognitionResult("inconnu", "Inconnu", "Autre", 0, false);
    }

    private float calculateConfidence(String fileName, String keyword) {
        // Calcul simple de confiance basé sur la longueur du mot-clé
        float baseConfidence = 70.0f;

        // Bonus si le mot-clé est au début du nom
        if (fileName.startsWith(keyword)) {
            baseConfidence += 15;
        }

        // Bonus si le mot-clé est exact (pas de lettres supplémentaires)
        if (fileName.equals(keyword) || fileName.equals(keyword + ".jpg") || fileName.equals(keyword + ".png")) {
            baseConfidence += 15;
        }

        return Math.min(baseConfidence, 99.0f);
    }

    private String formatProductName(String productKey) {
        String[] words = productKey.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }

    public List<String> suggestUnits(String productName) {
        List<String> suggestions = new ArrayList<>();
        String productLower = productName.toLowerCase();

        if (productLower.contains("lait") || productLower.contains("jus")) {
            suggestions.add("L");
            suggestions.add("ml");
        } else if (productLower.contains("oeuf") || productLower.contains("œuf")) {
            suggestions.add("douzaine");
            suggestions.add("pièce");
        } else if (productLower.contains("fromage") || productLower.contains("viande") ||
                productLower.contains("poisson") || productLower.contains("poulet")) {
            suggestions.add("kg");
            suggestions.add("g");
        } else if (productLower.contains("tomate") || productLower.contains("pomme") ||
                productLower.contains("carotte") || productLower.contains("oignon") ||
                productLower.contains("salade") || productLower.contains("concombre") ||
                productLower.contains("poivron") || productLower.contains("aubergine") ||
                productLower.contains("courgette") || productLower.contains("fraise") ||
                productLower.contains("raisin") || productLower.contains("orange") ||
                productLower.contains("citron") || productLower.contains("banane")) {
            suggestions.add("kg");
            suggestions.add("g");
            suggestions.add("pièce");
        } else {
            suggestions.add("kg");
            suggestions.add("L");
            suggestions.add("pièce");
        }

        return suggestions;
    }

    public static class RecognitionResult {
        private String productKey;
        private String productName;
        private String category;
        private float confidence;
        private boolean aiModelUsed;

        public RecognitionResult(String productKey, String productName, String category, float confidence, boolean aiModelUsed) {
            this.productKey = productKey;
            this.productName = productName;
            this.category = category;
            this.confidence = confidence;
            this.aiModelUsed = aiModelUsed;
        }

        public String getProductKey() { return productKey; }
        public String getProductName() { return productName; }
        public String getCategory() { return category; }
        public float getConfidence() { return confidence; }
        public boolean isAiModelUsed() { return aiModelUsed; }

        public boolean isRecognized() {
            return !"inconnu".equals(productKey) && confidence > 50;
        }

        public String getConfidenceText() {
            if (confidence >= 80) return "Excellente";
            if (confidence >= 60) return "Bonne";
            if (confidence >= 40) return "Moyenne";
            return "Faible";
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f%%) - %s", productName, confidence, category);
        }
    }
}