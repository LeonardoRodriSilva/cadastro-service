package com.repository;

import com.config.PostgresConfig;
import com.entity.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteRepository {

    public Cliente salvar(Cliente cliente) throws SQLException {
        if (cliente.id() != null) {
            return atualizar(cliente);
        }

        String sql = "INSERT INTO clientes (nome, email, telefone, endereco) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, cliente.nome());
            preparedStatement.setString(2, cliente.email());
            preparedStatement.setString(3, cliente.telefone());
            preparedStatement.setString(4, cliente.endereco());

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Long novoId = rs.getLong("id");
                    return new Cliente(novoId, cliente.nome(), cliente.email(),
                            cliente.telefone(), cliente.endereco());
                }
                throw new SQLException("Falha ao obter ID gerado");
            }
        }
    }

    public Optional<Cliente> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id, nome, email, telefone, endereco FROM clientes WHERE id = ?";
        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = new Cliente(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("telefone"),
                            rs.getString("endereco")
                    );
                    return Optional.of(cliente);

                }
            }
        }
        return Optional.empty();
    }

    public List<Cliente> listarTodos() throws SQLException {
        String sql = "SELECT id, nome, email, telefone, endereco FROM clientes ORDER BY id";

        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                List<Cliente> clientes = new ArrayList<>();
                while (rs.next()) {
                    Cliente cliente = new Cliente(
                            rs.getLong("id"),
                            rs.getString("nome"),
                            rs.getString("email"),
                            rs.getString("telefone"),
                            rs.getString("endereco")
                    );
                    clientes.add(cliente);
                }
                return clientes;
            }
        }
    }

    public Cliente atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET nome = ?, email = ?, telefone = ?, " +
                "endereco = ? " +
                "WHERE id = ?";

        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, cliente.nome());
            preparedStatement.setString(2, cliente.email());
            preparedStatement.setString(3, cliente.telefone());
            preparedStatement.setString(4, cliente.endereco());
            preparedStatement.setLong(5, cliente.id());

            int linhasAfetadas = preparedStatement.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new SQLException("Nenhum cliente atualizado");
            } else {
                return cliente;
           }
        }
    }

    public boolean deletar(Long id) throws SQLException {
        String sql = "DELETE FROM clientes WHERE id = ?";

        try (Connection connection = PostgresConfig.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            int linhasAfetadas = preparedStatement.executeUpdate();
            return linhasAfetadas > 0;
        }
    }
}
