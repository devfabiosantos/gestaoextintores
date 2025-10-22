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

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);

            stmt.setInt(1, remessa.getIdUsuarioTecnico());
            stmt.setInt(2, remessa.getIdFilial());
            stmt.setString(3, remessa.getStatusRemessa());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idGerado = generatedKeys.getInt(1);
                    remessa.setIdRemessa(idGerado);
                }
            }

            conn.commit();
            LOGGER.log(Level.INFO, "Remessa criada com sucesso (ID: {0})", idGerado);
            return idGerado;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao criar remessa!", ex);
            return -1;
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
        String sql = "SELECT * FROM remessa WHERE id_remessa = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
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
            LOGGER.log(Level.SEVERE, "Erro ao carregar remessa!", ex);
        }
        return remessa;
    }

    public boolean aprovarRemessa(int idRemessa, String novoStatus, int idUsuarioAdmin) {
        String sql = "UPDATE remessa SET status_remessa = ?, id_usuario_admin = ?, data_aprovacao = CURRENT_TIMESTAMP " +
                     "WHERE id_remessa = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            stmt.setString(1, novoStatus);
            stmt.setInt(2, idUsuarioAdmin);
            stmt.setInt(3, idRemessa);

            int affectedRows = stmt.executeUpdate();
            conn.commit();

            return affectedRows > 0;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao aprovar remessa (ID: " + idRemessa + ")!", ex);
            return false;
        }
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

        return remessa;
    }
}