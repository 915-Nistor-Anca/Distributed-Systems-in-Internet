package org.example.vetapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.vetapp.model.Specie;

import java.util.List;

public class SpecieService {
    private EntityManager entityManager;

    public SpecieService() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mssql-unit");
        this.entityManager = emf.createEntityManager();
    }

    public Specie findSpecieById(Long id) {
        return entityManager.find(Specie.class, id);
    }

    public List<Specie> findAllSpecies() {
        return entityManager.createQuery("SELECT s FROM Specie s", Specie.class).getResultList();
    }

    public void addSpecie(Specie specie) {
        entityManager.getTransaction().begin();
        entityManager.persist(specie);
        entityManager.getTransaction().commit();
    }

    public void updateSpecie(Specie specie) {
        entityManager.getTransaction().begin();
        entityManager.merge(specie);
        entityManager.getTransaction().commit();
    }

    public void deleteSpecie(Long id) {
        entityManager.getTransaction().begin();
        Specie specie = findSpecieById(id);
        if (specie != null) {
            entityManager.remove(specie);
        }
        entityManager.getTransaction().commit();
    }
}
