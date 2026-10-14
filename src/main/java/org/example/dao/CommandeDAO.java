package org.example.dao;

import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

public class CommandeDAO extends GenericDAO<Commande, Long> {

    public CommandeDAO() {
        super(Commande.class);
    }

    @Override
    public void create(Commande commande) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Ré-attachement des références produits détachées au contexte de persistance actif
            if (commande.getLignes() != null) {
                for (CommandeProduit ligne : commande.getLignes()) {
                    if (ligne.getProduit() != null && ligne.getProduit().getId() != null) {
                        Produit managedProduit = em.getReference(Produit.class, ligne.getProduit().getId());
                        ligne.setProduit(managedProduit);
                    }
                }
            }
            em.persist(commande);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Recherche d'une commande avec initialisation anticipée des lignes et des produits associés
     * via JOIN FETCH afin d'éviter les LazyInitializationException hors contexte de persistance.
     */
    @Override
    public Commande find(Long id) {
        EntityManager em = getEntityManager();
        try {
            List<Commande> results = em.createQuery(
                    "SELECT DISTINCT c FROM Commande c " +
                    "LEFT JOIN FETCH c.lignes l " +
                    "LEFT JOIN FETCH l.produit " +
                    "WHERE c.id = :id", Commande.class)
                    .setParameter("id", id)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}
