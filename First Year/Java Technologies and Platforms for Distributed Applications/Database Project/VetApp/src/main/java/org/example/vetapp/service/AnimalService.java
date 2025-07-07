package org.example.vetapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.vetapp.model.Animal;
import org.example.vetapp.model.Owner;
import org.example.vetapp.model.Specie;

import java.util.List;

public class AnimalService {
    private EntityManager entityManager;

    public AnimalService() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mssql-unit");
        this.entityManager = emf.createEntityManager();
    }

    public Animal findAnimalById(Long id) {
        return entityManager.find(Animal.class, id);
    }

    public List<Animal> findAllAnimals() {
        return entityManager.createQuery("SELECT a FROM Animal a", Animal.class).getResultList();
    }

    public boolean checkIfOwnerExists(Long ownerId) {
        Owner owner = entityManager.find(Owner.class, ownerId);
        return owner != null;
    }

    public boolean checkIfSpecieExists(Long specieId) {
        Specie specie = entityManager.find(Specie.class, specieId);
        return specie != null;
    }

    public void addAnimal(Animal animal) {
        entityManager.getTransaction().begin();
        entityManager.persist(animal);
        entityManager.getTransaction().commit();
    }

    public void updateAnimal(Animal animal) {
        entityManager.getTransaction().begin();
        entityManager.merge(animal);
        entityManager.getTransaction().commit();
    }

    public void deleteAnimal(Long id) {
        entityManager.getTransaction().begin();
        Animal animal = findAnimalById(id);
        if (animal != null) {
            entityManager.remove(animal);
        }
        entityManager.getTransaction().commit();
    }
}
