package org.example.vetapp.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.vetapp.model.VeterinaryClinic;
import org.example.vetapp.service.VeterinaryClinicService;

import java.io.IOException;
import java.util.List;

@WebServlet("/veterinary-clinics")
public class VeterinaryClinicController extends HttpServlet {
    private VeterinaryClinicService veterinaryClinicService;

    @Override
    public void init() throws ServletException {
        veterinaryClinicService = new VeterinaryClinicService();
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

            VeterinaryClinic clinic = veterinaryClinicService.findVeterinaryClinicById(id);
            if (clinic == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("Veterinary clinic not found");
                return;
            }

            resp.setContentType("application/json");
            resp.getWriter().write("{\"id\":" + clinic.getId() + ",\"name\":\"" + clinic.getName() + "\",\"address\":\"" + clinic.getAddress() + "\",\"phonenumber\":\"" + clinic.getPhonenumber() + "\"}");
        } else {
            resp.setContentType("application/json");
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("[");

            List<VeterinaryClinic> clinics = veterinaryClinicService.findAllVeterinaryClinics();
            for (int i = 0; i < clinics.size(); i++) {
                VeterinaryClinic clinic = clinics.get(i);
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(clinic.getId()).append(",");
                jsonResponse.append("\"name\":\"").append(clinic.getName()).append("\",");
                jsonResponse.append("\"address\":\"").append(clinic.getAddress()).append("\",");
                jsonResponse.append("\"phonenumber\":\"").append(clinic.getPhonenumber()).append("\"");
                jsonResponse.append("}");

                if (i < clinics.size() - 1) {
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
        String address = req.getParameter("address");
        String phonenumber = req.getParameter("phonenumber");

        if (name == null || address == null || phonenumber == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters: 'name', 'address' and 'phonenumber' are required");
            return;
        }

        VeterinaryClinic clinic = new VeterinaryClinic();
        clinic.setName(name);
        clinic.setAddress(address);
        clinic.setPhonenumber(phonenumber);

        veterinaryClinicService.addVeterinaryClinic(clinic);

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write("Veterinary clinic added successfully!");
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

        VeterinaryClinic existingClinic = veterinaryClinicService.findVeterinaryClinicById(id);
        if (existingClinic == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Veterinary clinic not found");
            return;
        }

        String name = req.getParameter("name");
        String address = req.getParameter("address");
        String phonenumber = req.getParameter("phonenumber");

        boolean updated = false;

        if (name != null) {
            existingClinic.setName(name);
            updated = true;
        }
        if (address != null) {
            existingClinic.setAddress(address);
            updated = true;
        }
        if (phonenumber != null) {
            existingClinic.setPhonenumber(phonenumber);
            updated = true;
        }

        if (!updated) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("No valid parameters to update");
            return;
        }

        veterinaryClinicService.updateVeterinaryClinic(existingClinic);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Veterinary clinic updated successfully");
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

        VeterinaryClinic clinic = veterinaryClinicService.findVeterinaryClinicById(id);
        if (clinic == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Veterinary clinic not found");
            return;
        }

        veterinaryClinicService.deleteVeterinaryClinic(id);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Veterinary clinic deleted successfully");
    }
}
