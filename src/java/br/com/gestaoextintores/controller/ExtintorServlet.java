package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.dao.SetorDAOImpl;
import br.com.gestaoextintores.dao.StatusExtintorDAOImpl;
import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Setor;
import br.com.gestaoextintores.model.StatusExtintor;
import br.com.gestaoextintores.model.Usuario;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

@WebServlet(name = "ExtintorServlet", urlPatterns = {"/ExtintorServlet"})
public class ExtintorServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ExtintorServlet.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

        try {
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            SetorDAOImpl setorDAO = new SetorDAOImpl();
            StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (acao == null || "listar".equals(acao)) {
                String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                Integer idFilialFiltro = null;
                if ("Admin".equals(usuarioLogado.getPerfil()) && idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                    try {
                        idFilialFiltro = Integer.parseInt(idFilialFiltroStr);
                    } catch (NumberFormatException e) {
                    }
                }
                List<Extintor> listaExtintores = extintorDAO.listar(usuarioLogado, idFilialFiltro);
                List<Filial> listaTodasFiliais = null;
                if ("Admin".equals(usuarioLogado.getPerfil())) {
                    listaTodasFiliais = filialDAO.listar(usuarioLogado);
                }
                request.setAttribute("listaExtintores", listaExtintores);
                if (listaTodasFiliais != null) {
                    request.setAttribute("listaTodasFiliais", listaTodasFiliais);
                }
                request.setAttribute("idFilialSelecionada", idFilialFiltro);
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorListar.jsp");
                rd.forward(request, response);

            } else if ("novo".equals(acao)) {
                List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                List<StatusExtintor> listaStatus = statusDAO.listar();
                request.setAttribute("listaSetores", listaSetores);
                request.setAttribute("listaStatus", listaStatus);
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorCadastrar.jsp");
                rd.forward(request, response);

            } else if ("editar".equals(acao)) {
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                Extintor extintor = extintorDAO.carregar(idExtintor, usuarioLogado);
                if (extintor == null) {
                    LOGGER.log(Level.WARNING, "Tentativa de editar extintor inexistente ou não permitido (ID: {0}) pelo usuário {1}",
                            new Object[]{idExtintor, usuarioLogado.getLogin()});
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Extintor não encontrado ou acesso negado.");
                    return;
                }
                List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                List<StatusExtintor> listaStatus = statusDAO.listar();
                request.setAttribute("extintor", extintor);
                request.setAttribute("listaSetores", listaSetores);
                request.setAttribute("listaStatus", listaStatus);
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorEditar.jsp");
                rd.forward(request, response);

            } else if ("excluir".equals(acao)) {
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                extintorDAO.excluir(idExtintor, usuarioLogado);
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida: " + acao);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro GERAL no doGet do ExtintorServlet", ex);
            throw new ServletException("Erro GET Extintor: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
            return;
        }

        String acao = request.getParameter("acao");
        String jspDestino = null;
        boolean sucessoOperacao = false;

        ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
        SetorDAOImpl setorDAO = new SetorDAOImpl();
        StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();

        Extintor extintor = new Extintor();

        try {
            String tipoEquipamento = request.getParameter("tipoEquipamento");
            String numeroControle = request.getParameter("numeroControle");
            String classeExtintora = request.getParameter("classeExtintora");
            String cargaNominal = request.getParameter("cargaNominal");
            String referenciaLocalizacao = request.getParameter("referenciaLocalizacao");
            String observacao = request.getParameter("observacao");
            String dataRecargaStr = request.getParameter("dataRecarga");
            String dataValidadeStr = request.getParameter("dataValidade");
            int idSetor = Integer.parseInt(request.getParameter("idSetor"));
            int idStatus = Integer.parseInt(request.getParameter("idStatus"));

            Date dataRecarga = null;
            Date dataValidade = null;

            try {
                if (dataRecargaStr != null && !dataRecargaStr.isEmpty()) {
                    dataRecarga = DATE_FORMAT.parse(dataRecargaStr);
                }
                if (dataValidadeStr != null && !dataValidadeStr.isEmpty()) {
                    dataValidade = DATE_FORMAT.parse(dataValidadeStr);
                }
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Erro ao converter datas", e);
                request.setAttribute("mensagemErro", "Formato de data inválido.");
                jspDestino = "/extintor/" + ("salvar".equals(acao) ? "extintorCadastrar.jsp" : "extintorEditar.jsp");
                request.setAttribute("listaSetores", setorDAO.listar(usuarioLogado));
                request.setAttribute("listaStatus", statusDAO.listar());
                request.setAttribute("dataRecargaValor", dataRecargaStr);
                request.setAttribute("dataValidadeValor", dataValidadeStr);
                extintor.setTipoEquipamento(tipoEquipamento);
                extintor.setNumeroControle(numeroControle);
                extintor.setClasseExtintora(classeExtintora);
                extintor.setCargaNominal(cargaNominal);
                extintor.setReferenciaLocalizacao(referenciaLocalizacao);
                extintor.setObservacao(observacao);
                extintor.setIdSetor(idSetor);
                extintor.setIdStatus(idStatus);
                if ("atualizar".equals(acao)) {
                    extintor.setIdExtintor(Integer.parseInt(request.getParameter("idExtintor")));
                }
                request.setAttribute("extintor", extintor);

                RequestDispatcher rd = request.getRequestDispatcher(jspDestino);
                rd.forward(request, response);
                return;
            }

            extintor.setTipoEquipamento(tipoEquipamento);
            extintor.setNumeroControle(numeroControle);
            extintor.setClasseExtintora(classeExtintora);
            extintor.setCargaNominal(cargaNominal);
            extintor.setReferenciaLocalizacao(referenciaLocalizacao);
            extintor.setObservacao(observacao);
            extintor.setIdSetor(idSetor);
            extintor.setIdStatus(idStatus);
            extintor.setDataRecarga(dataRecarga);
            extintor.setDataValidade(dataValidade);

            if ("salvar".equals(acao)) {
                jspDestino = "/extintor/extintorCadastrar.jsp";
                sucessoOperacao = extintorDAO.cadastrar(extintor);
                if (sucessoOperacao) {
                    sessao.setAttribute("mensagemSucesso", "Extintor cadastrado com sucesso!");
                } else {
                    request.setAttribute("mensagemErro", "Falha ao cadastrar. Verifique se o Número de Controle já existe nesta filial.");
                    request.setAttribute("extintor", extintor);
                    request.setAttribute("listaSetores", setorDAO.listar(usuarioLogado));
                    request.setAttribute("listaStatus", statusDAO.listar());
                    request.setAttribute("dataRecargaValor", dataRecargaStr);
                    request.setAttribute("dataValidadeValor", dataValidadeStr);
                }

            } else if ("atualizar".equals(acao)) {
                jspDestino = "/extintor/extintorEditar.jsp";
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                extintor.setIdExtintor(idExtintor);
                sucessoOperacao = extintorDAO.alterar(extintor, usuarioLogado);
                if (sucessoOperacao) {
                    sessao.setAttribute("mensagemSucesso", "Extintor alterado com sucesso!");
                } else {
                    request.setAttribute("mensagemErro", "Falha ao alterar. Verifique se o Número de Controle já existe nesta filial.");
                    request.setAttribute("extintor", extintor);
                    request.setAttribute("listaSetores", setorDAO.listar(usuarioLogado));
                    request.setAttribute("listaStatus", statusDAO.listar());
                    request.setAttribute("dataRecargaValor", dataRecargaStr);
                    request.setAttribute("dataValidadeValor", dataValidadeStr);
                }

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida.");
                return;
            }

            if (sucessoOperacao) {
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
            } else {
                RequestDispatcher rd = request.getRequestDispatcher(jspDestino);
                rd.forward(request, response);
            }

        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Erro ao converter IDs no doPost", ex);
            sessao.setAttribute("mensagemErro", "ID inválido fornecido para Setor, Status ou Extintor.");
            response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro GERAL no doPost do ExtintorServlet", ex);
            sessao.setAttribute("mensagemErro", "Erro interno ao processar: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Extintores";
    }
}
