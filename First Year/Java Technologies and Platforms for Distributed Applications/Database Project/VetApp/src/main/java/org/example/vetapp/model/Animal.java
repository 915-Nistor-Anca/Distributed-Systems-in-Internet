package org.example.vetapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "animals")
public class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "birthyear", nullable = false)
    private int birthyear;

    @Column(name = "vet_id", nullable = false)
    private Long vetId;

    @ManyToOne
    private Specie specie;

    @ManyToOne
    private Owner owner;

    public Animal(String name, String gender, int birthyear, Specie specie, Owner owner, Long vetId) {
        this.name = name;
        this.gender = gender;
        this.birthyear = birthyear;
        this.specie = specie;
        this.owner = owner;
        this.vetId = vetId;
    }

    public Animal() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getBirthyear() {
        return birthyear;
    }

    public void setBirthyear(int birthyear) {
        this.birthyear = birthyear;
    }

    public Long getVetId() {
        return vetId;
    }

    public void setVetId(Long vetId) {
        this.vetId = vetId;
    }

    public Specie getSpecie() {
        return specie;
    }

    public void setSpecie(Specie specie) {
        this.specie = specie;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }
}
