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
import javax.servlet.http.HttpSession;

/**
 * Servlet para atualizar os dados de uma filial
 * @author Dev Fabio Santos
 */
@WebServlet(name = "FilialEditar", urlPatterns = {"/FilialEditar"})
public class FilialEditar extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(FilialEditar.class.getName());
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            int idFilial = Integer.parseInt(request.getParameter("idFilial"));
            String nome = request.getParameter("nome");
            String endereco = request.getParameter("endereco");
            
            Filial filial = new Filial();
            filial.setIdFilial(idFilial);
            filial.setNome(nome);
            filial.setEndereco(endereco);
            
            FilialDAOImpl dao = new FilialDAOImpl();
            
            if (dao.alterar(filial)) {
                HttpSession session = request.getSession();
                session.setAttribute("mensagem", "Filial atualizada com sucesso!");
            } else {
                request.setAttribute("mensagem", "Erro ao atualizar filial!");
                request.setAttribute("filial", filial);
                request.getRequestDispatcher("/filial/filialEditar.jsp").forward(request, response);
                return;
            }
            
            response.sendRedirect(request.getContextPath() + "/FilialListar");
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao editar filial", ex);
            request.setAttribute("mensagem", "Erro ao editar filial: " + ex.getMessage());
            request.getRequestDispatcher("/FilialListar").forward(request, response);
        }
    }
}