<%-- 
    Document   : setorListar
    Created on : 28/10/2025, 11:31:12
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Gerenciamento de Setores</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-info text-white d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Setores Cadastrados</h4>
            <a href="${pageContext.request.contextPath}/SetorServlet?acao=novo" class="btn btn-light btn-sm">
                + Novo Setor
            </a>
        </div>
        <div class="card-body">

            <c:if test="${not empty sessionScope.mensagemSucesso}"><div class="alert alert-success text-center">${sessionScope.mensagemSucesso}</div><c:remove var="mensagemSucesso" scope="session"/></c:if>
            <c:if test="${not empty sessionScope.mensagemErro}"><div class="alert alert-danger text-center">${sessionScope.mensagemErro}</div><c:remove var="mensagemErro" scope="session"/></c:if>

            <c:choose>
                <c:when test="${empty listaSetores}">
                    <div class="alert alert-info text-center">Nenhum setor cadastrado.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover align-middle">
                            <thead class="table-dark text-center">
                                <tr>
                                    <th>ID Setor</th>
                                    <th>Nome Setor</th>
                                    <th>Filial</th>
                                    <th class="text-center">Ações</th>
                                </tr>
                            </thead>
                            <tbody class="text-center">
                                <c:forEach var="setor" items="${listaSetores}">
                                    <tr>
                                        <td>${setor.idSetor}</td>
                                        <td>${setor.nome}</td>
                                        <td>
                                            <c:if test="${not empty setor.filial}">${setor.filial.nome}</c:if>
                                            <c:if test="${empty setor.filial}">ID: ${setor.idFilial}</c:if> 
                                        </td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/SetorServlet?acao=editar&idSetor=${setor.idSetor}"
                                               class="btn btn-sm btn-warning me-1">Editar</a>
                                            <a href="${pageContext.request.contextPath}/SetorServlet?acao=excluir&idSetor=${setor.idSetor}"
                                               class="btn btn-sm btn-danger"
                                               onclick="return confirm('Atenção: Excluir um setor pode afetar extintores associados. Deseja continuar?');">Excluir</a>
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