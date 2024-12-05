<%@ page import="com.example.tpjadservletprojectvetapplication.model.Animal" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Animal Details</title>
  <style>
    html, body {
      margin: 0;
      padding: 0;
      width: 100%;
    }
    body {
      font-family: 'Arial', sans-serif;
      background-color: #f4f4f9;
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
    .details-container {
      background-color: white;
      padding: 2rem;
      border-radius: 0.5rem;
      box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.1);
      width: 80%;
      max-width: 31.25rem;
      margin-top: 1rem;
    }
    p {
      font-size: 1rem;
      line-height: 1.5;
      margin: 0.75rem 0;
    }
    a {
      display: inline-block;
      margin-top: 1.5rem;
      padding: 0.5rem 1rem;
      color: white;
      background-color: #00aaef;
      border-radius: 0.25rem;
      text-decoration: none;
      font-size: 1rem;
      text-align: center;
    }
    a:hover {
      background-color: #008fcc;
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
<div class="details-container">
  <h1>Animal Details</h1>
  <%
    Animal animal = (Animal) request.getAttribute("animal");
  %>
  <p><strong>Name:</strong> <%= animal.getName() %></p>
  <p><strong>Gender:</strong> <%= animal.getGender() %></p>
  <p><strong>Specie:</strong> <%= animal.getSpecie() %></p>
  <p><strong>Age:</strong> <%= animal.getAge() %></p>
  <p><strong>Owner name:</strong> <%= animal.getOwnerName() %></p>
  <a href="catalog">Back to the catalog</a>
</div>
</body>
</html>
