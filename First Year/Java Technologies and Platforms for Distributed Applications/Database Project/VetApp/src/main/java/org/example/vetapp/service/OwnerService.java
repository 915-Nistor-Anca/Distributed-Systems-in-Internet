package org.example.vetapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.vetapp.model.Animal;
import org.example.vetapp.model.Owner;

import java.util.List;

public class OwnerService {
    private EntityManager entityManager;

    public OwnerService() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mssql-unit");
        this.entityManager = emf.createEntityManager();
    }

    public Owner findOwnerById(Long id) {
        return entityManager.find(Owner.class, id);
    }

    public List<Owner> findAllOwners() {
        return entityManager.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
    }

    public void addOwner(Owner owner) {
        entityManager.getTransaction().begin();
        entityManager.persist(owner);
        entityManager.getTransaction().commit();
    }

    public void updateOwner(Owner owner) {
        entityManager.getTransaction().begin();
        entityManager.merge(owner);
        entityManager.getTransaction().commit();
    }

    public void deleteOwner(Long id) {
        entityManager.getTransaction().begin();
        Owner owner = findOwnerById(id);
        if (owner != null) {
            entityManager.remove(owner);
        }
        entityManager.getTransaction().commit();
    }

    public List<Animal> findAnimalsByOwnerId(Long ownerId) {
        Owner owner = findOwnerById(ownerId);
        if (owner != null) {
            return owner.getAnimals();
        }
        return null;
    }
}
