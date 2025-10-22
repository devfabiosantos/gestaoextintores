/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package br.com.gestaoextintores.controller;

/**
 *
 * @author Dev Fabio Santos
 */
import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.RemessaDAO;
import br.com.gestaoextintores.dao.RemessaItemDAO;
import br.com.gestaoextintores.dao.StatusExtintorDAOImpl;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.RemessaItem;
import br.com.gestaoextintores.model.StatusExtintor;
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "RemessaServlet", urlPatterns = {"/RemessaServlet"})
public class RemessaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RemessaServlet.class.getName());
    private static final String STATUS_EM_REMESSA = "Em Remessa";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String acao = request.getParameter("acao");
        RemessaDAO remessaDAO = new RemessaDAO();

        try {
            if (acao == null || "listar".equals(acao)) {
                List<Remessa> listaRemessas = remessaDAO.listar(usuarioLogado);
                request.setAttribute("listaRemessas", listaRemessas);
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaListar.jsp"); 
                rd.forward(request, response);
            } 
            else if ("detalhar".equals(acao)) {
                 response.getWriter().println("Funcionalidade Detalhar Remessa a ser implementada.");
            }
             else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida.");
            }
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Erro no doGet do RemessaServlet", e);
             throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null || !"Técnico".equals(usuarioLogado.getPerfil())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return;
        }

        String acao = request.getParameter("acao");

        if ("criar".equals(acao)) {
            try {
                String[] idsSelecionadosStr = request.getParameterValues("extintoresSelecionados");
                
                if (idsSelecionadosStr == null || idsSelecionadosStr.length == 0) {
                    sessao.setAttribute("mensagemErro", "Nenhum extintor foi selecionado para a remessa.");
                    response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
                    return;
                }
                
                List<Integer> idsExtintores = new ArrayList<>();
                for (String idStr : idsSelecionadosStr) {
                    idsExtintores.add(Integer.parseInt(idStr));
                }

                RemessaDAO remessaDAO = new RemessaDAO();
                RemessaItemDAO itemDAO = new RemessaItemDAO();
                ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
                StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();

                Remessa novaRemessa = new Remessa();
                novaRemessa.setIdUsuarioTecnico(usuarioLogado.getIdUsuario());
                novaRemessa.setIdFilial(usuarioLogado.getIdFilial());
                novaRemessa.setStatusRemessa("Enviado");

                int idNovaRemessa = remessaDAO.criarRemessa(novaRemessa);

                if (idNovaRemessa == -1) {
                    throw new ServletException("Falha ao criar o registro principal da remessa.");
                }

                List<RemessaItem> itensParaAdicionar = new ArrayList<>();
                for (int idExtintor : idsExtintores) {
                    RemessaItem item = new RemessaItem();
                    item.setIdRemessa(idNovaRemessa);
                    item.setIdExtintor(idExtintor);
                    item.setObservacaoTecnico(request.getParameter("observacao_" + idExtintor)); // Exemplo se houvesse campo de obs.
                    itensParaAdicionar.add(item);
                }

                if (!itemDAO.adicionarItens(idNovaRemessa, itensParaAdicionar)) {
                     throw new ServletException("Falha ao adicionar itens à remessa.");
                }

                int idStatusEmRemessa = -1;
                List<StatusExtintor> todosStatus = statusDAO.listar();
                for(StatusExtintor status : todosStatus) {
                    if(STATUS_EM_REMESSA.equals(status.getNome())) {
                        idStatusEmRemessa = status.getIdStatus();
                        break;
                    }
                }
                if(idStatusEmRemessa == -1){
                     throw new ServletException("Status '" + STATUS_EM_REMESSA + "' não encontrado no banco de dados.");
                }

                if (!extintorDAO.atualizarStatusVarios(idsExtintores, idStatusEmRemessa, usuarioLogado)) {
                     throw new ServletException("Falha ao atualizar o status dos extintores para 'Em Remessa'.");
                }

                sessao.setAttribute("mensagemSucesso", "Remessa criada com sucesso!");
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao criar remessa no doPost", e);
                sessao.setAttribute("mensagemErro", "Erro ao criar remessa: " + e.getMessage());
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
            }
        } 
        else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida.");
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o fluxo de Remessa/Orçamento";
    }
}