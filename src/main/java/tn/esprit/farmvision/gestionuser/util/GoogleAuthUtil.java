package tn.esprit.farmvision.gestionuser.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class GoogleAuthUtil {

    private static final String APPLICATION_NAME = "FarmVision Login";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // ✅ SCOPES CORRECTS - Chaque scope dans un élément séparé
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
    );

    private static final String CREDENTIALS_FILE_PATH = "/google-credentials.json";

    /**
     * Classe pour stocker les infos utilisateur Google
     */
    public static class GoogleUserInfo {
        public String email;
        public String name;
        public String given_name;
        public String family_name;
        public String picture;
        public String id;

        public String getPrenom() {
            if (given_name != null && !given_name.isEmpty()) {
                return given_name;
            }
            if (name != null && name.contains(" ")) {
                return name.split(" ")[0];
            }
            return name != null ? name : "";
        }

        public String getNom() {
            if (family_name != null && !family_name.isEmpty()) {
                return family_name;
            }
            if (name != null && name.contains(" ")) {
                String[] parts = name.split(" ");
                return parts.length > 1 ? parts[parts.length - 1] : "";
            }
            return "";
        }

        @Override
        public String toString() {
            return "GoogleUserInfo{" +
                    "email='" + email + '\'' +
                    ", name='" + name + '\'' +
                    ", given_name='" + given_name + '\'' +
                    ", family_name='" + family_name + '\'' +
                    '}';
        }
    }

    /**
     * ✅ MÉTHODE PRINCIPALE - VERSION SIMPLIFIÉE AVEC API OFFICIELLE
     */
    public static GoogleUserInfo authenticateAndGetUserInfo() throws Exception {
        // Supprimer les anciens tokens
        deleteOldTokens();

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Obtenir le credential
        Credential credential = getCredentials(HTTP_TRANSPORT);

        if (credential == null || credential.getAccessToken() == null) {
            throw new Exception("Échec de l'authentification Google");
        }

        System.out.println("✅ Token obtenu : " + credential.getAccessToken().substring(0, 30) + "...");

        // ✅ UTILISER L'API OFFICIELLE OAUTH2
        Oauth2 oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Récupérer les infos utilisateur
        Userinfo userinfo = oauth2.userinfo().get().execute();

        // Convertir en notre classe
        GoogleUserInfo info = new GoogleUserInfo();
        info.email = userinfo.getEmail();
        info.name = userinfo.getName();
        info.given_name = userinfo.getGivenName();
        info.family_name = userinfo.getFamilyName();
        info.picture = userinfo.getPicture();
        info.id = userinfo.getId();

        System.out.println("✅ Informations récupérées avec succès :");
        System.out.println("   📧 Email : " + info.email);
        System.out.println("   👤 Nom complet : " + info.name);
        System.out.println("   👤 Prénom : " + info.given_name);
        System.out.println("   👤 Nom : " + info.family_name);

        if (info.email == null || info.email.isEmpty()) {
            throw new Exception("Impossible de récupérer l'email Google. Vérifiez les autorisations.");
        }

        return info;
    }

    /**
     * Supprimer les anciens tokens
     */
    private static void deleteOldTokens() {
        try {
            File tokensDir = new File(TOKENS_DIRECTORY_PATH);
            if (tokensDir.exists() && tokensDir.isDirectory()) {
                File[] files = tokensDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        Files.deleteIfExists(file.toPath());
                    }
                }
                System.out.println("🗑️ Anciens tokens supprimés");
            }
        } catch (IOException e) {
            System.out.println("⚠️ Impossible de supprimer les tokens : " + e.getMessage());
        }
    }

    /**
     * Obtenir les credentials OAuth2
     */
    private static Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleAuthUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException(
                    "❌ Fichier introuvable : " + CREDENTIALS_FILE_PATH +
                            "\nAssurez-vous qu'il est dans src/main/resources/"
            );
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        System.out.println("🌐 Ouverture du navigateur pour authentification Google...");

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Méthode deprecated pour compatibilité
     */
    @Deprecated
    public static String authenticateAndGetEmail() throws Exception {
        GoogleUserInfo info = authenticateAndGetUserInfo();
        return info != null ? info.email : null;
    }
}