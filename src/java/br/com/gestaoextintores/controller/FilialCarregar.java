package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.model.Filial;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet para carregar os dados de uma filial para edição
 * @author Dev Fabio Santos
 */
@WebServlet(name = "FilialCarregar", urlPatterns = {"/FilialCarregar"})
public class FilialCarregar extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(FilialCarregar.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            int idFilial = Integer.parseInt(request.getParameter("id"));
            
            FilialDAOImpl dao = new FilialDAOImpl();
            Filial filial = (Filial) dao.carregar(idFilial);
            
            if (filial != null) {
                request.setAttribute("filial", filial);
                request.getRequestDispatcher("/filial/filialEditar.jsp").forward(request, response);
            } else {
                request.setAttribute("mensagem", "Filial não encontrada!");
                request.getRequestDispatcher("/FilialListar").forward(request, response);
            }
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar filial", ex);
            request.setAttribute("mensagem", "Erro ao carregar filial: " + ex.getMessage());
            request.getRequestDispatcher("/FilialListar").forward(request, response);
        }
    }
}