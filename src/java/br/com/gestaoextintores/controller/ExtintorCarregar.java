package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.model.Extintor;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet para carregar os dados de um extintor para edição
 * @author Dev Fabio Santos
 */
@WebServlet(name = "ExtintorCarregar", urlPatterns = {"/ExtintorCarregar"})
public class ExtintorCarregar extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(ExtintorCarregar.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            int idExtintor = Integer.parseInt(request.getParameter("id"));
            
            ExtintorDAOImpl dao = new ExtintorDAOImpl();
            Extintor extintor = (Extintor) dao.carregar(idExtintor);
            
            if (extintor != null) {
                // Carrega a lista de filiais para o formulário
                FilialDAOImpl filialDAO = new FilialDAOImpl();
                List<Object> listaFiliais = filialDAO.listar();
                request.setAttribute("listaFiliais", listaFiliais);
                
                request.setAttribute("extintor", extintor);
                request.getRequestDispatcher("/extintor/extintorEditar.jsp").forward(request, response);
            } else {
                request.setAttribute("mensagem", "Extintor não encontrado!");
                request.getRequestDispatcher("/ExtintorListar").forward(request, response);
            }
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar extintor", ex);
            request.setAttribute("mensagem", "Erro ao carregar extintor: " + ex.getMessage());
            request.getRequestDispatcher("/ExtintorListar").forward(request, response);
        }
    }
}