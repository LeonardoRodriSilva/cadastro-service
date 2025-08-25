package com.entity;

import java.math.BigDecimal;

public record ItemNotaFiscal(
        String id,
        String notaFiscalId,
        String produtoId,
        String nomeProduto,
        BigDecimal precoUnitario,
        Integer quantidade,
        BigDecimal valorTotal
) {
}
