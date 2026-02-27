package tn.esprit.farmvision.gestionuser.service;

import tn.esprit.farmvision.config.EnvConfig;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

public class EmailService {

    // ✅ Chargement depuis .env
    private static final String SENDER_EMAIL = EnvConfig.get("EMAIL_SENDER");
    private static final String SENDER_PASSWORD = EnvConfig.get("EMAIL_PASSWORD");

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    static {
        if (SENDER_EMAIL == null || SENDER_PASSWORD == null) {
            System.err.println("⚠️ Configuration email manquante dans .env");
        } else {
            System.out.println("✅ Email configuré avec: " + SENDER_EMAIL);
        }
    }

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
                        "Cordialement,\n" +
                        "L'équipe FarmVision",
                user.getNomComplet(),
                user.getEmail()
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    public static boolean sendAccountValidationEmail(Utilisateur user) {
        String subject = "✅ Votre compte FarmVision a été activé !";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Félicitations ! 🎉\n\n" +
                        "Votre compte FarmVision a été VALIDÉ ET ACTIVÉ par notre équipe.\n\n" +
                        "✅ Vous pouvez maintenant vous connecter !\n\n" +
                        "Utilisez vos identifiants pour accéder à toutes les fonctionnalités.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision",
                user.getNomComplet()
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    public static boolean sendPasswordResetEmail(Utilisateur user, String newPassword) {
        String subject = "🔒 Réinitialisation de votre mot de passe FarmVision";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Votre mot de passe FarmVision a été réinitialisé.\n\n" +
                        "🔑 Votre nouveau mot de passe : %s\n\n" +
                        "Nous vous recommandons de changer ce mot de passe lors de votre prochaine connexion.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision",
                user.getNomComplet(),
                newPassword
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    public static boolean send2FACode(Utilisateur user, String code) {
        String subject = "🔐 Votre code de vérification FarmVision";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Votre code de vérification à 6 chiffres :\n\n" +
                        "    %s\n\n" +
                        "⏱️ Ce code expire dans 5 minutes.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision",
                user.getNomComplet(),
                code
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    public static boolean sendPasswordResetCode(Utilisateur user, String code) {
        String subject = "🔐 Réinitialisation de votre mot de passe FarmVision";
        String message = String.format(
                "Bonjour %s,\n\n" +
                        "Vous avez demandé la réinitialisation de votre mot de passe.\n\n" +
                        "🔑 Votre code de vérification : %s\n\n" +
                        "Ce code expire dans 5 minutes.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe FarmVision",
                user.getNomComplet(),
                code
        );

        return sendEmail(user.getEmail(), subject, message);
    }

    private static boolean sendEmail(String recipient, String subject, String message) {
        if (SENDER_EMAIL == null || SENDER_PASSWORD == null) {
            System.err.println("❌ Email non configuré dans .env");
            return false;
        }

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
}