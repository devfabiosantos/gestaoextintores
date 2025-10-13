/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet responsável pela exclusão de filiais
 * @author Dev Fabio Santos
 */
@WebServlet(name = "FilialExcluir", urlPatterns = {"/FilialExcluir"})
public class FilialExcluir extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FilialExcluir.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int idFilial = Integer.parseInt(request.getParameter("id"));
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (filialDAO.excluir(idFilial)) {
                request.getSession().setAttribute("mensagem", "Filial excluída com sucesso!");
            } else {
                request.getSession().setAttribute("mensagem", "Erro ao excluir filial!");
            }

            // Redireciona para o servlet de listagem
            response.sendRedirect(request.getContextPath() + "/FilialListar");

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "ID inválido para exclusão", e);
            request.getSession().setAttribute("mensagem", "ID inválido para exclusão!");
            response.sendRedirect(request.getContextPath() + "/FilialListar");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir filial", e);
            request.getSession().setAttribute("mensagem", "Erro interno ao excluir filial!");
            response.sendRedirect(request.getContextPath() + "/FilialListar");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
