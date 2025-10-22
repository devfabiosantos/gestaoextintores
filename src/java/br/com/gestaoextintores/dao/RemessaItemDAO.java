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
import br.com.gestaoextintores.model.RemessaItem;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemessaItemDAO {

    private static final Logger LOGGER = Logger.getLogger(RemessaItemDAO.class.getName());

    public RemessaItemDAO() {}

    public boolean adicionarItens(int idRemessa, List<RemessaItem> itens) {
        if (itens == null || itens.isEmpty()) {
            return true;
        }

        String sql = "INSERT INTO remessa_item (id_remessa, id_extintor, observacao_tecnico) " +
                     "VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (RemessaItem item : itens) {
                stmt.setInt(1, idRemessa);
                stmt.setInt(2, item.getIdExtintor());
                stmt.setString(3, item.getObservacaoTecnico());
                stmt.addBatch();
            }

            int[] resultados = stmt.executeBatch();

            conn.commit();

            for (int resultado : resultados) {
                if (resultado == Statement.EXECUTE_FAILED) {
                    LOGGER.log(Level.SEVERE, "Falha ao adicionar um item à remessa ID: {0}", idRemessa);
                    return false;
                }
            }
            
            LOGGER.log(Level.INFO, "{0} itens adicionados com sucesso à remessa ID: {1}", new Object[]{itens.size(), idRemessa});
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar itens à remessa ID: " + idRemessa + "!", ex);
            return false;
        }
    }

    public List<RemessaItem> listarPorRemessa(int idRemessa) {
        List<RemessaItem> resultado = new ArrayList<>();
        String sql = "SELECT * FROM remessa_item WHERE id_remessa = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRemessa);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RemessaItem item = new RemessaItem();
                    item.setIdRemessaItem(rs.getInt("id_remessa_item"));
                    item.setIdRemessa(rs.getInt("id_remessa"));
                    item.setIdExtintor(rs.getInt("id_extintor"));
                    item.setObservacaoTecnico(rs.getString("observacao_tecnico"));
                    resultado.add(item);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar itens da remessa ID: " + idRemessa + "!", ex);
        }
        return resultado;
    }
}