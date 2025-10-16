package br.com.gestaoextintores.controller;

import br.com.gestaoextintores.dao.ExtintorDAOImpl;
import br.com.gestaoextintores.dao.FilialDAOImpl;
import br.com.gestaoextintores.model.Extintor;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet para cadastrar extintores
 * @author Dev Fabio Santos
 */
@WebServlet(name = "ExtintorCadastrar", urlPatterns = {"/ExtintorCadastrar"})
public class ExtintorCadastrar extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ExtintorCadastrar.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Carrega lista de filiais para o formulário
            FilialDAOImpl filialDAO = new FilialDAOImpl();
            List<Object> listaFiliais = filialDAO.listar();
            request.setAttribute("listaFiliais", listaFiliais);

            request.getRequestDispatcher("/extintor/extintorCadastrar.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao preparar formulário de cadastro", ex);
            request.setAttribute("mensagem", "Erro ao preparar formulário: " + ex.getMessage());
            request.getRequestDispatcher("/extintor/mensagem.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // Recupera os parâmetros do formulário
            String numeroControle = request.getParameter("numeroControle");
            String tipo = request.getParameter("tipo");

            Date dataRecarga = DATE_FORMAT.parse(request.getParameter("dataRecarga"));
            Date dataValidade = DATE_FORMAT.parse(request.getParameter("dataValidade"));
            String localizacao = request.getParameter("localizacao");
            int idFilial = Integer.parseInt(request.getParameter("idFilial"));

            // Cria o objeto Extintor
            Extintor extintor = new Extintor();
            extintor.setNumeroControle(numeroControle);
            extintor.setTipo(tipo);
            extintor.setDataRecarga(dataRecarga);
            extintor.setDataValidade(dataValidade);
            extintor.setLocalizacao(localizacao);
            extintor.setIdFilial(idFilial);

            // Salva no banco
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();

            if (extintorDAO.cadastrar(extintor)) {
                request.getSession().setAttribute("mensagem", "Extintor cadastrado com sucesso!");
                response.sendRedirect(request.getContextPath() + "/ExtintorListar");
            } else {
                FilialDAOImpl filialDAO = new FilialDAOImpl();
                List<Object> listaFiliais = filialDAO.listar();
                request.setAttribute("listaFiliais", listaFiliais);

                request.setAttribute("mensagem", "Erro ao cadastrar extintor!");
                request.setAttribute("extintor", extintor);
                request.getRequestDispatcher("/extintor/extintorCadastrar.jsp").forward(request, response);
            }

        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao converter datas", ex);
            request.setAttribute("mensagem", "Erro ao converter datas: " + ex.getMessage());
            request.getRequestDispatcher("/extintor/extintorCadastrar.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar extintor", ex);
            request.setAttribute("mensagem", "Erro ao cadastrar extintor: " + ex.getMessage());
            request.getRequestDispatcher("/extintor/extintorCadastrar.jsp").forward(request, response);
        }
    }
}
