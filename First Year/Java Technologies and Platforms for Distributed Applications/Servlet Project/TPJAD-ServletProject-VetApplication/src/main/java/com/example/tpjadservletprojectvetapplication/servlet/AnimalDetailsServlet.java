package com.example.tpjadservletprojectvetapplication.servlet;

import com.example.tpjadservletprojectvetapplication.model.Animal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/animal-details")
public class AnimalDetailsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        List<Animal> animals = (List<Animal>) getServletContext().getAttribute("animals");

        if (animals != null) {
            for (Animal animal : animals) {
                if (animal.getId().equals(id)) {
                    request.setAttribute("animal", animal);
                    break;
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The animal has not been found!");
        }
        request.getRequestDispatcher("/details.jsp").forward(request, response);
    }
}
