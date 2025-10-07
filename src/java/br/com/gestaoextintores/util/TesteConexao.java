package br.com.gestaoextintores.util;

import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {
    public static void main(String[] args) {
        try (Connection con = Conexao.getConnection()) {
            System.out.println("Conexão realizada com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}