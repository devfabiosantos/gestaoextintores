<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Editar Extintor</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-warning text-dark">
            <h4 class="mb-0">Editar Extintor</h4>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/ExtintorServlet" method="post" class="needs-validation" novalidate>
                <input type="hidden" name="acao" value="atualizar" />
                <input type="hidden" name="idExtintor" value="${extintor.idExtintor}" />

                 <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="numeroControle" class="form-label">Número de Controle*</label>
                        <input type="text" class="form-control" id="numeroControle" name="numeroControle" value="${extintor.numeroControle}" required>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="tipoEquipamento" class="form-label">Tipo Equipamento*</label>
                        <select class="form-select" id="tipoEquipamento" name="tipoEquipamento" required>
                             <option value="">Selecione...</option>
                             <option value="Portátil" ${extintor.tipoEquipamento == 'Portátil' ? 'selected' : ''}>Portátil</option>
                             <option value="Carreta" ${extintor.tipoEquipamento == 'Carreta' ? 'selected' : ''}>Carreta</option>
                         </select>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>

                 <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="classeExtintora" class="form-label">Classe Extintora*</label>
                        <select class="form-select" id="classeExtintora" name="classeExtintora" required>
                             <option value="">Selecione...</option>
                             <option value="Água" ${extintor.classeExtintora == 'Água' ? 'selected' : ''}>Água</option>
                             <option value="Espuma" ${extintor.classeExtintora == 'Espuma' ? 'selected' : ''}>Espuma</option>
                             <option value="CO2" ${extintor.classeExtintora == 'CO2' ? 'selected' : ''}>CO2</option>
                             <option value="Pó Químico BC" ${extintor.classeExtintora == 'Pó Químico BC' ? 'selected' : ''}>Pó Químico BC</option>
                             <option value="Pó Químico ABC" ${extintor.classeExtintora == 'Pó Químico ABC' ? 'selected' : ''}>Pó Químico ABC</option>
                         </select>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                     <div class="col-md-6">
                        <label for="cargaNominal" class="form-label">Carga Nominal*</label>
                        <input type="text" class="form-control" id="cargaNominal" name="cargaNominal" value="${extintor.cargaNominal}" required>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="dataRecarga" class="form-label">Data de Recarga</label>
                        <input type="date" class="form-control" id="dataRecarga" name="dataRecarga" value="<fmt:formatDate value='${extintor.dataRecarga}' pattern='yyyy-MM-dd'/>">
                    </div>
                    <div class="col-md-6">
                        <label for="dataValidade" class="form-label">Data de Validade*</label>
                        <input type="date" class="form-control" id="dataValidade" name="dataValidade" value="<fmt:formatDate value='${extintor.dataValidade}' pattern='yyyy-MM-dd'/>" required>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>

                <div class="row mb-3">
                     <div class="col-md-12">
                        <label for="referenciaLocalizacao" class="form-label">Referência de Localização*</label>
                        <input type="text" class="form-control" id="referenciaLocalizacao" name="referenciaLocalizacao" value="${extintor.referenciaLocalizacao}" required>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>
                
                 <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="idSetor" class="form-label">Setor*</label>
                        <select class="form-select" id="idSetor" name="idSetor" required>
                            <option value="">Selecione...</option>
                            <%-- O Servlet (doGet, acao=editar) DEVE enviar a 'listaSetores' --%>
                            <c:forEach var="setor" items="${listaSetores}">
                                <option value="${setor.idSetor}" ${extintor.idSetor == setor.idSetor ? 'selected' : ''}>${setor.nome}</option>
                            </c:forEach>
                        </select>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                     <div class="col-md-6">
                        <label for="idStatus" class="form-label">Status*</label>
                        <select class="form-select" id="idStatus" name="idStatus" required>
                             <option value="">Selecione...</option>
                              <%-- O Servlet (doGet, acao=editar) DEVE enviar a 'listaStatus' --%>
                             <c:forEach var="status" items="${listaStatus}">
                                <option value="${status.idStatus}" ${extintor.idStatus == status.idStatus ? 'selected' : ''}>${status.nome}</option>
                            </c:forEach>
                         </select>
                         <div class="invalid-feedback">Obrigatório.</div>
                    </div>
                </div>

                <div class="row mb-3">
                     <div class="col-md-12">
                        <label for="observacao" class="form-label">Observação</label>
                        <textarea class="form-control" id="observacao" name="observacao" rows="3">${extintor.observacao}</textarea>
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=listar" class="btn btn-outline-secondary me-2">Cancelar</a>
                    <button type="submit" class="btn btn-warning">Salvar Alterações</button>
                </div>
            </form>
        </div>
    </div>
</div>
<script> (function () { 'use strict'; var forms = document.querySelectorAll('.needs-validation'); Array.prototype.slice.call(forms).forEach(function (form) { form.addEventListener('submit', function (event) { if (!form.checkValidity()) { event.preventDefault(); event.stopPropagation(); } form.classList.add('was-validated'); }, false); }); })(); </script>
</body>
</html>