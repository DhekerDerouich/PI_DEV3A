package tn.esprit.farmvision.gestionuser.service;

import org.junit.jupiter.api.*;
import tn.esprit.farmvision.gestionuser.model.Agriculteur;
import tn.esprit.farmvision.gestionuser.model.Utilisateur;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de test pour UtilisateurService
 * Teste les opérations CRUD sur la base de données
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilisateurServiceTest {

    static UtilisateurService service;
    static int idTest = 0;  // ID de l'utilisateur créé pour les tests

    /**
     * Initialisation du service avant tous les tests
     */
    @BeforeAll
    static void setup() {
        service = new UtilisateurService();
        System.out.println("=== Tests UtilisateurService démarrés ===");
    }

    /**
     * Test 1 : Ajouter un utilisateur
     * Vérifie qu'un nouvel utilisateur peut être enregistré avec succès
     */
    @Test
    @Order(1)
    void testAjouterUtilisateur() {
        // Créer un utilisateur de test avec un email unique
        String emailUnique = "test" + System.currentTimeMillis() + "@gmail.com";
        Agriculteur a = new Agriculteur(
                "TestNom",
                "TestPrenom",
                emailUnique,
                "123456",
                "12345678",
                "Tunis"
        );

        // Vérifier que l'enregistrement ne lève pas d'exception
        assertDoesNotThrow(() -> service.register(a));

        // Vérifier que l'utilisateur est bien dans la base
        List<Utilisateur> users = service.getAll();
        assertFalse(users.isEmpty(), "La liste des utilisateurs ne devrait pas être vide");

        // Trouver l'utilisateur qu'on vient de créer
        boolean trouve = users.stream()
                .anyMatch(u -> u.getEmail().equals(emailUnique));
        assertTrue(trouve, "L'utilisateur créé devrait être dans la liste");

        // Sauvegarder l'ID pour les tests suivants
        Utilisateur userCree = users.stream()
                .filter(u -> u.getEmail().equals(emailUnique))
                .findFirst()
                .orElse(null);

        assertNotNull(userCree, "L'utilisateur devrait être trouvé");
        idTest = userCree.getId();
        System.out.println("✓ Utilisateur créé avec ID = " + idTest);
    }

    /**
     * Test 2 : Modifier un utilisateur
     * Vérifie qu'un utilisateur existant peut être modifié
     */
    @Test
    @Order(2)
    void testModifierUtilisateur() {
        // Récupérer l'utilisateur créé dans le test précédent
        List<Utilisateur> users = service.getAll();
        Utilisateur u = users.stream()
                .filter(user -> user.getId() == idTest)
                .findFirst()
                .orElse(null);

        assertNotNull(u, "L'utilisateur avec ID " + idTest + " devrait exister");

        // Modifier les données
        u.setNom("NomModifie");
        u.setPrenom("PrenomModifie");

        // Appliquer la modification
        assertDoesNotThrow(() -> service.update(u));

        // Vérifier que la modification a été appliquée
        List<Utilisateur> usersApres = service.getAll();
        Utilisateur uModifie = usersApres.stream()
                .filter(user -> user.getId() == idTest)
                .findFirst()
                .orElse(null);

        assertNotNull(uModifie, "L'utilisateur modifié devrait toujours exister");
        assertEquals("NomModifie", uModifie.getNom(), "Le nom devrait être modifié");
        assertEquals("PrenomModifie", uModifie.getPrenom(), "Le prénom devrait être modifié");

        System.out.println("✓ Utilisateur modifié avec succès");
    }

    /**
     * Test 3 : Supprimer un utilisateur
     * Vérifie qu'un utilisateur peut être supprimé de la base
     */
    @Test
    @Order(3)
    void testSupprimerUtilisateur() {
        // Supprimer l'utilisateur de test
        boolean resultat = service.delete(idTest);
        assertTrue(resultat, "La suppression devrait réussir");

        // Vérifier que l'utilisateur n'existe plus
        List<Utilisateur> users = service.getAll();
        boolean existe = users.stream()
                .anyMatch(u -> u.getId() == idTest);

        assertFalse(existe, "L'utilisateur supprimé ne devrait plus exister");
        System.out.println("✓ Utilisateur supprimé avec succès");
    }

    /**
     * Test 4 : Valider un utilisateur
     * Vérifie que le statut activated peut être changé
     */
    @Test
    @Order(4)
    void testValiderUtilisateur() {
        // Créer un nouvel utilisateur pour ce test
        String emailUnique = "validation" + System.currentTimeMillis() + "@gmail.com";
        Agriculteur a = new Agriculteur(
                "UserValidation",
                "Test",
                emailUnique,
                "password123",
                "55555555",
                "Sfax"
        );

        assertDoesNotThrow(() -> service.register(a));

        // Récupérer l'utilisateur créé
        List<Utilisateur> users = service.getAll();
        Utilisateur userCree = users.stream()
                .filter(u -> u.getEmail().equals(emailUnique))
                .findFirst()
                .orElse(null);

        assertNotNull(userCree, "L'utilisateur devrait être créé");
        assertFalse(userCree.isActivated(), "L'utilisateur devrait être désactivé par défaut");

        // Valider l'utilisateur
        boolean resultat = service.validerUtilisateur(userCree.getId());
        assertTrue(resultat, "La validation devrait réussir");

        // Vérifier que le statut a changé
        List<Utilisateur> usersApres = service.getAll();
        Utilisateur userValide = usersApres.stream()
                .filter(u -> u.getId() == userCree.getId())
                .findFirst()
                .orElse(null);

        assertNotNull(userValide, "L'utilisateur validé devrait exister");
        assertTrue(userValide.isActivated(), "L'utilisateur devrait être activé");

        // Nettoyage
        service.delete(userCree.getId());
        System.out.println("✓ Validation d'utilisateur testée avec succès");
    }

    /**
     * Test 5 : Réinitialiser le mot de passe
     * Vérifie que le mot de passe peut être changé
     */
    @Test
    @Order(5)
    void testResetPassword() {
        // Créer un utilisateur pour ce test
        String emailUnique = "resetpwd" + System.currentTimeMillis() + "@gmail.com";
        Agriculteur a = new Agriculteur(
                "UserReset",
                "Test",
                emailUnique,
                "oldPassword",
                "66666666",
                "Sousse"
        );

        assertDoesNotThrow(() -> service.register(a));

        // Récupérer l'utilisateur créé
        List<Utilisateur> users = service.getAll();
        Utilisateur userCree = users.stream()
                .filter(u -> u.getEmail().equals(emailUnique))
                .findFirst()
                .orElse(null);

        assertNotNull(userCree, "L'utilisateur devrait être créé");

        // Réinitialiser le mot de passe
        boolean resultat = service.resetPassword(userCree.getId(), "newPassword123");
        assertTrue(resultat, "La réinitialisation du mot de passe devrait réussir");

        // Nettoyage
        service.delete(userCree.getId());
        System.out.println("✓ Réinitialisation de mot de passe testée avec succès");
    }

    /**
     * Test 6 : Email déjà utilisé
     * Vérifie qu'on ne peut pas créer deux comptes avec le même email
     */
    @Test
    @Order(6)
    void testEmailDejUtilise() {
        String emailUnique = "duplicate" + System.currentTimeMillis() + "@gmail.com";

        // Créer le premier utilisateur
        Agriculteur a1 = new Agriculteur(
                "User1",
                "Test",
                emailUnique,
                "password",
                "77777777",
                "Tunis"
        );
        assertDoesNotThrow(() -> service.register(a1));

        // Essayer de créer un deuxième utilisateur avec le même email
        Agriculteur a2 = new Agriculteur(
                "User2",
                "Test",
                emailUnique,
                "password",
                "88888888",
                "Tunis"
        );

        Exception exception = assertThrows(Exception.class, () -> {
            service.register(a2);
        });

        assertTrue(exception.getMessage().contains("déjà utilisé"),
                "Le message d'erreur devrait mentionner que l'email est déjà utilisé");

        // Nettoyage : supprimer le premier utilisateur
        List<Utilisateur> users = service.getAll();
        Utilisateur userCree = users.stream()
                .filter(u -> u.getEmail().equals(emailUnique))
                .findFirst()
                .orElse(null);

        if (userCree != null) {
            service.delete(userCree.getId());
        }

        System.out.println("✓ Test email dupliqué réussi");
    }

    /**
     * Nettoyage final après tous les tests
     */
    @AfterAll
    static void cleanup() {
        System.out.println("=== Tests terminés ===");
    }
}