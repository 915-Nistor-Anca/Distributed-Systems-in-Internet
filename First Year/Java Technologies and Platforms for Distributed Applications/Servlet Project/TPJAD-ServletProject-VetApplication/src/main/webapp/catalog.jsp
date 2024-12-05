<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.tpjadservletprojectvetapplication.model.Animal" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Animal Catalog</title>
    <style>
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
        }
        body {
            font-family: 'Arial', serif;
            background-color: #f4f4f9;
            color: #333;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
        }
        h1 {
            text-align: center;
            margin-top: 1.25rem;
            color: #333;
        }
        table {
            width: 80%;
            margin: 1.25rem auto;
            box-shadow: 0 0.25rem 0.375rem rgba(0, 0, 0, 0.1);
            background-color: white;
            border-radius: 0.5rem;
            border: 0.03rem solid #333;
            max-width: 40.25rem;
        }
        th, td {
            padding: 0.625rem;
            text-align: center;
        }
        th {
            background-color: #00aaef;
            color: white;
        }
        tr:nth-child(even) {
            background-color: #f2f2f2;
        }
        tr:hover {
            background-color: #ddd;
        }
        a {
            color: #00aaef;
            text-decoration: none;
            padding: 0.3125rem 0.625rem;
            margin: 0.3rem;
            border: 0.05rem solid #00aaef;
            border-radius: 0.3rem;
        }
        a:hover {
            background-color: #00aaef;
            color: white;
        }
        .prev {
            border:none;
            color: black;
            font-size: larger;
        }
        .header {
            width: 100%;
            height: 3rem;
            background-color: #00aaef;
            display: flex;
            align-items: center;
            color: white;
            font-size: 1.5rem;
            font-weight: bold;
        }
        .title{
            margin-left: 1rem;
            font-weight: bolder;
        }
    </style>
</head>
<body>
<div class="header">
    <div class="title">Animal Catalog</div>
</div>
<h1>List of animals</h1>
<table>
    <tr>
        <th>Id</th>
        <th>Specie</th>
        <th>Name</th>
        <th>Actions</th>
    </tr>
    <%
        List<Animal> animals = (List<Animal>) request.getAttribute("animals");
        for (Animal animal : animals) {
    %>
    <tr>
        <td><%= animal.getId() %></td>
        <td><%= animal.getSpecie() %></td>
        <td><%= animal.getName() %></td>
        <td>
            <a href="animal-details?id=<%= animal.getId() %>">Details</a>
            <a href="manage?id=<%= animal.getId() %>&action=edit">Edit</a>
        </td>
    </tr>
    <%
        }
    %>
</table>
<%
    int currentPage = (int) request.getAttribute("currentPage");
    int totalPages = (int) request.getAttribute("totalPages");
%>
<div>
    <a class="prev" href="?page=<%= currentPage - 1 %>" <%= currentPage <= 1 ? "style='visibility:hidden'" : "" %>><</a>
    <span>Page <%= currentPage %> of <%= totalPages %></span>
    <a class="prev" href="?page=<%= currentPage + 1 %>" <%= currentPage >= totalPages ? "style='visibility:hidden'" : "" %>>></a>
</div>

<a href="add-animal">Add Animal</a>
</body>
</html>
