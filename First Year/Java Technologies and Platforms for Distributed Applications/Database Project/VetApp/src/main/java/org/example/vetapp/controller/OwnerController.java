package org.example.vetapp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.vetapp.model.Owner;
import org.example.vetapp.service.OwnerService;

import java.io.IOException;
import java.util.List;

@WebServlet("/owners")
public class OwnerController extends HttpServlet {
    private OwnerService ownerService;

    @Override
    public void init() throws ServletException {
        ownerService = new OwnerService();
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

            Owner owner = ownerService.findOwnerById(id);
            if (owner == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Owner not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":" + owner.getId() + ",\"name\":\"" + owner.getName() + "\",\"email\":\"" + owner.getEmail() + "\",\"phonenumber\":\"" + owner.getPhonenumber() + "\"}");
        } else {
            resp.setContentType("application/json");
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("[");

            List<Owner> owners = ownerService.findAllOwners();
            for (int i = 0; i < owners.size(); i++) {
                Owner owner = owners.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(owner.getId()).append(",");
                jsonResponse.append("\"name\":\"").append(owner.getName()).append("\",");
                jsonResponse.append("\"phonenumber\":\"").append(owner.getPhonenumber()).append("\",");
                jsonResponse.append("\"email\":\"").append(owner.getEmail()).append("\"");
                jsonResponse.append("}");

                if (i < owners.size() - 1) {
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
        String email = req.getParameter("email");

        if (name == null || phonenumber == null || email == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters: 'name', 'phonenumber', and 'email' are required");
            return;
        }

        Owner owner = new Owner();
        owner.setName(name);
        owner.setPhonenumber(phonenumber);
        owner.setEmail(email);

        ownerService.addOwner(owner);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write("Owner added successfully");
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

        Owner existingOwner = ownerService.findOwnerById(id);
        if (existingOwner == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Owner not found");
            return;
        }

        String name = req.getParameter("name");
        String phonenumber = req.getParameter("phonenumber");
        String email = req.getParameter("email");

        if (name != null) existingOwner.setName(name);
        if (phonenumber != null) existingOwner.setPhonenumber(phonenumber);
        if (email != null) existingOwner.setEmail(email);

        ownerService.updateOwner(existingOwner);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Owner updated successfully");
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

        Owner owner = ownerService.findOwnerById(id);
        if (owner == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Owner not found");
            return;
        }

        ownerService.deleteOwner(id);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Owner deleted successfully");
    }
}
