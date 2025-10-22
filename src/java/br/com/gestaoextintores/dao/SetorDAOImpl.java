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

    public boolean cadastrar(Setor setor) {
        String sql = "INSERT INTO setor (nome, id_filial) VALUES (?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            stmt.setString(1, setor.getNome());
            stmt.setInt(2, setor.getIdFilial());

            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar setor!", ex);
            return false;
        }
    }

    public List<Setor> listar(Usuario usuarioLogado) {
        List<Setor> resultado = new ArrayList<>();
        String sql = "SELECT * FROM setor";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " WHERE id_filial = ?";
        }

        sql += " ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(1, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(popularSetor(rs));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar setores!", ex);
        }
        return resultado;
    }

    public Setor carregar(int idSetor, Usuario usuarioLogado) {
        Setor setor = null;
        String sql = "SELECT * FROM setor WHERE id_setor = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSetor);

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    setor = popularSetor(rs);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar setor!", ex);
        }
        return setor;
    }

    public Boolean alterar(Setor setor, Usuario usuarioLogado) {
        String sql = "UPDATE setor SET nome = ? WHERE id_setor = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            stmt.setString(1, setor.getNome());
            stmt.setInt(2, setor.getIdSetor());

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(3, usuarioLogado.getIdFilial());
            }

            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar setor!", ex);
            return false;
        }
    }

    public Boolean excluir(int idSetor, Usuario usuarioLogado) {
        String sql = "DELETE FROM setor WHERE id_setor = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            stmt.setInt(1, idSetor);

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            stmt.executeUpdate();
            conn.commit();
            return true;

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir setor!", ex);
            return false;
        }
    }

    private Setor popularSetor(ResultSet rs) throws SQLException {
        Setor setor = new Setor();
        setor.setIdSetor(rs.getInt("id_setor"));
        setor.setNome(rs.getString("nome"));
        setor.setIdFilial(rs.getInt("id_filial"));
        return setor;
    }
}