package org.example.service;

import org.example.entities.Client;
import org.example.entities.Fournisseur;
import org.example.entities.User;

import java.util.List;

public class UserService extends CrudService<User, Long> {

    public UserService() {
        super(User.class);
    }

    public List<User> getAllUsers() {
        return getAll();
    }
}
