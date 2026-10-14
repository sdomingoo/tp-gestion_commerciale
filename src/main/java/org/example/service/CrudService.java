package org.example.service;

import org.example.dao.GenericDAO;
import java.io.Serializable;
import java.util.List;

public class CrudService<T, ID extends Serializable> {
    protected final GenericDAO<T, ID> dao;

    public CrudService(Class<T> entityClass) {
        this.dao = new GenericDAO<>(entityClass);
    }

    public CrudService(GenericDAO<T, ID> dao) {
        this.dao = dao;
    }

    public T getById(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("L'identifiant ne peut pas être nul");
        }
        return dao.find(id);
    }

    public List<T> getAll() {
        return dao.findAll();
    }

    public void create(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        dao.create(entity);
    }

    public T update(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        return dao.update(entity);
    }

    public void delete(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        }
        dao.delete(entity);
    }

    public void deleteById(ID id) {
        T entity = getById(id);
        if (entity != null) {
            delete(entity);
        }
    }

    public GenericDAO<T, ID> getDao() {
        return dao;
    }
}
