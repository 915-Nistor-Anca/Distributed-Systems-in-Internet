package org.example.vetapp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.vetapp.model.Specie;
import org.example.vetapp.service.SpecieService;

import java.io.IOException;
import java.util.List;

@WebServlet("/species")
public class SpecieController extends HttpServlet {
    private SpecieService specieService;

    @Override
    public void init() throws ServletException {
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

            Specie specie = specieService.findSpecieById(id);
            if (specie == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Specie not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":" + specie.getId() + ",\"name\":\"" + specie.getName() + "\",\"description\":\"" + specie.getDescription() + "\"}");
        } else {
            resp.setContentType("application/json");
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("[");

            List<Specie> species = specieService.findAllSpecies();
            for (int i = 0; i < species.size(); i++) {
                Specie specie = species.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(specie.getId()).append(",");
                jsonResponse.append("\"name\":\"").append(specie.getName()).append("\",");
                jsonResponse.append("\"description\":\"").append(specie.getDescription()).append("\"");
                jsonResponse.append("}");

                if (i < species.size() - 1) {
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
        String description = req.getParameter("description");

        if (name == null || description == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing 'name' or 'description' parameter");
            return;
        }

        Specie specie = new Specie();
        specie.setName(name);
        specie.setDescription(description);

        specieService.addSpecie(specie);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write("Specie added successfully");
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

        Specie existingSpecie = specieService.findSpecieById(id);
        if (existingSpecie == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Specie not found");
            return;
        }

        String name = req.getParameter("name");
        String description = req.getParameter("description");

        if (name != null) existingSpecie.setName(name);
        if (description != null) existingSpecie.setDescription(description);

        specieService.updateSpecie(existingSpecie);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Specie updated successfully");
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

        Specie specie = specieService.findSpecieById(id);
        if (specie == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Specie not found");
            return;
        }

        specieService.deleteSpecie(id);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Specie deleted successfully");
    }
}
