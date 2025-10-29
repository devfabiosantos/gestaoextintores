<%-- 
    Document   : setorEditar
    Created on : 28/10/2025, 11:38:02
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Editar Setor - ID ${setor.idSetor}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">

    <c:if test="${empty setor}">
         <div class="alert alert-danger text-center">Setor não encontrado ou acesso negado.</div>
         <a href="${pageContext.request.contextPath}/SetorServlet?acao=listar" class="btn btn-primary">Voltar para Lista</a>
    </c:if>

    <c:if test="${not empty setor}">
        <div class="card shadow-sm border-0 rounded-3">
            <div class="card-header bg-warning text-dark">
                <h4 class="mb-0">Editar Setor</h4>
            </div>
            <div class="card-body">

                <c:if test="${not empty mensagemErro}">
                    <div class="alert alert-danger text-center">${mensagemErro}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/SetorServlet" method="post" class="needs-validation" novalidate>
                    <input type="hidden" name="acao" value="atualizar" />
                    <input type="hidden" name="idSetor" value="${setor.idSetor}" />

                    <div class="mb-3">
                        <label for="nome" class="form-label">Nome do Setor*</label>
                        <input type="text" class="form-control" id="nome" name="nome" value="${setor.nome}" required>
                        <div class="invalid-feedback">O nome do setor é obrigatório.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Filial:</label>
                        <p class="form-control-plaintext">
                            <c:if test="${not empty setor.filial}">${setor.filial.nome}</c:if>
                            <c:if test="${empty setor.filial}">ID: ${setor.idFilial} (Nome não carregado)</c:if>
                        </p>
                    </div>

                    <div class="d-flex justify-content-end mt-4">
                        <a href="${pageContext.request.contextPath}/SetorServlet?acao=listar" class="btn btn-outline-secondary me-2">Cancelar</a>
                        <button type="submit" class="btn btn-warning">Salvar Alterações</button>
                    </div>
                </form>
            </div>
        </div>
    </c:if> 
</div>

<script>
    (function () { 'use strict'; 
        var forms = document.querySelectorAll('.needs-validation'); 
        Array.prototype.slice.call(forms).forEach(function (form) { 
            form.addEventListener('submit', function (event) { 
                if (!form.checkValidity()) { 
                    event.preventDefault(); 
                    event.stopPropagation(); } 
                form.classList.add('was-validated'); 
            }, false); 
        }); 
    })();
</script>
</body>
</html>