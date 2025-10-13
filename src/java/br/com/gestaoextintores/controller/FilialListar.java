/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.FilialDAOImpl;
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
 *
 * @author Dev Fabio Santos
 */

@WebServlet(name = "FilialListar", urlPatterns = {"/FilialListar"})
public class FilialListar extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(FilialListar.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            FilialDAOImpl dao = new FilialDAOImpl();
            List<Object> lista = dao.listar();
            
            request.setAttribute("listaFiliais", lista);
            request.getRequestDispatcher("/filial/filialListar.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar filiais", ex);
            request.setAttribute("mensagem", "Erro ao listar filiais: " + ex.getMessage());
            request.getRequestDispatcher("/filial/mensagem.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}