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
 * Servlet para editar extintores
 * @author Dev Fabio Santos
 */
@WebServlet(name = "ExtintorEditar", urlPatterns = {"/ExtintorEditar"})
public class ExtintorEditar extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ExtintorEditar.class.getName());
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
            String numeroControle = request.getParameter("numeroControle");
            String tipo = request.getParameter("tipo");

            Date dataRecarga = DATE_FORMAT.parse(request.getParameter("dataRecarga"));
            Date dataValidade = DATE_FORMAT.parse(request.getParameter("dataValidade"));
            String localizacao = request.getParameter("localizacao");
            int idFilial = Integer.parseInt(request.getParameter("idFilial"));

            // Cria o objeto Extintor atualizado
            Extintor extintor = new Extintor();
            extintor.setIdExtintor(idExtintor);
            extintor.setNumeroControle(numeroControle);
            extintor.setTipo(tipo);
            extintor.setDataRecarga(dataRecarga);
            extintor.setDataValidade(dataValidade);
            extintor.setLocalizacao(localizacao);
            extintor.setIdFilial(idFilial);

            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();

            if (extintorDAO.alterar(extintor)) {
                request.getSession().setAttribute("mensagem", "Extintor atualizado com sucesso!");
                response.sendRedirect(request.getContextPath() + "/ExtintorListar");
            } else {
                FilialDAOImpl filialDAO = new FilialDAOImpl();
                List<Object> listaFiliais = filialDAO.listar();
                request.setAttribute("listaFiliais", listaFiliais);

                request.setAttribute("mensagem", "Erro ao atualizar extintor!");
                request.setAttribute("extintor", extintor);
                request.getRequestDispatcher("/extintor/extintorEditar.jsp").forward(request, response);
            }

        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao converter datas", ex);
            request.setAttribute("mensagem", "Erro ao converter datas: " + ex.getMessage());
            request.getRequestDispatcher("/extintor/extintorEditar.jsp").forward(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao editar extintor", ex);
            request.setAttribute("mensagem", "Erro ao editar extintor: " + ex.getMessage());
            request.getRequestDispatcher("/extintor/extintorEditar.jsp").forward(request, response);
        }
    }
}
