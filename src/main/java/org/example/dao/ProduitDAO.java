package org.example.dao;

import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO extends GenericDAO<Produit, Long> {

    public ProduitDAO() {
        super(Produit.class);
    }

    /**
     * Recherche de produits par catégorie via NamedQuery
     */
    public List<Produit> findByCategorie(Long catId) {
        EntityManager em = getEntityManager();
        try {
            return em.createNamedQuery("Produit.findByCategorie", Produit.class)
                     .setParameter("catId", catId)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche de produits par mot-clé dans le libellé via NamedQuery
     */
    public List<Produit> searchByLibelle(String term) {
        EntityManager em = getEntityManager();
        try {
            return em.createNamedQuery("Produit.searchByLibelle", Produit.class)
                     .setParameter("term", term)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Recherche dynamique multicritère avec la Criteria API
     */
    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        EntityManager em = getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> cq = cb.createQuery(Produit.class);
            Root<Produit> root = cq.from(Produit.class);
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("libelle")), "%" + term.toLowerCase() + "%"));
            }
            if (minPrix != null) {
                predicates.add(cb.ge(root.get("prix"), minPrix));
            }
            if (maxPrix != null) {
                predicates.add(cb.le(root.get("prix"), maxPrix));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Pagination avec JPQL (setFirstResult et setMaxResults)
     */
    public List<Produit> findPage(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                     .setFirstResult((pageNumber - 1) * pageSize)
                     .setMaxResults(pageSize)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}
