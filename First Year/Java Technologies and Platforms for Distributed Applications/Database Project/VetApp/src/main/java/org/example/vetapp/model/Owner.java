package org.example.vetapp.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "owners")
public class Owner {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phonenumber", nullable = false)
    private String phonenumber;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Animal> animals;

    public Owner(Long id, String name, String phonenumber, String email) {
        this.id = id;
        this.name = name;
        this.phonenumber = phonenumber;
    }

    public Owner() {
        super();
    }

    @Override
    public String toString() {
        return "Owner{id=" + id + ", name='" + name + "', phonenumber='" + phonenumber + "', email='" + email + "'}";
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }
}
