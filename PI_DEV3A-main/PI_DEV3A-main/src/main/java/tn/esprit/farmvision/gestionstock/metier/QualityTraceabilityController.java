package tn.esprit.farmvision.gestionstock.metier;

import tn.esprit.farmvision.gestionstock.model.Stock;
import tn.esprit.farmvision.gestionstock.service.StockService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Métier avancé : Contrôleur de Qualité et Traçabilité
 * Gère les contrôles qualité et la traçabilité des lots
 */
public class QualityTraceabilityController {

    private StockService stockService;
    private List<ControleQualite> historiqueControles;
    private List<LotTrace> historiqueTraces;

    public QualityTraceabilityController() {
        this.stockService = new StockService();
        this.historiqueControles = new ArrayList<>();
        this.historiqueTraces = new ArrayList<>();
    }

    /**
     * Effectue un contrôle qualité sur un stock
     */
    public RapportQualite effectuerControleQualite(Stock stock) {
        RapportQualite rapport = new RapportQualite();
        rapport.setIdStock(stock.getIdStock());
        rapport.setProduit(stock.getNomProduit());
        rapport.setDateControle(LocalDate.now());
        rapport.setQuantite(stock.getQuantite());
        rapport.setUnite(stock.getUnite());
        rapport.setDateExpiration(stock.getDateExpiration());

        // Vérification de la date d'expiration
        if (stock.getDateExpiration() != null) {
            long joursAvantExpiration = ChronoUnit.DAYS.between(LocalDate.now(), stock.getDateExpiration());
            rapport.setJoursAvantExpiration(joursAvantExpiration);

            if (joursAvantExpiration < 0) {
                rapport.setStatut("EXPIRÉ");
                rapport.setCouleurStatut("#c62828"); // Rouge
                rapport.setAlerte("🔴 PRODUIT EXPIRÉ - À ÉLIMINER");
                rapport.setActionRequise("Élimination immédiate");
                rapport.setPriorite(1);
                rapport.setRecommendation("⚠️ Ce produit n'est plus consommable");
            } else if (joursAvantExpiration < 3) {
                rapport.setStatut("CRITIQUE");
                rapport.setCouleurStatut("#ff9800"); // Orange
                rapport.setAlerte("🟠 Expiration dans moins de 3 jours");
                rapport.setActionRequise("Vente urgente ou don");
                rapport.setPriorite(2);
                rapport.setRecommendation("📢 Mettre en promotion immédiate");
            } else if (joursAvantExpiration < 7) {
                rapport.setStatut("URGENT");
                rapport.setCouleurStatut("#2196F3"); // Bleu
                rapport.setAlerte("🔵 Expiration dans moins d'une semaine");
                rapport.setActionRequise("Priorité à la vente");
                rapport.setPriorite(3);
                rapport.setRecommendation("🏷️ Proposer une réduction");
            } else if (joursAvantExpiration < 30) {
                rapport.setStatut("ATTENTION");
                rapport.setCouleurStatut("#4caf50"); // Vert clair
                rapport.setAlerte("🟢 Expiration dans moins d'un mois");
                rapport.setActionRequise("Surveillance");
                rapport.setPriorite(4);
                rapport.setRecommendation("📅 Planifier la vente");
            } else {
                rapport.setStatut("CONFORME");
                rapport.setCouleurStatut("#2e7d32"); // Vert foncé
                rapport.setAlerte("✅ Produit frais");
                rapport.setActionRequise("Stock normal");
                rapport.setPriorite(5);
                rapport.setRecommendation("📦 Stock en bon état");
            }
        }

        // Vérification de la quantité
        if (stock.getQuantite() <= 0) {
            rapport.setStatut("RUPTURE");
            rapport.setCouleurStatut("#9e9e9e"); // Gris
            rapport.setAlerte("⚫ Stock épuisé");
            rapport.setActionRequise("Réapprovisionnement");
            rapport.setPriorite(1);
            rapport.setRecommendation("📦 Commander d'urgence");
        } else if (stock.getQuantite() < 10) {
            rapport.setAlerte("⚠️ Stock faible");
            if (rapport.getPriorite() > 3) {
                rapport.setPriorite(3);
            }
        }

        // Enregistrer le contrôle dans l'historique
        ControleQualite controle = new ControleQualite();
        controle.setIdStock(stock.getIdStock());
        controle.setDateControle(LocalDate.now());
        controle.setResultat(rapport.getStatut());
        historiqueControles.add(controle);

        return rapport;
    }

