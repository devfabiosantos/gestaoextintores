<%-- 
    Document   : mensagem
    Created on : 12/10/2025, 20:20:16
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Mensagem</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="alert alert-info">
            <h5>Mensagem</h5>
            <p>${mensagem}</p>
            <a href="${pageContext.request.contextPath}/FilialListar" class="btn btn-primary">Voltar à Lista</a>
        </div>
    </div>
</body>
</html>

