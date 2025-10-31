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
    private boolean existeNomeSetorNaFilial(String nome, int idFilial, Integer idSetorAtual) {
        if (nome == null || nome.trim().isEmpty()) { 
            return false;
        }
        String sql = "SELECT COUNT(id_setor) FROM setor WHERE UPPER(nome) = UPPER(?) AND id_filial = ? ";
        if (idSetorAtual != null) { 
            sql += " AND id_setor != ?"; 
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, nome);
            stmt.setInt(paramIndex++, idFilial);
            if (idSetorAtual != null) { 
                stmt.setInt(paramIndex++, idSetorAtual); 
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.log(Level.WARNING, "Nome de setor duplicado '{0}' na filial ID {1}.", new Object[]{nome, idFilial});
                    return true;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao verificar duplicidade nome setor!", ex);
            return true;
        }
        return false;
    }

    public boolean cadastrar(Setor setor) {
        if (existeNomeSetorNaFilial(setor.getNome(), setor.getIdFilial(), null)) {
            return false;
        }
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
        String sql = "SELECT s.*, f.nome as nome_filial FROM setor s JOIN filial f ON s.id_filial = f.id_filial";
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
        String sql = "SELECT s.*, f.nome as nome_filial FROM setor s JOIN filial f ON s.id_filial = f.id_filial WHERE s.id_setor = ?";
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
        Integer idFilialAtual = null;
        String sqlBuscaFilial = "SELECT id_filial FROM setor WHERE id_setor = ?";
         if ("Técnico".equals(usuarioLogado.getPerfil())) { 
             sqlBuscaFilial += " AND id_filial = ?"; 
         }
        try (Connection connCheck = ConnectionFactory.getConnection();
             PreparedStatement stmtCheck = connCheck.prepareStatement(sqlBuscaFilial)) {
            stmtCheck.setInt(1, setor.getIdSetor());
             if ("Técnico".equals(usuarioLogado.getPerfil())) { 
                 stmtCheck.setInt(2, usuarioLogado.getIdFilial()); 
             }
            try (ResultSet rsCheck = stmtCheck.executeQuery()) {
                if (rsCheck.next()) {
                    idFilialAtual = rsCheck.getInt("id_filial");
                } else {
                     LOGGER.log(Level.WARNING, "Setor ID {0} não encontrado ou sem permissão para buscar filial.", setor.getIdSetor());
                    return false;
                }
            }
        } catch (Exception exCheck) {
             LOGGER.log(Level.SEVERE, "Erro ao buscar id_filial para verificação de duplicidade!", exCheck);
             return false;
        }
        if (idFilialAtual != null && existeNomeSetorNaFilial(setor.getNome(), idFilialAtual, setor.getIdSetor())) {
            return false;
        }

        String sqlUpdate = "UPDATE setor SET nome = ? WHERE id_setor = ?";
        Integer idFilialDoUsuario = null;
        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sqlUpdate += " AND id_filial = ?";
            idFilialDoUsuario = usuarioLogado.getIdFilial();
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
             try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
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
            sql += " AND id_filial = ?"; idFilialDoUsuario = usuarioLogado.getIdFilial(); 
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
                } 
            } return false;
        } finally { if (conn != null) { 
            try { ConnectionFactory.closeConnection(conn); 
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