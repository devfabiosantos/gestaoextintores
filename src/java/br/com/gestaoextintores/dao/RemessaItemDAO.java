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
import br.com.gestaoextintores.model.Extintor;
import br.com.gestaoextintores.model.RemessaItem;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemessaItemDAO {

    private static final Logger LOGGER = Logger.getLogger(RemessaItemDAO.class.getName());

    public RemessaItemDAO() {}

    public boolean adicionarItens(int idRemessa, List<RemessaItem> itens) {
        String sql = "INSERT INTO remessa_item (id_remessa, id_extintor, observacao_tecnico) VALUES (?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
             conn.commit();
             return true;
        } catch (Exception ex) {
            return false; 
        }
    }

    public List<RemessaItem> listarPorRemessa(int idRemessa) {
        List<RemessaItem> resultado = new ArrayList<>();
        String sql = "SELECT ri.*, " +
                     "       e.numero_controle, e.classe_extintora " +
                     "FROM remessa_item ri " +
                     "JOIN extintor e ON ri.id_extintor = e.id_extintor " +
                     "WHERE ri.id_remessa = ? " +
                     "ORDER BY ri.id_remessa_item";

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

                    Extintor extintor = new Extintor();
                    extintor.setIdExtintor(rs.getInt("id_extintor"));
                    extintor.setNumeroControle(rs.getString("numero_controle"));

                    extintor.setClasseExtintora(rs.getString("classe_extintora"));

                    item.setExtintor(extintor);
                    resultado.add(item);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar itens detalhados da remessa ID: " + idRemessa + "!", ex);
        }
        return resultado;
    }

    public List<Map<String, Object>> getResumoItensPorClasse(int idRemessa) {
        List<Map<String, Object>> resumo = new ArrayList<>();
        String sql = "SELECT " +
                     "    e.classe_extintora, " +
                     "    COUNT(ri.id_extintor) AS quantidade " +
                     "FROM remessa_item ri " +
                     "JOIN extintor e ON ri.id_extintor = e.id_extintor " +
                     "WHERE ri.id_remessa = ? " +
                     "GROUP BY e.classe_extintora " +
                     "ORDER BY e.classe_extintora";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idRemessa);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> linha = new HashMap<>();
                    linha.put("classe_extintora", rs.getString("classe_extintora"));
                    linha.put("quantidade", rs.getInt("quantidade"));
                    resumo.add(linha);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar resumo de itens da remessa ID: " + idRemessa, ex);
        }
        return resumo;
    }
}