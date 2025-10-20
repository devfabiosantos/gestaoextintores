<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Cadastrar Extintor</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="card shadow-sm border-0 rounded-3">
        <div class="card-header bg-primary text-white">
            <h4 class="mb-0">Cadastrar Novo Extintor</h4>
        </div>
        <div class="card-body">
            <c:if test="${not empty mensagem}">
                <div class="alert alert-danger text-center">${mensagem}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/ExtintorServlet" method="post" class="needs-validation" novalidate>
                <input type="hidden" name="acao" value="salvar" />
                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="numeroControle" class="form-label">Número de Controle*</label>
                        <input type="text" class="form-control" id="numeroControle" name="numeroControle" value="${extintor.numeroControle}" required>
                        <div class="invalid-feedback">Por favor, informe o número de controle.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="tipo" class="form-label">Tipo*</label>
                        <select class="form-select" id="tipo" name="tipo" required>
                            <option value="">Selecione...</option>
                            <option value="Água" ${extintor.tipo == 'Água' ? 'selected' : ''}>Água</option>
                            <option value="Espuma" ${extintor.tipo == 'Espuma' ? 'selected' : ''}>Espuma</option>
                            <option value="CO2" ${extintor.tipo == 'CO2' ? 'selected' : ''}>CO2</option>
                            <option value="Pó Químico" ${extintor.tipo == 'Pó Químico' ? 'selected' : ''}>Pó Químico</option>
                            <option value="Pó ABC" ${extintor.tipo == 'Pó ABC' ? 'selected' : ''}>Pó ABC</option>
                        </select>
                        <div class="invalid-feedback">Por favor, selecione o tipo.</div>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="dataRecarga" class="form-label">Data de Recarga*</label>
                        <input type="date" class="form-control" id="dataRecarga" name="dataRecarga" value="<fmt:formatDate value="${extintor.dataRecarga}" pattern="yyyy-MM-dd"/>" required>
                        <div class="invalid-feedback">Por favor, informe a data de recarga.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="dataValidade" class="form-label">Data de Validade*</label>
                        <input type="date" class="form-control" id="dataValidade" name="dataValidade" value="<fmt:formatDate value="${extintor.dataValidade}" pattern="yyyy-MM-dd"/>" required>
                        <div class="invalid-feedback">Por favor, informe a data de validade.</div>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6">
                        <label for="localizacao" class="form-label">Localização*</label>
                        <input type="text" class="form-control" id="localizacao" name="localizacao" value="${extintor.localizacao}" required>
                        <div class="invalid-feedback">Por favor, informe a localização.</div>
                    </div>
                    <div class="col-md-6">
                        <label for="idFilial" class="form-label">Filial*</label>
                        <select class="form-select" id="idFilial" name="idFilial" required>
                            <option value="">Selecione...</option>
                            <c:forEach var="filial" items="${listaFiliais}">
                                <option value="${filial.idFilial}" ${extintor.idFilial == filial.idFilial ? 'selected' : ''}>${filial.nome}</option>
                            </c:forEach>
                        </select>
                        <div class="invalid-feedback">Por favor, selecione a filial.</div>
                    </div>
                </div>

                <div class="d-flex justify-content-end mt-4">
                    <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=listar" class="btn btn-outline-secondary me-2">Cancelar</a>
                    <button type="submit" class="btn btn-primary">Cadastrar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Validação Bootstrap
    (function () {
        'use strict';
        const forms = document.querySelectorAll('.needs-validation');
        Array.from(forms).forEach(function (form) {
            form.addEventListener('submit', function (event) {
                if (!form.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
                form.classList.add('was-validated');
            }, false);
        });
    })();
</script>
</body>
</html>