    /**
     * Effectue un contrôle qualité sur tous les stocks
     */
    public List<RapportQualite> effectuerControleQualiteGlobal() {
        List<RapportQualite> rapports = new ArrayList<>();
        List<Stock> tousStocks = stockService.getAllStocks();

        for (Stock stock : tousStocks) {
            rapports.add(effectuerControleQualite(stock));
        }

        // Trier par priorité
        rapports.sort(Comparator.comparingInt(RapportQualite::getPriorite));

        return rapports;
    }

    /**
     * Génère un certificat de traçabilité pour un lot
     */
    public CertificatTraçabilite genererCertificatTraçabilite(Stock stock) {
        CertificatTraçabilite certificat = new CertificatTraçabilite();
        certificat.setNumeroLot("LOT-" + stock.getIdStock() + "-" + LocalDate.now().getYear());
        certificat.setIdStock(stock.getIdStock());
        certificat.setProduit(stock.getNomProduit());
        certificat.setQuantite(stock.getQuantite());
        certificat.setUnite(stock.getUnite());
        certificat.setDateEntree(stock.getDateEntree());
        certificat.setDateExpiration(stock.getDateExpiration());
        certificat.setStatut(stock.getStatut());

        // Ajouter les contrôles qualité effectués
        List<RapportQualite> controles = new ArrayList<>();
        for (ControleQualite c : historiqueControles) {
            if (c.getIdStock() == stock.getIdStock()) {
                RapportQualite r = new RapportQualite();
                r.setDateControle(c.getDateControle());
                r.setStatut(c.getResultat());
                controles.add(r);
            }
        }
        certificat.setControlesQualite(controles);

        // Ajouter la traçabilité
        certificat.setCodeQR("FARMVISION-" + certificat.getNumeroLot() + "-" + System.currentTimeMillis());

        // Créer une trace pour l'historique
        LotTrace trace = new LotTrace();
        trace.setNumeroLot(certificat.getNumeroLot());
        trace.setDateTrace(LocalDate.now());
        trace.setAction("CERTIFICAT_GÉNÉRÉ");
        historiqueTraces.add(trace);

        return certificat;
    }

