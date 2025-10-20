package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilialDAOImpl implements GenericDAO {

    private static final Logger LOGGER = Logger.getLogger(FilialDAOImpl.class.getName());

    // Construtor vazio, assim como o ExtintorDAOImpl
    public FilialDAOImpl() {}

    @Override
    public boolean cadastrar(Object object) {
        Filial filial = (Filial) object;
        String sql = "INSERT INTO filial (nome, endereco) VALUES (?, ?)";
        
        // Conexão aberta e fechada por método
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

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

    @Override
    public List<Object> listar() {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM filial ORDER BY nome";
        
        // Conexão aberta e fechada por método
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Filial filial = new Filial();
                filial.setIdFilial(rs.getInt("id_filial"));
                filial.setNome(rs.getString("nome"));
                filial.setEndereco(rs.getString("endereco"));
                resultado.add(filial);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar filiais!", ex);
        }
        return resultado;
    }

    @Override
    public List<Object> listar(int idObject) {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM filial WHERE id_filial = ?";
        
        // Conexão aberta e fechada por método
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idObject);
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
            LOGGER.log(Level.SEVERE, "Erro ao listar filial por ID!", ex);
        }
        return resultado;
    }

    @Override
    public Boolean excluir(int idObject) {
        String sql = "DELETE FROM filial WHERE id_filial = ?";
        
        // Conexão aberta e fechada por método
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setInt(1, idObject);
            stmt.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir filial!", ex);
            return false;
        }
    }

    @Override
    public Object carregar(int idObject) {
        Filial filial = null;
        String sql = "SELECT * FROM filial WHERE id_filial = ?";
        
        // Conexão aberta e fechada por método
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idObject);
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

    @Override
    public Boolean alterar(Object object) {
        Filial filial = (Filial) object;
        String sql = "UPDATE filial SET nome = ?, endereco = ? WHERE id_filial = ?";
        
        // Conexão aberta e fechada por método
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setString(1, filial.getNome());
            stmt.setString(2, filial.getEndereco());
            
            // --- CORREÇÃO DO BUG DO ÍNDICE ---
            stmt.setInt(3, filial.getIdFilial()); 
            // ----------------------------------
            
            stmt.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar filial!", ex);
            return false;
        }
    }
}