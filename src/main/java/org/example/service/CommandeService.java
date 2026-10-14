package org.example.service;

import org.example.dao.CommandeDAO;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;

public class CommandeService extends CrudService<Commande, Long> {

    public CommandeService() {
        super(new CommandeDAO());
    }

    public CommandeService(CommandeDAO commandeDAO) {
        super(commandeDAO);
    }

    @Override
    public void create(Commande commande) {
        if (commande == null) {
            throw new IllegalArgumentException("La commande ne peut pas être nulle");
        }
        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new IllegalArgumentException("Une commande doit contenir au moins une ligne");
        }
        for (CommandeProduit ligne : commande.getLignes()) {
            if (ligne.getQuantite() <= 0) {
                throw new IllegalArgumentException("La quantité commandée doit être strictement positive");
            }
        }
        super.create(commande);
    }
}
