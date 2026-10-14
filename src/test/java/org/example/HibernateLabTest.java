package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.UserService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class HibernateLabTest {

    private CrudService<Categorie, Long> categorieService;
    private ProduitService produitService;
    private UserService userService;
    private CommandeService commandeService;

    @Before
    public void setUp() {
        categorieService = new CrudService<>(Categorie.class);
        produitService = new ProduitService();
        userService = new UserService();
        commandeService = new CommandeService();
    }

    @AfterClass
    public static void tearDown() {
        GenericDAO.closeEntityManagerFactory();
    }

    @Test
    public void testCreateCategorieAndProduit() {
        Categorie cat = new Categorie("Jeux Vidéo");
        categorieService.create(cat);
        assertNotNull("L'ID de la catégorie doit être généré", cat.getId());

        Produit p = new Produit("Console NextGen", new BigDecimal("499.99"), cat);
        produitService.create(p);
        assertNotNull("L'ID du produit doit être généré", p.getId());

        Produit reloaded = produitService.getById(p.getId());
        assertNotNull(reloaded);
        assertEquals("Console NextGen", reloaded.getLibelle());
        assertEquals(cat.getId(), reloaded.getCategorie().getId());
    }

    @Test
    public void testNamedQueries() {
        Categorie cat = new Categorie("Bureau");
        categorieService.create(cat);

        Produit p1 = new Produit("Chaise Ergonomique Bureau", new BigDecimal("199.00"), cat);
        Produit p2 = new Produit("Lampe LED de Bureau", new BigDecimal("45.00"), cat);
        produitService.create(p1);
        produitService.create(p2);

        // Test Produit.findByCategorie
        List<Produit> parCat = produitService.findByCategorie(cat.getId());
        assertTrue(parCat.size() >= 2);

        // Test Produit.searchByLibelle
        List<Produit> parLibelle = produitService.searchByLibelle("chaise");
        assertFalse(parLibelle.isEmpty());
        assertTrue(parLibelle.stream().anyMatch(p -> p.getLibelle().toLowerCase().contains("chaise")));
    }

    @Test
    public void testCriteriaApiSearch() {
        Categorie cat = new Categorie("Multimédia");
        categorieService.create(cat);

        Produit p = new Produit("Microphone USB Studio", new BigDecimal("79.90"), cat);
        produitService.create(p);

        // Recherche avec critères : mot-clé et tranche de prix
        List<Produit> resultats = produitService.search("Microphone", new BigDecimal("50.00"), new BigDecimal("100.00"));
        assertFalse(resultats.isEmpty());
        assertEquals("Microphone USB Studio", resultats.get(0).getLibelle());

        // Recherche sans résultat (prix hors tranche)
        List<Produit> horsTranche = produitService.search("Microphone", new BigDecimal("150.00"), new BigDecimal("300.00"));
        assertTrue(horsTranche.isEmpty());
    }

    @Test
    public void testPagination() {
        Categorie cat = new Categorie("Livres");
        categorieService.create(cat);

        for (int i = 1; i <= 5; i++) {
            produitService.create(new Produit("Livre Tome " + i, new BigDecimal("15." + i), cat));
        }

        List<Produit> page1 = produitService.findPage(1, 2);
        assertEquals(2, page1.size());

        List<Produit> page2 = produitService.findPage(2, 2);
        assertEquals(2, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    @Test
    public void testInheritanceJoinedStrategy() {
        Client client = new Client("Jean Dupont", LocalDate.of(2025, 1, 10));
        Fournisseur fournisseur = new Fournisseur("LogiCorp", "12 Rue du Commerce");

        userService.create(client);
        userService.create(fournisseur);

        assertNotNull(client.getId());
        assertNotNull(fournisseur.getId());

        User foundClient = userService.getById(client.getId());
        assertTrue("L'entité doit être une instance de Client", foundClient instanceof Client);

        User foundFournisseur = userService.getById(fournisseur.getId());
        assertTrue("L'entité doit être une instance de Fournisseur", foundFournisseur instanceof Fournisseur);
    }

    @Test
    public void testManyToManyWithCompositeKey() {
        Categorie cat = new Categorie("Accessoires");
        categorieService.create(cat);

        Produit p1 = new Produit("Câble HDMI 2.1", new BigDecimal("12.50"), cat);
        Produit p2 = new Produit("Adaptateur USB-C", new BigDecimal("18.00"), cat);
        produitService.create(p1);
        produitService.create(p2);

        Commande commande = new Commande(LocalDate.now());
        commande.addProduit(p1, 3);
        commande.addProduit(p2, 5);

        commandeService.create(commande);
        assertNotNull("L'ID de la commande doit être généré", commande.getId());

        Commande reloaded = commandeService.getById(commande.getId());
        assertNotNull(reloaded);
        assertEquals(2, reloaded.getLignes().size());

        CommandeProduit ligne1 = reloaded.getLignes().get(0);
        assertNotNull(ligne1.getId());
        assertNotNull(ligne1.getId().getCommandeId());
        assertNotNull(ligne1.getId().getProduitId());
        assertTrue(ligne1.getQuantite() > 0);
    }
}
