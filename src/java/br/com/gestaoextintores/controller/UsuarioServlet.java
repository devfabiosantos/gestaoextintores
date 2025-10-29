/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.controller;

/**
 *
 * @author Dev Fabio Santos
 */
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) {
            return;
        }
        
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuarioLogado");
        String acao = request.getParameter("acao");

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            FilialDAOImpl filialDAO = new FilialDAOImpl();
            if (acao == null || acao.equals("listar")) {

                String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                Integer idFilialFiltro = null;
                if (idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                    try {
                        idFilialFiltro = Integer.parseInt(idFilialFiltroStr);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "ID de filial inválido recebido no filtro: {0}", idFilialFiltroStr);
                    }
                }

                List<Usuario> listaUsuarios = usuarioDAO.listar(idFilialFiltro); 
                List<Filial> listaTodasFiliais = filialDAO.listar(usuarioLogado); 

                request.setAttribute("listaUsuarios", listaUsuarios);
                request.setAttribute("listaTodasFiliais", listaTodasFiliais);
                request.setAttribute("idFilialSelecionada", idFilialFiltro);

                RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioListar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("novo")) {
                List<Filial> listaFiliais = filialDAO.listar(usuarioLogado); 
                request.setAttribute("listaFiliais", listaFiliais);
                RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioCadastrar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("editar")) {
                 int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                 Usuario usuario = usuarioDAO.carregar(idUsuario);
                 if (usuario == null) {
                     return; 
                 }
                 List<Filial> listaFiliais = filialDAO.listar(usuarioLogado); 
                 request.setAttribute("usuario", usuario);
                 request.setAttribute("listaFiliais", listaFiliais);
                 RequestDispatcher rd = request.getRequestDispatcher("/usuario/usuarioEditar.jsp");
                 rd.forward(request, response);

            } else if (acao.equals("excluir")) {
                 int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                 usuarioDAO.excluir(idUsuario);
                 response.sendRedirect(request.getContextPath() + "/UsuarioServlet?acao=listar");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no UsuarioServlet (GET): ", ex);
            throw new ServletException("Erro ao processar a requisição GET: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) {
            return; 
        }

        String acao = request.getParameter("acao");

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            String nome = request.getParameter("nome");
            String login = request.getParameter("login");
            String perfil = request.getParameter("perfil");
            String idFilialStr = request.getParameter("idFilial");

            Integer idFilial = null;
            if ("Técnico".equals(perfil) && idFilialStr != null && !idFilialStr.isEmpty()) {
                 try {
                    idFilial = Integer.parseInt(idFilialStr);
                 } catch (NumberFormatException e) {
                     request.setAttribute("mensagemErro", "ID da Filial inválido.");

                     doGet(request, response);
                     return;
                 }
            } else if ("Admin".equals(perfil)) {
                 idFilial = null;
            }
             
            Usuario usuario = new Usuario();
            usuario.setNome(nome);
            usuario.setLogin(login);
            usuario.setPerfil(perfil);
            usuario.setIdFilial(idFilial);

            if ("salvar".equals(acao)) {
                String senha = request.getParameter("senha");
                if (senha == null || senha.trim().isEmpty()) {
                    request.setAttribute("mensagemErro", "A senha é obrigatória para novos usuários.");
                    doGet(request, response);
                    return;
                }
                usuario.setSenha(senha); 
                usuarioDAO.cadastrar(usuario);

            } else if ("atualizar".equals(acao)) {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                usuario.setIdUsuario(idUsuario);
                usuarioDAO.alterar(usuario); 
            }

            response.sendRedirect(request.getContextPath() + "/UsuarioServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no UsuarioServlet (POST): ", ex);

            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Usuários (Acesso Restrito ao Admin)";
    }
}