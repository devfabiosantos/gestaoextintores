<%-- 
    Document   : login
    Created on : 20/10/2025, 12:59:49
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Login - Gestão de Extintores</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <style>
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            background-color: #f8f9fa;
        }
        .login-card {
            width: 100%;
            max-width: 400px;
        }
    </style>
</head>
<body>
    <div class="card shadow-sm border-0 rounded-3 login-card">
        <div class="card-header bg-primary text-white text-center">
            <h4 class="mb-0">Gestão de Extintores</h4>
        </div>
        <div class="card-body p-4">
            
            <h5 class="card-title text-center mb-4">Acessar o Sistema</h5>

            <form action="${pageContext.request.contextPath}/LoginServlet" method="post">
                
                <c:if test="${not empty mensagemErro}">
                    <div class="alert alert-danger text-center p-2">
                        ${mensagemErro}
                    </div>
                </c:if>

                <div class="mb-3">
                    <label for="login" class="form-label">Login (Usuário)</label>
                    <input type="text" class="form-control" id="login" name="login" required autofocus>
                </div>

                <div class="mb-3">
                    <label for="senha" class="form-label">Senha</label>
                    <input type="password" class="form-control" id="senha" name="senha" required>
                </div>

                <div class="d-grid mt-4">
                    <button type="submit" class="btn btn-primary btn-lg">Entrar</button>
                </div>
            </form>
            
        </div>
    </div>
</body>
</html>