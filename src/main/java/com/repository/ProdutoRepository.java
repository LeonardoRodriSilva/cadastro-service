package com.repository;

import com.config.PostgresConfig;
import com.entity.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProdutoRepository {

    public Produto salvar(Produto produto) throws SQLException {
        if (produto.id() != null) {
            return atualizar(produto);
        }

        String sql = "INSERT INTO produtos (nome, preco, descricao) " +
                "VALUES (?, ?, ?) RETURNING id";

        try(Connection connection = PostgresConfig.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, produto.nome());
            preparedStatement.setBigDecimal(2, produto.preco());
            preparedStatement.setString(3, produto.descricao());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Long novoId = rs.getLong("id");
                    return new Produto(novoId, produto.nome(),
                            produto.preco(), produto.descricao());
                }
                throw new SQLException("Falha ao obter ID gerado");
            }

        }
    }

    public Optional<Produto> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, nome, preco, descricao FROM produtos WHERE id = ?";
        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Produto produto = new Produto(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getBigDecimal("preco"),
                            rs.getString("descricao"));
                    return Optional.of(produto);
                }
            }
        }
        return Optional.empty();
    }

    public List<Produto> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, preco, descricao FROM produtos ORDER BY id";

        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
             try (ResultSet rs = preparedStatement.executeQuery()){

                List<Produto> produtos = new java.util.ArrayList<>();
                while (rs.next()) {
                    Produto produto = new Produto(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getBigDecimal("preco"),
                            rs.getString("descricao"));
                    produtos.add(produto);
                }
                return produtos;
            }
        }
    }

    public Produto atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produtos SET nome = ?, preco = ?, descricao = ? " +
                "WHERE id = ?";

        try (Connection connection = PostgresConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, produto.nome());
            preparedStatement.setBigDecimal(2, produto.preco());
            preparedStatement.setString(3, produto.descricao());
            preparedStatement.setLong(4, produto.id());

            int linhasAfetadas = preparedStatement.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao atualizar produto");
            }
            return produto;
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM produtos WHERE id = ?";

        try (Connection connection = PostgresConfig.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            int linhasAfetadas = preparedStatement.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
}
