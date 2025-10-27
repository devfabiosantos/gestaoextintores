<%-- 
    Document   : remessaDetalhe
    Created on : 23/10/2025, 13:02:56
    Author     : Dev Fabio Santos
--%>

<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Detalhes da Remessa - ID ${remessa.idRemessa}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5 mb-5">
    
    <c:if test="${empty remessa}">
         <div class="alert alert-danger text-center">Remessa não encontrada ou acesso negado.</div>
         <a href="${pageContext.request.contextPath}/RemessaServlet?acao=listar" class="btn btn-primary">Voltar para Lista</a>
    </c:if>

    <c:if test="${not empty remessa}">
        <div class="card shadow-sm border-0 rounded-3">
            <div class="card-header bg-info text-white d-flex justify-content-between align-items-center">
                <h4 class="mb-0">Detalhes da Remessa ID: ${remessa.idRemessa}</h4>
            </div>
            
            <div class="card-body">
                <h5>Informações Gerais</h5>
                <dl class="row">
                  <dt class="col-sm-3">Status Atual:</dt>
                  <dd class="col-sm-9">${remessa.statusRemessa}</dd>

                  <dt class="col-sm-3">Data Criação:</dt>
                  <dd class="col-sm-9"><fmt:formatDate value="${remessa.dataCriacao}" pattern="dd/MM/yyyy HH:mm:ss"/></dd>

                  <dt class="col-sm-3">Filial:</dt>
                  <dd class="col-sm-9">
                      <c:if test="${not empty remessa.filial}">${remessa.filial.nome}</c:if>
                      <c:if test="${empty remessa.filial}">ID: ${remessa.idFilial}</c:if> <%-- Fallback --%>
                  </dd>

                   <dt class="col-sm-3">Técnico Solicitante:</dt>
                  <dd class="col-sm-9">
                      <c:if test="${not empty remessa.tecnico}">${remessa.tecnico.nome}</c:if>
                      <c:if test="${empty remessa.tecnico}">ID: ${remessa.idUsuarioTecnico}</c:if>
                  </dd>

                  <c:if test="${not empty remessa.idUsuarioAdmin}">
                      <dt class="col-sm-3">Admin Aprovador:</dt>
                      <dd class="col-sm-9">
                          <c:if test="${not empty remessa.admin}">${remessa.admin.nome}</c:if>
                          <c:if test="${empty remessa.admin}">ID: ${remessa.idUsuarioAdmin}</c:if>
                      </dd>

                      <dt class="col-sm-3">Data Aprovação:</dt>
                      <dd class="col-sm-9"><fmt:formatDate value="${remessa.dataAprovacao}" pattern="dd/MM/yyyy HH:mm:ss"/></dd>
                  </c:if>
                </dl>

                <hr/>

                 <h5>Resumo de Itens por Classe Extintora</h5>
                 <c:choose>
                    <c:when test="${empty resumoItens}">
                        <p class="text-muted">Nenhum item encontrado nesta remessa.</p>
                    </c:when>
                    <c:otherwise>
                        <table class="table table-sm table-bordered" style="width: auto;">
                            <thead class="table-light">
                                <tr><th>Classe Extintora</th><th>Quantidade</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="itemResumo" items="${resumoItens}">
                                    <tr><td>${itemResumo.classe_extintora}</td><td class="text-end">${itemResumo.quantidade}</td></tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                 </c:choose>

                 <hr/>

                 <h5>Lista Detalhada de Itens</h5>
                  <c:choose>
                    <c:when test="${empty listaItensDetalhada}">
                         <p class="text-muted">Nenhum item encontrado nesta remessa.</p>
                    </c:when>
                    <c:otherwise>
                         <table class="table table-sm table-striped">
                            <thead class="table-light">
                                <tr>
                                    <th>ID Item</th>
                                    <th>Extintor (Nº Controle)</th>
                                    <th>Classe</th>
                                    <th>Observação do Técnico</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="itemDetalhe" items="${listaItensDetalhada}">
                                    <tr>
                                        <td>${itemDetalhe.idRemessaItem}</td>
                                        <td>
                                            <c:if test="${not empty itemDetalhe.extintor}">
                                                ${itemDetalhe.extintor.numeroControle} (ID: ${itemDetalhe.idExtintor})
                                            </c:if>
                                             <c:if test="${empty itemDetalhe.extintor}">
                                                ID: ${itemDetalhe.idExtintor}
                                            </c:if>
                                        </td>
                                         <td>
                                             <c:if test="${not empty itemDetalhe.extintor}">
                                                ${itemDetalhe.extintor.classeExtintora}
                                            </c:if>
                                         </td>
                                        <td>${itemDetalhe.observacaoTecnico}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                         </table>
                    </c:otherwise>
                  </c:choose>

                <hr/>

                <div class="mt-3">
                    <a href="${pageContext.request.contextPath}/RemessaServlet?acao=listar" class="btn btn-outline-secondary">Voltar para Lista de Remessas</a>
                </div>

            </div>
        </div>
    </c:if>
    
</div>
</body>
</html>