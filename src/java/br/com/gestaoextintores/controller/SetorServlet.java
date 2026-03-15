package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.dao.SetorDAOImpl;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Setor;
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "SetorServlet", urlPatterns = {"/SetorServlet"})
public class SetorServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SetorServlet.class.getName());

    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Usuario usuarioLogado = getUsuarioLogado(request);
        if (usuarioLogado == null || !"Admin".equals(usuarioLogado.getPerfil())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return false;
        }
        return true;
    }

    private Usuario getUsuarioLogado(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        return (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
    }

    private void carregarFiliais(HttpServletRequest request, Usuario usuarioLogado) {
        FilialDAOImpl filialDAO = new FilialDAOImpl();
        List<Filial> listaFiliais = filialDAO.listar(usuarioLogado);
        request.setAttribute("listaFiliais", listaFiliais);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        Usuario usuarioLogado = getUsuarioLogado(request);
        String acao = request.getParameter("acao");

        try {
            SetorDAOImpl setorDAO = new SetorDAOImpl();

            if (acao == null || "listar".equals(acao)) {
                List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                request.setAttribute("listaSetores", listaSetores);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorListar.jsp");
                rd.forward(request, response);

            } else if ("novo".equals(acao)) {
                carregarFiliais(request, usuarioLogado);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorCadastrar.jsp");
                rd.forward(request, response);

            } else if ("editar".equals(acao)) {
                int idSetor = Integer.parseInt(request.getParameter("idSetor"));
                Setor setor = setorDAO.carregar(idSetor, usuarioLogado);
                if (setor == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Setor não encontrado.");
                    return;
                }
                request.setAttribute("setor", setor);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorEditar.jsp");
                rd.forward(request, response);

            } else if ("excluir".equals(acao)) {
                int idSetor = Integer.parseInt(request.getParameter("idSetor"));
                boolean excluido = setorDAO.excluir(idSetor, usuarioLogado);
                HttpSession sessao = request.getSession();
                if (excluido) {
                    sessao.setAttribute("mensagemSucesso", "Setor excluído com sucesso.");
                } else {
                    sessao.setAttribute("mensagemErro", "Falha ao excluir setor. Verifique se o registro ainda existe.");
                }
                response.sendRedirect(request.getContextPath() + "/SetorServlet?acao=listar");

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida.");
            }

        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "ID inválido recebido no SetorServlet (GET).", ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido.");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no SetorServlet (GET)", ex);
            throw new ServletException("Erro GET Setor: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }

        Usuario usuarioLogado = getUsuarioLogado(request);
        String acao = request.getParameter("acao");
        Setor setor = new Setor();

        try {
            SetorDAOImpl setorDAO = new SetorDAOImpl();
            String nome = request.getParameter("nome");
            setor.setNome(nome);

            if ("salvar".equals(acao)) {
                String idFilialStr = request.getParameter("idFilial");
                if (idFilialStr == null || idFilialStr.isEmpty()) {
                    request.setAttribute("mensagemErro", "A filial é obrigatória.");
                    carregarFiliais(request, usuarioLogado);
                    request.setAttribute("setor", setor);
                    RequestDispatcher rd = request.getRequestDispatcher("/setor/setorCadastrar.jsp");
                    rd.forward(request, response);
                    return;
                }

                int idFilial = Integer.parseInt(idFilialStr);
                setor.setIdFilial(idFilial);
                boolean cadastrado = setorDAO.cadastrar(setor);
                if (cadastrado) {
                    request.getSession().setAttribute("mensagemSucesso", "Setor cadastrado com sucesso.");
                    response.sendRedirect(request.getContextPath() + "/SetorServlet?acao=listar");
                    return;
                }

                request.setAttribute("mensagemErro", "Falha ao cadastrar. O nome '" + setor.getNome() + "' já existe nesta filial.");
                carregarFiliais(request, usuarioLogado);
                request.setAttribute("setor", setor);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorCadastrar.jsp");
                rd.forward(request, response);
                return;

            } else if ("atualizar".equals(acao)) {
                int idSetor = Integer.parseInt(request.getParameter("idSetor"));
                setor.setIdSetor(idSetor);
                boolean alterado = setorDAO.alterar(setor, usuarioLogado);
                if (alterado) {
                    request.getSession().setAttribute("mensagemSucesso", "Setor alterado com sucesso.");
                    response.sendRedirect(request.getContextPath() + "/SetorServlet?acao=listar");
                    return;
                }

                Setor setorAtual = setorDAO.carregar(idSetor, usuarioLogado);
                if (setorAtual == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Setor não encontrado.");
                    return;
                }
                setorAtual.setNome(setor.getNome());
                request.setAttribute("mensagemErro", "Falha ao alterar. O nome '" + setor.getNome() + "' já existe nesta filial ou o setor não foi encontrado.");
                request.setAttribute("setor", setorAtual);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorEditar.jsp");
                rd.forward(request, response);
                return;
            }

            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida.");

        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "ID inválido recebido no SetorServlet (POST).", ex);
            request.setAttribute("mensagemErro", "ID inválido fornecido.");
            if ("salvar".equals(acao)) {
                carregarFiliais(request, usuarioLogado);
                request.setAttribute("setor", setor);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorCadastrar.jsp");
                rd.forward(request, response);
                return;
            }

            String idSetorStr = request.getParameter("idSetor");
            if (idSetorStr != null && !idSetorStr.isEmpty()) {
                try {
                    setor.setIdSetor(Integer.parseInt(idSetorStr));
                } catch (NumberFormatException ignored) {
                }
            }
            request.setAttribute("setor", setor);
            RequestDispatcher rd = request.getRequestDispatcher("/setor/setorEditar.jsp");
            rd.forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no SetorServlet (POST)", ex);
            throw new ServletException("Erro POST Setor: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para CRUD de Setores (Admin)";
    }
}
