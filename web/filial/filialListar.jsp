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
                
                <a href="${pageContext.request.contextPath}/FilialServlet?acao=novo" class="btn btn-light btn-sm">+ Nova Filial</a>
            </div>
            <div class="card-body">
                
                <%-- --- BLOCO DE MENSAGENS ADICIONADO A PARTIR DO DIA 31/10/2025 AS 19:53 --- --%>
                <c:if test="${not empty sessionScope.mensagemSucesso}">
                    <div class="alert alert-success text-center">${sessionScope.mensagemSucesso}</div>
                    <c:remove var="mensagemSucesso" scope="session"/>
                </c:if>
                <c:if test="${not empty sessionScope.mensagemErro}">
                    <div class="alert alert-danger text-center">${sessionScope.mensagemErro}</div>
                    <c:remove var="mensagemErro" scope="session"/>
                </c:if>
                <%-- --- FIM DO BLOCO --- --%>

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
                                        <th>CNPJ</th>
                                        <th>CEP</th>
                                        <th>Endereço</th>
                                        <th>Cidade/UF</th>
                                        <th class="text-center">Ações</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="filial" items="${listaFiliais}">
                                        <tr>
                                            <td><c:out value="${filial.idFilial}"/></td>
                                            <td><c:out value="${filial.nome}"/></td>
                                            <td><c:out value="${filial.cnpjFormatado}"/></td>
                                            <td><c:out value="${filial.cepFormatado}"/></td>
                                            <td><c:out value="${filial.enderecoCompleto}"/></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty filial.cidade or not empty filial.estado}">
                                                        <c:out value="${filial.cidade}"/>/<c:out value="${filial.estado}"/>
                                                    </c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <a href="${pageContext.request.contextPath}/FilialServlet?acao=editar&idFilial=${filial.idFilial}" class="btn btn-sm btn-warning">Editar</a>
                                                
                                                <a href="${pageContext.request.contextPath}/FilialServlet?acao=excluir&idFilial=${filial.idFilial}" class="btn btn-sm btn-danger"
                                                   onclick="return confirm('Deseja realmente excluir esta filial?');">Excluir</a>
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
