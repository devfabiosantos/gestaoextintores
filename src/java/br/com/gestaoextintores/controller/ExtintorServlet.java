package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.dao.SetorDAOImpl;
import br.com.gestaoextintores.dao.StatusExtintorDAOImpl;
import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.model.Setor;
import br.com.gestaoextintores.model.StatusExtintor;
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
            return; 
        }
        
        String acao = request.getParameter("acao");

        try {
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            SetorDAOImpl setorDAO = new SetorDAOImpl(); 
            StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (acao == null || acao.equals("listar")) {

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
 
            } else if (acao.equals("novo")) {
                List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                List<StatusExtintor> listaStatus = statusDAO.listar(); 
                request.setAttribute("listaSetores", listaSetores);
                request.setAttribute("listaStatus", listaStatus);
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorCadastrar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("editar")) {
                 int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                 Extintor extintor = extintorDAO.carregar(idExtintor, usuarioLogado);
                 if (extintor == null) {
                     return; 
                 }
                 List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                 List<StatusExtintor> listaStatus = statusDAO.listar();
                 request.setAttribute("extintor", extintor);
                 request.setAttribute("listaSetores", listaSetores);
                 request.setAttribute("listaStatus", listaStatus);
                 RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorEditar.jsp");
                 rd.forward(request, response);

            } else if (acao.equals("excluir")) {
                 int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                 extintorDAO.excluir(idExtintor, usuarioLogado);
                 response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
            }

        } catch (Exception ex) {
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;

        if (usuarioLogado == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado");
            return;
        }

        String acao = request.getParameter("acao");

        try {
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();

            String tipoEquipamento = request.getParameter("tipoEquipamento");
            String numeroControle = request.getParameter("numeroControle");
            String classeExtintora = request.getParameter("classeExtintora");
            String cargaNominal = request.getParameter("cargaNominal");
            String referenciaLocalizacao = request.getParameter("referenciaLocalizacao");
            String observacao = request.getParameter("observacao");
            int idSetor = Integer.parseInt(request.getParameter("idSetor"));
            int idStatus = Integer.parseInt(request.getParameter("idStatus"));
            Date dataRecarga = null;
            Date dataValidade = null;
            try {
                String dataRecargaStr = request.getParameter("dataRecarga");
                String dataValidadeStr = request.getParameter("dataValidade");
                if (dataRecargaStr != null && !dataRecargaStr.isEmpty()) {
                    dataRecarga = DATE_FORMAT.parse(dataRecargaStr);
                }
                if (dataValidadeStr != null && !dataValidadeStr.isEmpty()) {
                    dataValidade = DATE_FORMAT.parse(dataValidadeStr);
                }
            } catch (ParseException e) {
                 LOGGER.log(Level.WARNING, "Erro ao converter datas", e);
            }

            Extintor extintor = new Extintor();
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
                extintorDAO.cadastrar(extintor);

            } else if ("atualizar".equals(acao)) {
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                extintor.setIdExtintor(idExtintor);
                extintorDAO.alterar(extintor, usuarioLogado); 
            }

            response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no ExtintorServlet (POST): ", ex);
            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Extintores";
    }
}