    /**
     * Analyse la qualité globale des stocks
     */
    public RapportQualiteGlobal analyserQualiteGlobale() {
        RapportQualiteGlobal rapport = new RapportQualiteGlobal();
        List<Stock> tousStocks = stockService.getAllStocks();

        int conformes = 0;
        int attention = 0;
        int urgent = 0;
        int critique = 0;
        int expire = 0;
        int rupture = 0;

        for (Stock stock : tousStocks) {
            RapportQualite r = effectuerControleQualite(stock);
            switch (r.getStatut()) {
                case "CONFORME":
                    conformes++;
                    break;
                case "ATTENTION":
                    attention++;
                    break;
                case "URGENT":
                    urgent++;
                    break;
                case "CRITIQUE":
                    critique++;
                    break;
                case "EXPIRÉ":
                    expire++;
                    break;
                case "RUPTURE":
                    rupture++;
                    break;
            }
        }

        rapport.setTotalStocks(tousStocks.size());
        rapport.setConformes(conformes);
        rapport.setAttention(attention);
        rapport.setUrgent(urgent);
        rapport.setCritique(critique);
        rapport.setExpire(expire);
        rapport.setRupture(rupture);

        // Calcul des pourcentages
        if (tousStocks.size() > 0) {
            rapport.setPourcentageConformes(conformes * 100.0 / tousStocks.size());
            rapport.setPourcentageAttention(attention * 100.0 / tousStocks.size());
            rapport.setPourcentageUrgent(urgent * 100.0 / tousStocks.size());
            rapport.setPourcentageCritique(critique * 100.0 / tousStocks.size());
            rapport.setPourcentageExpire(expire * 100.0 / tousStocks.size());
            rapport.setPourcentageRupture(rupture * 100.0 / tousStocks.size());
        }

        // Note de qualité globale (A, B, C, D, E)
        if (critique == 0 && expire == 0 && urgent < 3) {
            rapport.setNoteQualite("A");
            rapport.setMessageGlobal("✅ Excellente qualité des stocks");
        } else if (critique < 2 && expire == 0) {
            rapport.setNoteQualite("B");
            rapport.setMessageGlobal("🟢 Bonne qualité, quelques points d'attention");
        } else if (critique < 5 && expire < 2) {
            rapport.setNoteQualite("C");
            rapport.setMessageGlobal("🟡 Qualité moyenne, actions recommandées");
        } else if (critique < 10 || expire < 5) {
            rapport.setNoteQualite("D");
            rapport.setMessageGlobal("🟠 Qualité préoccupante, actions urgentes");
        } else {
            rapport.setNoteQualite("E");
            rapport.setMessageGlobal("🔴 Qualité critique, intervention immédiate");
        }

        return rapport;
    }

    // Classes internes
    public static class RapportQualite {
        private int idStock;
        private String produit;
        private LocalDate dateControle;
        private double quantite;
        private String unite;
        private LocalDate dateExpiration;
        private long joursAvantExpiration;
        private String statut;
        private String couleurStatut;
        private String alerte;
        private String actionRequise;
        private String recommendation;
        private int priorite;

        // Getters et setters
        public int getIdStock() { return idStock; }
        public void setIdStock(int id) { this.idStock = id; }

        public String getProduit() { return produit; }
        public void setProduit(String p) { this.produit = p; }

        public LocalDate getDateControle() { return dateControle; }
        public void setDateControle(LocalDate d) { this.dateControle = d; }

        public double getQuantite() { return quantite; }
        public void setQuantite(double q) { this.quantite = q; }

        public String getUnite() { return unite; }
        public void setUnite(String u) { this.unite = u; }

        public LocalDate getDateExpiration() { return dateExpiration; }
        public void setDateExpiration(LocalDate d) { this.dateExpiration = d; }

        public long getJoursAvantExpiration() { return joursAvantExpiration; }
        public void setJoursAvantExpiration(long j) { this.joursAvantExpiration = j; }

        public String getStatut() { return statut; }
        public void setStatut(String s) { this.statut = s; }

        public String getCouleurStatut() { return couleurStatut; }
        public void setCouleurStatut(String c) { this.couleurStatut = c; }

        public String getAlerte() { return alerte; }
        public void setAlerte(String a) { this.alerte = a; }

        public String getActionRequise() { return actionRequise; }
        public void setActionRequise(String a) { this.actionRequise = a; }

        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String r) { this.recommendation = r; }

