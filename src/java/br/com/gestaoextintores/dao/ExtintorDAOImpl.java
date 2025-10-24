package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Extintor;
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

    public boolean cadastrar(Extintor extintor) {
        String sql = "INSERT INTO extintor (tipo_equipamento, numero_controle, " +
                     "classe_extintora, carga_nominal, referencia_localizacao, " +
                     "data_recarga, data_validade, observacao, id_setor, id_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

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
            conn.commit();
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar extintor!", ex);
            return false;
        }
    }

    public List<Extintor> listar(Usuario usuarioLogado) {
        List<Extintor> resultado = new ArrayList<>();
        String sql = "SELECT e.* FROM extintor e " +
                     "JOIN setor s ON e.id_setor = s.id_setor";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " WHERE s.id_filial = ?";
        }
        sql += " ORDER BY e.id_extintor";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(1, usuarioLogado.getIdFilial());
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
        String sql = "SELECT e.* FROM extintor e " +
                     "JOIN setor s ON e.id_setor = s.id_setor " +
                     "WHERE e.id_extintor = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND s.id_filial = ?";
        }

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
        String sql = "UPDATE extintor SET tipo_equipamento = ?, numero_controle = ?, " +
                     "classe_extintora = ?, carga_nominal = ?, referencia_localizacao = ?, " +
                     "data_recarga = ?, data_validade = ?, observacao = ?, " +
                     "id_setor = ?, id_status = ? " +
                     "WHERE id_extintor = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

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

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(12, usuarioLogado.getIdFilial());
            }

            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar extintor!", ex);
            return false;
        }
    }

    public Boolean excluir(int idExtintor, Usuario usuarioLogado) {
        String sql = "DELETE FROM extintor WHERE id_extintor = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            stmt.setInt(1, idExtintor);
            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }
            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir extintor!", ex);
            return false;
        }
    }

    public boolean atualizarStatusVarios(List<Integer> idsExtintores, int idNovoStatus, Usuario usuarioLogado) {
        if (idsExtintores == null || idsExtintores.isEmpty()) {
            return true;
        }

        StringBuilder sql = new StringBuilder(
            "UPDATE extintor SET id_status = ? WHERE id_extintor IN ("
        );
        for (int i = 0; i < idsExtintores.size(); i++) {
            sql.append("?");
            if (i < idsExtintores.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
             sql.append(" AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)");
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            conn.setAutoCommit(false);

            stmt.setInt(1, idNovoStatus);

            int index = 2;
            for (Integer idExtintor : idsExtintores) {
                stmt.setInt(index++, idExtintor);
            }

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                 stmt.setInt(index, usuarioLogado.getIdFilial());
            }

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            if (affectedRows == idsExtintores.size()) {
                 LOGGER.log(Level.INFO, "{0} extintores tiveram status atualizado para {1}", new Object[]{affectedRows, idNovoStatus});
                 return true;
            } else {
                 LOGGER.log(Level.WARNING, "Número inesperado de extintores atualizados ({0} em vez de {1}) para status {2}",
                           new Object[]{affectedRows, idsExtintores.size(), idNovoStatus});
                 return false;
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar status de múltiplos extintores!", ex);
            return false;
        }
    }
    
    public boolean atualizarDadosPosRecarga(List<Integer> idsExtintores, int idStatusOperacional, 
                                            java.util.Date novaDataRecarga, java.util.Date novaDataValidade, 
                                            Usuario usuarioLogado) {
        
        if (idsExtintores == null || idsExtintores.isEmpty()) {
            return true;
        }

        StringBuilder sql = new StringBuilder(
            "UPDATE extintor SET id_status = ?, data_recarga = ?, data_validade = ? WHERE id_extintor IN ("
        );
        for (int i = 0; i < idsExtintores.size(); i++) {
            sql.append("?");
            if (i < idsExtintores.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
             sql.append(" AND id_setor IN (SELECT id_setor FROM setor WHERE id_filial = ?)");
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            conn.setAutoCommit(false);

            stmt.setInt(1, idStatusOperacional);
            stmt.setDate(2, new java.sql.Date(novaDataRecarga.getTime()));
            stmt.setDate(3, new java.sql.Date(novaDataValidade.getTime()));

            int index = 4;
            for (Integer idExtintor : idsExtintores) {
                stmt.setInt(index++, idExtintor);
            }

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                 stmt.setInt(index, usuarioLogado.getIdFilial());
            }

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            if (affectedRows == idsExtintores.size()) {
                 LOGGER.log(Level.INFO, "{0} extintores tiveram dados pós-recarga atualizados", affectedRows);
                 return true;
            } else {
                 LOGGER.log(Level.WARNING, "Número inesperado de extintores atualizados pós-recarga ({0} em vez de {1})", 
                           new Object[]{affectedRows, idsExtintores.size()});
                 return false; 
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar dados pós-recarga de múltiplos extintores!", ex);
            return false;
        }
    }

    private Extintor popularExtintor(ResultSet rs) throws SQLException {
        Extintor extintor = new Extintor();
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
        return extintor;
    }
}