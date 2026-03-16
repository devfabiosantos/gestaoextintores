package br.com.gestaoextintores.dao;

import br.com.gestaoextintores.model.Filial;
import br.com.gestaoextintores.model.Usuario;
import br.com.gestaoextintores.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilialDAOImpl {

    private static final Logger LOGGER = Logger.getLogger(FilialDAOImpl.class.getName());
    private static final String PERFIL_TECNICO = "Técnico";
    private static final String FILIAL_COLUMNS = "id_filial, nome, cnpj, cep, logradouro, numero, complemento, bairro, endereco, cidade, estado";

    public FilialDAOImpl() {}

    public boolean cadastrar(Filial filial) {
        String sql = "INSERT INTO filial "
                + "(nome, cnpj, cep, logradouro, numero, complemento, bairro, endereco, cidade, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            stmt.setString(1, filial.getNome());
            stmt.setString(2, filial.getCnpj());
            stmt.setString(3, filial.getCep());
            stmt.setString(4, filial.getLogradouro());
            stmt.setString(5, filial.getNumero());
            stmt.setString(6, filial.getComplemento());
            stmt.setString(7, filial.getBairro());
            stmt.setString(8, filial.getEndereco());
            stmt.setString(9, filial.getCidade());
            stmt.setString(10, filial.getEstado());
            stmt.executeUpdate();
            conn.commit();
            return true;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problema ao cadastrar filial!", ex);
            return false;
        }
    }

    public List<Filial> listar(Usuario usuarioLogado) {
        List<Filial> resultado = new ArrayList<>();
        String sql = "SELECT " + FILIAL_COLUMNS + " FROM filial";

        if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
            sql += " WHERE id_filial = ?";
        }

        sql += " ORDER BY nome";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
                stmt.setInt(1, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(popularFilial(rs));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao listar filiais!", ex);
        }
        return resultado;
    }

    public Boolean excluir(int idFilial, Usuario usuarioLogado) {
        String sql = "DELETE FROM filial WHERE id_filial = ?";

        if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setInt(1, idFilial);

            if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            int affectedRows = stmt.executeUpdate();
            conn.commit();
            return affectedRows > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao excluir filial!", ex);
            return false;
        }
    }

    public Filial carregar(int idFilial, Usuario usuarioLogado) {
        Filial filial = null;
        String sql = "SELECT " + FILIAL_COLUMNS + " FROM filial WHERE id_filial = ?";

        if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFilial);

            if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
                stmt.setInt(2, usuarioLogado.getIdFilial());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    filial = popularFilial(rs);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao carregar filial!", ex);
        }
        return filial;
    }

    public Boolean alterar(Filial filial, Usuario usuarioLogado) {
        String sql = "UPDATE filial SET nome = ?, cnpj = ?, cep = ?, logradouro = ?, numero = ?, "
                + "complemento = ?, bairro = ?, endereco = ?, cidade = ?, estado = ? WHERE id_filial = ?";

        if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
            sql += " AND id_filial = ?";
        }

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            stmt.setString(1, filial.getNome());
            stmt.setString(2, filial.getCnpj());
            stmt.setString(3, filial.getCep());
            stmt.setString(4, filial.getLogradouro());
            stmt.setString(5, filial.getNumero());
            stmt.setString(6, filial.getComplemento());
            stmt.setString(7, filial.getBairro());
            stmt.setString(8, filial.getEndereco());
            stmt.setString(9, filial.getCidade());
            stmt.setString(10, filial.getEstado());
            stmt.setInt(11, filial.getIdFilial());

            if (PERFIL_TECNICO.equals(usuarioLogado.getPerfil())) {
                stmt.setInt(12, usuarioLogado.getIdFilial());
            }

            int affectedRows = stmt.executeUpdate();
            conn.commit();
            return affectedRows > 0;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro ao atualizar filial!", ex);
            return false;
        }
    }

    private Filial popularFilial(ResultSet rs) throws Exception {
        Filial filial = new Filial();
        filial.setIdFilial(rs.getInt("id_filial"));
        filial.setNome(rs.getString("nome"));
        filial.setCnpj(rs.getString("cnpj"));
        filial.setCep(rs.getString("cep"));
        filial.setLogradouro(rs.getString("logradouro"));
        filial.setNumero(rs.getString("numero"));
        filial.setComplemento(rs.getString("complemento"));
        filial.setBairro(rs.getString("bairro"));
        filial.setEndereco(rs.getString("endereco"));
        filial.setCidade(rs.getString("cidade"));
        filial.setEstado(rs.getString("estado"));
        return filial;
    }
}