        public int getPriorite() { return priorite; }
        public void setPriorite(int p) { this.priorite = p; }
    }

    public static class CertificatTraçabilite {
        private String numeroLot;
        private int idStock;
        private String produit;
        private double quantite;
        private String unite;
        private LocalDate dateEntree;
        private LocalDate dateExpiration;
        private String statut;
        private List<RapportQualite> controlesQualite;
        private String codeQR;

        // Getters et setters
        public String getNumeroLot() { return numeroLot; }
        public void setNumeroLot(String n) { this.numeroLot = n; }

        public int getIdStock() { return idStock; }
        public void setIdStock(int id) { this.idStock = id; }

        public String getProduit() { return produit; }
        public void setProduit(String p) { this.produit = p; }

        public double getQuantite() { return quantite; }
        public void setQuantite(double q) { this.quantite = q; }

        public String getUnite() { return unite; }
        public void setUnite(String u) { this.unite = u; }

        public LocalDate getDateEntree() { return dateEntree; }
        public void setDateEntree(LocalDate d) { this.dateEntree = d; }

        public LocalDate getDateExpiration() { return dateExpiration; }
        public void setDateExpiration(LocalDate d) { this.dateExpiration = d; }

        public String getStatut() { return statut; }
        public void setStatut(String s) { this.statut = s; }

        public List<RapportQualite> getControlesQualite() { return controlesQualite; }
        public void setControlesQualite(List<RapportQualite> c) { this.controlesQualite = c; }

        public String getCodeQR() { return codeQR; }
        public void setCodeQR(String code) { this.codeQR = code; }
    }

    public static class RapportQualiteGlobal {
        private int totalStocks;
        private int conformes;
        private int attention;
        private int urgent;
        private int critique;
        private int expire;
        private int rupture;
        private double pourcentageConformes;
        private double pourcentageAttention;
        private double pourcentageUrgent;
        private double pourcentageCritique;
        private double pourcentageExpire;
        private double pourcentageRupture;
        private String noteQualite;
        private String messageGlobal;

        // Getters et setters
        public int getTotalStocks() { return totalStocks; }
        public void setTotalStocks(int t) { this.totalStocks = t; }

        public int getConformes() { return conformes; }
        public void setConformes(int c) { this.conformes = c; }

        public int getAttention() { return attention; }
        public void setAttention(int a) { this.attention = a; }

        public int getUrgent() { return urgent; }
        public void setUrgent(int u) { this.urgent = u; }

        public int getCritique() { return critique; }
        public void setCritique(int c) { this.critique = c; }

        public int getExpire() { return expire; }
        public void setExpire(int e) { this.expire = e; }

        public int getRupture() { return rupture; }
        public void setRupture(int r) { this.rupture = r; }

        public double getPourcentageConformes() { return pourcentageConformes; }
        public void setPourcentageConformes(double p) { this.pourcentageConformes = p; }

        public double getPourcentageAttention() { return pourcentageAttention; }
        public void setPourcentageAttention(double p) { this.pourcentageAttention = p; }

        public double getPourcentageUrgent() { return pourcentageUrgent; }
        public void setPourcentageUrgent(double p) { this.pourcentageUrgent = p; }

        public double getPourcentageCritique() { return pourcentageCritique; }
        public void setPourcentageCritique(double p) { this.pourcentageCritique = p; }

        public double getPourcentageExpire() { return pourcentageExpire; }
        public void setPourcentageExpire(double p) { this.pourcentageExpire = p; }

        public double getPourcentageRupture() { return pourcentageRupture; }
        public void setPourcentageRupture(double p) { this.pourcentageRupture = p; }

        public String getNoteQualite() { return noteQualite; }
        public void setNoteQualite(String n) { this.noteQualite = n; }

        public String getMessageGlobal() { return messageGlobal; }
        public void setMessageGlobal(String m) { this.messageGlobal = m; }
    }

    // Classes de persistance
    private static class ControleQualite {
        private int idStock;
        private LocalDate dateControle;
        private String resultat;

        public int getIdStock() { return idStock; }
        public void setIdStock(int id) { this.idStock = id; }

        public LocalDate getDateControle() { return dateControle; }
        public void setDateControle(LocalDate d) { this.dateControle = d; }

        public String getResultat() { return resultat; }
        public void setResultat(String r) { this.resultat = r; }
    }

    private static class LotTrace {
        private String numeroLot;
        private LocalDate dateTrace;
        private String action;

        public String getNumeroLot() { return numeroLot; }
        public void setNumeroLot(String n) { this.numeroLot = n; }

        public LocalDate getDateTrace() { return dateTrace; }
        public void setDateTrace(LocalDate d) { this.dateTrace = d; }

        public String getAction() { return action; }
        public void setAction(String a) { this.action = a; }
    }
}