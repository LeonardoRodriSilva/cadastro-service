package com.service;

import com.entity.Produto;
import com.exception.DataAccessException;
import com.repository.ProdutoRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto criarProduto(String nome, BigDecimal preco, String descricao) {

        try {
            if (nome == null || nome.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome do produto não pode ser vazio");
            }
            if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
            }

            Produto produto = Produto.novo(nome, preco, descricao);
            return produtoRepository.salvar(produto);
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao salvar produto: " + e.getMessage(), e);
        }
    }

    public Optional<Produto> buscarPorId(Long id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("ID deve ser maior que zero");
            }
            return produtoRepository.buscarPorId(id);
        } catch (SQLException e) {
            throw new DataAccessException("Erro ao buscar produto: " + e.getMessage(), e);
        }
    }
}
