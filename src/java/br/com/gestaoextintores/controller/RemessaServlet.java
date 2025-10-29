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
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.dao.RemessaDAO;
import br.com.gestaoextintores.dao.RemessaItemDAO;
import br.com.gestaoextintores.dao.StatusExtintorDAOImpl;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.RemessaItem;
import br.com.gestaoextintores.model.StatusExtintor;
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    private static final String STATUS_EM_RECARGA = "Em Recarga";
    private static final String STATUS_OPERACIONAL = "Operacional";
    private static final SimpleDateFormat DATE_FORMAT_FORM = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                    
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null) { response.sendRedirect(request.getContextPath() + "/LoginServlet"); return; }

        String acao = request.getParameter("acao");
        RemessaDAO remessaDAO = new RemessaDAO();
        FilialDAOImpl filialDAO = new FilialDAOImpl();

        try {
            if (acao == null || "listar".equals(acao)) {
                String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                Integer idFilialFiltro = null;
                if ("Admin".equals(usuarioLogado.getPerfil()) && idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                    try { idFilialFiltro = Integer.parseInt(idFilialFiltroStr); } catch (NumberFormatException e) {
                    }
                }
                List<Remessa> listaRemessas = remessaDAO.listar(usuarioLogado, idFilialFiltro);
                List<Filial> listaTodasFiliais = null;
                if ("Admin".equals(usuarioLogado.getPerfil())) { listaTodasFiliais = filialDAO.listar(usuarioLogado); }
                request.setAttribute("listaRemessas", listaRemessas);
                if (listaTodasFiliais != null) { request.setAttribute("listaTodasFiliais", listaTodasFiliais); }
                request.setAttribute("idFilialSelecionada", idFilialFiltro);
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaListar.jsp"); 
                rd.forward(request, response);
            }
            else if ("detalhar".equals(acao)) {
                int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                Remessa remessa = remessaDAO.carregar(idRemessa, usuarioLogado);
                if (remessa == null) {
                    LOGGER.warning("Remessa não encontrada ou acesso negado pelo DAO.");
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Remessa não encontrada ou acesso negado."); return;
                }
                RemessaItemDAO itemDAO = new RemessaItemDAO();
                List<Map<String, Object>> resumoItens = itemDAO.getResumoItensPorClasse(idRemessa);
                List<RemessaItem> listaItensDetalhada = itemDAO.listarPorRemessa(idRemessa);
                request.setAttribute("remessa", remessa);
                request.setAttribute("resumoItens", resumoItens);
                request.setAttribute("listaItensDetalhada", listaItensDetalhada);
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaDetalhe.jsp");
                rd.forward(request, response);
            }
            else if ("prepararRecebimento".equals(acao)) {
                 if (!"Técnico".equals(usuarioLogado.getPerfil())) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado."); return; }
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaRecebimento.jsp");
                rd.forward(request, response);
            }
             else { response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida: " + acao); }
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Erro GERAL no doGet do RemessaServlet", e);
             throw new ServletException("Erro ao processar GET: " + e.getMessage(), e); 
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //request.setCharacterEncoding("UTF-8");
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null) { 
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado."); 
            return; 
        }

        String acao = request.getParameter("acao");
        String redirectUrl = request.getContextPath() + "/RemessaServlet?acao=listar";

        try {
            RemessaDAO remessaDAO = new RemessaDAO();
            RemessaItemDAO itemDAO = new RemessaItemDAO();
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();

            if ("criar".equals(acao)) {
                if (!"Técnico".equals(usuarioLogado.getPerfil())) {
                    return; 
                }
                String[] idsSelecionadosStr = request.getParameterValues("extintoresSelecionados");
                if (idsSelecionadosStr == null || idsSelecionadosStr.length == 0) {
                     LOGGER.warning("Nenhum extintor selecionado."); sessao.setAttribute("mensagemErro", "Nenhum extintor selecionado.");
                     redirectUrl = request.getContextPath() + "/ExtintorServlet?acao=listar"; throw new Exception("Seleção vazia");
                }
                List<Integer> idsExtintores = new ArrayList<>(); 
                for (String idStr : idsSelecionadosStr) { 
                    idsExtintores.add(Integer.parseInt(idStr)); 
                }
                Remessa novaRemessa = new Remessa(); 
                novaRemessa.setIdUsuarioTecnico(usuarioLogado.getIdUsuario()); 
                novaRemessa.setIdFilial(usuarioLogado.getIdFilial()); 
                novaRemessa.setStatusRemessa("Enviado");
                int idNovaRemessa = remessaDAO.criarRemessa(novaRemessa);
                if (idNovaRemessa == -1) {
                    throw new ServletException("Falha ao criar remessa."); 
                }
                List<RemessaItem> itensParaAdicionar = new ArrayList<>(); 
                for (int idExtintor : idsExtintores) {
                }
                boolean itensAdicionados = itemDAO.adicionarItens(idNovaRemessa, itensParaAdicionar);
                if (!itensAdicionados) { 
                    throw new ServletException("Falha ao adicionar itens."); 
                }
                int idStatusEmRemessa = statusDAO.getIdPorNome(STATUS_EM_REMESSA);
                if (idStatusEmRemessa == -1){
                    throw new ServletException("Status '" + STATUS_EM_REMESSA + "' não encontrado."); 
                }
                boolean statusAtualizado = extintorDAO.atualizarStatusVarios(idsExtintores, idStatusEmRemessa, usuarioLogado);
                if (!statusAtualizado) { 
                    throw new ServletException("Falha ao atualizar status para 'Em Remessa'."); 
                }
                sessao.setAttribute("mensagemSucesso", "Remessa criada com sucesso!");
                redirectUrl = request.getContextPath() + "/ExtintorServlet?acao=listar";

            } else if ("aprovarRecolhimento".equals(acao)) {
                 if (!"Admin".equals(usuarioLogado.getPerfil())) {
                     return; 
                 }
                 int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                 boolean aprovado = remessaDAO.aprovarParaRecolhimento(idRemessa, usuarioLogado.getIdUsuario());
                 if (aprovado) { 
                     sessao.setAttribute("mensagemSucesso", "Remessa ID " + idRemessa + " aprovada!"); 
                 } 
                 else { 
                     sessao.setAttribute("mensagemErro", "Falha ao aprovar remessa ID " + idRemessa + "."); }

            } else if ("confirmarRecolhimento".equals(acao)) {
                 if (!"Técnico".equals(usuarioLogado.getPerfil())) {
                     return; 
                 }
                 int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                 int idStatusEmRecarga = statusDAO.getIdPorNome(STATUS_EM_RECARGA);
                 if (idStatusEmRecarga == -1){ 
                     throw new ServletException("Status '" + STATUS_EM_RECARGA + "' não encontrado."); 
                 }
                 boolean confirmado = remessaDAO.confirmarRecolhimento(idRemessa);
                 if (!confirmado) { 
                     throw new ServletException("Falha ao confirmar recolhimento (ID: " + idRemessa + ")"); 
                 }
                 List<RemessaItem> itensDaRemessa = itemDAO.listarPorRemessa(idRemessa);
                 if (!itensDaRemessa.isEmpty()) {
                    List<Integer> idsExtintores = new ArrayList<>(); 
                    for (RemessaItem item : itensDaRemessa) { 
                        idsExtintores.add(item.getIdExtintor()); 
                    }
                    if (!extintorDAO.atualizarStatusVarios(idsExtintores, idStatusEmRecarga, usuarioLogado)) {
                         throw new ServletException("Falha ao atualizar status para 'Em Recarga' (ID: " + idRemessa + ")");
                    }
                 } else { 
                     LOGGER.log(Level.WARNING, "Remessa ID {0} sem itens ao confirmar recolhimento.", idRemessa); 
                 }
                 sessao.setAttribute("mensagemSucesso", "Recolhimento da Remessa ID " + idRemessa + " confirmado!");

            } else if ("finalizarRecebimento".equals(acao)) {
                if (!"Técnico".equals(usuarioLogado.getPerfil())) {
                    return; 
                }
                int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                String dataRecargaRealStr = request.getParameter("dataRecargaReal");
                String novaDataValidadeStr = request.getParameter("novaDataValidade");
                Date dataRecargaReal = null; Date novaDataValidade = null;
                try {
                     if (dataRecargaRealStr == null || dataRecargaRealStr.isEmpty() || novaDataValidadeStr == null || novaDataValidadeStr.isEmpty()) { 
                         throw new ParseException("Datas obrigatórias.", 0); 
                     }
                    dataRecargaReal = DATE_FORMAT_FORM.parse(dataRecargaRealStr);
                    novaDataValidade = DATE_FORMAT_FORM.parse(novaDataValidadeStr);
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "Erro ao converter datas.", e); request.setAttribute("mensagemErro", "Datas inválidas.");
                    RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaRecebimento.jsp"); rd.forward(request, response); 
                    return; 
                }
                int idStatusOperacional = statusDAO.getIdPorNome(STATUS_OPERACIONAL);
                if (idStatusOperacional == -1){ 
                    throw new ServletException("Status '" + STATUS_OPERACIONAL + "' não encontrado."); 
                }
                boolean concluido = remessaDAO.concluirRemessa(idRemessa);
                if (!concluido) { 
                    throw new ServletException("Falha ao concluir remessa (ID: " + idRemessa + ")"); 
                }
                List<RemessaItem> itensDaRemessa = itemDAO.listarPorRemessa(idRemessa);
                if (!itensDaRemessa.isEmpty()) {
                    List<Integer> idsExtintores = new ArrayList<>(); 
                    for (RemessaItem item : itensDaRemessa) { 
                        idsExtintores.add(item.getIdExtintor()); 
                    }
                    if (!extintorDAO.atualizarDadosPosRecarga(idsExtintores, idStatusOperacional, dataRecargaReal, novaDataValidade, usuarioLogado)) {
                         throw new ServletException("Falha ao atualizar dados pós-recarga (ID: " + idRemessa + ")");
                    }
                } else { 
                    LOGGER.log(Level.WARNING, "Remessa ID {0} sem itens ao finalizar recebimento.", idRemessa); 
                }
                sessao.setAttribute("mensagemSucesso", "Recebimento da Remessa ID " + idRemessa + " confirmado!");

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida: " + acao);
                return;
            }

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro GERAL no doPost do RemessaServlet", e);
            sessao.setAttribute("mensagemErro", "Erro interno: " + e.getMessage());
            response.sendRedirect(redirectUrl);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o fluxo de Remessa/Orçamento";
    }
}