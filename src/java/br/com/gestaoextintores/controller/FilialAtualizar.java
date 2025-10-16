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

@WebServlet(name = "FilialAtualizar", urlPatterns = {"/FilialAtualizar"})
public class FilialAtualizar extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int idFilial = Integer.parseInt(request.getParameter("idFilial"));
        String nome = request.getParameter("nome");
        String endereco = request.getParameter("endereco");

        Filial filial = new Filial();
        filial.setIdFilial(idFilial);
        filial.setNome(nome);
        filial.setEndereco(endereco);

        try {
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (filialDAO.alterar(filial)) {
                request.setAttribute("mensagem", "Filial atualizada com sucesso!");
            } else {
                request.setAttribute("mensagem", "Erro ao atualizar filial!");
            }

            // Após atualizar, redireciona para o servlet de listagem
            request.getRequestDispatcher("FilialListar").forward(request, response);

        } catch (Exception ex) {
            Logger.getLogger(FilialAtualizar.class.getName()).log(Level.SEVERE, "Erro no servlet FilialAtualizar", ex);
            request.setAttribute("mensagem", "Erro interno ao atualizar filial!");
            request.getRequestDispatcher("filial/filialEditar.jsp").forward(request, response);
        }
    }

}
