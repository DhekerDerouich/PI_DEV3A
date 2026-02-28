package tn.esprit.farmvision.integrations.model;

import java.time.LocalDate;

/**
 * Modèle représentant l'historique d'un stock dans InvenTree
 */
public class InvenTreeStockHistory {
    private int id;
    private int partId;
    private String partName;
    private double quantity;
    private LocalDate date;
    private String action;
    private String notes;
    private String user;

    // Types d'actions possibles
    public static final String ACTION_STOCK_IN = "STOCK_IN";
    public static final String ACTION_STOCK_OUT = "STOCK_OUT";
    public static final String ACTION_STOCK_ADJUST = "STOCK_ADJUST";
    public static final String ACTION_STOCK_MOVE = "STOCK_MOVE";
    public static final String ACTION_EXPIRY = "EXPIRY";

    // Constructeurs
    public InvenTreeStockHistory() {}

    public InvenTreeStockHistory(int partId, double quantity, String action, String notes) {
        this.partId = partId;
        this.quantity = quantity;
        this.action = action;
        this.notes = notes;
        this.date = LocalDate.now();
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPartId() { return partId; }
    public void setPartId(int partId) { this.partId = partId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    // Méthodes utilitaires
    public String getActionDisplay() {
        switch (action) {
            case ACTION_STOCK_IN: return "📥 Entrée stock";
            case ACTION_STOCK_OUT: return "📤 Sortie stock";
            case ACTION_STOCK_ADJUST: return "⚖️ Ajustement";
            case ACTION_STOCK_MOVE: return "🔄 Transfert";
            case ACTION_EXPIRY: return "⏰ Expiration";
            default: return action;
        }
    }

    public String getQuantityDisplay() {
        if (action.equals(ACTION_STOCK_IN)) {
            return "+" + quantity;
        } else if (action.equals(ACTION_STOCK_OUT)) {
            return "-" + quantity;
        }
        return String.valueOf(quantity);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", date, getActionDisplay(), getQuantityDisplay());
    }
}