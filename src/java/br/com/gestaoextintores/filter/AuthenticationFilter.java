/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.filter;

import br.com.gestaoextintores.model.Usuario; // Precisamos disso
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter; // A anotação mais importante
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Dev Fabio Santos
 */

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Filtro de Autenticação INICIADO!");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();

        HttpSession sessao = request.getSession(false);

        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        boolean ehPaginaPublica = uri.endsWith("/LoginServlet") || uri.endsWith("/login.jsp");

        if (usuarioLogado != null) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            chain.doFilter(req, res);
            
        } else if (ehPaginaPublica) {
            chain.doFilter(req, res);
            
        } else {
            LOGGER.warning("Acesso não autorizado à URI: " + uri);
            response.sendRedirect(contextPath + "/LoginServlet");
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("Filtro de Autenticação DESTRUÍDO!");
    }
}
