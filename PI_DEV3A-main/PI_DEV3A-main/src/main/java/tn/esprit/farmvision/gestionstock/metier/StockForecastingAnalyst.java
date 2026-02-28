package tn.esprit.farmvision.gestionstock.metier;

import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Métier avancé : Analyste en Prévision des Stocks
 * Prédit les besoins futurs en fonction de l'historique de consommation
 */
public class StockForecastingAnalyst {

    private StockService stockService;
    private Map<String, List<Double>> historiqueConsommation;

    public StockForecastingAnalyst() {
        this.stockService = new StockService();
        this.historiqueConsommation = new HashMap<>();
        initialiserHistoriqueSimule();
    }

    /**
     * Initialise un historique simulé pour la démonstration
     */
    private void initialiserHistoriqueSimule() {
        // Simulation d'historique pour différents produits
        historiqueConsommation.put("Tomates", Arrays.asList(15.0, 12.0, 18.0, 14.0, 16.0));
        historiqueConsommation.put("Pommes", Arrays.asList(8.0, 10.0, 9.0, 11.0, 7.0));
        historiqueConsommation.put("Blé", Arrays.asList(50.0, 45.0, 55.0, 48.0, 52.0));
    }

    /**
     * Analyse la vitesse d'écoulement d'un stock
     * @param stock Le stock à analyser
     * @return Rapport d'analyse détaillé
     */
    public RapportPrevision analyserVitesseEcoulement(Stock stock) {
        RapportPrevision rapport = new RapportPrevision();
        rapport.setIdStock(stock.getIdStock());
        rapport.setNomProduit(stock.getNomProduit());
        rapport.setStockActuel(stock.getQuantite());
        rapport.setUnite(stock.getUnite());

        // Calcul du taux de consommation quotidien moyen
        double tauxConsommationQuotidien = calculerTauxConsommationMoyen(stock);
        rapport.setTauxConsommationQuotidien(tauxConsommationQuotidien);

        // Calcul des jours restants avant rupture
        if (tauxConsommationQuotidien > 0) {
            double joursRestants = stock.getQuantite() / tauxConsommationQuotidien;
            rapport.setJoursAvantRupture(joursRestants);

            // Date prévue de rupture
            LocalDate dateRupture = LocalDate.now().plusDays((long) joursRestants);
            rapport.setDateRupturePrevue(dateRupture);

            // Vérifier si la date d'expiration est plus proche que la rupture
            if (stock.getDateExpiration() != null) {
                long joursAvantExpiration = ChronoUnit.DAYS.between(LocalDate.now(), stock.getDateExpiration());
                if (joursAvantExpiration < joursRestants) {
                    rapport.setDateCritique(stock.getDateExpiration());
                    rapport.setRaisonCritique("EXPIRATION");
                    rapport.setJoursAvantCritique(joursAvantExpiration);
                } else {
                    rapport.setDateCritique(dateRupture);
                    rapport.setRaisonCritique("RUPTURE");
                    rapport.setJoursAvantCritique((long) joursRestants);
                }
            } else {
                rapport.setDateCritique(dateRupture);
                rapport.setRaisonCritique("RUPTURE");
                rapport.setJoursAvantCritique((long) joursRestants);
            }

            // Évaluation du risque
            evaluerNiveauRisque(rapport);
        }

        return rapport;
    }

    /**
     * Évalue le niveau de risque basé sur les jours critiques
     */
    private void evaluerNiveauRisque(RapportPrevision rapport) {
        long jours = rapport.getJoursAvantCritique();

        if (jours < 3) {
            rapport.setNiveauRisque("CRITIQUE");
            rapport.setCouleurRisque("#c62828"); // Rouge
            rapport.setRecommendation("🔴 ACTION URGENTE ! Réapprovisionnement immédiat");
            rapport.setPriorite(1);
        } else if (jours < 7) {
            rapport.setNiveauRisque("URGENT");
            rapport.setCouleurRisque("#ff9800"); // Orange
            rapport.setRecommendation("🟠 Réapprovisionnement dans les 3 jours");
            rapport.setPriorite(2);
        } else if (jours < 15) {
            rapport.setNiveauRisque("ÉLEVÉ");
            rapport.setCouleurRisque("#2196F3"); // Bleu
            rapport.setRecommendation("🔵 Planifier un réapprovisionnement cette semaine");
            rapport.setPriorite(3);
        } else if (jours < 30) {
            rapport.setNiveauRisque("MODÉRÉ");
            rapport.setCouleurRisque("#4caf50"); // Vert clair
            rapport.setRecommendation("🟢 Surveiller régulièrement");
            rapport.setPriorite(4);
        } else {
            rapport.setNiveauRisque("FAIBLE");
            rapport.setCouleurRisque("#2e7d32"); // Vert foncé
            rapport.setRecommendation("✅ Stock confortable");
            rapport.setPriorite(5);
        }
    }

    /**
     * Calcule le taux de consommation moyen à partir de l'historique
     */
    private double calculerTauxConsommationMoyen(Stock stock) {
        String produit = stock.getNomProduit();
        List<Double> historique = historiqueConsommation.getOrDefault(produit, new ArrayList<>());

        if (historique.isEmpty()) {
            // Si pas d'historique, estimation basée sur la quantité
            return stock.getQuantite() / 30.0; // Estimation : stock pour 30 jours
        }

        // Calcul de la moyenne de l'historique
        OptionalDouble moyenne = historique.stream()
                .mapToDouble(Double::doubleValue)
                .average();

        return moyenne.orElse(stock.getQuantite() / 30.0);
    }

