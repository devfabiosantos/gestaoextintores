package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.dao.UsuarioDAO;
import br.com.gestaoextintores.model.Filial;
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

@WebServlet(name = "UsuarioServlet", urlPatterns = {"/UsuarioServlet"})
public class UsuarioServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(UsuarioServlet.class.getName());
    private static final String PERFIL_TECNICO = "Técnico";

    private boolean isAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null || !"Admin".equals(usuarioLogado.getPerfil())) {
            LOGGER.log(Level.WARNING, "Tentativa de acesso não autorizado à administração de usuários por: {0}",
                    (usuarioLogado != null ? usuarioLogado.getLogin() : "Usuário não logado"));
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Apenas administradores podem gerenciar usuários.");
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
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            FilialDAOImpl filialDAO = new FilialDAOImpl();
            if (acao == null || "listar".equals(acao)) {

                String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                Integer idFilialFiltro = null;
                if (idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                    try {
                        idFilialFiltro = Integer.parseInt(idFilialFiltroStr);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "ID de filial inválido recebido no filtro: {0}", idFilialFiltroStr);
                        request.setAttribute("mensagemErro", "ID de filial inválido no filtro.");
                    }
                }

                List<Usuario> listaUsuarios = usuarioDAO.listar(idFilialFiltro);
                List<Filial> listaTodasFiliais = filialDAO.listar(usuarioLogado);

                request.setAttribute("listaUsuarios", listaUsuarios);
                request.setAttribute("listaTodasFiliais", listaTodasFiliais);
                request.setAttribute("idFilialSelecionada", idFilialFiltro);

                RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioListar.jsp");
                rd.forward(request, response);

            } else if ("novo".equals(acao)) {
                carregarFiliais(request, usuarioLogado);
                RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioCadastrar.jsp");
                rd.forward(request, response);

            } else if ("editar".equals(acao)) {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                Usuario usuario = usuarioDAO.carregar(idUsuario);
                if (usuario == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Usuário não encontrado.");
                    return;
                }
                carregarFiliais(request, usuarioLogado);
                request.setAttribute("usuario", usuario);
                RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioEditar.jsp");
                rd.forward(request, response);

            } else if ("excluir".equals(acao)) {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                if (usuarioLogado.getIdUsuario() == idUsuario) {
                    HttpSession sessao = request.getSession();
                    sessao.setAttribute("mensagemErro", "Você não pode excluir seu próprio usuário.");
                    response.sendRedirect(request.getContextPath() + "/UsuarioServlet?acao=listar");
                    return;
                }

                boolean excluido = usuarioDAO.excluir(idUsuario);
                HttpSession sessao = request.getSession();
                if (excluido) {
                    sessao.setAttribute("mensagemSucesso", "Usuário excluído com sucesso.");
                } else {
                    sessao.setAttribute("mensagemErro", "Falha ao excluir usuário. Verifique se o registro ainda existe.");
                }
                response.sendRedirect(request.getContextPath() + "/UsuarioServlet?acao=listar");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida.");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no UsuarioServlet (GET)", ex);
            throw new ServletException("Erro ao processar a requisição GET: " + ex.getMessage(), ex);
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

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            String nome = request.getParameter("nome");
            String login = request.getParameter("login");
            String perfil = request.getParameter("perfil");
            String idFilialStr = request.getParameter("idFilial");

            Integer idFilial = null;
            if (PERFIL_TECNICO.equals(perfil)) {
                if (idFilialStr == null || idFilialStr.isEmpty()) {
                    request.setAttribute("mensagemErro", "A filial é obrigatória para usuários técnicos.");
                    carregarFiliais(request, usuarioLogado);
                    request.setAttribute("usuario", montarUsuarioParcial(request, null));
                    RequestDispatcher rd = request.getRequestDispatcher("/usuario/" + ("salvar".equals(acao) ? "usuarioCadastrar.jsp" : "usuarioEditar.jsp"));
                    rd.forward(request, response);
                    return;
                }
                try {
                    idFilial = Integer.parseInt(idFilialStr);
                } catch (NumberFormatException e) {
                    request.setAttribute("mensagemErro", "ID da filial inválido.");
                    carregarFiliais(request, usuarioLogado);
                    request.setAttribute("usuario", montarUsuarioParcial(request, null));
                    RequestDispatcher rd = request.getRequestDispatcher("/usuario/" + ("salvar".equals(acao) ? "usuarioCadastrar.jsp" : "usuarioEditar.jsp"));
                    rd.forward(request, response);
                    return;
                }
            }

            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setPerfil(perfil);
            usuario.setIdFilial(idFilial);

            HttpSession sessao = request.getSession();
            if ("salvar".equals(acao)) {
                String senha = request.getParameter("senha");
                if (senha == null || senha.trim().isEmpty()) {
                    request.setAttribute("mensagemErro", "A senha é obrigatória para novos usuários.");
                    carregarFiliais(request, usuarioLogado);
                    request.setAttribute("usuario", usuario);
                    RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioCadastrar.jsp");
                    rd.forward(request, response);
                    return;
                }
                usuario.setSenha(senha);
                int idGerado = usuarioDAO.cadastrar(usuario);
                if (idGerado > 0) {
                    sessao.setAttribute("mensagemSucesso", "Usuário cadastrado com sucesso.");
                } else {
                    request.setAttribute("mensagemErro", "Falha ao cadastrar usuário.");
                    carregarFiliais(request, usuarioLogado);
                    request.setAttribute("usuario", usuario);
                    RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioCadastrar.jsp");
                    rd.forward(request, response);
                    return;
                }

            } else if ("atualizar".equals(acao)) {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                usuario.setIdUsuario(idUsuario);
                boolean alterado = usuarioDAO.alterar(usuario);
                if (alterado) {
                    sessao.setAttribute("mensagemSucesso", "Usuário alterado com sucesso.");
                } else {
                    request.setAttribute("mensagemErro", "Falha ao alterar usuário. Verifique se o registro ainda existe.");
                    carregarFiliais(request, usuarioLogado);
                    request.setAttribute("usuario", usuario);
                    RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioEditar.jsp");
                    rd.forward(request, response);
                    return;
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida.");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/UsuarioServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no UsuarioServlet (POST)", ex);
            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }

    private Usuario montarUsuarioParcial(HttpServletRequest request, Integer idUsuario) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.getParameter("nome"));
        usuario.setLogin(request.getParameter("login"));
        usuario.setPerfil(request.getParameter("perfil"));
        String idFilialStr = request.getParameter("idFilial");
        if (idFilialStr != null && !idFilialStr.isEmpty()) {
            try {
                usuario.setIdFilial(Integer.parseInt(idFilialStr));
            } catch (NumberFormatException e) {
                usuario.setIdFilial(null);
            }
        }
        if (idUsuario != null) {
            usuario.setIdUsuario(idUsuario);
        }
        return usuario;
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Usuários (Acesso Restrito ao Admin)";
    }
}
