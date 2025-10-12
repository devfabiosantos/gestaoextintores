/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author Dev Fabio Santos
 */

@WebServlet(name = "FilialCadastrar", urlPatterns = {"/FilialCadastrar"})
public class FilialCadastrar extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("filial/filialCadastrar.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String nome = request.getParameter("nome");
        String endereco = request.getParameter("endereco");
        
        Filial filial = new Filial();
        filial.setNome(nome);
        filial.setEndereco(endereco);
        
        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();
            
            if (filialDAO.cadastrar(filial)){
                request.setAttribute("mensagem", "Filial cadastrada com sucesso!");
            } else {
                request.setAttribute("mensagem", "Erro ao cadastrar filial!");
            }
            
            request.getRequestDispatcher("filial/filialCadastrar.jsp").forward(request, response);
            
        } catch (Exception ex) {
            Logger.getLogger(FilialCadastrar.class.getName()).log(Level.SEVERE, "Erro no servlet FilialCadastrar", ex);
            request.setAttribute("mensagem", "Erro interno ao cadastrar filial!");
            request.getRequestDispatcher("filial/filialCadastrar.jsp").forward(request, response);
        }
    }
}
