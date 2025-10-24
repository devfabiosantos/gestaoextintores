/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.dao;

/**
 *
 * @author Dev Fabio Santos
 */
import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemessaDAO {

    private static final Logger LOGGER = Logger.getLogger(RemessaDAO.class.getName());

    public RemessaDAO() {}

    public int criarRemessa(Remessa remessa) {
        String sql = "INSERT INTO remessa (id_usuario_tecnico, id_filial, status_remessa, data_criacao) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        int idGerado = -1;
        Connection conn = null; 

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, remessa.getIdUsuarioTecnico());
                stmt.setInt(2, remessa.getIdFilial());
                stmt.setString(3, remessa.getStatusRemessa());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idGerado = generatedKeys.getInt(1);
                        remessa.setIdRemessa(idGerado);
                    } else {
                         throw new SQLException("Falha ao obter ID gerado para remessa.");
                    }
                }
            } 
            conn.commit(); 
            LOGGER.log(Level.INFO, "Remessa criada com sucesso (ID: {0})", idGerado);
            return idGerado;

        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao criar remessa!", ex);
            if (conn != null) { try { conn.rollback(); } catch (SQLException rbEx) { LOGGER.log(Level.SEVERE, "Erro crítico ao tentar rollback!", rbEx); } }
            return -1;
        } finally {
            if (conn != null) { try { ConnectionFactory.closeConnection(conn); } catch (Exception closeEx) { LOGGER.log(Level.SEVERE, "Erro ao fechar conexão!", closeEx); } }
        }
    }

    public List<Remessa> listar(Usuario usuarioLogado) {
        List<Remessa> resultado = new ArrayList<>();
        String sql = "SELECT * FROM remessa";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " WHERE id_filial = ?";
        }
        sql += " ORDER BY data_criacao DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(1, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(popularRemessa(rs));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar remessas!", ex);
        }
        return resultado;
    }

    public Remessa carregar(int idRemessa, Usuario usuarioLogado) {
        Remessa remessa = null;
        String sql = "SELECT r.*, " +
                     "       t.nome AS nome_tecnico, " +
                     "       a.nome AS nome_admin, " +
                     "       f.nome AS nome_filial " +
                     "FROM remessa r " +
                     "JOIN usuario t ON r.id_usuario_tecnico = t.id_usuario " +
                     "JOIN filial f ON r.id_filial = f.id_filial " +
                     "LEFT JOIN usuario a ON r.id_usuario_admin = a.id_usuario " +
                     "WHERE r.id_remessa = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND r.id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRemessa);
            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    remessa = popularRemessa(rs);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar remessa com detalhes!", ex);
        }
        return remessa;
    }
  
    public boolean aprovarParaRecolhimento(int idRemessa, int idUsuarioAdmin) {
        String sql = "UPDATE remessa SET status_remessa = ?, id_usuario_admin = ?, data_aprovacao = CURRENT_TIMESTAMP WHERE id_remessa = ?";
        String NOVO_STATUS = "Aprovado p/ Recolhimento"; 
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); stmt.setString(1, NOVO_STATUS); stmt.setInt(2, idUsuarioAdmin); stmt.setInt(3, idRemessa);
            int affectedRows = stmt.executeUpdate(); conn.commit();
            if (affectedRows > 0) {
                return true; 
            } 
            else {
                return false; 
            }
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Erro ao aprovar remessa (ID: " + idRemessa + ")!", ex); return false; }
    }
    public boolean confirmarRecolhimento(int idRemessa) {
        String sql = "UPDATE remessa SET status_remessa = ? WHERE id_remessa = ?";
        String NOVO_STATUS = "Em Recarga";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); stmt.setString(1, NOVO_STATUS); stmt.setInt(2, idRemessa);
            int affectedRows = stmt.executeUpdate(); conn.commit();
            if (affectedRows > 0) {
                return true; 
            } 
            else {
                return false; 
            }
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Erro ao confirmar recolhimento (ID: " + idRemessa + ")!", ex); return false; }
    }
     public boolean concluirRemessa(int idRemessa) {
        String sql = "UPDATE remessa SET status_remessa = ? WHERE id_remessa = ?";
        String NOVO_STATUS = "Concluído";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false); stmt.setString(1, NOVO_STATUS); stmt.setInt(2, idRemessa);
            int affectedRows = stmt.executeUpdate(); conn.commit();
            if (affectedRows > 0) {
                return true; 
            } 
            else {
                return false; 
            }
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Erro ao concluir remessa (ID: " + idRemessa + ")!", ex); return false; }
    }

    private Remessa popularRemessa(ResultSet rs) throws SQLException {
        Remessa remessa = new Remessa();
        remessa.setIdRemessa(rs.getInt("id_remessa"));
        remessa.setIdUsuarioTecnico(rs.getInt("id_usuario_tecnico"));
        remessa.setIdFilial(rs.getInt("id_filial"));
        remessa.setDataCriacao(rs.getTimestamp("data_criacao"));
        remessa.setStatusRemessa(rs.getString("status_remessa"));
        remessa.setIdUsuarioAdmin(rs.getObject("id_usuario_admin") != null ? rs.getInt("id_usuario_admin") : null);
        remessa.setDataAprovacao(rs.getTimestamp("data_aprovacao"));

        if (hasColumn(rs, "nome_filial")) {
            Filial filial = new Filial();
            filial.setIdFilial(remessa.getIdFilial());
            filial.setNome(rs.getString("nome_filial"));
            remessa.setFilial(filial);
        }

         if (hasColumn(rs, "nome_tecnico")) {
            Usuario tecnico = new Usuario();
            tecnico.setIdUsuario(remessa.getIdUsuarioTecnico());
            tecnico.setNome(rs.getString("nome_tecnico"));
            remessa.setTecnico(tecnico);
         }

        if (remessa.getIdUsuarioAdmin() != null && hasColumn(rs, "nome_admin")) {
             Usuario admin = new Usuario();
             admin.setIdUsuario(remessa.getIdUsuarioAdmin());
             admin.setNome(rs.getString("nome_admin"));
             remessa.setAdmin(admin);
        }

        return remessa;
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