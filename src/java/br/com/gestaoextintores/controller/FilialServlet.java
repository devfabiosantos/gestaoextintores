package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "FilialServlet", urlPatterns = {"/FilialServlet"})
public class FilialServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FilialServlet.class.getName());
    private static final Pattern CNPJ_PATTERN = Pattern.compile("\\d{14}");
    private static final Pattern CEP_PATTERN = Pattern.compile("\\d{8}");

    private String normalizarNumeros(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    private String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private String montarEnderecoLegado(Filial filial) {
        StringBuilder sb = new StringBuilder();
        sb.append(filial.getLogradouro()).append(", ").append(filial.getNumero());
        if (filial.getComplemento() != null) {
            sb.append(" - ").append(filial.getComplemento());
        }
        sb.append(" - ").append(filial.getBairro());
        sb.append(", ").append(filial.getCidade()).append(" - ").append(filial.getEstado());
        if (filial.getCep() != null) {
            sb.append(", ").append(filial.getCepFormatado());
        }
        return sb.toString();
    }

    private String validarFilial(Filial filial) {
        if (filial.getNome() == null) {
            return "O nome da filial é obrigatório.";
        }
        if (filial.getCep() == null) {
            return "O CEP é obrigatório.";
        }
        if (!CEP_PATTERN.matcher(filial.getCep()).matches()) {
            return "Informe um CEP válido com 8 dígitos.";
        }
        if (filial.getLogradouro() == null) {
            return "Rua/Avenida é obrigatória.";
        }
        if (filial.getNumero() == null) {
            return "O número é obrigatório.";
        }
        if (filial.getBairro() == null) {
            return "O bairro é obrigatório.";
        }
        if (filial.getCidade() == null) {
            return "A cidade é obrigatória.";
        }
        if (filial.getEstado() == null) {
            return "O estado é obrigatório.";
        }
        if (filial.getCnpj() != null && !CNPJ_PATTERN.matcher(filial.getCnpj()).matches()) {
            return "Informe um CNPJ válido com 14 dígitos.";
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String acao = request.getParameter("acao");

        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (acao == null || "listar".equals(acao)) {
                List<Filial> listaFiliais = filialDAO.listar(usuarioLogado);
                request.setAttribute("listaFiliais", listaFiliais);
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialListar.jsp");
                rd.forward(request, response);

            } else if ("novo".equals(acao)) {
                if (!"Admin".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
                    return;
                }
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialCadastrar.jsp");
                rd.forward(request, response);

            } else if ("editar".equals(acao)) {
                if (!"Admin".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
                    return;
                }
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                Filial filial = filialDAO.carregar(idFilial, usuarioLogado);

                if (filial == null) {
                    LOGGER.log(Level.WARNING, "Tentativa de editar filial inexistente ou não permitida (ID: {0}) pelo usuário {1}",
                            new Object[]{idFilial, usuarioLogado.getLogin()});
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Filial não encontrada ou acesso negado.");
                    return;
                }

                request.setAttribute("filial", filial);
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialEditar.jsp");
                rd.forward(request, response);

            } else if ("excluir".equals(acao)) {
                if (!"Admin".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
                    return;
                }
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                boolean excluida = filialDAO.excluir(idFilial, usuarioLogado);
                HttpSession sessaoAtual = request.getSession();
                if (excluida) {
                    sessaoAtual.setAttribute("mensagemSucesso", "Filial excluída com sucesso.");
                } else {
                    sessaoAtual.setAttribute("mensagemErro", "Falha ao excluir filial. Verifique se o registro ainda existe.");
                }
                response.sendRedirect(request.getContextPath() + "/FilialServlet?acao=listar");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida.");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no FilialServlet (GET)", ex);
            throw new ServletException("Erro ao processar a requisição GET: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null || !"Admin".equals(usuarioLogado.getPerfil())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
            return;
        }

        String acao = request.getParameter("acao");

        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            String nome = trimToNull(request.getParameter("nome"));
            String cnpj = normalizarNumeros(request.getParameter("cnpj"));
            String cep = normalizarNumeros(request.getParameter("cep"));
            String logradouro = trimToNull(request.getParameter("logradouro"));
            String numero = trimToNull(request.getParameter("numero"));
            String complemento = trimToNull(request.getParameter("complemento"));
            String bairro = trimToNull(request.getParameter("bairro"));
            String cidade = trimToNull(request.getParameter("cidade"));
            String estado = trimToNull(request.getParameter("estado"));

            Filial filial = new Filial();
            filial.setNome(nome);
            filial.setCnpj(trimToNull(cnpj));
            filial.setCep(trimToNull(cep));
            filial.setLogradouro(logradouro);
            filial.setNumero(numero);
            filial.setComplemento(complemento);
            filial.setBairro(bairro);
            filial.setCidade(cidade);
            filial.setEstado(estado != null ? estado.toUpperCase() : null);

            String mensagemValidacao = validarFilial(filial);
            if (mensagemValidacao != null) {
                request.setAttribute("mensagemErro", mensagemValidacao);
                request.setAttribute("filial", filial);
                String jspDestino = "salvar".equals(acao) ? "/filial/filialCadastrar.jsp" : "/filial/filialEditar.jsp";
                RequestDispatcher rd = request.getRequestDispatcher(jspDestino);
                rd.forward(request, response);
                return;
            }

            filial.setEndereco(montarEnderecoLegado(filial));

            HttpSession sessaoAtual = request.getSession();
            if ("salvar".equals(acao)) {
                boolean cadastrada = filialDAO.cadastrar(filial);
                if (cadastrada) {
                    sessaoAtual.setAttribute("mensagemSucesso", "Filial cadastrada com sucesso.");
                } else {
                    request.setAttribute("mensagemErro", "Falha ao cadastrar filial.");
                    request.setAttribute("filial", filial);
                    RequestDispatcher rd = request.getRequestDispatcher("/filial/filialCadastrar.jsp");
                    rd.forward(request, response);
                    return;
                }

            } else if ("atualizar".equals(acao)) {
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                filial.setIdFilial(idFilial);
                boolean alterada = filialDAO.alterar(filial, usuarioLogado);
                if (alterada) {
                    sessaoAtual.setAttribute("mensagemSucesso", "Filial alterada com sucesso.");
                } else {
                    request.setAttribute("mensagemErro", "Falha ao alterar filial. Verifique se o registro ainda existe.");
                    request.setAttribute("filial", filial);
                    RequestDispatcher rd = request.getRequestDispatcher("/filial/filialEditar.jsp");
                    rd.forward(request, response);
                    return;
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida.");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/FilialServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no FilialServlet (POST)", ex);
            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Filiais";
    }
}
