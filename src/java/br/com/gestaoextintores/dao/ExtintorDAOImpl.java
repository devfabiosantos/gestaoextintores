package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.model.Setor;
import br.com.gestaoextintores.model.StatusExtintor;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtintorDAOImpl {

    private static final Logger LOGGER = Logger.getLogger(ExtintorDAOImpl.class.getName());

    public ExtintorDAOImpl() {}

    private boolean existeNumeroControleNaFilial(String numeroControle, int idSetor, Integer idExtintorAtual) {
        if (numeroControle == null || numeroControle.trim().isEmpty()) { 
            return false; 
        }
        String sql = "SELECT COUNT(e.id_extintor) FROM extintor e JOIN setor s ON e.id_setor = s.id_setor " +
                     "WHERE UPPER(e.numero_controle) = UPPER(?) AND s.id_filial = (SELECT id_filial FROM setor WHERE id_setor = ?) ";
        if (idExtintorAtual != null) { 
            sql += " AND e.id_extintor != ?"; 
        }
        try (Connection conn = ConnectionFactory.getConnection(); 
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1; 
            stmt.setString(paramIndex++, numeroControle); 
            stmt.setInt(paramIndex++, idSetor);
            if (idExtintorAtual != null) { 
                stmt.setInt(paramIndex++, idExtintorAtual); 
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    LOGGER.log(Level.WARNING, "Numero controle duplicado '{0}' na filial do setor {1}.", new Object[]{numeroControle, idSetor}); // Mantido Warning
                    return true;
                }
            }
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao verificar duplicidade!", ex); 
            return true; 
        }
        return false;
    }

    public boolean cadastrar(Extintor extintor) {
        if (existeNumeroControleNaFilial(extintor.getNumeroControle(), extintor.getIdSetor(), null)) {
            return false; 
        }
        String sql = "INSERT INTO extintor (tipo_equipamento, numero_controle, classe_extintora, carga_nominal, " +
                     "referencia_localizacao, data_recarga, data_validade, observacao, id_setor, id_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection(); 
            conn.setAutoCommit(false); 
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, extintor.getTipoEquipamento()); 
                stmt.setString(2, extintor.getNumeroControle()); 
                stmt.setString(3, extintor.getClasseExtintora());
                stmt.setString(4, extintor.getCargaNominal()); 
                stmt.setString(5, extintor.getReferenciaLocalizacao());
                stmt.setDate(6, extintor.getDataRecarga() != null ? new java.sql.Date(extintor.getDataRecarga().getTime()) : null);
                stmt.setDate(7, extintor.getDataValidade() != null ? new java.sql.Date(extintor.getDataValidade().getTime()) : null);
                stmt.setString(8, extintor.getObservacao()); 
                stmt.setInt(9, extintor.getIdSetor()); 
                stmt.setInt(10, extintor.getIdStatus());
                stmt.executeUpdate();
            }
            conn.commit();
            LOGGER.log(Level.INFO, "Extintor '{0}' cadastrado.", extintor.getNumeroControle());
            return true;
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar extintor!", ex); 
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

    public List<Extintor> listar(Usuario usuarioLogado, Integer idFilialFiltro) {
        List<Extintor> resultado = new ArrayList<>();
        String sql = "SELECT e.*, s.nome AS nome_setor, s.id_filial, st.nome AS nome_status " +
                     "FROM extintor e JOIN setor s ON e.id_setor = s.id_setor LEFT JOIN statusextintor st ON e.id_status = st.id_status ";
        Integer idFilialParaFiltrar = null;
        if ("Técnico".equals(usuarioLogado.getPerfil())) { 
            sql += " WHERE s.id_filial = ?"; 
            idFilialParaFiltrar = usuarioLogado.getIdFilial(); 
        }
        else if ("Admin".equals(usuarioLogado.getPerfil()) && idFilialFiltro != null) { 
            sql += " WHERE s.id_filial = ?"; 
            idFilialParaFiltrar = idFilialFiltro; 
        }
        sql += " ORDER BY e.id_extintor";
        try (Connection conn = ConnectionFactory.getConnection(); 
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (idFilialParaFiltrar != null) { 
                stmt.setInt(1, idFilialParaFiltrar); 
            }
            try (ResultSet rs = stmt.executeQuery()) { 
                while (rs.next()) { 
                    resultado.add(popularExtintor(rs)); 
                } 
            }
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao listar extintores!", ex); 
        }
        return resultado;
    }

    public Extintor carregar(int idExtintor, Usuario usuarioLogado) {
        Extintor extintor = null;
        String sql = "SELECT e.*, s.nome AS nome_setor, s.id_filial, st.nome AS nome_status " +
                     "FROM extintor e JOIN setor s ON e.id_setor = s.id_setor LEFT JOIN statusextintor st ON e.id_status = st.id_status " +
                     "WHERE e.id_extintor = ?";
        if ("Técnico".equals(usuarioLogado.getPerfil())) { 
            sql += " AND s.id_filial = ?"; }
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idExtintor);
            if ("Técnico".equals(usuarioLogado.getPerfil())) { 
                stmt.setInt(2, usuarioLogado.getIdFilial()); 
            }
            try (ResultSet rs = stmt.executeQuery()) { 
                if (rs.next()) { 
                    extintor = popularExtintor(rs); 
                } 
            }
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao carregar extintor!", ex); 
        }
        return extintor;
    }

    public Boolean alterar(Extintor extintor, Usuario usuarioLogado) {
         if (existeNumeroControleNaFilial(extintor.getNumeroControle(), extintor.getIdSetor(), extintor.getIdExtintor())) { 
             return false; 
         }
        String sql = "UPDATE extintor SET tipo_equipamento = ?, numero_controle = ?, classe_extintora = ?, carga_nominal = ?, " +
                     "referencia_localizacao = ?, data_recarga = ?, data_validade = ?, observacao = ?, id_setor = ?, id_status = ? " +
                     "WHERE id_extintor = ?";
        Integer idFilialDoUsuario = null;
        if ("Técnico".equals(usuarioLogado.getPerfil())) { 
            sql += " AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)"; 
            idFilialDoUsuario = usuarioLogado.getIdFilial(); 
        }
        Connection conn = null;
        try {
            conn = ConnectionFactory.getConnection(); 
            conn.setAutoCommit(false); 
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, extintor.getTipoEquipamento()); 
                stmt.setString(2, extintor.getNumeroControle()); 
                stmt.setString(3, extintor.getClasseExtintora());
                stmt.setString(4, extintor.getCargaNominal()); 
                stmt.setString(5, extintor.getReferenciaLocalizacao());
                stmt.setDate(6, extintor.getDataRecarga() != null ? new java.sql.Date(extintor.getDataRecarga().getTime()) : null);
                stmt.setDate(7, extintor.getDataValidade() != null ? new java.sql.Date(extintor.getDataValidade().getTime()) : null);
                stmt.setString(8, extintor.getObservacao()); 
                stmt.setInt(9, extintor.getIdSetor()); 
                stmt.setInt(10, extintor.getIdStatus());
                stmt.setInt(11, extintor.getIdExtintor());
                if (idFilialDoUsuario != null) { 
                    stmt.setInt(12, idFilialDoUsuario); }
                stmt.executeUpdate();
            }
            conn.commit();
            LOGGER.log(Level.INFO,"Extintor ID {0} alterado.", extintor.getIdExtintor());
            return true;
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao atualizar extintor!", ex); 
            if (conn != null) { 
                try { conn.rollback(); 
                } catch (SQLException rbEx) {
                } 
            } return false;
        } finally { 
            if (conn != null) { 
            try { ConnectionFactory.closeConnection(conn); 
            } catch (Exception e) {
            } 
            } 
        }
    }

    public Boolean excluir(int idExtintor, Usuario usuarioLogado) {
        String sql = "DELETE FROM extintor WHERE id_extintor = ?";
        Integer idFilialDoUsuario = null;
        if ("Técnico".equals(usuarioLogado.getPerfil())) { 
            sql += " AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)"; 
            idFilialDoUsuario = usuarioLogado.getIdFilial(); 
        }
        Connection conn = null;
        try {
             conn = ConnectionFactory.getConnection(); 
             conn.setAutoCommit(false); 
             conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idExtintor);
                if (idFilialDoUsuario != null) { 
                    stmt.setInt(2, idFilialDoUsuario); 
                }
                stmt.executeUpdate();
            }
            conn.commit();
            LOGGER.log(Level.INFO,"Extintor ID {0} excluído.", idExtintor);
            return true;
        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao excluir extintor!", ex); 
            if (conn != null) { 
                try { conn.rollback(); 
                } catch (SQLException rbEx) {
                } 
            } return false;
        } finally { 
            if (conn != null) { 
                try { ConnectionFactory.closeConnection(conn); 
                } catch (Exception e) {
                } 
            } 
        }
    }

     public boolean atualizarStatusVarios(List<Integer> idsExtintores, int idNovoStatus, Usuario usuarioLogado) {
        if (idsExtintores == null || idsExtintores.isEmpty()) { 
            return true; 
        }
        StringBuilder sql = new StringBuilder("UPDATE extintor SET id_status = ? WHERE id_extintor IN (");
        for (int i = 0; i < idsExtintores.size(); 
                i++) { 
            sql.append("?").append(i < idsExtintores.size() - 1 ? "," : ""); 
        } sql.append(")");
        Integer idFilialDoUsuario = null;
        if ("Técnico".equals(usuarioLogado.getPerfil())) { 
            sql.append(" AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)"); 
            idFilialDoUsuario = usuarioLogado.getIdFilial(); 
        }
        Connection conn = null;
        try {conn = ConnectionFactory.getConnection(); 
             conn.setAutoCommit(false); 
             conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try(PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                stmt.setInt(1, idNovoStatus); 
                int index = 2; for (Integer idExtintor : idsExtintores) { 
                    stmt.setInt(index++, idExtintor); 
                }
                if (idFilialDoUsuario != null) { 
                    stmt.setInt(index, idFilialDoUsuario); 
                }
                int affectedRows = stmt.executeUpdate(); 
                conn.commit();
                if (affectedRows == idsExtintores.size()) { 
                    LOGGER.log(Level.INFO, "{0} extintores -> status {1}", new Object[]{affectedRows, idNovoStatus}); 
                    return true; 
                }
                else { LOGGER.log(Level.WARNING, "Update status: esperava {0}, afetou {1}", new Object[]{idsExtintores.size(), affectedRows});
                return false; 
                }
            }
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Erro ao atualizar status múltiplos!", ex); 
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

    public boolean atualizarDadosPosRecarga(List<Integer> idsExtintores, int idStatusOp, java.util.Date dtRec, java.util.Date dtVal, Usuario usr) {
        if (idsExtintores == null || idsExtintores.isEmpty()) { 
            return true; 
        }
        StringBuilder sql = new StringBuilder("UPDATE extintor SET id_status = ?, data_recarga = ?, data_validade = ? WHERE id_extintor IN (");
        for (int i = 0; i < idsExtintores.size(); 
                i++) { 
            sql.append("?").append(i < idsExtintores.size() - 1 ? "," : ""); 
        } sql.append(")");
        Integer idFilialDoUsuario = null;
        if ("Técnico".equals(usr.getPerfil())) { 
            sql.append(" AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)"); 
            idFilialDoUsuario = usr.getIdFilial(); 
        }
        Connection conn = null;
        try {conn = ConnectionFactory.getConnection(); 
             conn.setAutoCommit(false); 
             conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
             try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                stmt.setInt(1, idStatusOp); 
                stmt.setDate(2, new java.sql.Date(dtRec.getTime())); 
                stmt.setDate(3, new java.sql.Date(dtVal.getTime()));
                int index = 4; 
                for (Integer idExt : idsExtintores) { 
                    stmt.setInt(index++, idExt); 
                }
                if (idFilialDoUsuario != null) { 
                    stmt.setInt(index, idFilialDoUsuario); 
                }
                int affectedRows = stmt.executeUpdate(); 
                conn.commit();
                if (affectedRows == idsExtintores.size()) { 
                    LOGGER.log(Level.INFO, "{0} extintores pós-recarga atualizados.", affectedRows); 
                    return true; 
                }
                else { LOGGER.log(Level.WARNING, "Update pós-recarga: esperava {0}, afetou {1}", new Object[]{idsExtintores.size(), affectedRows}); 
                return false; 
                }
            }
        } catch (Exception ex) { LOGGER.log(Level.SEVERE, "Erro ao atualizar dados pós-recarga!", ex); 
        if (conn != null) { 
            try { conn.rollback(); 
            } catch (SQLException rbEx) {
            } 
        } return false;
        } finally { 
            if (conn != null) { 
                try { ConnectionFactory.closeConnection(conn); 
                } catch (Exception e) {
                } 
            } 
        }
    }

    private Extintor popularExtintor(ResultSet rs) throws SQLException {
        Extintor extintor = new Extintor();
        try {
            extintor.setIdExtintor(rs.getInt("id_extintor"));
            extintor.setTipoEquipamento(rs.getString("tipo_equipamento"));
            extintor.setNumeroControle(rs.getString("numero_controle"));
            extintor.setClasseExtintora(rs.getString("classe_extintora"));
            extintor.setCargaNominal(rs.getString("carga_nominal"));
            extintor.setReferenciaLocalizacao(rs.getString("referencia_localizacao"));
            extintor.setDataRecarga(rs.getDate("data_recarga"));
            extintor.setDataValidade(rs.getDate("data_validade"));
            extintor.setObservacao(rs.getString("observacao"));
            extintor.setIdSetor(rs.getInt("id_setor"));
            extintor.setIdStatus(rs.getInt("id_status"));

            Setor setor = new Setor();
            setor.setIdSetor(extintor.getIdSetor());
            setor.setNome(rs.getString("nome_setor"));
            setor.setIdFilial(rs.getInt("id_filial"));
            extintor.setSetor(setor);

            if (rs.getObject("id_status") != null && rs.getString("nome_status") != null) {
                StatusExtintor status = new StatusExtintor();
                status.setIdStatus(extintor.getIdStatus());
                status.setNome(rs.getString("nome_status"));
                extintor.setStatus(status);
            } else {
                 extintor.setStatus(null);
            }
        } catch (SQLException e) {
             LOGGER.log(Level.SEVERE, "Erro SQL DENTRO de popularExtintor!", e); 
             throw e;
        } catch (Throwable t) {
             LOGGER.log(Level.SEVERE, "Erro INESPERADO DENTRO de popularExtintor!", t); 
             throw new SQLException("Erro", t);
        }
        return extintor;
    }
}