package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.UserService;
import org.example.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Programme principal de démonstration du Lab Hibernate / JEE.
 * Exécute successivement :
 * 1. Configuration Hibernate & JPA
 * 2. CRUD Générique sur Catégorie
 * 3. Modélisation Produit & relations ManyToOne / OneToMany bidirectionnelles
 * 4. Requêtes avancées : Named Queries & Criteria API dynamique
 * 5. Pagination avec JPQL
 * 6. Stratégie d'héritage JOINED (User, Client, Fournisseur)
 * 7. Relations N:N avec entité associative et clé composée (Commande, CommandeProduit, CommandeProduitPK)
 * 8. Gestion manuelle des transactions et rollback
 */
public class App {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("   LAB JEE / HIBERNATE - GESTION COMMERCIALE (DEMONSTRATION COMPLETE)   ");
        System.out.println("========================================================================\n");

        try {
            // Initialisation des services
            CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
            ProduitService produitService = new ProduitService();
            UserService userService = new UserService();
            CommandeService commandeService = new CommandeService();

            // Nettoyage préalable des tables pour garantir l'idempotence des exécutions successives
            cleanDatabase();

            // -------------------------------------------------------------------------
            // ETAPE 1 : Insertion d'une catégorie via le CrudService / GenericDAO
            // -------------------------------------------------------------------------
            System.out.println(">>> 1. INSERTION DES CATEGORIES VIA GENERIC DAO / SERVICE :");
            Categorie catInfo = new Categorie("Informatique");
            Categorie catElec = new Categorie("Électronique");
            catService.create(catInfo);
            catService.create(catElec);
            System.out.println("   [OK] Catégorie 1 créée : " + catInfo);
            System.out.println("   [OK] Catégorie 2 créée : " + catElec);

            // -------------------------------------------------------------------------
            // ETAPE 2 : Création de produits et relation ManyToOne / OneToMany
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 2. CREATION DES PRODUITS ET RELATIONS :");
            Produit p1 = new Produit("PC Portable Gamer", new BigDecimal("1299.99"), catInfo);
            Produit p2 = new Produit("Souris Sans Fil Ergonomique", new BigDecimal("35.50"), catInfo);
            Produit p3 = new Produit("Clavier Mécanique RGB", new BigDecimal("89.90"), catInfo);
            Produit p4 = new Produit("Écran 27 Pouces 4K", new BigDecimal("349.00"), catInfo);
            Produit p5 = new Produit("Casque Audio Bluetooth ANC", new BigDecimal("129.00"), catElec);

            produitService.create(p1);
            produitService.create(p2);
            produitService.create(p3);
            produitService.create(p4);
            produitService.create(p5);
            System.out.println("   [OK] 5 Produits enregistrés avec succès.");

            // -------------------------------------------------------------------------
            // ETAPE 3 : Requêtes avancées - Named Queries
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 3. EXECUTION DES NAMED QUERIES :");
            System.out.println("   a) Produit.findByCategorie (Informatique ID=" + catInfo.getId() + ") :");
            List<Produit> prodsByCat = produitService.findByCategorie(catInfo.getId());
            prodsByCat.forEach(p -> System.out.println("      - " + p.getLibelle() + " (" + p.getPrix() + " EUR)"));

            System.out.println("   b) Produit.searchByLibelle ('sans fil') :");
            List<Produit> prodsByTerm = produitService.searchByLibelle("sans fil");
            prodsByTerm.forEach(p -> System.out.println("      - " + p.getLibelle()));

            // -------------------------------------------------------------------------
            // ETAPE 4 : Criteria API dynamique multicritère
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 4. CRITERIA API DYNAMIQUE :");
            System.out.println("   Recherche multicritère : term='Audio', minPrix=50, maxPrix=200 :");
            List<Produit> searchResults = produitService.search("Audio", new BigDecimal("50"), new BigDecimal("200"));
            searchResults.forEach(p -> System.out.println("      - Trouvé : " + p.getLibelle() + " - " + p.getPrix() + " EUR"));

            // -------------------------------------------------------------------------
            // ETAPE 5 : Pagination JPQL (setFirstResult / setMaxResults)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 5. PAGINATION AVEC JPQL :");
            int pageSize = 2;
            for (int page = 1; page <= 3; page++) {
                List<Produit> pageItems = produitService.findPage(page, pageSize);
                System.out.println("   Page " + page + " (taille " + pageSize + ", " + pageItems.size() + " résultats) :");
                pageItems.forEach(p -> System.out.println("      * [ID " + p.getId() + "] " + p.getLibelle()));
            }

            // -------------------------------------------------------------------------
            // ETAPE 6 : Stratégie d'héritage JOINED (User -> Client, Fournisseur)
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 6. HERITAGE JPA (STRATEGIE JOINED) :");
            Client client = new Client("Sophie Laurent", LocalDate.of(2025, 3, 15));
            Fournisseur fournisseur = new Fournisseur("TechLogistics SARL", "42 Avenue des Champs, Paris");

            userService.create(client);
            userService.create(fournisseur);
            System.out.println("   [OK] Client créé (ID=" + client.getId() + ") : " + client);
            System.out.println("   [OK] Fournisseur créé (ID=" + fournisseur.getId() + ") : " + fournisseur);

            System.out.println("   Requête polymorphique sur User.class (SELECT u FROM User u) :");
            List<User> allUsers = userService.getAllUsers();
            for (User u : allUsers) {
                System.out.println("      - [" + u.getClass().getSimpleName() + "] ID=" + u.getId() + ", Nom=" + u.getNom());
            }

            // -------------------------------------------------------------------------
            // ETAPE 7 : Relations N:N avec entité associative & clé composée
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 7. RELATION N:N AVEC ENTITE ASSOCIATIVE & CLE COMPOSEE :");
            Commande commande = new Commande(LocalDate.now());
            commande.addProduit(p1, 2); // 2 PC Portables
            commande.addProduit(p3, 4); // 4 Claviers Mécaniques
            commandeService.create(commande);
            System.out.println("   [OK] Commande créée avec ID = " + commande.getId());

            Commande reloadedCmd = commandeService.getById(commande.getId());
            System.out.println("   Lignes de la commande ID=" + reloadedCmd.getId() + " :");
            for (CommandeProduit ligne : reloadedCmd.getLignes()) {
                System.out.println("      - Produit ID=" + ligne.getId().getProduitId() +
                        ", Commande ID=" + ligne.getId().getCommandeId() +
                        ", Quantité=" + ligne.getQuantite() +
                        ", Libellé=" + (ligne.getProduit() != null ? ligne.getProduit().getLibelle() : "N/A"));
            }

            // -------------------------------------------------------------------------
            // ETAPE 8 : Gestion des transactions & Rollback
            // -------------------------------------------------------------------------
            System.out.println("\n>>> 8. DEMONSTRATION TRANSACTION MANUELLE & ROLLBACK :");
            EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Produit pValide = new Produit("Produit Valide", new BigDecimal("49.99"), catInfo);
                em.persist(pValide);

                System.out.println("   Simulation d'une erreur métier (prix négatif)...");
                Produit pInvalide = new Produit("Produit Erroné", new BigDecimal("-10.00"), catInfo);
                if (pInvalide.getPrix().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Règle métier violée : Prix strictement négatif interdit !");
                }
                em.persist(pInvalide);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                System.out.println("   [SUCCESS] Exception interceptée et transaction annulée (ROLLBACK) : " + e.getMessage());
            } finally {
                em.close();
            }

