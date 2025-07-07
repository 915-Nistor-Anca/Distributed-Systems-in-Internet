package org.example.vetapp.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "species")
public class Specie {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "specie", cascade = CascadeType.ALL)
    private List<Animal> animals;

    public Specie(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Specie() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
