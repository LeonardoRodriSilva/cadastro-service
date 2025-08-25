package com.entity;

public record Cliente(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco
) {

    public static Cliente novo(String nome, String email, String telefone, String endereco) {
        return new Cliente(null, nome, email, telefone, endereco);
    }

    public Cliente atualizar(String nome, String email, String telefone, String endereco) {
        return new Cliente(this.id, nome, email, telefone, endereco);
    }
}
