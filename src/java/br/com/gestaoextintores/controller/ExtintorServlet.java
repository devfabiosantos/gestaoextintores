package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.model.Extintor;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ExtintorServlet", urlPatterns = {"/ExtintorServlet"})
public class ExtintorServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ExtintorServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String acao = request.getParameter("acao");

        try {
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            if (acao == null || acao.equals("listar")) {
                List<Object> listaExtintores = extintorDAO.listar();
                request.setAttribute("listaExtintores", listaExtintores);

                // CORRIGIDO:
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorListar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("novo")) {
                List<Object> listaFiliais = filialDAO.listar();
                request.setAttribute("listaFiliais", listaFiliais);

                // CORRIGIDO:
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorCadastrar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("editar")) {
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                Extintor extintor = (Extintor) extintorDAO.carregar(idExtintor);
                List<Object> listaFiliais = filialDAO.listar();

                request.setAttribute("extintor", extintor);
                request.setAttribute("listaFiliais", listaFiliais);

                // CORRIGIDO (assumindo que extintorEditar.jsp também está na pasta):
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorEditar.jsp");
                rd.forward(request, response);

            } else if (acao.equals("excluir")) {
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                extintorDAO.excluir(idExtintor);
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar"); // Este redirect está correto
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no ExtintorServlet (GET): ", ex);
            throw new ServletException("Erro ao processar a requisição GET: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        String acao = request.getParameter("acao");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();

            String numeroControle = request.getParameter("numeroControle");
            String tipo = request.getParameter("tipo");
            String localizacao = request.getParameter("localizacao");
            int idFilial = Integer.parseInt(request.getParameter("idFilial"));

            java.util.Date dataRecarga = null;
            java.util.Date dataValidade = null;

            try {
                if (request.getParameter("dataRecarga") != null && !request.getParameter("dataRecarga").isEmpty()) {
                    dataRecarga = sdf.parse(request.getParameter("dataRecarga"));
                }
                if (request.getParameter("dataValidade") != null && !request.getParameter("dataValidade").isEmpty()) {
                    dataValidade = sdf.parse(request.getParameter("dataValidade"));
                }
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Erro ao converter datas", e);
            }

            Extintor extintor = new Extintor();
            extintor.setNumeroControle(numeroControle);
            extintor.setTipo(tipo);
            extintor.setLocalizacao(localizacao);
            extintor.setIdFilial(idFilial);
            extintor.setDataRecarga(dataRecarga);
            extintor.setDataValidade(dataValidade);

            if ("salvar".equals(acao)) {
                extintorDAO.cadastrar(extintor);
            } else if ("atualizar".equals(acao)) {
                extintor.setIdExtintor(Integer.parseInt(request.getParameter("idExtintor")));
                extintorDAO.alterar(extintor);
            }

            response.sendRedirect("ExtintorServlet?acao=listar");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro no ExtintorServlet (POST): ", ex);
            throw new ServletException("Erro ao processar a requisição POST: " + ex.getMessage(), ex);
        }
    }
}
