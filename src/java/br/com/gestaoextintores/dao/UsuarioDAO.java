package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {

    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());

    public UsuarioDAO() {
    }

    public Usuario validarLogin(String login, String senha) {
        String sql = "SELECT u.id_usuario, u.nome AS nome_usuario, u.login, u.perfil, u.id_filial, " +
                     "       f.id_filial AS f_id_filial, f.nome AS nome_filial, f.endereco AS endereco_filial " + // Colunas da Filial com alias 'f'
                     "FROM usuario u " +
                     "LEFT JOIN filial f ON u.id_filial = f.id_filial " +
                     "WHERE u.login = ? AND u.senha = ?";
        
        Usuario usuarioLogado = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuarioLogado = popularUsuario(rs);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao validar login!", ex);
        } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Erro geral ao validar login!", ex);
        }
        return usuarioLogado;
    }

    public int cadastrar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, login, senha, perfil, id_filial) VALUES (?, ?, ?, ?, ?)";
        int idGerado = -1;
        Connection conn = null; 

        try {
             conn = ConnectionFactory.getConnection();
             conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, usuario.getNome());
                stmt.setString(2, usuario.getLogin());
                stmt.setString(3, usuario.getSenha());
                stmt.setString(4, usuario.getPerfil());
                if (usuario.getIdFilial() == null) { stmt.setNull(5, Types.INTEGER); } 
                else { stmt.setInt(5, usuario.getIdFilial()); }
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) { idGerado = generatedKeys.getInt(1); } 
                    else { throw new SQLException("Falha ao obter ID gerado para usuário."); }
                }
            } 
            conn.commit(); 
            LOGGER.log(Level.INFO, "Usuário cadastrado com sucesso (ID: {0})", idGerado);
            return idGerado;

        } catch (Exception ex) { 
            LOGGER.log(Level.SEVERE, "Erro ao cadastrar usuário!", ex);
            if (conn != null) { try { conn.rollback(); } catch (SQLException rbEx) { LOGGER.log(Level.SEVERE, "Erro crítico ao tentar rollback!", rbEx); } }
            return -1;
        } finally {
            if (conn != null) { try { ConnectionFactory.closeConnection(conn); } catch (Exception closeEx) { LOGGER.log(Level.SEVERE, "Erro ao fechar conexão!", closeEx); } }
        }
    }

    public List<Usuario> listar() {
        List<Usuario> resultado = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nome AS nome_usuario, u.login, u.perfil, u.id_filial, " +
                     "       f.id_filial AS f_id_filial, f.nome AS nome_filial, f.endereco AS endereco_filial " +
                     "FROM usuario u " +
                     "LEFT JOIN filial f ON u.id_filial = f.id_filial " +
                     "ORDER BY u.nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultado.add(popularUsuario(rs));
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar usuários!", ex);
        }
        return resultado;
    }

    public Usuario carregar(int idUsuario) {
        Usuario usuario = null;
        String sql = "SELECT u.id_usuario, u.nome AS nome_usuario, u.login, u.perfil, u.id_filial, " +
                     "       f.id_filial AS f_id_filial, f.nome AS nome_filial, f.endereco AS endereco_filial " +
                     "FROM usuario u " +
                     "LEFT JOIN filial f ON u.id_filial = f.id_filial " +
                     "WHERE u.id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario = popularUsuario(rs);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar usuário!", ex);
        }
        return usuario;
    }

    public boolean alterar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, login = ?, perfil = ?, id_filial = ? WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getLogin());
            stmt.setString(3, usuario.getPerfil());
            if (usuario.getIdFilial() == null) { stmt.setNull(4, Types.INTEGER); } 
            else { stmt.setInt(4, usuario.getIdFilial()); }
            stmt.setInt(5, usuario.getIdUsuario());
            int affectedRows = stmt.executeUpdate(); 
            conn.commit();
            if (affectedRows > 0) { LOGGER.log(Level.INFO, "Usuário alterado com sucesso (ID: {0})", usuario.getIdUsuario()); return true; } 
            else { LOGGER.log(Level.WARNING, "Nenhum usuário encontrado para alterar (ID: {0})", usuario.getIdUsuario()); return false; }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar usuário!", ex);
            return false;
        }
    }

    public boolean excluir(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            stmt.setInt(1, idUsuario);
            int affectedRows = stmt.executeUpdate();
            conn.commit();
             if (affectedRows > 0) { LOGGER.log(Level.INFO, "Usuário excluído com sucesso (ID: {0})", idUsuario); return true; } 
             else { LOGGER.log(Level.WARNING, "Nenhum usuário encontrado para excluir (ID: {0})", idUsuario); return false; }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir usuário!", ex);
            return false;
        }
    }

    private Usuario popularUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNome(rs.getString("nome_usuario")); 
        usuario.setLogin(rs.getString("login"));
        usuario.setPerfil(rs.getString("perfil"));

        int idFilialUsuario = rs.getInt("id_filial");
        if (rs.wasNull()) {
            usuario.setIdFilial(null);
            usuario.setFilial(null);
        } else {
            usuario.setIdFilial(idFilialUsuario);
            Filial filial = new Filial();
            filial.setIdFilial(rs.getInt("f_id_filial")); 
            filial.setNome(rs.getString("nome_filial"));
            filial.setEndereco(rs.getString("endereco_filial"));
            usuario.setFilial(filial); 
        }
        
        return usuario;
    }
}