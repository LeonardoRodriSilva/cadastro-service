package com.entity;

import java.math.BigDecimal;

public record Produto(
        String id,
        String nome,
        BigDecimal preco,
        String descricao
) {

    public static Produto novo(String nome, BigDecimal preco, String descricao) {
        return new Produto(null, nome, preco, descricao);
    }

    public Produto atualizar(String nome, BigDecimal preco, String descricao) {
        return new Produto(this.id, nome, preco, descricao);
    }
}
