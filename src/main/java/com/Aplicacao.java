package com;

import com.config.RedisPublisher;
import com.entity.Cliente;
import com.repository.ClienteRepository;
import com.service.ClienteService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class Aplicacao {

    public static void main(String[] args) {
        log.info("INICIANDO A APLICAÇÃO DE CADASTRO...");

        ClienteRepository clienteRepository = new ClienteRepository();
        RedisPublisher redisPublisher = new RedisPublisher();


        ClienteService clienteService = new ClienteService(clienteRepository, redisPublisher);

        log.info("Serviços configurados. Pronto para operar.");
        log.info("----------------------------------------------");


        try {
            log.info("Tentando criar um novo cliente...");

            Cliente novoCliente = clienteService.criarCliente(
                    "Ana Carolina",
                    "ana.carolina@exemplo.com",
                    "11987654321",
                    "Avenida Paulista, 1000"
            );

            log.info("Cliente criado com sucesso! ID: {}", novoCliente.id());

            log.info("\n Buscando cliente recém-criado...");
            Optional<Cliente> clienteBuscado = clienteService.buscarPorId(novoCliente.id());
            clienteBuscado.ifPresent(c -> log.info("✅ Cliente encontrado: {}", c.nome()));

        } catch (Exception e) {
            log.error("Ocorreu um erro na execução: {}", e.getMessage(), e);
        } finally {
            log.info("----------------------------------------------");
            log.info("Encerrando recursos...");
            RedisPublisher.close();
            log.info("Aplicação finalizada.");
        }
    }
}