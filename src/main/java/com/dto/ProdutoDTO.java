package com.dto;

import java.math.BigDecimal;

public record ProdutoDTO(
        String nome,
        BigDecimal preco,
        String descricao
) {
}
