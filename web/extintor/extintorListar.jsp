<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Lista de Extintores</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">

    <div class="container mt-5">
        <div class="card shadow-sm border-0 rounded-3">
            <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center">
                <h4 class="mb-0">Extintores Cadastrados</h4>
                
                <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=novo" class="btn btn-light btn-sm">
                    + Novo Extintor
                </a>
            </div>

            <div class="card-body">

                <c:if test="${not empty sessionScope.mensagem}">
                    <div class="alert alert-info text-center">${sessionScope.mensagem}</div>
                    <c:remove var="mensagem" scope="session"/>
                </c:if>

                <form action="${pageContext.request.contextPath}/ExtintorServlet" method="get" class="mb-4">
                    <input type="hidden" name="acao" value="listar" /> 
                    
                    <div class="row g-3 align-items-end">
                        <div class="col-md-6">
                            <label for="idFilial" class="form-label">Filtrar por Filial:</label>
                            <select name="idFilial" id="idFilial" class="form-select">
                                <option value="">Todas as Filiais</option>
                                <c:forEach var="filial" items="${listaFiliais}">
                                    <option value="${filial.idFilial}"
                                        <c:if test="${idFilialSelecionada == filial.idFilial}">selected</c:if>>
                                        <c:out value="${filial.nome}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100">Filtrar</button>
                        </div>
                    </div>
                </form>

                <c:choose>
                    <c:when test="${empty listaExtintores}">
                        <div class="alert alert-info text-center">Nenhum extintor cadastrado.</div>
                    </c:when>

                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-striped table-hover align-middle">
                                <thead class="table-dark text-center">
                                    <tr>
                                        <th>ID</th>
                                        <th>Nº Controle</th>
                                        <th>Tipo</th>
                                        <th>Data Recarga</th>
                                        <th>Validade</th>
                                        <th>Localização</th>
                                        <th>Filial</th>
                                        <th class="text-center">Ações</th>
                                    </tr>
                                </thead>
                                <tbody class="text-center">
                                    <c:forEach var="extintor" items="${listaExtintores}">
                                        <tr>
                                            <td><c:out value="${extintor.idExtintor}"/></td>
                                            <td><c:out value="${extintor.numeroControle}"/></td>
                                            <td><c:out value="${extintor.tipo}"/></td>
                                            <td><fmt:formatDate value="${extintor.dataRecarga}" pattern="dd/MM/yyyy"/></td>
                                            <td><fmt:formatDate value="${extintor.dataValidade}" pattern="dd/MM/yyyy"/></td>
                                            <td><c:out value="${extintor.localizacao}"/></td>
                                            <td><c:out value="${extintor.idFilial}"/></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=editar&idExtintor=${extintor.idExtintor}" 
                                                   class="btn btn-sm btn-warning me-1">Editar</a>
                                                
                                                <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=excluir&idExtintor=${extintor.idExtintor}" 
                                                   class="btn btn-sm btn-danger"
                                                   onclick="return confirm('Deseja realmente excluir este extintor?');">
                                                    Excluir
                                                </a>
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