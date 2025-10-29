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
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null || !"Admin".equals(usuarioLogado.getPerfil())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado."); 
            return false;
        } return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request, response)) { return; }
        
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuarioLogado");
        String acao = request.getParameter("acao");

        try {
            SetorDAOImpl setorDAO = new SetorDAOImpl();
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (acao == null || "listar".equals(acao)) {
                List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                request.setAttribute("listaSetores", listaSetores);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorListar.jsp");
                rd.forward(request, response);

            } else if ("novo".equals(acao)) {
                List<Filial> listaFiliais = filialDAO.listar(usuarioLogado);
                request.setAttribute("listaFiliais", listaFiliais);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorCadastrar.jsp");
                rd.forward(request, response);

            } else if ("editar".equals(acao)) {
                int idSetor = Integer.parseInt(request.getParameter("idSetor"));
                Setor setor = setorDAO.carregar(idSetor, usuarioLogado);
                if (setor == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND, "Setor não encontrado."); 
                return; 
                }
                request.setAttribute("setor", setor);
                RequestDispatcher rd = request.getRequestDispatcher("/setor/setorEditar.jsp");
                rd.forward(request, response);

            } else if ("excluir".equals(acao)) {
                int idSetor = Integer.parseInt(request.getParameter("idSetor"));
                setorDAO.excluir(idSetor, usuarioLogado);
                response.sendRedirect(request.getContextPath() + "/SetorServlet?acao=listar");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no SetorServlet (GET): ", ex);
            throw new ServletException("Erro GET Setor: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) { return; }
        
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (Usuario) sessao.getAttribute("usuarioLogado");
        String acao = request.getParameter("acao");

        try {
            SetorDAOImpl setorDAO = new SetorDAOImpl();

            String nome = request.getParameter("nome");
            int idFilial = 0;
            if ("salvar".equals(acao)) {
                idFilial = Integer.parseInt(request.getParameter("idFilial"));
            }
            
            Setor setor = new Setor();
            setor.setNome(nome);

            if ("salvar".equals(acao)) {
                setor.setIdFilial(idFilial);
                setorDAO.cadastrar(setor);

            } else if ("atualizar".equals(acao)) {
                int idSetor = Integer.parseInt(request.getParameter("idSetor"));
                setor.setIdSetor(idSetor);
                setorDAO.alterar(setor, usuarioLogado);
            }

            response.sendRedirect(request.getContextPath() + "/SetorServlet?acao=listar");

        } catch (NumberFormatException ex) {
             LOGGER.log(Level.WARNING, "Erro ao converter ID de Filial/Setor", ex);
             FilialDAOImpl filialDAO = new FilialDAOImpl();
             List<Filial> listaFiliais = filialDAO.listar(usuarioLogado);
             request.setAttribute("listaFiliais", listaFiliais);
             request.setAttribute("mensagemErro", "ID inválido fornecido.");
             String jspDestino = "/setor/" + ("salvar".equals(acao) ? "setorCadastrar.jsp" : "setorEditar.jsp");
             RequestDispatcher rd = request.getRequestDispatcher(jspDestino);
             rd.forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no SetorServlet (POST): ", ex);
            throw new ServletException("Erro POST Setor: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para CRUD de Setores (Admin)";
    }
}