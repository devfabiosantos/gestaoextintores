<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Editar Filial</title>
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">

    <div class="container mt-5">
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white">
                <h4 class="mb-0">Editar Filial</h4>
            </div>

            <div class="card-body">
                <c:if test="${not empty mensagem}">
                    <div class="alert alert-info">
                        ${mensagem}
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/FilialAtualizar" method="post">
                    <input type="hidden" name="idFilial" value="${filial.idFilial}" />

                    <div class="mb-3">
                        <label for="nome" class="form-label">Nome da Filial</label>
                        <input type="text" class="form-control" id="nome" name="nome" 
                               value="${filial.nome}" required>
                    </div>

                    <div class="mb-3">
                        <label for="endereco" class="form-label">Endereço</label>
                        <input type="text" class="form-control" id="endereco" name="endereco" 
                               value="${filial.endereco}" required>
                    </div>

                    <div class="d-flex justify-content-between">
                        <a href="${pageContext.request.contextPath}/FilialListar" 
                           class="btn btn-secondary">Voltar</a>
                        <button type="submit" class="btn btn-success">Atualizar</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
