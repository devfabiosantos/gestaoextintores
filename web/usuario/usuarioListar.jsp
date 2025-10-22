<%-- 
    Document   : usuarioListar
    Created on : 22/10/2025, 12:27:13
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Gerenciamento de Usuários</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-secondary text-white d-flex justify-content-between align-items-center">
            <h4 class="mb-0">Usuários Cadastrados</h4>
            <a href="${pageContext.request.contextPath}/UsuarioServlet?acao=novo" class="btn btn-light btn-sm">
                + Novo Usuário
            </a>
        </div>
        <div class="card-body">

            <c:choose>
                <c:when test="${empty listaUsuarios}">
                    <div class="alert alert-info text-center">Nenhum usuário cadastrado.</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-striped table-hover align-middle">
                            <thead class="table-dark text-center">
                                <tr>
                                    <th>ID</th>
                                    <th>Nome</th>
                                    <th>Login</th>
                                    <th>Perfil</th>
                                    <th>ID Filial</th>
                                    <th class="text-center">Ações</th>
                                </tr>
                            </thead>
                            <tbody class="text-center">
                                <c:forEach var="usuario" items="${listaUsuarios}">
                                    <tr>
                                        <td>${usuario.idUsuario}</td>
                                        <td>${usuario.nome}</td>
                                        <td>${usuario.login}</td>
                                        <td>${usuario.perfil}</td>
                                        <td>${usuario.idFilial != null ? usuario.idFilial : '-'}</td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/UsuarioServlet?acao=editar&idUsuario=${usuario.idUsuario}"
                                               class="btn btn-sm btn-warning me-1">Editar</a>
                                            <c:if test="${sessionScope.usuarioLogado.idUsuario != usuario.idUsuario}">
                                                <a href="${pageContext.request.contextPath}/UsuarioServlet?acao=excluir&idUsuario=${usuario.idUsuario}"
                                                   class="btn btn-sm btn-danger"
                                                   onclick="return confirm('Deseja realmente excluir este usuário?');">Excluir</a>
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