            System.out.println("\n========================================================================");
            System.out.println("              TOUTES LES ETAPES DU LAB ONT REUSSI !                     ");
            System.out.println("========================================================================");

        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de l'exécution du lab : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Nettoyage propre des ressources
            GenericDAO.closeEntityManagerFactory();
            HibernateUtil.shutdown();
            try {
                com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * Nettoie les tables de la base de données avant chaque exécution de la démonstration
     * afin de garantir l'idempotence et d'éviter les violations de contrainte d'unicité (Duplicate entry).
     */
    private static void cleanDatabase() {
        EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Désactivation temporaire des contraintes de clés étrangères (MySQL / MariaDB ou H2)
            try {
                em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            } catch (Exception ignored) {
                try {
                    em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
                } catch (Exception ignored2) {}
            }

            try {
                em.createNativeQuery("TRUNCATE TABLE commande_produit").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE commandes").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE produits").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE categories").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE clients").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE fournisseurs").executeUpdate();
                em.createNativeQuery("TRUNCATE TABLE users").executeUpdate();
            } catch (Exception fallback) {
                em.createQuery("DELETE FROM CommandeProduit").executeUpdate();
                em.createQuery("DELETE FROM Commande").executeUpdate();
                em.createQuery("DELETE FROM Produit").executeUpdate();
                em.createQuery("DELETE FROM Categorie").executeUpdate();
                em.createQuery("DELETE FROM Client").executeUpdate();
                em.createQuery("DELETE FROM Fournisseur").executeUpdate();
                em.createQuery("DELETE FROM User").executeUpdate();
            }

            try {
                em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            } catch (Exception ignored) {
                try {
                    em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
                } catch (Exception ignored2) {}
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } finally {
            em.close();
        }
    }
}

