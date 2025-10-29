package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
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

@WebServlet(name = "FilialServlet", urlPatterns = {"/FilialServlet"})
public class FilialServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FilialServlet.class.getName());

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

            if (acao == null || acao.equals("listar")) {
                List<Filial> listaFiliais = filialDAO.listar(usuarioLogado);
                request.setAttribute("listaFiliais", listaFiliais);
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialListar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("novo")) {
                 if (!"Admin".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
                    return;
                }
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialCadastrar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("editar")) {
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

            } else if (acao.equals("excluir")) {
                 if (!"Admin".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
                    return;
                }
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                filialDAO.excluir(idFilial, usuarioLogado);
                response.sendRedirect(request.getContextPath() + "/FilialServlet?acao=listar");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no FilialServlet (GET): ", ex);
            throw new ServletException("Erro ao processar a requisição GET: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //request.setCharacterEncoding("UTF-8");

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null || !"Admin".equals(usuarioLogado.getPerfil())) {
             response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
             return;
        }

        String acao = request.getParameter("acao");

        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            String nome = request.getParameter("nome");
            String endereco = request.getParameter("endereco");

            Filial filial = new Filial();
            filial.setNome(nome);
            filial.setEndereco(endereco);

            if ("salvar".equals(acao)) {
                filialDAO.cadastrar(filial);

            } else if ("atualizar".equals(acao)) {
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                filial.setIdFilial(idFilial);
                filialDAO.alterar(filial, usuarioLogado);
            }

            response.sendRedirect(request.getContextPath() + "/FilialServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no FilialServlet (POST): ", ex);
            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Filiais";
    }
}