<%-- 
    Document   : remessaListar
    Created on : 22/10/2025, 14:39:28
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Lista de Remessas</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-success text-white d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Remessas para Orçamento/Recarga</h4>
        </div>
        <div class="card-body">

            <c:if test="${not empty sessionScope.mensagemSucesso}">
                <div class="alert alert-success text-center">${sessionScope.mensagemSucesso}</div>
                <c:remove var="mensagemSucesso" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.mensagemErro}">
                <div class="alert alert-danger text-center">${sessionScope.mensagemErro}</div>
                <c:remove var="mensagemErro" scope="session"/>
            </c:if>

            <c:choose>
                <c:when test="${empty listaRemessas}">
                    <div class="alert alert-info text-center">Nenhuma remessa encontrada.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover align-middle">
                            <thead class="table-dark text-center">
                                <tr>
                                    <th>ID Remessa</th>
                                    <th>Data Criação</th>
                                    <th>Filial ID</th>
                                    <th>Técnico ID</th>
                                    <th>Status</th>
                                    <th>Data Aprovação</th>
                                    <th>Admin ID</th>
                                    <th class="text-center">Ações</th>
                                </tr>
                            </thead>
                            <tbody class="text-center">
                                <c:forEach var="remessa" items="${listaRemessas}">
                                    <tr>
                                        <td>${remessa.idRemessa}</td>
                                        <td><fmt:formatDate value="${remessa.dataCriacao}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>${remessa.idFilial}</td>
                                        <td>${remessa.idUsuarioTecnico}</td>
                                        <td>${remessa.statusRemessa}</td>
                                        <td><fmt:formatDate value="${remessa.dataAprovacao}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td>${remessa.idUsuarioAdmin != null ? remessa.idUsuarioAdmin : '-'}</td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/RemessaServlet?acao=detalhar&idRemessa=${remessa.idRemessa}"
                                               class="btn btn-sm btn-info me-1">Detalhar</a>

                                            <c:if test="${sessionScope.usuarioLogado.perfil == 'Admin' and remessa.statusRemessa == 'Enviado'}">
                                                <form action="${pageContext.request.contextPath}/RemessaServlet" method="post" style="display: inline;">
                                                    <input type="hidden" name="acao" value="aprovar"/>
                                                    <input type="hidden" name="idRemessa" value="${remessa.idRemessa}"/>
                                                    <button type="submit" class="btn btn-sm btn-success"
                                                            onclick="return confirm('Confirmar aprovação desta remessa? Os extintores serão marcados como Em Recarga.');">
                                                        Aprovar
                                                    </button>
                                                </form>
                                            </c:if>

                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>

            <div class="mt-3">
                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary">Voltar para Home</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>