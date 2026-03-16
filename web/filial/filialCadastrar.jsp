<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <title>Cadastrar Filial</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
    <div class="container mt-5 mb-5">
        <div class="card shadow-sm border-0 rounded-3">
            <div class="card-header bg-primary text-white">
                <h4 class="mb-0">Cadastro de Filial</h4>
            </div>
            <div class="card-body">
                <c:if test="${not empty mensagemErro}">
                    <div class="alert alert-danger">${mensagemErro}</div>
                </c:if>

                <form action="${pageContext.request.contextPath}/FilialServlet" method="post" class="needs-validation" novalidate>
                    <input type="hidden" name="acao" value="salvar">

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="nome" class="form-label">Nome da Filial</label>
                            <input type="text" class="form-control" id="nome" name="nome" value="${filial.nome}" required>
                            <div class="invalid-feedback">Por favor, informe o nome da filial.</div>
                        </div>
                        <div class="col-md-6">
                            <label for="cnpj" class="form-label">CNPJ</label>
                            <input type="text" class="form-control" id="cnpj" name="cnpj" value="${filial.cnpjFormatado}" maxlength="18" placeholder="00.000.000/0000-00">
                        </div>

                        <div class="col-md-4">
                            <label for="cep" class="form-label">CEP</label>
                            <div class="input-group">
                                <input type="text" class="form-control" id="cep" name="cep" value="${filial.cepFormatado}" maxlength="9" placeholder="00000-000" required>
                                <button type="button" class="btn btn-outline-secondary" id="buscarCep">Buscar</button>
                            </div>
                            <div class="form-text" id="cepFeedback">A busca automática preenche o endereço, mas você pode ajustar os campos manualmente.</div>
                            <div class="invalid-feedback">Por favor, informe o CEP.</div>
                        </div>
                        <div class="col-md-8">
                            <label for="logradouro" class="form-label">Rua / Avenida</label>
                            <input type="text" class="form-control" id="logradouro" name="logradouro" value="${filial.logradouro}" required>
                            <div class="invalid-feedback">Por favor, informe a rua ou avenida.</div>
                        </div>

                        <div class="col-md-3">
                            <label for="numero" class="form-label">Número</label>
                            <input type="text" class="form-control" id="numero" name="numero" value="${filial.numero}" required>
                            <div class="invalid-feedback">Por favor, informe o número.</div>
                        </div>
                        <div class="col-md-5">
                            <label for="complemento" class="form-label">Complemento</label>
                            <input type="text" class="form-control" id="complemento" name="complemento" value="${filial.complemento}">
                        </div>
                        <div class="col-md-4">
                            <label for="bairro" class="form-label">Bairro</label>
                            <input type="text" class="form-control" id="bairro" name="bairro" value="${filial.bairro}" required>
                            <div class="invalid-feedback">Por favor, informe o bairro.</div>
                        </div>

                        <div class="col-md-6">
                            <label for="cidade" class="form-label">Cidade</label>
                            <input type="text" class="form-control" id="cidade" name="cidade" value="${filial.cidade}" required>
                            <div class="invalid-feedback">Por favor, informe a cidade.</div>
                        </div>
                        <div class="col-md-2">
                            <label for="estado" class="form-label">Estado</label>
                            <input type="text" class="form-control text-uppercase" id="estado" name="estado" value="${filial.estado}" maxlength="2" required>
                            <div class="invalid-feedback">Por favor, informe o estado.</div>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between mt-4">
                        <a href="${pageContext.request.contextPath}/FilialServlet?acao=listar" class="btn btn-outline-secondary">Voltar</a>
                        <button type="submit" class="btn btn-primary">Cadastrar</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <script>
        (function () {
            'use strict';

            function somenteNumeros(valor) {
                return (valor || '').replace(/\D/g, '');
            }

            function aplicarMascaraCep(valor) {
                var digits = somenteNumeros(valor).slice(0, 8);
                if (digits.length <= 5) {
                    return digits;
                }
                return digits.slice(0, 5) + '-' + digits.slice(5);
            }

            function aplicarMascaraCnpj(valor) {
                var digits = somenteNumeros(valor).slice(0, 14);
                if (digits.length <= 2) return digits;
                if (digits.length <= 5) return digits.slice(0, 2) + '.' + digits.slice(2);
                if (digits.length <= 8) return digits.slice(0, 2) + '.' + digits.slice(2, 5) + '.' + digits.slice(5);
                if (digits.length <= 12) return digits.slice(0, 2) + '.' + digits.slice(2, 5) + '.' + digits.slice(5, 8) + '/' + digits.slice(8);
                return digits.slice(0, 2) + '.' + digits.slice(2, 5) + '.' + digits.slice(5, 8) + '/' + digits.slice(8, 12) + '-' + digits.slice(12);
            }

            var forms = document.querySelectorAll('.needs-validation');
            Array.prototype.slice.call(forms).forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false);
            });

            var cepInput = document.getElementById('cep');
            var cnpjInput = document.getElementById('cnpj');
            var buscarCepButton = document.getElementById('buscarCep');
            var feedback = document.getElementById('cepFeedback');
            var logradouroInput = document.getElementById('logradouro');
            var bairroInput = document.getElementById('bairro');
            var cidadeInput = document.getElementById('cidade');
            var estadoInput = document.getElementById('estado');

            if (cepInput) {
                cepInput.addEventListener('input', function () {
                    cepInput.value = aplicarMascaraCep(cepInput.value);
                });
            }

            if (cnpjInput) {
                cnpjInput.addEventListener('input', function () {
                    cnpjInput.value = aplicarMascaraCnpj(cnpjInput.value);
                });
            }

            function preencherEndereco(dados) {
                logradouroInput.value = dados.logradouro || '';
                bairroInput.value = dados.bairro || '';
                cidadeInput.value = dados.localidade || '';
                estadoInput.value = (dados.uf || '').toUpperCase();
            }

            function atualizarFeedback(mensagem, tipo) {
                feedback.textContent = mensagem;
                feedback.className = 'form-text';
                if (tipo === 'erro') {
                    feedback.classList.add('text-danger');
                } else if (tipo === 'sucesso') {
                    feedback.classList.add('text-success');
                }
            }

            async function buscarCep() {
                var cep = somenteNumeros(cepInput.value);
                if (cep.length !== 8) {
                    atualizarFeedback('Informe um CEP válido com 8 dígitos.', 'erro');
                    return;
                }

                atualizarFeedback('Consultando CEP...', '');

                try {
                    var response = await fetch('https://viacep.com.br/ws/' + cep + '/json/');
                    if (!response.ok) {
                        throw new Error('Falha ao consultar o CEP.');
                    }
                    var data = await response.json();
                    if (data.erro) {
                        atualizarFeedback('CEP não encontrado. Preencha o endereço manualmente.', 'erro');
                        return;
                    }
                    preencherEndereco(data);
                    atualizarFeedback('Endereço preenchido com sucesso. Confira os dados antes de salvar.', 'sucesso');
                } catch (error) {
                    atualizarFeedback('Não foi possível consultar o CEP agora. Você pode preencher manualmente.', 'erro');
                }
            }

            buscarCepButton.addEventListener('click', buscarCep);
            cepInput.addEventListener('blur', function () {
                if (somenteNumeros(cepInput.value).length === 8) {
                    buscarCep();
                }
            });
        })();
    </script>
</body>
</html>
