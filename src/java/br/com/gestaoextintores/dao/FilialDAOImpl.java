package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Usuario; // Precisamos do Usuário
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilialDAOImpl {

    private static final Logger LOGGER = Logger.getLogger(FilialDAOImpl.class.getName());

    public FilialDAOImpl() {}

    public boolean cadastrar(Filial filial) {
        String sql = "INSERT INTO filial (nome, endereco) VALUES (?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            stmt.setString(1, filial.getNome());
            stmt.setString(2, filial.getEndereco());
            stmt.execute();
            conn.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problema ao cadastrar filial!", ex);
            return false;
        }
    }

    public List<Filial> listar(Usuario usuarioLogado) {
        List<Filial> resultado = new ArrayList<>();

        String sql = "SELECT * FROM filial";

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
                    Filial filial = new Filial();
                    filial.setIdFilial(rs.getInt("id_filial"));
                    filial.setNome(rs.getString("nome"));
                    filial.setEndereco(rs.getString("endereco"));
                    resultado.add(filial);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar filiais!", ex);
        }
        return resultado;
    }

    public Boolean excluir(int idFilial, Usuario usuarioLogado) {
        String sql = "DELETE FROM filial WHERE id_filial = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idFilial);

            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            stmt.executeUpdate();
            conn.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir filial!", ex);
            return false;
        }
    }

    public Filial carregar(int idFilial, Usuario usuarioLogado) {
        Filial filial = null;
        String sql = "SELECT * FROM filial WHERE id_filial = ?";

        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idFilial);
 
            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    filial = new Filial();
                    filial.setIdFilial(rs.getInt("id_filial"));
                    filial.setNome(rs.getString("nome"));
                    filial.setEndereco(rs.getString("endereco"));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar filial!", ex);
        }
        return filial;
    }

    public Boolean alterar(Filial filial, Usuario usuarioLogado) {
        String sql = "UPDATE filial SET nome = ?, endereco = ? WHERE id_filial = ?";
        
        if ("Técnico".equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);

            stmt.setString(1, filial.getNome());
            stmt.setString(2, filial.getEndereco());
            stmt.setInt(3, filial.getIdFilial());
            
            if ("Técnico".equals(usuarioLogado.getPerfil())) {
                stmt.setInt(4, usuarioLogado.getIdFilial());
            }
            
            stmt.executeUpdate();
            conn.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar filial!", ex);
            return false;
        }
    }
}