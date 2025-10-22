<%-- 
    Document   : usuarioCadastrar
    Created on : 22/10/2025, 12:28:16
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Cadastrar Usuário</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-primary text-white">
            <h4 class="mb-0">Cadastrar Novo Usuário</h4>
        </div>
        <div class="card-body">

            <c:if test="${not empty mensagemErro}">
                <div class="alert alert-danger text-center">${mensagemErro}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/UsuarioServlet" method="post" class="needs-validation" novalidate>
                <input type="hidden" name="acao" value="salvar" />

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="nome" class="form-label">Nome Completo*</label>
                        <input type="text" class="form-control" id="nome" name="nome" required>
                        <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="login" class="form-label">Login (Nome de Usuário)*</label>
                        <input type="text" class="form-control" id="login" name="login" required>
                        <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>

                 <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="senha" class="form-label">Senha*</label>
                        <input type="password" class="form-control" id="senha" name="senha" required>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                     <div class="col-md-6">
                        <label for="perfil" class="form-label">Perfil*</label>
                        <select class="form-select" id="perfil" name="perfil" required onchange="toggleFilial()">
                             <option value="">Selecione...</option>
                             <option value="Admin">Admin</option>
                             <option value="Técnico">Técnico</option>
                         </select>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>

                <div class="row mb-3" id="filialRow" style="display: none;"> 
                    <div class="col-md-12">
                        <label for="idFilial" class="form-label">Filial (Obrigatório para Técnico)*</label>
                        <select class="form-select" id="idFilial" name="idFilial">
                            <option value="">Selecione...</option>
                            <c:forEach var="filial" items="${listaFiliais}">
                                <option value="${filial.idFilial}">${filial.nome}</option>
                            </c:forEach>
                        </select>
                         <div class="invalid-feedback">Obrigatório para Técnicos.</div>
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <a href="${pageContext.request.contextPath}/UsuarioServlet?acao=listar" class="btn btn-outline-secondary me-2">Cancelar</a>
                    <button type="submit" class="btn btn-primary">Cadastrar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function toggleFilial() {
        const perfilSelect = document.getElementById('perfil');
        const filialRow = document.getElementById('filialRow');
        const filialSelect = document.getElementById('idFilial');

        if (perfilSelect.value === 'Técnico') {
            filialRow.style.display = 'flex';
            filialSelect.required = true;
        } else {
            filialRow.style.display = 'none';
            filialSelect.required = false;
            filialSelect.value = '';
        }
    }
    document.addEventListener('DOMContentLoaded', toggleFilial);

    (function () { 'use strict'; var forms = document.querySelectorAll('.needs-validation'); Array.prototype.slice.call(forms).forEach(function (form) { form.addEventListener('submit', function (event) { if (!form.checkValidity()) { event.preventDefault(); event.stopPropagation(); } form.classList.add('was-validated'); }, false); }); })();
</script>
</body>
</html>