package org.example.entities;

import javax.persistence.*;

@Entity
@Table(name = "commande_produit")
public class CommandeProduit {

    @EmbeddedId
    private CommandeProduitPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    public CommandeProduit() {
        this.id = new CommandeProduitPK();
    }

    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        this.id = new CommandeProduitPK();
        if (commande != null && commande.getId() != null) {
            this.id.setCommandeId(commande.getId());
        }
        if (produit != null && produit.getId() != null) {
            this.id.setProduitId(produit.getId());
        }
    }

    public CommandeProduitPK getId() {
        return id;
    }

    public void setId(CommandeProduitPK id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
        if (commande != null && commande.getId() != null) {
            if (this.id == null) {
                this.id = new CommandeProduitPK();
            }
            this.id.setCommandeId(commande.getId());
        }
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null && produit.getId() != null) {
            if (this.id == null) {
                this.id = new CommandeProduitPK();
            }
            this.id.setProduitId(produit.getId());
        }
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    @Override
    public String toString() {
        return "CommandeProduit{" +
                "id=" + id +
                ", quantite=" + quantite +
                ", produit=" + (produit != null ? produit.getLibelle() : "null") +
                '}';
    }
}
