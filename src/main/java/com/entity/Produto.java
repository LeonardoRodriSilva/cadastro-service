package com.entity;

import java.math.BigDecimal;

public record Produto(
        Long id,
        String nome,
        BigDecimal preco,
        String descricao
) {

    public static Produto novo(String nome, BigDecimal preco, String descricao) {
        return new Produto(null, nome, preco, descricao);
    }
}
