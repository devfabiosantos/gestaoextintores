package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.dao.RemessaDAO;
import br.com.gestaoextintores.dao.RemessaItemDAO;
import br.com.gestaoextintores.dao.StatusExtintorDAOImpl;
import br.com.gestaoextintores.dao.UsuarioDAO;
import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.RemessaItem;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.EmailService;
import br.com.gestaoextintores.util.PdfRemessaGenerator;
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
    private static final String PERFIL_ADMIN = "Admin";
    private static final String PERFIL_TECNICO = "Técnico";
    private static final String STATUS_ENVIADO = "Enviado";
    private static final String STATUS_APROVADO_RECOLHIMENTO = "Aprovado p/ Recolhimento";
    private static final String STATUS_EM_REMESSA = "Em Remessa";
    private static final String STATUS_EM_RECARGA = "Em Recarga";
    private static final String STATUS_OPERACIONAL = "Operacional";
    private static final String STATUS_CONCLUIDO = "Concluído";
    private static final SimpleDateFormat DATE_FORMAT_FORM = new SimpleDateFormat("yyyy-MM-dd");

    private Usuario getUsuarioLogado(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        return (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
    }

    private boolean isAdmin(Usuario usuarioLogado) {
        return usuarioLogado != null && PERFIL_ADMIN.equals(usuarioLogado.getPerfil());
    }

    private boolean isTecnico(Usuario usuarioLogado) {
        return usuarioLogado != null && PERFIL_TECNICO.equals(usuarioLogado.getPerfil());
    }

    private Integer getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private Remessa carregarRemessaOu404(HttpServletResponse response, RemessaDAO remessaDAO, Usuario usuarioLogado, Integer idRemessa)
            throws IOException {
        if (idRemessa == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID da remessa inválido.");
            return null;
        }

        Remessa remessa = remessaDAO.carregar(idRemessa, usuarioLogado);
        if (remessa == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Remessa não encontrada ou acesso negado.");
            return null;
        }
        return remessa;
    }

    private String tentarEnviarEmailRemessa(Remessa remessa, Usuario tecnicoSolicitante, byte[] pdfConteudo, String nomeArquivo)
            throws Exception {
        EmailService emailService = new EmailService();
        EmailService.ResultadoEnvio resultadoEnvio = emailService.enviarNovaRemessa(remessa, tecnicoSolicitante, pdfConteudo, nomeArquivo);
        if (resultadoEnvio.isEnviado()) {
            return "E-mail enviado com sucesso.";
        }
        if (resultadoEnvio.getMensagem() != null && !resultadoEnvio.getMensagem().trim().isEmpty()) {
            return resultadoEnvio.getMensagem();
        }
        return "Envio de e-mail não realizado.";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioLogado = getUsuarioLogado(request);
        if (usuarioLogado == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String acao = request.getParameter("acao");
        RemessaDAO remessaDAO = new RemessaDAO();
        FilialDAOImpl filialDAO = new FilialDAOImpl();

        try {
            if (acao == null || "listar".equals(acao)) {
                Integer idFilialFiltro = null;
                if (isAdmin(usuarioLogado)) {
                    String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                    if (idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                        try {
                            idFilialFiltro = Integer.parseInt(idFilialFiltroStr);
                        } catch (NumberFormatException e) {
                            request.setAttribute("mensagemErro", "ID de filial inválido no filtro.");
                        }
                    }
                }

                List<Remessa> listaRemessas = remessaDAO.listar(usuarioLogado, idFilialFiltro);
                request.setAttribute("listaRemessas", listaRemessas);
                request.setAttribute("idFilialSelecionada", idFilialFiltro);

                if (isAdmin(usuarioLogado)) {
                    List<Filial> listaTodasFiliais = filialDAO.listar(usuarioLogado);
                    request.setAttribute("listaTodasFiliais", listaTodasFiliais);
                }

                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaListar.jsp");
                rd.forward(request, response);

            } else if ("detalhar".equals(acao)) {
                Remessa remessa = carregarRemessaOu404(response, remessaDAO, usuarioLogado, getIntParameter(request, "idRemessa"));
                if (remessa == null) {
                    return;
                }

                RemessaItemDAO itemDAO = new RemessaItemDAO();
                List<Map<String, Object>> resumoItens = itemDAO.getResumoItensPorClasse(remessa.getIdRemessa());
                List<RemessaItem> listaItensDetalhada = itemDAO.listarPorRemessa(remessa.getIdRemessa());
                request.setAttribute("remessa", remessa);
                request.setAttribute("resumoItens", resumoItens);
                request.setAttribute("listaItensDetalhada", listaItensDetalhada);
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaDetalhe.jsp");
                rd.forward(request, response);

            } else if ("prepararRecebimento".equals(acao)) {
                if (!isTecnico(usuarioLogado)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
                    return;
                }

                Remessa remessa = carregarRemessaOu404(response, remessaDAO, usuarioLogado, getIntParameter(request, "idRemessa"));
                if (remessa == null) {
                    return;
                }

                if (!STATUS_EM_RECARGA.equals(remessa.getStatusRemessa())) {
                    request.getSession().setAttribute("mensagemErro", "A remessa precisa estar em recarga para finalizar o recebimento.");
                    response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=detalhar&idRemessa=" + remessa.getIdRemessa());
                    return;
                }

                request.setAttribute("remessa", remessa);
                RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaRecebimento.jsp");
                rd.forward(request, response);

            } else if ("baixarPdf".equals(acao)) {
                Integer idRemessa = getIntParameter(request, "idRemessa");
                if (idRemessa == null) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID da remessa inválido.");
                    return;
                }

                Remessa remessa = remessaDAO.carregarPdf(idRemessa, usuarioLogado);
                if (remessa == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF da remessa não encontrado ou acesso negado.");
                    return;
                }
                if (remessa.getPdfConteudo() == null || remessa.getPdfConteudo().length == 0) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "A remessa ainda não possui PDF gerado.");
                    return;
                }

                response.setContentType(remessa.getPdfMimeType() != null ? remessa.getPdfMimeType() : "application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + remessa.getPdfNomeArquivo() + "\"");
                response.setContentLength(remessa.getPdfConteudo().length);
                response.getOutputStream().write(remessa.getPdfConteudo());
                response.getOutputStream().flush();

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida: " + acao);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro GERAL no doGet do RemessaServlet", e);
            throw new ServletException("Erro ao processar GET: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioLogado = getUsuarioLogado(request);
        if (usuarioLogado == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return;
        }

        HttpSession sessao = request.getSession();
        String acao = request.getParameter("acao");
        String redirectUrl = request.getContextPath() + "/RemessaServlet?acao=listar";

        try {
            RemessaDAO remessaDAO = new RemessaDAO();
            RemessaItemDAO itemDAO = new RemessaItemDAO();
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();
            UsuarioDAO usuarioDAO = new UsuarioDAO();

            if ("criar".equals(acao)) {
                if (!isTecnico(usuarioLogado)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
                    return;
                }

                String[] idsSelecionadosStr = request.getParameterValues("extintoresSelecionados");
                if (idsSelecionadosStr == null || idsSelecionadosStr.length == 0) {
                    sessao.setAttribute("mensagemErro", "Nenhum extintor selecionado.");
                    response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
                    return;
                }

                List<Integer> idsExtintores = new ArrayList<>();
                for (String idStr : idsSelecionadosStr) {
                    idsExtintores.add(Integer.parseInt(idStr));
                }

                Remessa novaRemessa = new Remessa();
                novaRemessa.setIdUsuarioTecnico(usuarioLogado.getIdUsuario());
                novaRemessa.setIdFilial(usuarioLogado.getIdFilial());
                novaRemessa.setStatusRemessa(STATUS_ENVIADO);

                int idNovaRemessa = remessaDAO.criarRemessa(novaRemessa);
                if (idNovaRemessa <= 0) {
                    throw new ServletException("Falha ao criar remessa.");
                }

                List<RemessaItem> itensParaAdicionar = new ArrayList<>();
                for (int idExtintor : idsExtintores) {
                    RemessaItem item = new RemessaItem();
                    item.setIdRemessa(idNovaRemessa);
                    item.setIdExtintor(idExtintor);
                    itensParaAdicionar.add(item);
                }

                if (!itemDAO.adicionarItens(idNovaRemessa, itensParaAdicionar)) {
                    throw new ServletException("Falha ao adicionar itens à remessa.");
                }

                int idStatusEmRemessa = statusDAO.getIdPorNome(STATUS_EM_REMESSA);
                if (idStatusEmRemessa == -1) {
                    throw new ServletException("Status '" + STATUS_EM_REMESSA + "' não encontrado.");
                }

                if (!extintorDAO.atualizarStatusVarios(idsExtintores, idStatusEmRemessa, usuarioLogado)) {
                    throw new ServletException("Falha ao atualizar status dos extintores para remessa.");
                }

                Remessa remessaPdf = remessaDAO.carregar(idNovaRemessa, usuarioLogado);
                if (remessaPdf == null) {
                    throw new ServletException("Falha ao carregar remessa recém-criada para geração do PDF.");
                }
                List<Extintor> extintoresSelecionados = extintorDAO.carregarPorIds(idsExtintores, usuarioLogado);
                LOGGER.log(Level.INFO, "Gerando PDF da remessa ID {0} com {1} extintores.",
                        new Object[]{idNovaRemessa, extintoresSelecionados.size()});
                byte[] pdfConteudo = PdfRemessaGenerator.gerarPdfRemessa(remessaPdf, extintoresSelecionados);
                String nomeArquivo = "remessa_" + idNovaRemessa + ".pdf";
                if (!remessaDAO.salvarPdf(idNovaRemessa, nomeArquivo, "application/pdf", pdfConteudo)) {
                    throw new ServletException("Falha ao salvar PDF da remessa.");
                }

                String mensagemSucesso = "Remessa criada com sucesso. PDF gerado.";
                try {
                    mensagemSucesso += " " + tentarEnviarEmailRemessa(remessaPdf, usuarioLogado, pdfConteudo, nomeArquivo);
                } catch (Exception emailEx) {
                    LOGGER.log(Level.WARNING, "Falha ao enviar e-mail da remessa ID " + idNovaRemessa, emailEx);
                    mensagemSucesso += " Não foi possível enviar o e-mail nesta operação.";
                }

                sessao.setAttribute("mensagemSucesso", mensagemSucesso);
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
                return;

            } else if ("aprovarRecolhimento".equals(acao)) {
                if (!isAdmin(usuarioLogado)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
                    return;
                }

                Remessa remessa = carregarRemessaOu404(response, remessaDAO, usuarioLogado, getIntParameter(request, "idRemessa"));
                if (remessa == null) {
                    return;
                }

                if (!STATUS_ENVIADO.equals(remessa.getStatusRemessa())) {
                    sessao.setAttribute("mensagemErro", "Só é possível aprovar remessas com status 'Enviado'.");
                    response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=detalhar&idRemessa=" + remessa.getIdRemessa());
                    return;
                }

                boolean aprovado = remessaDAO.aprovarParaRecolhimento(remessa.getIdRemessa(), usuarioLogado.getIdUsuario());
                if (aprovado) {
                    sessao.setAttribute("mensagemSucesso", "Remessa ID " + remessa.getIdRemessa() + " aprovada.");
                } else {
                    sessao.setAttribute("mensagemErro", "Falha ao aprovar remessa ID " + remessa.getIdRemessa() + ".");
                }

            } else if ("confirmarRecolhimento".equals(acao)) {
                if (!isTecnico(usuarioLogado)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
                    return;
                }

                Remessa remessa = carregarRemessaOu404(response, remessaDAO, usuarioLogado, getIntParameter(request, "idRemessa"));
                if (remessa == null) {
                    return;
                }

                if (!STATUS_APROVADO_RECOLHIMENTO.equals(remessa.getStatusRemessa())) {
                    sessao.setAttribute("mensagemErro", "Só é possível confirmar recolhimento de remessas aprovadas.");
                    response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=detalhar&idRemessa=" + remessa.getIdRemessa());
                    return;
                }

                int idStatusEmRecarga = statusDAO.getIdPorNome(STATUS_EM_RECARGA);
                if (idStatusEmRecarga == -1) {
                    throw new ServletException("Status '" + STATUS_EM_RECARGA + "' não encontrado.");
                }

                boolean confirmado = remessaDAO.confirmarRecolhimento(remessa.getIdRemessa());
                if (!confirmado) {
                    throw new ServletException("Falha ao confirmar recolhimento da remessa ID " + remessa.getIdRemessa() + ".");
                }

                List<RemessaItem> itensDaRemessa = itemDAO.listarPorRemessa(remessa.getIdRemessa());
                if (itensDaRemessa.isEmpty()) {
                    throw new ServletException("Remessa sem itens para recolhimento.");
                }

                List<Integer> idsExtintores = new ArrayList<>();
                for (RemessaItem item : itensDaRemessa) {
                    idsExtintores.add(item.getIdExtintor());
                }

                if (!extintorDAO.atualizarStatusVarios(idsExtintores, idStatusEmRecarga, usuarioLogado)) {
                    throw new ServletException("Falha ao atualizar status dos extintores para recarga.");
                }

                sessao.setAttribute("mensagemSucesso", "Recolhimento da Remessa ID " + remessa.getIdRemessa() + " confirmado.");

            } else if ("finalizarRecebimento".equals(acao)) {
                if (!isTecnico(usuarioLogado)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
                    return;
                }

                Remessa remessa = carregarRemessaOu404(response, remessaDAO, usuarioLogado, getIntParameter(request, "idRemessa"));
                if (remessa == null) {
                    return;
                }

                if (!STATUS_EM_RECARGA.equals(remessa.getStatusRemessa())) {
                    sessao.setAttribute("mensagemErro", "Só é possível finalizar remessas em recarga.");
                    response.sendRedirect(request.getContextPath() + "/RemessaServlet?acao=detalhar&idRemessa=" + remessa.getIdRemessa());
                    return;
                }

                String dataRecargaRealStr = request.getParameter("dataRecargaReal");
                String novaDataValidadeStr = request.getParameter("novaDataValidade");
                Date dataRecargaReal;
                Date novaDataValidade;

                try {
                    if (dataRecargaRealStr == null || dataRecargaRealStr.isEmpty()
                            || novaDataValidadeStr == null || novaDataValidadeStr.isEmpty()) {
                        throw new ParseException("Datas obrigatórias.", 0);
                    }
                    dataRecargaReal = DATE_FORMAT_FORM.parse(dataRecargaRealStr);
                    novaDataValidade = DATE_FORMAT_FORM.parse(novaDataValidadeStr);
                } catch (ParseException e) {
                    request.setAttribute("mensagemErro", "Datas inválidas.");
                    request.setAttribute("remessa", remessa);
                    RequestDispatcher rd = request.getRequestDispatcher("/remessa/remessaRecebimento.jsp");
                    rd.forward(request, response);
                    return;
                }

                int idStatusOperacional = statusDAO.getIdPorNome(STATUS_OPERACIONAL);
                if (idStatusOperacional == -1) {
                    throw new ServletException("Status '" + STATUS_OPERACIONAL + "' não encontrado.");
                }

                boolean concluido = remessaDAO.concluirRemessa(remessa.getIdRemessa());
                if (!concluido) {
                    throw new ServletException("Falha ao concluir remessa ID " + remessa.getIdRemessa() + ".");
                }

                List<RemessaItem> itensDaRemessa = itemDAO.listarPorRemessa(remessa.getIdRemessa());
                if (itensDaRemessa.isEmpty()) {
                    throw new ServletException("Remessa sem itens para finalizar recebimento.");
                }

                List<Integer> idsExtintores = new ArrayList<>();
                for (RemessaItem item : itensDaRemessa) {
                    idsExtintores.add(item.getIdExtintor());
                }

                if (!extintorDAO.atualizarDadosPosRecarga(idsExtintores, idStatusOperacional, dataRecargaReal, novaDataValidade, usuarioLogado)) {
                    throw new ServletException("Falha ao atualizar dados pós-recarga.");
                }

                sessao.setAttribute("mensagemSucesso", "Recebimento da Remessa ID " + remessa.getIdRemessa() + " confirmado.");

            } else if ("reenviarEmail".equals(acao)) {
                if (!isAdmin(usuarioLogado)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
                    return;
                }

                Remessa remessa = carregarRemessaOu404(response, remessaDAO, usuarioLogado, getIntParameter(request, "idRemessa"));
                if (remessa == null) {
                    return;
                }

                redirectUrl = request.getContextPath() + "/RemessaServlet?acao=detalhar&idRemessa=" + remessa.getIdRemessa();

                if (remessa.getPdfNomeArquivo() == null || remessa.getPdfNomeArquivo().trim().isEmpty()) {
                    sessao.setAttribute("mensagemErro", "A remessa ainda não possui PDF gerado para reenvio.");
                    response.sendRedirect(redirectUrl);
                    return;
                }

                Remessa remessaComPdf = remessaDAO.carregarPdf(remessa.getIdRemessa(), usuarioLogado);
                if (remessaComPdf == null || remessaComPdf.getPdfConteudo() == null || remessaComPdf.getPdfConteudo().length == 0) {
                    sessao.setAttribute("mensagemErro", "Não foi possível carregar o PDF da remessa para reenvio.");
                    response.sendRedirect(redirectUrl);
                    return;
                }

                remessa.setPdfConteudo(remessaComPdf.getPdfConteudo());
                remessa.setPdfMimeType(remessaComPdf.getPdfMimeType());
                remessa.setPdfNomeArquivo(remessaComPdf.getPdfNomeArquivo());
                remessa.setPdfGeradoEm(remessaComPdf.getPdfGeradoEm());

                Usuario tecnicoSolicitante = usuarioDAO.carregar(remessa.getIdUsuarioTecnico());
                if (tecnicoSolicitante != null) {
                    remessa.setTecnico(tecnicoSolicitante);
                }

                try {
                    String mensagemResultado = tentarEnviarEmailRemessa(
                            remessa,
                            tecnicoSolicitante,
                            remessa.getPdfConteudo(),
                            remessa.getPdfNomeArquivo());
                    sessao.setAttribute("mensagemSucesso", "Reenvio da remessa ID " + remessa.getIdRemessa() + ": " + mensagemResultado);
                } catch (Exception emailEx) {
                    LOGGER.log(Level.WARNING, "Falha ao reenviar e-mail da remessa ID " + remessa.getIdRemessa(), emailEx);
                    sessao.setAttribute("mensagemErro", "Não foi possível reenviar o e-mail da remessa ID " + remessa.getIdRemessa() + ".");
                }

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida: " + acao);
                return;
            }

            response.sendRedirect(redirectUrl);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID inválido.");
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
