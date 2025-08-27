package com.service;

import com.entity.Produto;
import com.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto criarProduto(String nome, BigDecimal preco, String descricao) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto não pode ser vazio");
        }
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }
        Produto produto = Produto.novo(nome, preco, descricao);
        return produtoRepository.criar(produto);
    }

    public Optional<Produto> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
        return produtoRepository.buscarPorId(id);
    }

    public List<Produto> listarTodosProdutos() {
        return produtoRepository.listarTodos();
    }

    public Optional<Produto> atualizarProduto(String id, String novoNome, BigDecimal novoPreco, String novaDescricao) {
        Optional<Produto> produtoExistenteOpt = produtoRepository.buscarPorId(id);

        if (produtoExistenteOpt.isEmpty()) {
            return Optional.empty();
        }

        Produto produtoExistente = produtoExistenteOpt.get();
        Produto produtoAtualizado = produtoExistente.atualizar(novoNome, novoPreco, novaDescricao);

        boolean sucesso = produtoRepository.atualizar(produtoAtualizado);

        return sucesso ? Optional.of(produtoAtualizado) : Optional.empty();
    }

    public boolean deletarProduto(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID não pode ser nulo ou vazio");
        }
        return produtoRepository.deletar(id);
    }
}