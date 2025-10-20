package br.com.gestaoextintores.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    
    private static final String URL =    "jdbc:postgresql://localhost:5432/gestao_extintores?characterEncoding=UTF-8";
    
    private static final String USER = "postgres";
    private static final String PASSWORD = "1401";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new SQLException("Erro ao conectar ao banco de dados: " + ex.getMessage(), ex);
        }
    }
    
    public static void closeConnection(Connection conn) throws Exception {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            throw new Exception("Erro ao fechar a conexão: " + ex.getMessage(), ex);
        }
    }
}