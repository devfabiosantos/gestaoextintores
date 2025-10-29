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
import java.text.ParseException; // Import necessário
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
            response.sendRedirect(request.getContextPath() + "/LoginServlet"); // Adicionado Redirect
            return; 
        }
        
        String acao = request.getParameter("acao");

        try {
            ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
            SetorDAOImpl setorDAO = new SetorDAOImpl(); 
            StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();
            FilialDAOImpl filialDAO = new FilialDAOImpl();

            // Ação: LISTAR
            if (acao == null || "listar".equals(acao)) {
                String idFilialFiltroStr = request.getParameter("idFilialFiltro");
                Integer idFilialFiltro = null;
                if ("Admin".equals(usuarioLogado.getPerfil()) && idFilialFiltroStr != null && !idFilialFiltroStr.isEmpty()) {
                    try { idFilialFiltro = Integer.parseInt(idFilialFiltroStr); } catch (NumberFormatException e) { /* Ignora */ }
                }
                List<Extintor> listaExtintores = extintorDAO.listar(usuarioLogado, idFilialFiltro); 
                List<Filial> listaTodasFiliais = null;
                if ("Admin".equals(usuarioLogado.getPerfil())) { listaTodasFiliais = filialDAO.listar(usuarioLogado); }
                request.setAttribute("listaExtintores", listaExtintores);
                if (listaTodasFiliais != null) { request.setAttribute("listaTodasFiliais", listaTodasFiliais); }
                request.setAttribute("idFilialSelecionada", idFilialFiltro);
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorListar.jsp");
                rd.forward(request, response);
            
            // Ação: NOVO
            } else if ("novo".equals(acao)) {
                List<Setor> listaSetores = setorDAO.listar(usuarioLogado);
                List<StatusExtintor> listaStatus = statusDAO.listar(); 
                request.setAttribute("listaSetores", listaSetores);
                request.setAttribute("listaStatus", listaStatus);
                RequestDispatcher rd = request.getRequestDispatcher("/extintor/extintorCadastrar.jsp");
                rd.forward(request, response);

            // Ação: EDITAR
            } else if ("editar".equals(acao)) {
                 int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                 Extintor extintor = extintorDAO.carregar(idExtintor, usuarioLogado);
                 if (extintor == null) { 
                     // Adicionado tratamento de erro se não encontrar
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

            // Ação: EXCLUIR
            } else if ("excluir".equals(acao)) {
                 int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                 // Segurança extra no filtro é melhor, mas podemos adicionar aqui também se Admin não puder excluir
                 // if (!"Admin".equals(usuarioLogado.getPerfil())) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
                 extintorDAO.excluir(idExtintor, usuarioLogado); // DAO aplica segurança de filial
                 response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
            } else {
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação GET inválida: " + acao);
            }

        } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Erro GERAL no doGet do ExtintorServlet", ex);
             throw new ServletException("Erro GET Extintor: " + ex.getMessage(), ex);
        }
    }

    // --- doPost ATUALIZADO ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //request.setCharacterEncoding("UTF-8");
        HttpSession sessao = request.getSession(false);
        Usuario usuarioLogado = (sessao != null) ? (Usuario) sessao.getAttribute("usuarioLogado") : null;
        if (usuarioLogado == null) { response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado."); return; }

        String acao = request.getParameter("acao");
        String jspDestino = null; // Para onde encaminhar em caso de erro
        boolean sucessoOperacao = false; // Flag

        // Instanciar DAOs fora do try principal para usar no catch de ParseException
        ExtintorDAOImpl extintorDAO = new ExtintorDAOImpl();
        SetorDAOImpl setorDAO = new SetorDAOImpl();
        StatusExtintorDAOImpl statusDAO = new StatusExtintorDAOImpl();
        
        Extintor extintor = new Extintor(); // Criar o objeto antes para usar no catch

        try {
            // Coleta de Dados
            String tipoEquipamento = request.getParameter("tipoEquipamento");
            String numeroControle = request.getParameter("numeroControle");
            String classeExtintora = request.getParameter("classeExtintora");
            String cargaNominal = request.getParameter("cargaNominal");
            String referenciaLocalizacao = request.getParameter("referenciaLocalizacao");
            String observacao = request.getParameter("observacao");
            int idSetor = Integer.parseInt(request.getParameter("idSetor")); // Pode lançar NumberFormatException
            int idStatus = Integer.parseInt(request.getParameter("idStatus")); // Pode lançar NumberFormatException
            
            Date dataRecarga = null; 
            Date dataValidade = null;
            
            // --- BLOCO TRY-CATCH DE DATAS CORRIGIDO ---
            try {
                // Colocar o parse DENTRO do try
                String dataRecargaStr = request.getParameter("dataRecarga");
                String dataValidadeStr = request.getParameter("dataValidade");
                if (dataRecargaStr != null && !dataRecargaStr.isEmpty()) {
                    dataRecarga = DATE_FORMAT.parse(dataRecargaStr); // << CORRIGIDO
                }
                if (dataValidadeStr != null && !dataValidadeStr.isEmpty()) {
                    dataValidade = DATE_FORMAT.parse(dataValidadeStr); // << CORRIGIDO
                }
            } catch (ParseException e) { // << CATCH VÁLIDO AGORA
                 LOGGER.log(Level.WARNING, "Erro ao converter datas", e);
                 request.setAttribute("mensagemErro", "Formato de data inválido.");
                 jspDestino = "/extintor/" + ("salvar".equals(acao) ? "extintorCadastrar.jsp" : "extintorEditar.jsp");
                 // Recarrega dados e reenvia para o formulário
                 request.setAttribute("listaSetores", setorDAO.listar(usuarioLogado));
                 request.setAttribute("listaStatus", statusDAO.listar());
                 // Preenche o objeto com dados já digitados para repopular o form
                 extintor.setTipoEquipamento(tipoEquipamento); extintor.setNumeroControle(numeroControle);
                 extintor.setClasseExtintora(classeExtintora); extintor.setCargaNominal(cargaNominal);
                 extintor.setReferenciaLocalizacao(referenciaLocalizacao); extintor.setObservacao(observacao);
                 extintor.setIdSetor(idSetor); extintor.setIdStatus(idStatus);
                 if ("atualizar".equals(acao)) { extintor.setIdExtintor(Integer.parseInt(request.getParameter("idExtintor"))); }
                 request.setAttribute("extintor", extintor); 
                 
                 RequestDispatcher rd = request.getRequestDispatcher(jspDestino);
                 rd.forward(request, response);
                 return; // Interrompe
            }
            // --- FIM DO BLOCO DE DATAS ---

            // Preenche o objeto Extintor (movido para depois do try-catch de datas)
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

            // Ação: SALVAR
            if ("salvar".equals(acao)) {
                jspDestino = "/extintor/extintorCadastrar.jsp";
                // --- VERIFICA RETORNO DO DAO ---
                sucessoOperacao = extintorDAO.cadastrar(extintor);
                if (sucessoOperacao) {
                    sessao.setAttribute("mensagemSucesso", "Extintor cadastrado com sucesso!");
                } else {
                    // Falha (provavelmente duplicidade)
                    request.setAttribute("mensagemErro", "Falha ao cadastrar. Verifique se o Número de Controle já existe nesta filial.");
                    request.setAttribute("extintor", extintor); // Reenvia dados digitados
                    request.setAttribute("listaSetores", setorDAO.listar(usuarioLogado));
                    request.setAttribute("listaStatus", statusDAO.listar());
                }
                // -----------------------------

            // Ação: ATUALIZAR
            } else if ("atualizar".equals(acao)) {
                jspDestino = "/extintor/extintorEditar.jsp";
                int idExtintor = Integer.parseInt(request.getParameter("idExtintor"));
                extintor.setIdExtintor(idExtintor);
                // --- VERIFICA RETORNO DO DAO ---
                sucessoOperacao = extintorDAO.alterar(extintor, usuarioLogado);
                if (sucessoOperacao) {
                    sessao.setAttribute("mensagemSucesso", "Extintor alterado com sucesso!");
                } else {
                    // Falha (provavelmente duplicidade)
                    request.setAttribute("mensagemErro", "Falha ao alterar. Verifique se o Número de Controle já existe nesta filial.");
                    request.setAttribute("extintor", extintor); // Reenvia dados digitados
                    request.setAttribute("listaSetores", setorDAO.listar(usuarioLogado));
                    request.setAttribute("listaStatus", statusDAO.listar());
                }
                // -----------------------------
                
            } else {
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ação POST inválida."); return;
            }
            
            // --- Decisão Final: Redirecionar ou Encaminhar ---
            if (sucessoOperacao) {
                response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar");
            } else {
                // Reenvia para o formulário com a mensagem de erro e dados preenchidos
                RequestDispatcher rd = request.getRequestDispatcher(jspDestino);
                rd.forward(request, response);
            }

        } catch (NumberFormatException ex) { // Erro ao converter ID Setor/Status/Extintor
            LOGGER.log(Level.WARNING, "Erro ao converter IDs no doPost", ex);
            sessao.setAttribute("mensagemErro", "ID inválido fornecido para Setor, Status ou Extintor.");
            response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar"); // Volta pra lista
        } catch (Exception ex) { // Erro geral não previsto
            LOGGER.log(Level.SEVERE, "Erro GERAL no doPost do ExtintorServlet", ex);
            sessao.setAttribute("mensagemErro", "Erro interno ao processar: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/ExtintorServlet?acao=listar"); // Volta pra lista
        }
    }
    // --- FIM DO doPost ATUALIZADO ---

    @Override
    public String getServletInfo() {
        return "Controlador para o CRUD de Extintores";
    }
}