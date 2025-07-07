package org.example.vetapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.vetapp.model.Vet;
import org.example.vetapp.model.VeterinaryClinic;

import java.util.List;

public class VetService {
    private EntityManager entityManager;
    private EntityManager entityManager2;

    public VetService() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("postgresql-unit");
        EntityManagerFactory emf2 = Persistence.createEntityManagerFactory("oracle-unit");
        this.entityManager = emf.createEntityManager();
        this.entityManager2 = emf2.createEntityManager();
    }

    public Vet findVetById(Long id) {
        return entityManager.find(Vet.class, id);
    }

    public List<Vet> findAllVets() {
        return entityManager.createQuery("SELECT v FROM Vet v", Vet.class).getResultList();
    }

    public boolean checkIfClinicExists(Long clinicId) {
        VeterinaryClinic clinic = entityManager2.find(VeterinaryClinic.class, clinicId);
        return clinic != null;
    }

    public int addVet(Vet vet) {
        if (checkIfClinicExists(vet.getVeterinaryclinicId())) {
            entityManager.getTransaction().begin();
            entityManager.persist(vet);
            entityManager.getTransaction().commit();
            return 1;
        }
        return 0;
    }

    public void updateVet(Vet vet) {
        entityManager.getTransaction().begin();
        entityManager.merge(vet);
        entityManager.getTransaction().commit();
    }

    public void deleteVet(Long id) {
        entityManager.getTransaction().begin();
        Vet vet = findVetById(id);
        if (vet != null) {
            entityManager.remove(vet);
        }
        entityManager.getTransaction().commit();
    }
}
