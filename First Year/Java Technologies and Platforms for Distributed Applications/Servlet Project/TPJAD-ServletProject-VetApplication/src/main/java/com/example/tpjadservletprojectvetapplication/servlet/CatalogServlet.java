package com.example.tpjadservletprojectvetapplication.servlet;

import com.example.tpjadservletprojectvetapplication.model.Animal;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/")
public class CatalogServlet extends HttpServlet {
    private List<Animal> animals;

    @Override
    public void init() {
        animals = new ArrayList<>();
        animals.add(new Animal(0, "Happy", "Cat", "Male", 1, "Anca Nistor"));
        animals.add(new Animal(1, "Luna", "Cat", "Female", 4, "Ana Maria Pop"));
        animals.add(new Animal(2, "Lucky", "Dog", "Male", 9, "Anca Nistor"));
        animals.add(new Animal(3, "Buddy", "Dog", "Male", 3, "John Smith"));
        animals.add(new Animal(4, "Bella", "Rabbit", "Female", 2, "Emma White"));
        animals.add(new Animal(5, "Max", "Bird", "Male", 5, "Liam Brown"));
        animals.add(new Animal(6, "Charlie", "Dog", "Male", 7, "Olivia Johnson"));
        animals.add(new Animal(7, "Molly", "Cat", "Female", 6, "Sophia Lee"));
        animals.add(new Animal(8, "Rocky", "Hamster", "Male", 1, "Mason Davis"));
        animals.add(new Animal(9, "Daisy", "Fish", "Female", 1, "Isabella Miller"));
        animals.add(new Animal(10, "Chloe", "Parrot", "Female", 4, "Alexander King"));
        animals.add(new Animal(11, "Cooper", "Turtle", "Male", 10, "Ella Scott"));
        animals.add(new Animal(12, "Lola", "Cat", "Female", 5, "Ava Adams"));
        animals.add(new Animal(13, "Simba", "Lion", "Male", 3, "Ethan Turner"));
        animals.add(new Animal(14, "Zoe", "Rabbit", "Female", 1, "Harper Hill"));
        animals.add(new Animal(15, "Baxter", "Dog", "Male", 8, "Jack Walker"));

        ServletContext context = getServletContext();
        context.setAttribute("animals", animals);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int page = 1;
        int pageSize = 5;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try{
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        int start = (page - 1) * pageSize;

        List<Animal> animalsForPage = animals.subList(start, Math.min(start + pageSize, animals.size()));

        int totalAnimals = animals.size();
        int totalPages = (int) Math.ceil(totalAnimals / (double) pageSize);

        request.setAttribute("animals", animalsForPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", page);

        request.getRequestDispatcher("/catalog.jsp").forward(request, response);
    }
}
