package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔐 Service de double authentification (2FA) par email
 * Génère et vérifie des codes à 6 chiffres
 *
 * @author FarmVision Team
 * @version 1.0
 */
public class TwoFactorAuthService {

    // Stockage temporaire des codes (en production, utilisez Redis ou la BD)
    private static final Map<String, CodeData> activeCodes = new HashMap<>();

    // Durée de validité du code (5 minutes)
    private static final int CODE_VALIDITY_MINUTES = 5;

    private static final SecureRandom random = new SecureRandom();

    /**
     * 📨 Générer et envoyer un code 2FA par email
     *
     * @param user Utilisateur qui se connecte
     * @return true si le code a été envoyé avec succès
     */
    public static boolean sendVerificationCode(Utilisateur user) {
        try {
            // Générer un code à 6 chiffres
            String code = generateSixDigitCode();

            // Stocker le code avec expiration
            CodeData codeData = new CodeData(code, LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES));
            activeCodes.put(user.getEmail(), codeData);

            // Envoyer par email
            boolean sent = EmailService.send2FACode(user, code);

            if (sent) {
                System.out.println("✅ Code 2FA envoyé à: " + user.getEmail());
                System.out.println("🔐 Code généré: " + code + " (expire dans " + CODE_VALIDITY_MINUTES + " min)");
            }

            return sent;

        } catch (Exception e) {
            System.err.println("❌ Erreur génération code 2FA: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ Vérifier un code 2FA
     *
     * @param email Email de l'utilisateur
     * @param code Code saisi par l'utilisateur
     * @return true si le code est valide
     */
    public static boolean verifyCode(String email, String code) {
        CodeData codeData = activeCodes.get(email);

        if (codeData == null) {
            System.out.println("❌ Aucun code trouvé pour: " + email);
            return false;
        }

        // Vérifier expiration
        if (LocalDateTime.now().isAfter(codeData.expirationTime)) {
            System.out.println("⏰ Code expiré pour: " + email);
            activeCodes.remove(email);
            return false;
        }

        // Vérifier le code
        boolean isValid = codeData.code.equals(code);

        if (isValid) {
            System.out.println("✅ Code valide pour: " + email);
            activeCodes.remove(email); // Supprimer après utilisation
        } else {
            System.out.println("❌ Code invalide pour: " + email);
        }

        return isValid;
    }

    /**
     * 🔢 Générer un code à 6 chiffres aléatoire
     */
    private static String generateSixDigitCode() {
        int code = 100000 + random.nextInt(900000); // Entre 100000 et 999999
        return String.valueOf(code);
    }

    /**
     * 🧹 Nettoyer les codes expirés (appeler périodiquement)
     */
    public static void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        activeCodes.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expirationTime));
        System.out.println("🧹 Codes expirés nettoyés");
    }

    /**
     * ❓ Vérifier si un code existe pour cet email
     */
    public static boolean hasActiveCode(String email) {
        CodeData codeData = activeCodes.get(email);
        if (codeData == null) return false;

        // Vérifier si non expiré
        if (LocalDateTime.now().isAfter(codeData.expirationTime)) {
            activeCodes.remove(email);
            return false;
        }

        return true;
    }

    /**
     * 🔄 Renvoyer un nouveau code
     */
    public static boolean resendCode(Utilisateur user) {
        // Supprimer l'ancien code
        activeCodes.remove(user.getEmail());

        // Envoyer un nouveau
        return sendVerificationCode(user);
    }

    /**
     * Classe interne pour stocker les données du code
     */
    private static class CodeData {
        String code;
        LocalDateTime expirationTime;

        CodeData(String code, LocalDateTime expirationTime) {
            this.code = code;
            this.expirationTime = expirationTime;
        }
    }
}