package org.example.vetapp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.vetapp.model.Animal;
import org.example.vetapp.model.Owner;
import org.example.vetapp.model.Specie;
import org.example.vetapp.service.AnimalService;
import org.example.vetapp.service.OwnerService;
import org.example.vetapp.service.SpecieService;

import java.io.IOException;
import java.util.List;

@WebServlet("/animals")
public class AnimalController extends HttpServlet {
    private AnimalService animalService;
    private OwnerService ownerService;
    private SpecieService specieService;

    @Override
    public void init() throws ServletException {
        animalService = new AnimalService();
        ownerService = new OwnerService();
        specieService = new SpecieService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam != null && !idParam.isEmpty()) {
            Long id;
            try {
                id = Long.parseLong(idParam);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid 'id' parameter");
                return;
            }

            Animal animal = animalService.findAnimalById(id);
            if (animal == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Animal not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":" + animal.getId() + ",\"name\":\"" + animal.getName() + "\"}");
        } else {
            resp.setContentType("application/json");
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("[");

            List<Animal> animals = animalService.findAllAnimals();
            for (int i = 0; i < animals.size(); i++) {
                Animal animal = animals.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(animal.getId()).append(",");
                jsonResponse.append("\"name\":\"").append(animal.getName()).append("\",");
                jsonResponse.append("\"gender\":\"").append(animal.getGender()).append("\",");
                jsonResponse.append("\"birthyear\":\"").append(animal.getBirthyear()).append("\",");
                jsonResponse.append("\"owner\":\"").append(animal.getOwner()).append("\"");
                jsonResponse.append("}");

                if (i < animals.size() - 1) {
                    jsonResponse.append(",");
                }
            }

            jsonResponse.append("]");

            resp.getWriter().write(jsonResponse.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String gender = req.getParameter("gender");
        String birthyearParam = req.getParameter("birthyear");
        String ownerIdParam = req.getParameter("owner_id");
        String specieIdParam = req.getParameter("specie_id");
        String vetIdParam = req.getParameter("vet_id");

        if (name == null || gender == null || birthyearParam == null || ownerIdParam == null || specieIdParam == null || vetIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters: 'name', 'gender', 'birthyear', 'owner_id', 'specie_id', and 'vet_id' are required");
            return;
        }

        int birthyear;
        Long ownerId, specieId, vetId;

        try {
            birthyear = Integer.parseInt(birthyearParam);
            ownerId = Long.parseLong(ownerIdParam);
            specieId = Long.parseLong(specieIdParam);
            vetId = Long.parseLong(vetIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid number format for 'birthyear', 'owner_id', 'specie_id', or 'vet_id'");
            return;
        }

        Animal animal = new Animal();
        animal.setName(name);
        animal.setGender(gender);
        animal.setBirthyear(birthyear);

        Owner owner = ownerService.findOwnerById(ownerId);
        Specie specie = specieService.findSpecieById(specieId);
        animal.setOwner(owner);
        animal.setSpecie(specie);
        animal.setVetId(vetId);

        animalService.addAnimal(animal);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write("Animal added successfully!");
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing 'id' parameter");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid 'id' parameter");
            return;
        }

        Animal existingAnimal = animalService.findAnimalById(id);
        if (existingAnimal == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Animal not found");
            return;
        }

        String name = req.getParameter("name");
        String gender = req.getParameter("gender");
        String birthyearParam = req.getParameter("birthyear");
        String ownerIdParam = req.getParameter("owner_id");
        String specieIdParam = req.getParameter("specie_id");
        String vetIdParam = req.getParameter("vet_id");

        boolean updated = false;

        if (name != null) {
            existingAnimal.setName(name);
            updated = true;
        }
        if (gender != null) {
            existingAnimal.setGender(gender);
            updated = true;
        }

        int birthyear = -1;
        Long ownerId = null, specieId = null, vetId = null;

        if (birthyearParam != null) {
            try {
                birthyear = Integer.parseInt(birthyearParam);
                existingAnimal.setBirthyear(birthyear);
                updated = true;
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid 'birthyear' parameter");
                return;
            }
        }

        if (ownerIdParam != null) {
            try {
                ownerId = Long.parseLong(ownerIdParam);
                Owner owner = ownerService.findOwnerById(ownerId);
                existingAnimal.setOwner(owner);
                updated = true;
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid 'owner_id' parameter");
                return;
            }
        }

        if (specieIdParam != null) {
            try {
                specieId = Long.parseLong(specieIdParam);
                Specie specie = specieService.findSpecieById(specieId);
                existingAnimal.setSpecie(specie);
                updated = true;
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid 'specie_id' parameter");
                return;
            }
        }

        if (vetIdParam != null) {
            try {
                vetId = Long.parseLong(vetIdParam);
                existingAnimal.setVetId(vetId);
                updated = true;
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid 'vet_id' parameter");
                return;
            }
        }

        if (!updated) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("No valid parameters to update");
            return;
        }

        animalService.updateAnimal(existingAnimal);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Animal updated successfully");
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing 'id' parameter");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid 'id' parameter");
            return;
        }

        Animal animal = animalService.findAnimalById(id);
        if (animal == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Animal not found");
            return;
        }

        animalService.deleteAnimal(id);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Animal deleted successfully");
    }
}
