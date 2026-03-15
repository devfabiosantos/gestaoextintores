/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Remessa;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemessaDAO {

    private static final Logger LOGGER = Logger.getLogger(RemessaDAO.class.getName());
    private static final String REMESSA_COLUMNS = "r.id_remessa, r.id_usuario_tecnico, r.id_filial, "
            + "r.data_criacao, r.status_remessa, r.id_usuario_admin, r.data_aprovacao, "
            + "r.pdf_nome_arquivo, r.pdf_mime_type, r.pdf_gerado_em";

    public RemessaDAO() {}

    public int criarRemessa(Remessa remessa) {
        String sql = "INSERT INTO remessa (id_usuario_tecnico, id_filial, status_remessa, data_criacao) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        int idGerado = -1;
        Connection conn = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

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
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    LOGGER.log(Level.SEVERE, "Erro rollback!", rbEx);
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    ConnectionFactory.closeConnection(conn);
                } catch (Exception closeEx) {
                    LOGGER.log(Level.SEVERE, "Erro fechar conexão!", closeEx);
                }
            }
        }
    }

    public List<Remessa> listar(Usuario usuarioLogado, Integer idFilialFiltro) {
        List<Remessa> resultado = new ArrayList<>();
        String sql = "SELECT " + REMESSA_COLUMNS + " FROM remessa r";
        Integer idFilialParaFiltrar = null;
        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " WHERE r.id_filial = ?";
            idFilialParaFiltrar = usuarioLogado.getIdFilial();
        } else if ("Admin".equals(usuarioLogado.getPerfil()) && idFilialFiltro != null) {
            sql += " WHERE r.id_filial = ?";
            idFilialParaFiltrar = idFilialFiltro;
        }
        sql += " ORDER BY r.data_criacao DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (idFilialParaFiltrar != null) {
                stmt.setInt(1, idFilialParaFiltrar);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(popularRemessa(rs));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar remessas com filtro!", ex);
        }
        return resultado;
    }

    public Remessa carregar(int idRemessa, Usuario usuarioLogado) {
        Remessa remessa = null;
        String sql = "SELECT " + REMESSA_COLUMNS + ", t.nome AS nome_tecnico, a.nome AS nome_admin, f.nome AS nome_filial "
                + "FROM remessa r "
                + "LEFT JOIN usuario t ON r.id_usuario_tecnico = t.id_usuario "
                + "LEFT JOIN filial f ON r.id_filial = f.id_filial "
                + "LEFT JOIN usuario a ON r.id_usuario_admin = a.id_usuario "
                + "WHERE r.id_remessa = ?";
        boolean ehTecnico = "Técnico".equals(usuarioLogado.getPerfil());
        if (ehTecnico) {
            sql += " AND r.id_filial = ?";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ConnectionFactory.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idRemessa);
            if (ehTecnico) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                remessa = popularRemessa(rs);
            } else {
                LOGGER.log(Level.WARNING, "Remessa ID {0} não encontrada ou acesso negado.", idRemessa);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro SQL ao carregar remessa (ID: " + idRemessa + ")!", ex);
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Erro INESPERADO ao carregar remessa (ID: " + idRemessa + ")!", t);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
            }
            if (conn != null) {
                try {
                    ConnectionFactory.closeConnection(conn);
                } catch (Exception e) {
                }
            }
        }
        return remessa;
    }

    public boolean salvarPdf(int idRemessa, String nomeArquivo, String mimeType, byte[] conteudo) {
        String sql = "UPDATE remessa "
                + "SET pdf_nome_arquivo = ?, pdf_mime_type = ?, pdf_conteudo = ?, pdf_gerado_em = CURRENT_TIMESTAMP "
                + "WHERE id_remessa = ?";
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nomeArquivo);
                stmt.setString(2, mimeType);
                stmt.setBytes(3, conteudo);
                stmt.setInt(4, idRemessa);
                int affectedRows = stmt.executeUpdate();
                conn.commit();
                LOGGER.log(Level.INFO, "PDF salvo para remessa ID {0}. Linhas afetadas: {1}. Bytes: {2}",
                        new Object[]{idRemessa, affectedRows, conteudo != null ? conteudo.length : 0});
                return affectedRows > 0;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao salvar PDF da remessa (ID: " + idRemessa + ")!", ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
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

    public Remessa carregarPdf(int idRemessa, Usuario usuarioLogado) {
        Remessa remessa = null;
        String sql = "SELECT " + REMESSA_COLUMNS + ", r.pdf_conteudo "
                + "FROM remessa r "
                + "WHERE r.id_remessa = ?";
        boolean ehTecnico = "Técnico".equals(usuarioLogado.getPerfil());
        if (ehTecnico) {
            sql += " AND r.id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRemessa);
            if (ehTecnico) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    remessa = popularRemessa(rs);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar PDF da remessa (ID: " + idRemessa + ")!", ex);
        }
        return remessa;
    }

    // ESSE BLOCO DE CÓDIGO PASSOU A SER UTILIZADO A PARTIR DE 31/10/2025 - 19:46
    public boolean aprovarParaRecolhimento(int idRemessa, int idUsuarioAdmin) {
        String sql = "UPDATE remessa SET status_remessa = ?, id_usuario_admin = ?, data_aprovacao = CURRENT_TIMESTAMP WHERE id_remessa = ?";
        String novoStatus = "Aprovado p/ Recolhimento";
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, novoStatus);
                stmt.setInt(2, idUsuarioAdmin);
                stmt.setInt(3, idRemessa);
                int affectedRows = stmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao aprovar remessa (ID: " + idRemessa + ")!", ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
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

    public boolean confirmarRecolhimento(int idRemessa) {
        String sql = "UPDATE remessa SET status_remessa = ? WHERE id_remessa = ?";
        String novoStatus = "Em Recarga";
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, novoStatus);
                stmt.setInt(2, idRemessa);
                int affectedRows = stmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao confirmar recolhimento (ID: " + idRemessa + ")!", ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
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

    public boolean concluirRemessa(int idRemessa) {
        String sql = "UPDATE remessa SET status_remessa = ? WHERE id_remessa = ?";
        String novoStatus = "Concluído";
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, novoStatus);
                stmt.setInt(2, idRemessa);
                int affectedRows = stmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao concluir remessa (ID: " + idRemessa + ")!", ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
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

    private Remessa popularRemessa(ResultSet rs) throws SQLException {
        Remessa remessa = new Remessa();
        try {
            remessa.setIdRemessa(rs.getInt("id_remessa"));
            remessa.setIdUsuarioTecnico(rs.getInt("id_usuario_tecnico"));
            remessa.setIdFilial(rs.getInt("id_filial"));
            remessa.setDataCriacao(rs.getTimestamp("data_criacao"));
            remessa.setStatusRemessa(rs.getString("status_remessa"));
            remessa.setIdUsuarioAdmin(rs.getObject("id_usuario_admin") != null ? rs.getInt("id_usuario_admin") : null);
            remessa.setDataAprovacao(rs.getTimestamp("data_aprovacao"));
            if (hasColumn(rs, "pdf_nome_arquivo")) {
                remessa.setPdfNomeArquivo(rs.getString("pdf_nome_arquivo"));
            }
            if (hasColumn(rs, "pdf_mime_type")) {
                remessa.setPdfMimeType(rs.getString("pdf_mime_type"));
            }
            if (hasColumn(rs, "pdf_gerado_em")) {
                remessa.setPdfGeradoEm(rs.getTimestamp("pdf_gerado_em"));
            }
            if (hasColumn(rs, "pdf_conteudo")) {
                remessa.setPdfConteudo(rs.getBytes("pdf_conteudo"));
            }

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

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro SQL DENTRO de popularRemessa!", e);
            throw e;
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "Erro INESPERADO DENTRO de popularRemessa!", t);
            throw new SQLException("Erro inesperado", t);
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
