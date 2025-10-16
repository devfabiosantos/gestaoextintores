package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
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
 * Servlet para listar extintores
 * @author Dev Fabio Santos
 */
@WebServlet(name = "ExtintorListar", urlPatterns = {"/ExtintorListar"})
public class ExtintorListar extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(ExtintorListar.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ExtintorDAOImpl dao = new ExtintorDAOImpl();
            
            // Verifica se foi passado um ID de filial para filtrar
            String idFilialParam = request.getParameter("idFilial");
            List<Object> lista;
            
            if (idFilialParam != null && !idFilialParam.isEmpty()) {
                int idFilial = Integer.parseInt(idFilialParam);
                lista = dao.listar(idFilial);
                request.setAttribute("idFilial", idFilial);
            } else {
                lista = dao.listar();
            }
            
            request.setAttribute("listaExtintores", lista);
            request.getRequestDispatcher("/extintor/extintorListar.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar extintores", ex);
            request.setAttribute("mensagem", "Erro ao listar extintores: " + ex.getMessage());
            request.getRequestDispatcher("/extintor/mensagem.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}