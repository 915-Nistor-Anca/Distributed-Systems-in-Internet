package org.example.vetapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.vetapp.model.VeterinaryClinic;

import java.util.List;

public class VeterinaryClinicService {
    private EntityManager entityManager;

    public VeterinaryClinicService() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("oracle-unit");
        this.entityManager = emf.createEntityManager();
    }

    public VeterinaryClinic findVeterinaryClinicById(Long id) {
        return entityManager.find(VeterinaryClinic.class, id);
    }

    public List<VeterinaryClinic> findAllVeterinaryClinics() {
        return entityManager.createQuery("SELECT v FROM VeterinaryClinic v", VeterinaryClinic.class).getResultList();
    }

    public void addVeterinaryClinic(VeterinaryClinic veterinaryClinic) {
        entityManager.getTransaction().begin();
        entityManager.persist(veterinaryClinic);
        entityManager.getTransaction().commit();
    }

    public void updateVeterinaryClinic(VeterinaryClinic veterinaryClinic) {
        entityManager.getTransaction().begin();
        entityManager.merge(veterinaryClinic);
        entityManager.getTransaction().commit();
    }

    public void deleteVeterinaryClinic(Long id) {
        entityManager.getTransaction().begin();
        VeterinaryClinic veterinaryClinic = findVeterinaryClinicById(id);
        if (veterinaryClinic != null) {
            entityManager.remove(veterinaryClinic);
        }
        entityManager.getTransaction().commit();
    }
}
