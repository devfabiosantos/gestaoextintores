/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.UsuarioDAO;
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Dev Fabio Santos
 */

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //request.setCharacterEncoding("UTF-8");

        String login = request.getParameter("login");
        String senha = request.getParameter("senha");

        String mensagemErro = "Login ou senha inválidos.";

        if (login != null && !login.isEmpty() && senha != null && !senha.isEmpty()) {
            try {
                
                UsuarioDAO dao = new UsuarioDAO();
                Usuario usuarioLogado = dao.validarLogin(login, senha);

                if (usuarioLogado != null) {
                    
                    HttpSession sessao = request.getSession(true);
                    
                    sessao.setAttribute("usuarioLogado", usuarioLogado);
                    
                    LOGGER.log(Level.INFO, "Usuário {0} ({1}) logado com sucesso.", 
                            new Object[]{usuarioLogado.getLogin(), usuarioLogado.getPerfil()});

                    response.sendRedirect(request.getContextPath() + "/");
                    return;

                }
                
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erro ao tentar validar login no DAO.", ex);
                mensagemErro = "Erro interno no servidor. Tente novamente.";
            }
        }

        LOGGER.log(Level.WARNING, "Tentativa de login falhou para o usuário: {0}", login);
        request.setAttribute("mensagemErro", mensagemErro);
        RequestDispatcher rd = request.getRequestDispatcher("/login.jsp");
        rd.forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Servlet responsável pela autenticação de usuários";
    }
}