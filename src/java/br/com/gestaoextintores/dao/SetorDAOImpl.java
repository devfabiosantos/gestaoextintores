package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Setor;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SetorDAOImpl {

    private static final Logger LOGGER = Logger.getLogger(SetorDAOImpl.class.getName());

    public SetorDAOImpl() {}

    public boolean cadastrar(Setor setor) {
        String sql = "INSERT INTO setor (nome, id_filial) VALUES (?, ?)";
        Connection conn = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, setor.getNome());
                stmt.setInt(2, setor.getIdFilial());
                stmt.executeUpdate();
            }
            conn.commit();
            LOGGER.log(Level.INFO, "Setor '{0}' cadastrado com sucesso para filial ID {1}.", new Object[]{setor.getNome(), setor.getIdFilial()});
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar setor!", ex);
            if (conn != null) {
                try { conn.rollback(); 
                } catch (SQLException rbEx) { 
                    LOGGER.log(Level.SEVERE, "Erro rollback!", rbEx); 
                } 
            }

            return false;
        } finally {
             if (conn != null) { 
                 try { ConnectionFactory.closeConnection(conn); 
                 } catch (Exception closeEx) { 
                     LOGGER.log(Level.SEVERE, "Erro fechar conexão!", closeEx); 
                 } 
             }
        }
    }

    public List<Setor> listar(Usuario usuarioLogado) {
        List<Setor> resultado = new ArrayList<>();
        String sql = "SELECT s.*, f.nome as nome_filial " +
                     "FROM setor s JOIN filial f ON s.id_filial = f.id_filial";
        Integer idFilialParaFiltrar = null;

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " WHERE s.id_filial = ?";
            idFilialParaFiltrar = usuarioLogado.getIdFilial();
        }
        sql += " ORDER BY f.nome, s.nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (idFilialParaFiltrar != null) {
                stmt.setInt(1, idFilialParaFiltrar);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(popularSetorComFilial(rs));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar setores!", ex);
        }
        return resultado;
    }

    public Setor carregar(int idSetor, Usuario usuarioLogado) {
        Setor setor = null;
        String sql = "SELECT s.*, f.nome as nome_filial " +
                     "FROM setor s JOIN filial f ON s.id_filial = f.id_filial " +
                     "WHERE s.id_setor = ?";
        Integer idFilialDoUsuario = null;

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND s.id_filial = ?";
            idFilialDoUsuario = usuarioLogado.getIdFilial();
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSetor);
            if (idFilialDoUsuario != null) {
                stmt.setInt(2, idFilialDoUsuario);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    setor = popularSetorComFilial(rs);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar setor!", ex);
        }
        return setor;
    }

    public Boolean alterar(Setor setor, Usuario usuarioLogado) {
        String sql = "UPDATE setor SET nome = ? WHERE id_setor = ?";
        Integer idFilialDoUsuario = null;

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
            idFilialDoUsuario = usuarioLogado.getIdFilial();
        }

        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, setor.getNome());
                stmt.setInt(2, setor.getIdSetor());
                if (idFilialDoUsuario != null) {
                    stmt.setInt(3, idFilialDoUsuario);
                }
                stmt.executeUpdate();
             }
            conn.commit();
            LOGGER.log(Level.INFO,"Setor ID {0} alterado com sucesso.", setor.getIdSetor());
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar setor!", ex);
             if (conn != null) { 
                 try { conn.rollback(); 
                 } catch (SQLException rbEx) { 
                     LOGGER.log(Level.SEVERE, "Erro rollback!", rbEx); 
                 } 
             }

            return false;
        } finally {
            if (conn != null) { 
                try { ConnectionFactory.closeConnection(conn); 
                } catch (Exception e) {
                } 
            }
        }
    }

    public Boolean excluir(int idSetor, Usuario usuarioLogado) {
        String sql = "DELETE FROM setor WHERE id_setor = ?";
         Integer idFilialDoUsuario = null;

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
            idFilialDoUsuario = usuarioLogado.getIdFilial();
        }

        Connection conn = null;
        try {
             conn = ConnectionFactory.getConnection();
             conn.setAutoCommit(false);
             conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
             
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idSetor);
                if (idFilialDoUsuario != null) {
                    stmt.setInt(2, idFilialDoUsuario);
                }
                stmt.executeUpdate();
            }
            conn.commit();
            LOGGER.log(Level.INFO,"Setor ID {0} excluído com sucesso.", idSetor);
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir setor!", ex);
             if (conn != null) { 
                 try { conn.rollback(); 
                 } catch (SQLException rbEx) { 
                     LOGGER.log(Level.SEVERE, "Erro rollback!", rbEx); 
                 } 
             }
            return false;
        } finally {
            if (conn != null) { 
                try { 
                    ConnectionFactory.closeConnection(conn); 
                } catch (Exception e) {
                } 
            }
        }
    }

    private Setor popularSetorComFilial(ResultSet rs) throws SQLException {
        Setor setor = new Setor();
        setor.setIdSetor(rs.getInt("id_setor"));
        setor.setNome(rs.getString("nome"));
        setor.setIdFilial(rs.getInt("id_filial"));

        if (hasColumn(rs, "nome_filial")) {
            Filial filial = new Filial();
            filial.setIdFilial(setor.getIdFilial());
            filial.setNome(rs.getString("nome_filial"));
            setor.setFilial(filial);
        }

        return setor;
    }
    
    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int columns = rsMetaData.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equalsIgnoreCase(rsMetaData.getColumnLabel(x))) {
                return true;
            }
        }
        return false;
    }
}