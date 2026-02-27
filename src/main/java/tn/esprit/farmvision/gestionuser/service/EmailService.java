package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.gestionuser.model.Utilisateur;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

/**
 * 📧 Service d'envoi d'emails automatiques pour FarmVision
 * Version complète avec tous les types d'emails
 *
 * @author FarmVision Team
 * @version 3.0
 */
public class EmailService {

    // ⚠️ CONFIGUREZ vos credentials Gmail ici
    private static final String SENDER_EMAIL = "dzikoudrh@gmail.com";
    private static final String SENDER_PASSWORD = "rnhd bnbj dfps eswq";

    // Configuration SMTP Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    /**
     * 🌾 Envoyer un email de bienvenue (inscription)
     */
    public static boolean sendWelcomeEmail(Utilisateur user) {
        String subject = "🌾 Bienvenue chez FarmVision !";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Merci de votre inscription sur FarmVision !\n\n" +
                        "Votre compte a été créé avec succès et est actuellement EN ATTENTE DE VALIDATION " +
                        "par notre équipe d'administration.\n\n" +
                        "📧 Votre email : %s\n\n" +
                        "⏳ Prochaine étape :\n" +
                        "Un administrateur va examiner votre demande et activer votre compte dans les plus brefs délais.\n" +
                        "Vous recevrez un email de confirmation dès que votre compte sera activé.\n\n" +
                        "En attendant, vous pouvez :\n" +
                        "  ✅ Découvrir nos fonctionnalités sur notre site\n" +
                        "  ✅ Préparer vos données agricoles\n" +
                        "  ✅ Contacter notre support si besoin\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision\n" +
                        "© 2026 FarmVision - Gestion Agricole Intelligente",
                user.getNomComplet(),
                user.getEmail()
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    /**
     * ✅ Envoyer un email de validation de compte (activation)
     */
    public static boolean sendAccountValidationEmail(Utilisateur user) {
        String subject = "✅ Votre compte FarmVision a été activé !";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Félicitations ! 🎉\n\n" +
                        "Votre compte FarmVision a été VALIDÉ ET ACTIVÉ par notre équipe.\n\n" +
                        "✅ Vous pouvez maintenant vous connecter !\n\n" +
                        "Utilisez vos identifiants pour accéder à toutes les fonctionnalités de FarmVision.\n\n" +
                        "Ce que vous pouvez faire maintenant :\n" +
                        "  🌾 Gérer vos exploitations agricoles\n" +
                        "  📊 Suivre vos cultures et récoltes\n" +
                        "  📈 Analyser vos performances\n" +
                        "  👥 Collaborer avec votre équipe\n\n" +
                        "Besoin d'aide ? Consultez notre guide de démarrage ou contactez notre support.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision\n" +
                        "© 2026 FarmVision - Gestion Agricole Intelligente",
                user.getNomComplet()
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    /**
     * 🔄 Envoyer un email de notification de modification de profil
     */
    public static boolean sendProfileUpdateEmail(Utilisateur user, Map<String, String> changes, String updatedBy) {
        String subject = "🔄 Modification de votre profil FarmVision";

        StringBuilder changesList = new StringBuilder();
        for (Map.Entry<String, String> entry : changes.entrySet()) {
            changesList.append("  - ").append(entry.getKey())
                    .append(": ").append(entry.getValue()).append("\n");
        }

        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Vos informations de profil FarmVision ont été modifiées.\n\n" +
                        "📋 Modifications effectuées :\n%s\n" +
                        "Modifié par : %s\n" +
                        "Date : %s\n\n" +
                        "⚠️ Important :\n" +
                        "Si vous n'avez pas demandé cette modification ou si vous pensez qu'il s'agit d'une erreur,\n" +
                        "veuillez contacter immédiatement notre support.\n\n" +
                        "Ces modifications prennent effet immédiatement sur votre compte.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision\n" +
                        "© 2026 FarmVision - Gestion Agricole Intelligente",
                user.getNomComplet(),
                changesList.toString(),
                updatedBy,
                new Date().toString()
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    /**
     * 🔒 Envoyer un email de réinitialisation de mot de passe
     */
    public static boolean sendPasswordResetEmail(Utilisateur user, String newPassword) {
        String subject = "🔒 Réinitialisation de votre mot de passe FarmVision";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Votre mot de passe FarmVision a été réinitialisé par un administrateur.\n\n" +
                        "🔑 Votre nouveau mot de passe temporaire :\n" +
                        "    %s\n\n" +
                        "⚠️ Pour votre sécurité :\n" +
                        "Nous vous recommandons fortement de changer ce mot de passe lors de votre prochaine connexion.\n\n" +
                        "Comment changer votre mot de passe :\n" +
                        "  1. Connectez-vous avec ce mot de passe temporaire\n" +
                        "  2. Allez dans Mon Profil\n" +
                        "  3. Modifiez votre mot de passe\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, contactez immédiatement notre support.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision\n" +
                        "© 2026 FarmVision - Gestion Agricole Intelligente",
                user.getNomComplet(),
                newPassword
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    /**
     * 🔐 Envoyer un code 2FA par email
     */
    public static boolean send2FACode(Utilisateur user, String code) {
        String subject = "🔐 Votre code de vérification FarmVision";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Votre code de vérification à 6 chiffres :\n\n" +
                        "    %s\n\n" +
                        "⏱️ Ce code expire dans 5 minutes.\n\n" +
                        "⚠️ Sécurité :\n" +
                        "  - Ne partagez JAMAIS ce code\n" +
                        "  - FarmVision ne vous demandera jamais ce code par téléphone ou email\n" +
                        "  - Si vous n'avez pas demandé ce code, ignorez cet email\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision\n" +
                        "© 2026 FarmVision - Gestion Agricole Intelligente",
                user.getNomComplet(),
                code
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    /**
     * 📧 Méthode principale d'envoi d'email
     */
    private static boolean sendEmail(String recipient, String subject, String message) {
        try {
            System.out.println("📧 Envoi email à: " + recipient);

            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(SENDER_EMAIL, "FarmVision"));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            email.setSubject(subject);
            email.setText(message);
            email.setSentDate(new Date());

            Transport.send(email);

            System.out.println("✅ Email envoyé avec succès à: " + recipient);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email à " + recipient + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 🧪 Test de configuration email
     */
    public static void testEmailConfiguration() {
        System.out.println("\n🧪 Test de configuration email...");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("📧 Email : " + SENDER_EMAIL);
        System.out.println("🔐 Password : " + (SENDER_PASSWORD.contains("xxxx") ? "❌ NON CONFIGURÉ" : "✅ CONFIGURÉ"));
        System.out.println("🌐 SMTP Host : " + SMTP_HOST);
        System.out.println("🔌 SMTP Port : " + SMTP_PORT);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (SENDER_PASSWORD.contains("xxxx")) {
            System.err.println("\n⚠️  ATTENTION : Configurez vos credentials Gmail dans EmailService.java!");
            System.err.println("   Ligne 18-19 : SENDER_EMAIL et SENDER_PASSWORD\n");
        } else {
            System.out.println("✅ Configuration complète et prête à l'emploi!\n");
        }
    }
    /**
     * 📧 Envoyer un code de réinitialisation de mot de passe
     */
    public static boolean sendPasswordResetCode(Utilisateur user, String code) {
        String subject = "🔐 Réinitialisation de votre mot de passe FarmVision";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                        "🔑 Votre code de vérification : %s\n\n" +
                        "Ce code expire dans 5 minutes.\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision",
                user.getNomComplet(),
                code
        );

        return sendEmail(user.getEmail(), subject, message);
    }
}