package com;

import com.config.RedisPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handler.ClienteHttpHandler;
import com.repository.ClienteRepository;
import com.service.ClienteService;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Aplicacao {

    public static void main(String[] args) throws Exception {
        log.info("### INICIANDO SERVIÇO DE CADASTROS ###");

        ClienteRepository clienteRepository = new ClienteRepository();
        RedisPublisher redisPublisher = new RedisPublisher();
        ObjectMapper objectMapper = new ObjectMapper();
        ClienteService clienteService = new ClienteService(clienteRepository, redisPublisher);
        ClienteHttpHandler clienteHandler = new ClienteHttpHandler(clienteService, objectMapper);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/clientes", clienteHandler);
        server.setExecutor(null);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("\n DESLIGANDO SERVIÇO DE CADASTROS ");
            server.stop(1);
            RedisPublisher.close();
            log.info("Recursos liberados. Servidor desligado.");
        }));

        server.start();
        log.info("Servidor rodando em http://localhost:8080");
        log.info("Endpoint de Clientes disponível em /api/clientes");
    }
}