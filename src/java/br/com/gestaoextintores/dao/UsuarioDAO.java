/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dev Fabio Santos
 */
public class UsuarioDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());

    public UsuarioDAO() {
    }
    
    public Usuario validarLogin(String login, String senha) {
        
        String sql = "SELECT id_usuario, nome, login, perfil, id_filial " +
                     "FROM usuario WHERE login = ? AND senha = ?";
        
        Usuario usuarioLogado = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    usuarioLogado = new Usuario();
                    usuarioLogado.setIdUsuario(rs.getInt("id_usuario"));
                    usuarioLogado.setNome(rs.getString("nome"));
                    usuarioLogado.setLogin(rs.getString("login"));

                    usuarioLogado.setPerfil(rs.getString("perfil"));

                    int idFilialDb = rs.getInt("id_filial");
                    if (rs.wasNull()) {
                        usuarioLogado.setIdFilial(null); 
                    } else {
                        usuarioLogado.setIdFilial(idFilialDb);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Erro ao validar login!", ex);
        }
        return usuarioLogado;
    }
}
