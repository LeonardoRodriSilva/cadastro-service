package com.service;

import com.config.RedisPublisher;
import com.entity.Cliente;
import com.exception.DataAccessException;
import com.repository.ClienteRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RedisPublisher redisPublisher;

    public ClienteService(ClienteRepository clienteRepository, RedisPublisher redisPublisher) {
        this.clienteRepository = clienteRepository;
        this.redisPublisher = redisPublisher;
    }

    public Cliente criarCliente(String nome, String email, String telefone,
                                String endereco) {
        try {
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do cliente não pode ser vazio");
            }
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                throw new IllegalArgumentException("Email inválido");
            }

            Cliente cliente = Cliente.novo(nome, email, telefone, endereco);
            Cliente clienteSalvo = clienteRepository.salvar(cliente);

            if (clienteSalvo != null) {
                redisPublisher.publish("clientes-topic", clienteSalvo);
            }

            return clienteSalvo;
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao salvar cliente: " + e.getMessage(), e);
        }
    }

    public Optional<Cliente> buscarPorId(Long id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID deve ser maior que zero");
            }
            return clienteRepository.buscarPorId(id);
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao buscar cliente: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listarTodos() {
        try {
            return clienteRepository.listarTodos();
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao listar clientes: " + e.getMessage(), e);
        }
    }

    public Cliente atualizarCliente(Long id, String nome, String email, String telefone, String endereco) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID deve ser maior que zero");
            }
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do cliente não pode ser vazio");
            }
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                throw new IllegalArgumentException("Email inválido");
            }

            Optional<Cliente> clienteExistente = clienteRepository.buscarPorId(id);
            if (clienteExistente.isEmpty()) {
                throw new IllegalArgumentException("Cliente não encontrado com ID: " + id);
            }

            Cliente clienteAtualizado = clienteExistente.get().atualizar(nome, email, telefone, endereco);
            return clienteRepository.atualizar(clienteAtualizado);

        } catch (SQLException e) {
            throw new DataAccessException("Erro ao atualizar cliente: " + e.getMessage(), e);
        }
    }

    public boolean deletarCliente(Long id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID deve ser maior que zero");
            }

            Optional<Cliente> cliente = clienteRepository.buscarPorId(id);
            if (cliente.isEmpty()) {
                throw new IllegalArgumentException("Cliente não encontrado com ID: " + id);
            }

            return clienteRepository.deletar(id);

        } catch (SQLException e) {
            throw new DataAccessException("Erro ao deletar cliente: " + e.getMessage(), e);
        }
    }

    public void atualizarEmailCliente(Long id, String novoEmail) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID do cliente é inválido");
            }
            if (novoEmail == null || novoEmail.trim().isEmpty() || !novoEmail.contains("@")) {
                throw new IllegalArgumentException("O novo e-mail fornecido é inválido");
            }
            clienteRepository.atualizarEmailComProcedure(id, novoEmail);
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao atualizar e-mail via procedure: " + e.getMessage(), e);
        }
    }
}
