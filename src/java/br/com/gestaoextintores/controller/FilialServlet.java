package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl; // Importa seu DAO
import br.com.gestaoextintores.model.Filial;   // Importa seu Model
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

/**
 *
 * @author Dev Fabio Santos
 */
@WebServlet(name = "FilialServlet", urlPatterns = {"/FilialServlet"})
public class FilialServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FilialServlet.class.getName());

    /**
     * Handles the HTTP <code>GET</code> method.
     * Usado para carregar páginas e buscar dados (Listar, Novo, Editar, Excluir).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String acao = request.getParameter("acao");

        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            // Ação: LISTAR (ou se nenhuma ação for informada)
            if (acao == null || acao.equals("listar")) {
                List<Object> listaFiliais = filialDAO.listar();
                request.setAttribute("listaFiliais", listaFiliais); // Envia a lista para o JSP
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialListar.jsp");
                rd.forward(request, response);

            // Ação: NOVO (apenas abre o formulário de cadastro)
            } else if (acao.equals("novo")) {
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialCadastrar.jsp");
                rd.forward(request, response);

            // Ação: EDITAR (carrega um objeto para a tela de edição)
            } else if (acao.equals("editar")) {
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                Filial filial = (Filial) filialDAO.carregar(idFilial);
                request.setAttribute("filial", filial); // Envia o objeto para o JSP
                RequestDispatcher rd = request.getRequestDispatcher("/filial/filialEditar.jsp");
                rd.forward(request, response);

            // Ação: EXCLUIR
            } else if (acao.equals("excluir")) {
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                filialDAO.excluir(idFilial);
                // Redireciona de volta para a listagem
                response.sendRedirect(request.getContextPath() + "/FilialServlet?acao=listar");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no FilialServlet (GET): ", ex);
            throw new ServletException("Erro ao processar a requisição GET: " + ex.getMessage(), ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * Usado para enviar dados (Salvar, Atualizar).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        String acao = request.getParameter("acao");

        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            // 1. Coletar dados do formulário
            String nome = request.getParameter("nome");
            String endereco = request.getParameter("endereco");

            // 2. Criar o objeto Filial
            Filial filial = new Filial();
            filial.setNome(nome);
            filial.setEndereco(endereco);

            // Ação: SALVAR (Novo Cadastro)
            if ("salvar".equals(acao)) {
                filialDAO.cadastrar(filial);
            
            // Ação: ATUALIZAR (Edição)
            } else if ("atualizar".equals(acao)) {
                // Pega o ID que veio do formulário de edição
                int idFilial = Integer.parseInt(request.getParameter("idFilial"));
                filial.setIdFilial(idFilial);
                filialDAO.alterar(filial);
            }

            // 3. Redirecionar para a listagem em ambos os casos
            response.sendRedirect(request.getContextPath() + "/FilialServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no FilialServlet (POST): ", ex);
            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Filiais";
    }// </editor-fold>

}