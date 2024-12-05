<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.tpjadservletprojectvetapplication.model.Animal" %>

<%
  Animal animal = (Animal) request.getAttribute("animal");
%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Edit Animal</title>
  <style>
    html, body {
      margin: 0;
      padding: 0;
      width: 100%;
    }
    body {
      font-family: 'Arial', sans-serif;
      background-color: #f4f4f9;;
      color: #333;
      display: flex;
      flex-direction: column;
      align-items: center;
      min-height: 100vh;
    }
    h1 {
      text-align: center;
      color: #00aaef;
      margin-bottom: 1.5rem;
    }
    .form-container {
      background-color: white;
      padding: 2rem;
      border-radius: 0.5rem;
      box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1);
      width: 80%;
      max-width: 31.25rem;
      margin-top: 1rem;
    }
    label {
      font-weight: bold;
      display: block;
      margin-top: 1rem;
      font-size: 1rem;
    }
    input[type="text"] {
      width: 100%;
      padding: 0.5rem;
      margin-top: 0.3rem;
      border: 1px solid #ddd;
      border-radius: 0.25rem;
      font-size: 1rem;
    }
    input[type="submit"] {
      background-color: #00aaef;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      margin-top: 1.5rem;
      border-radius: 0.25rem;
      font-size: 1rem;
      cursor: pointer;
    }
    input[type="submit"]:hover {
      background-color: #008fcc;
    }
    .back-link {
      display: inline-block;
      margin-top: 1.5rem;
      text-align: center;
      width: 100%;
      color: #00aaef;
      text-decoration: none;
      font-size: 1rem;
    }
    .back-link:hover {
      text-decoration: underline;
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
<div class="header">
  <div class="title">Animal Catalog</div>
</div>
<body>
<div class="form-container">
  <h1>Edit Animal</h1>

  <form action="<%= request.getContextPath() %>/manage" method="post">
    <input type="hidden" name="action" value="edit">
    <% if (animal != null) { %>
    <input type="hidden" name="id" value="<%= animal.getId() %>">
    <% } %>
    <label>Name:</label>
    <input type="text" name="name" value="<%= animal != null ? animal.getName() : "" %>">

    <label>Species:</label>
    <input type="text" name="species" value="<%= animal != null ? animal.getSpecie() : "" %>">

    <label>Gender:</label>
    <input type="text" name="gender" value="<%= animal != null ? animal.getGender() : "" %>">

    <label>Age:</label>
    <input type="text" name="age" value="<%= animal != null ? animal.getAge() : "" %>">

    <label>Owner Name:</label>
    <input type="text" name="ownerName" value="<%= animal != null ? animal.getOwnerName() : "" %>">

    <input type="submit" value="Save Changes">
  </form>

  <form action="<%= request.getContextPath() %>/manage" method="post">
    <input type="hidden" name="action" value="remove">
    <% if (animal != null) { %>
    <input type="hidden" name="id" value="<%= animal.getId() %>">
    <% } %>
    <input type="submit" value="Delete Animal" style="background-color: #e74c3c;">
  </form>

  <a href="catalog" class="back-link">Back to Catalog</a>
</div>
</body>
</html>
