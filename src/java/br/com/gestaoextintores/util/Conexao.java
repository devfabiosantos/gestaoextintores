package br.com.gestaoextintores.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    
    private static final String URL =   "jdbc:postgresql://localhost:5432/gestao_extintores";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1401";
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontnrado.", e);
        }
    }
}