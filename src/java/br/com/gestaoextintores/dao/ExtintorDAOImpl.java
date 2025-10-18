package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtintorDAOImpl implements GenericDAO {

    private static final Logger LOGGER = Logger.getLogger(ExtintorDAOImpl.class.getName());

    public ExtintorDAOImpl() {}

    @Override
    public boolean cadastrar(Object object) {
        Extintor extintor = (Extintor) object;
        String sql = "INSERT INTO extintor (numero_controle, tipo, data_recarga, data_validade, localizacao, id_filial) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setString(1, extintor.getNumeroControle());
            stmt.setString(2, extintor.getTipo());
            stmt.setDate(3, extintor.getDataRecarga() != null ? new java.sql.Date(extintor.getDataRecarga().getTime()) : null);
            stmt.setDate(4, extintor.getDataValidade() != null ? new java.sql.Date(extintor.getDataValidade().getTime()) : null);
            stmt.setString(5, extintor.getLocalizacao());
            stmt.setInt(6, extintor.getIdFilial());

            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar extintor!", ex);
            return false;
        }
    }

    @Override
    public List<Object> listar() {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM extintor ORDER BY id_extintor";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Extintor extintor = new Extintor();
                extintor.setIdExtintor(rs.getInt("id_extintor"));
                extintor.setNumeroControle(rs.getString("numero_controle"));
                extintor.setTipo(rs.getString("tipo"));
                extintor.setDataRecarga(rs.getDate("data_recarga"));
                extintor.setDataValidade(rs.getDate("data_validade"));
                extintor.setLocalizacao(rs.getString("localizacao"));
                extintor.setIdFilial(rs.getInt("id_filial"));
                resultado.add(extintor);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar extintores!", ex);
        }

        return resultado;
    }

    @Override
    public List<Object> listar(int idFilial) {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM extintor WHERE id_filial = ? ORDER BY id_extintor";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFilial);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Extintor extintor = new Extintor();
                    extintor.setIdExtintor(rs.getInt("id_extintor"));
                    extintor.setNumeroControle(rs.getString("numero_controle"));
                    extintor.setTipo(rs.getString("tipo"));
                    extintor.setDataRecarga(rs.getDate("data_recarga"));
                    extintor.setDataValidade(rs.getDate("data_validade"));
                    extintor.setLocalizacao(rs.getString("localizacao"));
                    extintor.setIdFilial(rs.getInt("id_filial"));
                    resultado.add(extintor);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar extintores por filial!", ex);
        }

        return resultado;
    }

    @Override
    public Boolean excluir(int idExtintor) {
        String sql = "DELETE FROM extintor WHERE id_extintor = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setInt(1, idExtintor);
            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir extintor!", ex);
            return false;
        }
    }

    @Override
    public Object carregar(int idExtintor) {
        Extintor extintor = null;
        String sql = "SELECT * FROM extintor WHERE id_extintor = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idExtintor);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    extintor = new Extintor();
                    extintor.setIdExtintor(rs.getInt("id_extintor"));
                    extintor.setNumeroControle(rs.getString("numero_controle"));
                    extintor.setTipo(rs.getString("tipo"));
                    extintor.setDataRecarga(rs.getDate("data_recarga"));
                    extintor.setDataValidade(rs.getDate("data_validade"));
                    extintor.setLocalizacao(rs.getString("localizacao"));
                    extintor.setIdFilial(rs.getInt("id_filial"));
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar extintor!", ex);
        }

        return extintor;
    }

    @Override
    public Boolean alterar(Object object) {
        Extintor extintor = (Extintor) object;
        String sql = "UPDATE extintor SET numero_controle = ?, tipo = ?, data_recarga = ?, "
                   + "data_validade = ?, localizacao = ?, id_filial = ? WHERE id_extintor = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setString(1, extintor.getNumeroControle());
            stmt.setString(2, extintor.getTipo());
            stmt.setDate(3, extintor.getDataRecarga() != null ? new java.sql.Date(extintor.getDataRecarga().getTime()) : null);
            stmt.setDate(4, extintor.getDataValidade() != null ? new java.sql.Date(extintor.getDataValidade().getTime()) : null);
            stmt.setString(5, extintor.getLocalizacao());
            stmt.setInt(6, extintor.getIdFilial());
            stmt.setInt(7, extintor.getIdExtintor());

            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar extintor!", ex);
            return false;
        }
    }
}
