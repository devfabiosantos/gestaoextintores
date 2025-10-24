<%-- 
    Document   : remessaRecebimento
    Created on : 23/10/2025, 19:16:24
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Confirmar Recebimento - Remessa ID ${param.idRemessa}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5 mb-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-info text-white">
            <h4 class="mb-0">Confirmar Recebimento da Remessa ID: ${param.idRemessa}</h4>
        </div>
        <div class="card-body">

            <c:if test="${not empty mensagemErro}">
                <div class="alert alert-danger text-center">${mensagemErro}</div>
            </c:if>

            <p>Por favor, informe as datas de recarga e validade conforme as etiquetas dos extintores recebidos.</p>
>
            <form action="${pageContext.request.contextPath}/RemessaServlet" method="post" class="needs-validation" novalidate>
                <input type="hidden" name="acao" value="finalizarRecebimento" />
                <input type="hidden" name="idRemessa" value="${param.idRemessa}" />

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="dataRecargaReal" class="form-label">Data de Recarga Efetiva*</label>
                        <input type="date" class="form-control" id="dataRecargaReal" name="dataRecargaReal" required>
                        <div class="invalid-feedback">Informe a data real da recarga.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="novaDataValidade" class="form-label">Nova Data de Validade*</label>
                        <input type="date" class="form-control" id="novaDataValidade" name="novaDataValidade" required>
                        <div class="invalid-feedback">Informe a nova data de validade.</div>
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <%-- Botão Cancelar volta para a lista de remessas --%>
                    <a href="${pageContext.request.contextPath}/RemessaServlet?acao=listar" class="btn btn-outline-secondary me-2">Cancelar</a>
                    <button type="submit" class="btn btn-info">Confirmar Recebimento Final</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    (function () { 'use strict'; var forms = document.querySelectorAll('.needs-validation'); Array.prototype.slice.call(forms).forEach(function (form) { form.addEventListener('submit', function (event) { if (!form.checkValidity()) { event.preventDefault(); event.stopPropagation(); } form.classList.add('was-validated'); }, false); }); })();
</script>
</body>
</html>