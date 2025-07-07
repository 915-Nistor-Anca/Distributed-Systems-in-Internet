package org.example.vetapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "veterinaryclinics")
public class VeterinaryClinic {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phonenumber", nullable = false)
    private String phonenumber;

    public VeterinaryClinic(Long id, String name, String address, String phonenumber) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public VeterinaryClinic() {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
