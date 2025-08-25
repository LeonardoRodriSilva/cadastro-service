package com.entity;

import com.enums.StatusNotaFiscal;

import java.time.LocalDate;
import java.util.List;

public record NotaFiscal(
        String id,
        String numero,
        LocalDate dataEmissao,
        StatusNotaFiscal status,
        Cliente cliente,
        List<ItemNotaFiscal> itens
) {
}