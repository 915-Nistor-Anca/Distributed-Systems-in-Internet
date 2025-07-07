package org.example.vetapp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.vetapp.model.Vet;
import org.example.vetapp.service.VetService;

import java.io.IOException;
import java.util.List;

@WebServlet("/vets")
public class VetController extends HttpServlet {
    private VetService vetService;

    @Override
    public void init() throws ServletException {
        vetService = new VetService();
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

            Vet vet = vetService.findVetById(id);
            if (vet == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Vet not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":" + vet.getId() + ",\"name\":\"" + vet.getName() + "\",\"phonenumber\":\"" + vet.getPhonenumber() + "\",\"veterinaryclinicId\":" + vet.getVeterinaryclinicId() + "}");
        } else {
            resp.setContentType("application/json");
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("[");

            List<Vet> vets = vetService.findAllVets();
            for (int i = 0; i < vets.size(); i++) {
                Vet vet = vets.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(vet.getId()).append(",");
                jsonResponse.append("\"name\":\"").append(vet.getName()).append("\",");
                jsonResponse.append("\"phonenumber\":\"").append(vet.getPhonenumber()).append("\",");
                jsonResponse.append("\"veterinaryclinicId\":").append(vet.getVeterinaryclinicId());
                jsonResponse.append("}");

                if (i < vets.size() - 1) {
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
        String phonenumber = req.getParameter("phonenumber");
        String veterinaryclinicIdParam = req.getParameter("veterinaryclinic_id");

        if (name == null || phonenumber == null || veterinaryclinicIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters: 'name', 'phonenumber', and 'veterinaryclinic_id' are required");
            return;
        }

        Long veterinaryclinicId;
        try {
            veterinaryclinicId = Long.parseLong(veterinaryclinicIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid 'veterinaryclinic_id' parameter");
            return;
        }

        Vet vet = new Vet();
        vet.setName(name);
        vet.setPhonenumber(phonenumber);
        vet.setVeterinaryclinicId(veterinaryclinicId);

        int r = vetService.addVet(vet);

        resp.setStatus(HttpServletResponse.SC_CREATED);

        if(r == 1) {
            resp.getWriter().write("Vet added successfully!");
        }
        else {
            resp.getWriter().write("Vet wasn't added because the clinic does not exist.");
        }
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

        Vet existingVet = vetService.findVetById(id);
        if (existingVet == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Vet not found");
            return;
        }

        String name = req.getParameter("name");
        String phonenumber = req.getParameter("phonenumber");
        String veterinaryclinicIdParam = req.getParameter("veterinaryclinic_id");

        boolean updated = false;

        if (name != null) {
            existingVet.setName(name);
            updated = true;
        }
        if (phonenumber != null) {
            existingVet.setPhonenumber(phonenumber);
            updated = true;
        }

        if (veterinaryclinicIdParam != null) {
            try {
                Long veterinaryclinicId = Long.parseLong(veterinaryclinicIdParam);
                existingVet.setVeterinaryclinicId(veterinaryclinicId);
                updated = true;
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid 'veterinaryclinic_id' parameter");
                return;
            }
        }

        if (!updated) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("No valid parameters to update");
            return;
        }

        vetService.updateVet(existingVet);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Vet updated successfully");
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

        Vet vet = vetService.findVetById(id);
        if (vet == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Vet not found");
            return;
        }

        vetService.deleteVet(id);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Vet deleted successfully");
    }
}
