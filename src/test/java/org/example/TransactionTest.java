package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.Produit;
import org.example.service.CrudService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;

import static org.junit.Assert.*;

public class TransactionTest {

    private CrudService<Categorie, Long> categorieService;

    @Before
    public void setUp() {
        categorieService = new CrudService<>(Categorie.class);
    }

    @AfterClass
    public static void tearDown() {
        GenericDAO.closeEntityManagerFactory();
    }

    @Test
    public void testTransactionRollbackOnException() {
        Categorie cat = new Categorie("Catégorie Transaction");
        categorieService.create(cat);

        EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        Long produitId = null;
        try {
            tx.begin();

            Produit p = new Produit("Produit En Test Rollback", new BigDecimal("50.00"), cat);
            em.persist(p);

            // Simulation d'une condition d'échec
            boolean conditionErreur = true;
            if (conditionErreur) {
                throw new IllegalStateException("Erreur simulée pour forcer le rollback !");
            }

            tx.commit();
            produitId = p.getId();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        } finally {
            em.close();
        }

        // Vérification qu'aucune entité n'a été persistée en base suite au rollback
        EntityManager verifyEm = GenericDAO.getEntityManagerFactory().createEntityManager();
        try {
            Long count = verifyEm.createQuery(
                    "SELECT COUNT(p) FROM Produit p WHERE p.libelle = :lib", Long.class)
                    .setParameter("lib", "Produit En Test Rollback")
                    .getSingleResult();
            assertEquals("Le produit ne doit pas exister en base après rollback", 0L, count.longValue());
        } finally {
            verifyEm.close();
        }
    }
}
