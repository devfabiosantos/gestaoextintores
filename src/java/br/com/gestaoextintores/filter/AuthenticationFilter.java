/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.filter;

import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());
    private static final String ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("Filtro de Autenticação INICIADO!");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        req.setCharacterEncoding(ENCODING);
        res.setCharacterEncoding(ENCODING);

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String acao = request.getParameter("acao");

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        boolean ehPaginaPublica = uri.endsWith("/LoginServlet") || uri.endsWith("/login.jsp");
        boolean ehRecursoEstatico = uri.contains("/css/") || uri.contains("/js/") || uri.contains("/images/");

        if (usuarioLogado != null) {
            boolean ehAcaoAdmin = false;
            if (uri.contains("/UsuarioServlet")) { ehAcaoAdmin = true; }
            else if (uri.contains("/FilialServlet")) { ehAcaoAdmin = true; }
            else if (uri.contains("/ExtintorServlet") && "excluir".equals(acao)) { ehAcaoAdmin = true; }
            else if (uri.contains("/RemessaServlet") && "aprovarRecolhimento".equals(acao)) { ehAcaoAdmin = true; }

            if (ehAcaoAdmin && !"Admin".equals(usuarioLogado.getPerfil())) {
                LOGGER.log(Level.WARNING, "ACESSO NEGADO (Permissão): Usuário {0} ({1}) tentou ação Admin: {2}?acao={3}",
                           new Object[]{usuarioLogado.getLogin(), usuarioLogado.getPerfil(), uri, acao});
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Permissão insuficiente para esta ação.");
                return;
            }
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            chain.doFilter(req, res);

        } else if (ehPaginaPublica || ehRecursoEstatico) {
            chain.doFilter(req, res);

        } else {
            LOGGER.log(Level.WARNING, "ACESSO NEGADO (Não Autenticado) à URI: {0}", uri);
            response.sendRedirect(contextPath + "/LoginServlet");
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("Filtro de Autenticação DESTRUÍDO!");
    }
}