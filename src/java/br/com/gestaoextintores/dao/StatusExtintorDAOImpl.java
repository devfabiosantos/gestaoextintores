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
import br.com.gestaoextintores.model.StatusExtintor;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatusExtintorDAOImpl {

    private static final Logger LOGGER = Logger.getLogger(StatusExtintorDAOImpl.class.getName());

    public StatusExtintorDAOImpl() {}

    public List<StatusExtintor> listar() {
        List<StatusExtintor> resultado = new ArrayList<>();
        String sql = "SELECT id_status, nome FROM statusextintor ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                StatusExtintor status = new StatusExtintor();
                status.setIdStatus(rs.getInt("id_status"));
                status.setNome(rs.getString("nome"));
                resultado.add(status);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar status de extintor!", ex);
        }
        return resultado;
    }

    public StatusExtintor carregar(int idStatus) {
        StatusExtintor status = null;
        String sql = "SELECT id_status, nome FROM statusextintor WHERE id_status = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idStatus);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    status = new StatusExtintor();
                    status.setIdStatus(rs.getInt("id_status"));
                    status.setNome(rs.getString("nome"));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar status de extintor!", ex);
        }
        return status;
    }

    public int getIdPorNome(String nomeStatus) {
        int idStatus = -1;
        String sql = "SELECT id_status FROM statusextintor WHERE nome = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeStatus);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    idStatus = rs.getInt("id_status");
                } else {
                    LOGGER.log(Level.WARNING, "Status ''{0}'' não encontrado no banco de dados.", nomeStatus);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar ID do status por nome!", ex);
        }
        return idStatus;
    }
}