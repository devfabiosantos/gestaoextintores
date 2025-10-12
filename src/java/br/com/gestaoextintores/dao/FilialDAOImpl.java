/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilialDAOImpl implements GenericDAO {

    private Connection conn;

    public FilialDAOImpl() throws Exception {
        try {
            this.conn = ConnectionFactory.getConnection();
            System.out.println("Conectado com sucesso | {FILIAL}!");
        } catch (Exception ex) {
            throw new Exception("Erro ao conectar com o banco de dados: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean cadastrar(Object object) {
        Filial filial = (Filial) object;
        String sql = "INSERT INTO filial (nome, endereco) VALUES (?, ?)";
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, filial.getNome());
                stmt.setString(2, filial.getEndereco());
                stmt.execute();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Problema ao cadastrar filial!", ex);
            try {
                conn.rollback();
            } catch (SQLException erroRollback) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao realizar rollback!", erroRollback);
            }
            return false;
        } finally {
            try {
                ConnectionFactory.closeConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão!", ex);
            }
        }
    }

    @Override
    public List<Object> listar() {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM filial ORDER BY nome";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Filial filial = new Filial();
                filial.setIdFilial(rs.getInt("id_filial"));
                filial.setNome(rs.getString("nome"));
                filial.setEndereco(rs.getString("endereco"));
                resultado.add(filial);
            }
        } catch (SQLException ex) {
            Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao listar filiais!", ex);
        } finally {
            try {
                ConnectionFactory.closeConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão!", ex);
            }
        }
        return resultado;
    }

    @Override
    public List<Object> listar(int idObject) {
        List<Object> resultado = new ArrayList<>();
        String sql = "SELECT * FROM filial WHERE id_filial = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        } catch (SQLException ex) {
            Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao listar filial por ID!", ex);
        } finally {
            try {
                ConnectionFactory.closeConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão", ex);
            }
        }
        return resultado;
    }

    @Override
    public Boolean excluir(int idObject) {
        String sql = "DELETE FROM filial WHERE id_filial = ?";
        try {
            conn.setAutoCommit(false);
            //conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idObject);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao excluir filial!", ex);
            try {
                conn.rollback();
            } catch (SQLException erroRollback) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao realizar rollback!", erroRollback);
            }
            return false;
        } finally {
            try {
                ConnectionFactory.closeConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão!", ex);
            }
        }
    }

    @Override
    public Object carregar(int idObject) {
        Filial filial = null;
        String sql = "SELECT * FROM filial WHERE id_filial = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idObject);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    filial = new Filial();
                    filial.setIdFilial(rs.getInt("id_filial"));
                    filial.setNome(rs.getString("nome"));
                    filial.setEndereco(rs.getString("endereco"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao carregar filial!", ex);
        } finally {
            try {
                ConnectionFactory.closeConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão!", ex);
            }
        }
        return filial;
    }

    @Override
    public Boolean alterar(Object object) {
        Filial filial = (Filial) object;
        String sql = "UPDATE filial SET nome = ?, endereco = ? WHERE id_filial = ?";
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, filial.getNome());
                stmt.setString(2, filial.getEndereco());
                stmt.setInt(5, filial.getIdFilial());
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao atualizar filial!", ex);
            try {
                conn.rollback();
            } catch (SQLException erroRollback) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao realizar rollback!", erroRollback);
            }
            return false;
        } finally {
            try {
                ConnectionFactory.closeConnection(conn);
            } catch (Exception ex) {
                Logger.getLogger(FilialDAOImpl.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão!", ex);
            }
        }
    }
}
