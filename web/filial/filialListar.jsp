<%-- 
    Document   : filialListar
    Created on : 12/10/2025, 20:18:15
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Lista de Filiais</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="card shadow-sm border-0 rounded-3">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                <h4 class="mb-0">Filiais Cadastradas</h4>
                <a href="${pageContext.request.contextPath}/filial/filialCadastrar.jsp" class="btn btn-light btn-sm">+ Nova Filial</a>
            </div>
            <div class="card-body">

                <c:if test="${not empty sessionScope.mensagem}">
                    <div class="alert alert-info text-center">${sessionScope.mensagem}</div>
                    <c:remove var="mensagem" scope="session"/>
                </c:if>

                <c:choose>
                    <c:when test="${empty listaFiliais}">
                        <div class="alert alert-info text-center">Nenhuma filial cadastrada.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                        <table class="table table-striped table-hover">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Nome</th>
                                    <th>Endereço</th>
                                    <th class="text-center">Ações</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="filial" items="${listaFiliais}">
                                    <tr>
                                        <td><c:out value="${filial.idFilial}"/></td>
                                        <td><c:out value="${filial.nome}"/></td>
                                        <td><c:out value="${filial.endereco}"/></td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/FilialCarregar?id=${filial.idFilial}" class="btn btn-sm btn-warning">Editar</a>
                                            <a href="${pageContext.request.contextPath}/FilialExcluir?id=${filial.idFilial}" class="btn btn-sm btn-danger"
                                               onclick="return confirm('Deseja realmente excluir esta filial?');">Excluir</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>
</body>
</html>

