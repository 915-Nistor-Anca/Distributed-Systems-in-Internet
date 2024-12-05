<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Add Animal</title>
    <style>
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
        }
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f9;;
            color: #333;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        h1 {
            color: #333;
            font-size: 1.5rem;
            margin-top: 1rem;
        }
        form {
            background-color: white;
            padding: 1.5rem;
            border-radius: 0.5rem;
            box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 25rem;
            margin-top: 1rem;
        }
        input[type="text"], input[type="number"] {
            width: 100%;
            padding: 0.5rem;
            margin: 0.5rem 0;
            border: 1px solid #ccc;
            border-radius: 0.3rem;
            font-size: 1rem;
        }
        input[type="submit"] {
            background-color: #00aaef;
            color: white;
            border: none;
            padding: 0.6rem 1.2rem;
            font-size: 1rem;
            border-radius: 0.3rem;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        input[type="submit"]:hover {
            background-color: #008cc1;
        }
        a {
            display: inline-block;
            margin-top: 1rem;
            text-decoration: none;
            color: #00aaef;
            font-size: 1rem;
        }
        a:hover {
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
<body>
<div class="header">
    <div class="title">Animal Catalog</div>
</div>
<h1>Add New Animal</h1>
<form action="<%= request.getContextPath() %>/add-animal" method="post">
    <label>Name: <input type="text" name="name" required></label><br>
    <label>Species: <input type="text" name="species" required></label><br>
    <label>Gender: <input type="text" name="gender" required></label><br>
    <label>Age: <input type="number" name="age" required></label><br>
    <label>Owner Name: <input type="text" name="ownerName" required></label><br>
    <input type="submit" value="Add Animal">
</form>
<a href="catalog">Back to Catalog</a>
</body>
</html>
