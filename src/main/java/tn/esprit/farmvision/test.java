package tn.esprit.farmvision;  // ← change si ton package racine est différent

import org.mindrot.jbcrypt.BCrypt;

public class test {

    public static void main(String[] args) {
        String motDePasseClair = "dheker123";

        // Génère un hash avec cost 12 (valeur recommandée et utilisée dans ton projet)
        String hash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt(12));

        System.out.println("Mot de passe en clair : " + motDePasseClair);
        System.out.println("Hash BCrypt généré : ");
        System.out.println(hash);
        System.out.println();
        System.out.println("Copie ce hash et colle-le dans ton INSERT SQL :");
        System.out.println("password = '" + hash + "'");
    }
}