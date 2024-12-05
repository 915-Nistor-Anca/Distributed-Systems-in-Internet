package com.example.tpjadservletprojectvetapplication.servlet;

import com.example.tpjadservletprojectvetapplication.model.Animal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@WebServlet("/manage")
public class ManageAnimalsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        int id = Integer.parseInt(request.getParameter("id"));

        if ("edit".equals(action)) {
            List<Animal> animals = (List<Animal>) getServletContext().getAttribute("animals");

            Optional<Animal> animalToEdit = animals.stream()
                    .filter(animal -> animal.getId() == id)
                    .findFirst();
            if (animalToEdit.isPresent()) {
                request.setAttribute("animal", animalToEdit.get());
                request.setAttribute("action", "edit");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Animal not found");
                return;
            }
        }
        request.getRequestDispatcher("/manage.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        Integer id = Integer.parseInt(request.getParameter("id"));

        List<Animal> animals = (List<Animal>) getServletContext().getAttribute("animals");

        if ("edit".equals(action)) {
            String name = request.getParameter("name");
            String species = request.getParameter("species");
            String gender = request.getParameter("gender");
            Integer age = Integer.parseInt(request.getParameter("age"));
            String ownerName = request.getParameter("ownerName");

            for (Animal animal : animals) {
                if (Objects.equals(animal.getId(), id)) {
                    animal.setName(name);
                    animal.setSpecie(species);
                    animal.setGender(gender);
                    animal.setAge(age);
                    animal.setOwnerName(ownerName);
                    break;
                }
            }
        }
        else if ("remove".equals(action)) {
            animals.removeIf(animal -> animal.getId().equals(id));
        }

        response.sendRedirect(request.getContextPath() + "/catalog");
    }
}
