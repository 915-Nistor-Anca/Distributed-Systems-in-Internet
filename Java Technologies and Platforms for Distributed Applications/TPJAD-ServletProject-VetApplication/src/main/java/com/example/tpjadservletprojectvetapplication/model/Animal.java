package com.example.tpjadservletprojectvetapplication.model;

public class Animal {
    private int id;
    private String name;
    private String specie;
    private String gender;
    private int age;
    private String ownerName;

    public Animal(Integer id, String name, String specie, String gender, Integer age, String ownerName) {
        this.id = id;
        this.name = name;
        this.specie = specie;
        this.gender = gender;
        this.age = age;
        this.ownerName = ownerName;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecie() {
        return specie;
    }

    public String getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
