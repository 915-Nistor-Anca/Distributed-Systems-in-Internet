package org.example.vetapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vets")
public class Vet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phonenumber", nullable = false)
    private String phonenumber;

    @Column(name = "veterinaryclinic_id", nullable = false)
    private Long veterinaryclinicId;

    public Vet(Long id, String name, String phonenumber, Long veterinaryclinicId) {
        this.id = id;
        this.name = name;
        this.phonenumber = phonenumber;
        this.veterinaryclinicId = veterinaryclinicId;
    }

    public Vet() {
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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Long getVeterinaryclinicId() {
        return veterinaryclinicId;
    }

    public void setVeterinaryclinicId(Long veterinaryclinicId) {
        this.veterinaryclinicId = veterinaryclinicId;
    }
}
