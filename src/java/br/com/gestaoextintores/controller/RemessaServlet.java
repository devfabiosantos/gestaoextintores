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
import br.com.gestaoextintores.dao.RemessaItemDAO; // Importado
import br.com.gestaoextintores.dao.StatusExtintorDAOImpl;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.RemessaItem; // Importado
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.text.ParseException; // Import ParseException
import java.text.SimpleDateFormat; // Import SimpleDateFormat
import java.util.ArrayList;
import java.util.Date; // Import java.util.Date
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
    private static final String STATUS_EM_RECARGA = "Em Recarga";
    private static final String STATUS_OPERACIONAL = "Operacional";
    private static final SimpleDateFormat DATE_FORMAT_FORM = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null) {
            return; 
        }

        String acao = request.getParameter("acao");
        RemessaDAO remessaDAO = new RemessaDAO();
        RemessaItemDAO itemDAO = new RemessaItemDAO(); 
        FilialDAOImpl filialDAO = new FilialDAOImpl();

        try {
            if (acao == null || "listar".equals(acao)) {

                String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                Integer idFilialFiltro = null;
                if ("Admin".equals(usuarioLogado.getPerfil()) && idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                    try { idFilialFiltro = Integer.parseInt(idFilialFiltroStr); } 
                    catch (NumberFormatException e) {
                    }
                }

                List<Remessa> listaRemessas = remessaDAO.listar(usuarioLogado, idFilialFiltro); 

                List<Filial> listaTodasFiliais = null;
                if ("Admin".equals(usuarioLogado.getPerfil())) {
                    listaTodasFiliais = filialDAO.listar(usuarioLogado); 
                }

                request.setAttribute("listaRemessas", listaRemessas);
                if (listaTodasFiliais != null) { 
                    request.setAttribute("listaTodasFiliais", listaTodasFiliais); 
                }
                request.setAttribute("idFilialSelecionada", idFilialFiltro); 

                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaListar.jsp"); 
                rd.forward(request, response);

            } else if ("detalhar".equals(acao)) {
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaDetalhe.jsp");
                rd.forward(request, response);
            }
            else if ("prepararRecebimento".equals(acao)) {
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaRecebimento.jsp");
                rd.forward(request, response);
            }
             else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida: " + acao);
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        LOGGER.log(Level.INFO, ">>> RemessaServlet doPost() INICIADO! Acao: {0}", request.getParameter("acao"));

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado."); return;
        }

        String acao = request.getParameter("acao");

        try {
            RemessaDAO remessaDAO = new RemessaDAO();
            RemessaItemDAO itemDAO = new RemessaItemDAO();
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();

            if ("criar".equals(acao)) {
                if (!"Técnico".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Apenas Técnicos podem criar remessas."); return;
                }

                String[] idsSelecionadosStr = request.getParameterValues("extintoresSelecionados");
                if (idsSelecionadosStr == null || idsSelecionadosStr.length == 0) {
                     LOGGER.warning("Nenhum extintor selecionado.");
                     sessao.setAttribute("mensagemErro", "Nenhum extintor foi selecionado para a remessa.");
                     response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar"); return;
                }
                LOGGER.log(Level.INFO, "Extintores selecionados: {0}", String.join(",", idsSelecionadosStr));
                List<Integer> idsExtintores = new ArrayList<>();
                for (String idStr : idsSelecionadosStr) { idsExtintores.add(Integer.parseInt(idStr)); }
                Remessa novaRemessa = new Remessa();
                novaRemessa.setIdUsuarioTecnico(usuarioLogado.getIdUsuario());
                novaRemessa.setIdFilial(usuarioLogado.getIdFilial());
                novaRemessa.setStatusRemessa("Enviado");
                LOGGER.info("Chamando remessaDAO.criarRemessa()...");
                int idNovaRemessa = remessaDAO.criarRemessa(novaRemessa);
                LOGGER.log(Level.INFO, "remessaDAO.criarRemessa() retornou ID: {0}", idNovaRemessa);
                if (idNovaRemessa == -1) { throw new ServletException("Falha ao criar o registro principal da remessa."); }
                List<RemessaItem> itensParaAdicionar = new ArrayList<>();
                for (int idExtintor : idsExtintores) {
                    RemessaItem item = new RemessaItem(); item.setIdRemessa(idNovaRemessa);
                    item.setIdExtintor(idExtintor); itensParaAdicionar.add(item);
                }
                LOGGER.info("Chamando itemDAO.adicionarItens()...");
                boolean itensAdicionados = itemDAO.adicionarItens(idNovaRemessa, itensParaAdicionar);
                LOGGER.log(Level.INFO, "itemDAO.adicionarItens() retornou: {0}", itensAdicionados);
                if (!itensAdicionados) { throw new ServletException("Falha ao adicionar itens à remessa."); }
                LOGGER.info("Chamando statusDAO.getIdPorNome(STATUS_EM_REMESSA)...");
                int idStatusEmRemessa = statusDAO.getIdPorNome(STATUS_EM_REMESSA);
                LOGGER.log(Level.INFO, "statusDAO.getIdPorNome(STATUS_EM_REMESSA) retornou ID: {0}", idStatusEmRemessa);
                if (idStatusEmRemessa == -1){ throw new ServletException("Status '" + STATUS_EM_REMESSA + "' não encontrado."); }
                LOGGER.info("Chamando extintorDAO.atualizarStatusVarios()...");
                boolean statusAtualizado = extintorDAO.atualizarStatusVarios(idsExtintores, idStatusEmRemessa, usuarioLogado);
                LOGGER.log(Level.INFO, "extintorDAO.atualizarStatusVarios() retornou: {0}", statusAtualizado);
                if (!statusAtualizado) { throw new ServletException("Falha ao atualizar status para 'Em Remessa'."); }
                LOGGER.info("Remessa criada com SUCESSO. Redirecionando...");
                sessao.setAttribute("mensagemSucesso", "Remessa criada com sucesso!");
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");

            } else if ("aprovarRecolhimento".equals(acao)) {
                if (!"Admin".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Apenas Administradores podem aprovar remessas."); return;
                }

                int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                boolean aprovado = remessaDAO.aprovarParaRecolhimento(idRemessa, usuarioLogado.getIdUsuario());
                if (aprovado) {
                    sessao.setAttribute("mensagemSucesso", "Remessa ID " + idRemessa + " aprovada para recolhimento!");
                } else {
                    sessao.setAttribute("mensagemErro", "Falha ao aprovar remessa ID " + idRemessa + ".");
                }
                response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=listar");

            } else if ("confirmarRecolhimento".equals(acao)) {
                 if (!"Técnico".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Apenas Técnicos podem confirmar o recolhimento."); return;
                }

                int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                int idStatusEmRecarga = statusDAO.getIdPorNome(STATUS_EM_RECARGA);
                if (idStatusEmRecarga == -1){ throw new ServletException("Status '" + STATUS_EM_RECARGA + "' não encontrado."); }
                boolean confirmado = remessaDAO.confirmarRecolhimento(idRemessa);
                if (!confirmado) { throw new ServletException("Falha ao confirmar recolhimento da remessa (ID: " + idRemessa + ")"); }
                List<RemessaItem> itensDaRemessa = itemDAO.listarPorRemessa(idRemessa);
                if (itensDaRemessa.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Recolhimento confirmado para Remessa ID {0}, mas ela não continha itens.", idRemessa);
                } else {
                    List<Integer> idsExtintoresParaAtualizar = new ArrayList<>();
                    for (RemessaItem item : itensDaRemessa) { idsExtintoresParaAtualizar.add(item.getIdExtintor()); }
                    if (!extintorDAO.atualizarStatusVarios(idsExtintoresParaAtualizar, idStatusEmRecarga, usuarioLogado)) {
                         throw new ServletException("Falha ao atualizar o status dos extintores para 'Em Recarga' (ID: " + idRemessa + ")");
                    }
                }
                sessao.setAttribute("mensagemSucesso", "Recolhimento da Remessa ID " + idRemessa + " confirmado! Extintores marcados como 'Em Recarga'.");
                response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=listar");

            } else if ("finalizarRecebimento".equals(acao)) {
                if (!"Técnico".equals(usuarioLogado.getPerfil())) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Apenas Técnicos podem confirmar o recebimento."); return;
                }

                int idRemessa = Integer.parseInt(request.getParameter("idRemessa"));
                String dataRecargaRealStr = request.getParameter("dataRecargaReal");
                String novaDataValidadeStr = request.getParameter("novaDataValidade");
                Date dataRecargaReal = null; Date novaDataValidade = null;
                try {
                     if (dataRecargaRealStr == null || dataRecargaRealStr.trim().isEmpty() || novaDataValidadeStr == null || novaDataValidadeStr.trim().isEmpty()) {
                         throw new ParseException("Datas são obrigatórias.", 0);
                     }
                    dataRecargaReal = DATE_FORMAT_FORM.parse(dataRecargaRealStr);
                    novaDataValidade = DATE_FORMAT_FORM.parse(novaDataValidadeStr);
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "Erro ao converter datas do formulário de recebimento.", e);
                    request.setAttribute("mensagemErro", "Formato de data inválido ou datas não informadas.");
                    RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaRecebimento.jsp");
                    rd.forward(request, response); return; 
                }
                int idStatusOperacional = statusDAO.getIdPorNome(STATUS_OPERACIONAL);
                if (idStatusOperacional == -1){ throw new ServletException("Status '" + STATUS_OPERACIONAL + "' não encontrado."); }
                boolean concluido = remessaDAO.concluirRemessa(idRemessa);
                if (!concluido) { throw new ServletException("Falha ao marcar remessa como concluída (ID: " + idRemessa + ")"); }
                List<RemessaItem> itensDaRemessa = itemDAO.listarPorRemessa(idRemessa);
                if (itensDaRemessa.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Recebimento confirmado para Remessa ID {0}, mas ela não continha itens.", idRemessa);
                } else {
                    List<Integer> idsExtintoresParaAtualizar = new ArrayList<>();
                    for (RemessaItem item : itensDaRemessa) { idsExtintoresParaAtualizar.add(item.getIdExtintor()); }
                    if (!extintorDAO.atualizarDadosPosRecarga(idsExtintoresParaAtualizar, idStatusOperacional, dataRecargaReal, novaDataValidade, usuarioLogado)) {
                         throw new ServletException("Falha ao atualizar status e datas dos extintores para 'Operacional' (ID: " + idRemessa + ")");
                    }
                }
                sessao.setAttribute("mensagemSucesso", "Recebimento da Remessa ID " + idRemessa + " confirmado! Extintores marcados como 'Operacional' e datas atualizadas.");
                response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=listar");

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida: " + acao);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro GERAL no doPost do RemessaServlet", e);
            sessao.setAttribute("mensagemErro", "Erro interno ao processar a requisição: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=listar"); 
            return;
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o fluxo de Remessa/Orçamento";
    }
}