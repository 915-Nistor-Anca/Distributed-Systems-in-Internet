package com.example.tpjadservletprojectvetapplication.servlet;

import com.example.tpjadservletprojectvetapplication.model.Animal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/add-animal")
public class AddAnimalServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/addAnimal.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String species = request.getParameter("species");
        String gender = request.getParameter("gender");
        Integer age = Integer.parseInt(request.getParameter("age"));
        String ownerName = request.getParameter("ownerName");

        List<Animal> animals = (List<Animal>) getServletContext().getAttribute("animals");
        if (animals == null) {
            animals = new ArrayList<>();
        }

        Integer id = animals.size() > 0 ? animals.get(animals.size() - 1).getId() + 1 : 0;
        Animal newAnimal = new Animal(id, name, species, gender, age, ownerName);


        animals.add(newAnimal);
        getServletContext().setAttribute("animals", animals);

        response.sendRedirect("catalog");
    }
}
