<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Lista de Extintores</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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

            <form action="${pageContext.request.contextPath}/RemessaServlet" method="post">
                <input type="hidden" name="acao" value="criar"/>

                <c:choose>
                    <c:when test="${empty listaExtintores}">
                        <div class="alert alert-info text-center">Nenhum extintor cadastrado para esta filial.</div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-striped table-hover align-middle">
                                <thead class="table-dark text-center">
                                    <tr>

                                        <c:if test="${sessionScope.usuarioLogado.perfil == 'Técnico'}">
                                            <th>Selecionar</th>
                                        </c:if>
                                        
                                        <th>ID</th>
                                        <th>Nº Controle</th>
                                        <th>Equipamento</th>
                                        <th>Classe</th>
                                        <th>Carga</th>
                                        <th>Localização</th>
                                        <th>Validade</th>
                                        <th>Setor ID</th>
                                        <th>Status ID</th>
                                        <th class="text-center">Ações</th>
                                    </tr>
                                </thead>
                                <tbody class="text-center">
                                    <c:forEach var="extintor" items="${listaExtintores}">
                                        <tr>
                                            
                                            <c:if test="${sessionScope.usuarioLogado.perfil == 'Técnico'}">
                                                <td>
                                                    <input class="form-check-input" type="checkbox" 
                                                           name="extintoresSelecionados" 
                                                           value="${extintor.idExtintor}">
                                                </td>
                                            </c:if>
                                            
                                            <td>${extintor.idExtintor}</td>
                                            <td>${extintor.numeroControle}</td>
                                            <td>${extintor.tipoEquipamento}</td>
                                            <td>${extintor.classeExtintora}</td>
                                            <td>${extintor.cargaNominal}</td>
                                            <td>${extintor.referenciaLocalizacao}</td>
                                            <td><fmt:formatDate value="${extintor.dataValidade}" pattern="dd/MM/yyyy"/></td>
                                            <td>${extintor.idSetor}</td>
                                            <td>${extintor.idStatus}</td>
                                            <td class="text-center">
                                                
                                                <c:if test="${sessionScope.usuarioLogado.perfil == 'Admin'}">
                                                    <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=editar&idExtintor=${extintor.idExtintor}" 
                                                       class="btn btn-sm btn-warning me-1">Editar</a>
                                                    <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=excluir&idExtintor=${extintor.idExtintor}" 
                                                       class="btn btn-sm btn-danger"
                                                       onclick="return confirm('Deseja realmente excluir este extintor?');">Excluir</a>
                                                </c:if>
                                                
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <c:if test="${sessionScope.usuarioLogado.perfil == 'Técnico'}">
                             <div class="d-flex justify-content-end mt-3">
                                <button type="submit" class="btn btn-success">Criar Remessa com Selecionados</button>
                            </div>
                        </c:if>
                        
                    </c:otherwise>
                </c:choose>
            
            </form>

            <div class="mt-3">
                <a href="${pageContext.request.contextPath}/" class="btn btn-outline-secondary">Voltar para Home</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>