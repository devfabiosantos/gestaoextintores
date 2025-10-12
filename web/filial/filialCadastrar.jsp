<%-- 
    Document   : filialCadastrar
    Created on : 10/10/2025, 23:52:51
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Cadastrar Filial</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">

    <div class="container mt-5">
        <div class="card shadow-sm border-0 rounded-3">
            <div class="card-header bg-primary text-white">
                <h4 class="mb-0">Cadastro de Filial</h4>
            </div>
            <div class="card-body">

                <!-- Mensagem de sucesso/erro -->
                <c:if test="${not empty mensagem}">
                    <div class="alert alert-info text-center">${mensagem}</div>
                </c:if>

                <!-- Formulário -->
                <form action="${pageContext.request.contextPath}/FilialCadastrar" method="post" class="needs-validation" novalidate>
                    <div class="mb-3">
                        <label for="nome" class="form-label">Nome da Filial</label>
                        <input type="text" class="form-control" id="nome" name="nome" required>
                        <div class="invalid-feedback">Por favor, informe o nome da filial.</div>
                    </div>

                    <div class="mb-3">
                        <label for="endereco" class="form-label">Endereço</label>
                        <input type="text" class="form-control" id="endereco" name="endereco" required>
                        <div class="invalid-feedback">Por favor, informe o endereço.</div>
                    </div>

                    <div class="d-flex justify-content-between mt-4">
                        <a href="FilialListar" class="btn btn-outline-secondary">Voltar</a>
                        <button type="submit" class="btn btn-primary">Cadastrar</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        // Validação front-end Bootstrap
        (function () {
            'use strict';
            var forms = document.querySelectorAll('.needs-validation');
            Array.prototype.slice.call(forms)
                .forEach(function (form) {
                    form.addEventListener('submit', function (event) {
                        if (!form.checkValidity()) {
                            event.preventDefault();
                            event.stopPropagation();
                        }
                        form.classList.add('was-validated');
                    }, false);
                });
        })();
    </script>

</body>
</html>
