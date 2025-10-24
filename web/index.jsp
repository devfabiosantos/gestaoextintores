<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="pt-br">
    <head>
        <title>Sistema de Gestão de Extintores</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    </head>
    <body class="bg-light">
        <div class="container mt-5">
            <div class="card shadow-sm border-0 rounded-3">
                <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center">
                    <h2 class="mb-0">Sistema de Gestão de Extintores</h2>
                    
                    <c:if test="${not empty sessionScope.usuarioLogado}">
                        <div>
                            <span class="me-3">
                                Bem-vindo(a), ${sessionScope.usuarioLogado.nome}
                            </span>
                            
                            <a href="${pageContext.request.contextPath}/LogoutServlet" class="btn btn-light btn-sm">Sair</a>
                        </div>
                    </c:if>
                </div>
                <div class="card-body">
                    <div class="row">

                        <c:if test="${sessionScope.usuarioLogado.perfil == 'Admin'}">
                            <div class="col-md-4 mb-4">
                                <div class="card h-100">
                                    <div class="card-header bg-primary text-white">
                                        <h4>Gestão de Filiais</h4>
                                    </div>
                                    <div class="card-body">
                                        <p>Cadastre e gerencie as filiais da empresa.</p>
                                        <div class="d-grid gap-2">
                                            <a href="${pageContext.request.contextPath}/FilialServlet?acao=listar" class="btn btn-outline-primary">Listar Filiais</a>
                                            <a href="${pageContext.request.contextPath}/FilialServlet?acao=novo" class="btn btn-primary">Nova Filial</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                        
                        <c:if test="${sessionScope.usuarioLogado.perfil == 'Admin'}">
                             <div class="col-md-4 mb-4">
                                <div class="card h-100">
                                    <div class="card-header bg-secondary text-white">
                                        <h4>Gestão de Usuários</h4>
                                    </div>
                                    <div class="card-body">
                                        <p>Cadastre e gerencie os usuários do sistema.</p>
                                        <div class="d-grid gap-2">
                                            <a href="${pageContext.request.contextPath}/UsuarioServlet?acao=listar" class="btn btn-outline-secondary">Listar Usuários</a>
                                            <a href="${pageContext.request.contextPath}/UsuarioServlet?acao=novo" class="btn btn-secondary">Novo Usuário</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <div class="col-md-4 mb-4">
                            <div class="card h-100">
                                <div class="card-header bg-danger text-white">
                                    <h4>Gestão de Extintores</h4>
                                </div>
                                <div class="card-body">
                                    <p>Cadastre e gerencie os extintores de incêndio.</p>
                                    <div class="d-grid gap-2">
                                        <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=listar" class="btn btn-outline-danger">Listar Extintores</a>
                                        <a href="${pageContext.request.contextPath}/ExtintorServlet?acao=novo" class="btn btn-danger">Novo Extintor</a>
                                    </div>
                                </div>
                            </div>
                        </div>

                         <div class="col-md-4 mb-4">
                            <div class="card h-100">
                                <div class="card-header bg-success text-white">
                                    <h4>Gestão de Remessas</h4>
                                </div>
                                <div class="card-body">
                                    <p>Crie e gerencie as remessas para recarga.</p>
                                    <div class="d-grid gap-2">
                                        <a href="${pageContext.request.contextPath}/RemessaServlet?acao=listar" class="btn btn-outline-success">Listar Remessas</a>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
                <div class="card-footer text-center text-muted">
                    <small>Sistema de Gestão de Extintores &copy; 2025</small>
                </div>
            </div>
        </div>
    </body>
</html>