    /**
     * Génère des recommandations d'achat basées sur les prévisions
     */
    public List<RecommandationAchat> genererRecommandationsAchat() {
        List<RecommandationAchat> recommandations = new ArrayList<>();
        List<Stock> tousStocks = stockService.getAllStocks();

        for (Stock stock : tousStocks) {
            RapportPrevision rapport = analyserVitesseEcoulement(stock);

            if (rapport.getPriorite() <= 3) { // CRITIQUE, URGENT, ÉLEVÉ
                RecommandationAchat rec = new RecommandationAchat();
                rec.setIdStock(stock.getIdStock());
                rec.setProduit(stock.getNomProduit());
                rec.setStockActuel(stock.getQuantite());
                rec.setUnite(stock.getUnite());

                // Calcul de la quantité recommandée (stock de sécurité pour 30 jours)
                double quantiteRecommandee = calculerQuantiteRecommandee(stock, rapport);
                rec.setQuantiteRecommande(quantiteRecommandee);

                rec.setPriorite(rapport.getNiveauRisque());
                rec.setJustification(rapport.getRecommendation());
                rec.setJoursRestants(rapport.getJoursAvantCritique());

                recommandations.add(rec);
            }
        }

        // Trier par priorité
        recommandations.sort(Comparator.comparingInt(r -> {
            switch (r.getPriorite()) {
                case "CRITIQUE": return 1;
                case "URGENT": return 2;
                case "ÉLEVÉ": return 3;
                default: return 4;
            }
        }));

        return recommandations;
    }

    private double calculerQuantiteRecommandee(Stock stock, RapportPrevision rapport) {
        // Stock de sécurité = consommation moyenne * 30 jours
        double stockSecurite = rapport.getTauxConsommationQuotidien() * 30;

        // Quantité à commander = stock de sécurité - stock actuel
        return Math.max(0, Math.ceil(stockSecurite - stock.getQuantite()));
    }

    /**
     * Ajoute une consommation à l'historique (appelé lors des ventes)
     */
    public void enregistrerConsommation(String produit, double quantite) {
        List<Double> historique = historiqueConsommation.computeIfAbsent(produit, k -> new ArrayList<>());
        historique.add(quantite);
        // Garder seulement les 30 dernières entrées
        if (historique.size() > 30) {
            historique.remove(0);
        }
    }

    // Classes internes pour les rapports
    public static class RapportPrevision {
        private int idStock;
        private String nomProduit;
        private double stockActuel;
        private String unite;
        private double tauxConsommationQuotidien;
        private double joursAvantRupture;
        private LocalDate dateRupturePrevue;
        private LocalDate dateCritique;
        private String raisonCritique;
        private long joursAvantCritique;
        private String niveauRisque;
        private String couleurRisque;
        private String recommendation;
        private int priorite;

        // Getters et setters
        public int getIdStock() { return idStock; }
        public void setIdStock(int id) { this.idStock = id; }

        public String getNomProduit() { return nomProduit; }
        public void setNomProduit(String nom) { this.nomProduit = nom; }

        public double getStockActuel() { return stockActuel; }
        public void setStockActuel(double stock) { this.stockActuel = stock; }

        public String getUnite() { return unite; }
        public void setUnite(String unite) { this.unite = unite; }

        public double getTauxConsommationQuotidien() { return tauxConsommationQuotidien; }
        public void setTauxConsommationQuotidien(double taux) { this.tauxConsommationQuotidien = taux; }

        public double getJoursAvantRupture() { return joursAvantRupture; }
        public void setJoursAvantRupture(double jours) { this.joursAvantRupture = jours; }

        public LocalDate getDateRupturePrevue() { return dateRupturePrevue; }
        public void setDateRupturePrevue(LocalDate date) { this.dateRupturePrevue = date; }

        public LocalDate getDateCritique() { return dateCritique; }
        public void setDateCritique(LocalDate date) { this.dateCritique = date; }

        public String getRaisonCritique() { return raisonCritique; }
        public void setRaisonCritique(String raison) { this.raisonCritique = raison; }

        public long getJoursAvantCritique() { return joursAvantCritique; }
        public void setJoursAvantCritique(long jours) { this.joursAvantCritique = jours; }

        public String getNiveauRisque() { return niveauRisque; }
        public void setNiveauRisque(String niveau) { this.niveauRisque = niveau; }

        public String getCouleurRisque() { return couleurRisque; }
        public void setCouleurRisque(String couleur) { this.couleurRisque = couleur; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String rec) { this.recommendation = rec; }

        public int getPriorite() { return priorite; }
        public void setPriorite(int priorite) { this.priorite = priorite; }
    }

    public static class RecommandationAchat {
        private int idStock;
        private String produit;
        private double stockActuel;
        private String unite;
        private double quantiteRecommande;
        private String priorite;
        private String justification;
        private long joursRestants;

        // Getters et setters
        public int getIdStock() { return idStock; }
        public void setIdStock(int id) { this.idStock = id; }

        public String getProduit() { return produit; }
        public void setProduit(String produit) { this.produit = produit; }

        public double getStockActuel() { return stockActuel; }
        public void setStockActuel(double stock) { this.stockActuel = stock; }

        public String getUnite() { return unite; }
        public void setUnite(String unite) { this.unite = unite; }

        public double getQuantiteRecommande() { return quantiteRecommande; }
        public void setQuantiteRecommande(double qte) { this.quantiteRecommande = qte; }

        public String getPriorite() { return priorite; }
        public void setPriorite(String priorite) { this.priorite = priorite; }

        public String getJustification() { return justification; }
        public void setJustification(String justif) { this.justification = justif; }

        public long getJoursRestants() { return joursRestants; }
        public void setJoursRestants(long jours) { this.joursRestants = jours; }
    }
}