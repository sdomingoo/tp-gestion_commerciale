package org.example.service;

import org.example.dao.ProduitDAO;
import org.example.entities.Produit;

import java.math.BigDecimal;
import java.util.List;

public class ProduitService extends CrudService<Produit, Long> {
    private final ProduitDAO produitDAO;

    public ProduitService() {
        this(new ProduitDAO());
    }

    public ProduitService(ProduitDAO produitDAO) {
        super(produitDAO);
        this.produitDAO = produitDAO;
    }

    @Override
    public void create(Produit produit) {
        validateProduit(produit);
        super.create(produit);
    }

    @Override
    public Produit update(Produit produit) {
        validateProduit(produit);
        return super.update(produit);
    }

    private void validateProduit(Produit produit) {
        if (produit == null) {
            throw new IllegalArgumentException("Le produit ne doit pas être nul");
        }
        if (produit.getLibelle() == null || produit.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé du produit est obligatoire");
        }
        if (produit.getPrix() != null && produit.getPrix().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix du produit ne peut pas être négatif");
        }
        if (produit.getCategorie() == null) {
            throw new IllegalArgumentException("La catégorie du produit est obligatoire");
        }
    }

    public List<Produit> findByCategorie(Long catId) {
        if (catId == null) {
            throw new IllegalArgumentException("L'identifiant de catégorie est obligatoire");
        }
        return produitDAO.findByCategorie(catId);
    }

    public List<Produit> searchByLibelle(String term) {
        return produitDAO.searchByLibelle(term != null ? term : "");
    }

    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        return produitDAO.search(term, minPrix, maxPrix);
    }

    public List<Produit> findPage(int pageNumber, int pageSize) {
        return produitDAO.findPage(pageNumber, pageSize);
    }
}
