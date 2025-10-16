package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet para excluir extintores
 * @author Dev Fabio Santos
 */
@WebServlet(name = "ExtintorExcluir", urlPatterns = {"/ExtintorExcluir"})
public class ExtintorExcluir extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ExtintorExcluir.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int idExtintor = Integer.parseInt(request.getParameter("id"));
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();

            if (extintorDAO.excluir(idExtintor)) {
                request.getSession().setAttribute("mensagem", "Extintor excluído com sucesso!");
            } else {
                request.getSession().setAttribute("mensagem", "Erro ao excluir extintor!");
            }

            // Redireciona para o servlet de listagem
            response.sendRedirect(request.getContextPath() + "/ExtintorListar");

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "ID inválido para exclusão", e);
            request.getSession().setAttribute("mensagem", "ID inválido para exclusão!");
            response.sendRedirect(request.getContextPath() + "/ExtintorListar");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir extintor", e);
            request.getSession().setAttribute("mensagem", "Erro ao excluir extintor: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/ExtintorListar");
        }
    }
}