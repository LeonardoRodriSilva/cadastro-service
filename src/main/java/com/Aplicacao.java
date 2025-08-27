package com;

import com.config.RedisPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handler.ClienteHttpHandler;
import com.handler.ProdutoHttpHandler;
import com.repository.ClienteRepository;
import com.repository.ProdutoRepository;
import com.service.ClienteService;
import com.service.ProdutoService;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class Aplicacao {

    public static void main(String[] args) throws Exception {
        log.info("### INICIANDO SERVIÇO DE CADASTROS (NA UNHA) ###");

        ClienteRepository clienteRepository = new ClienteRepository();
        RedisPublisher redisPublisher = new RedisPublisher();
        ClienteService clienteService = new ClienteService(clienteRepository, redisPublisher);
        ClienteHttpHandler clienteHandler = new ClienteHttpHandler(clienteService, new ObjectMapper());

        ProdutoRepository produtoRepository = new ProdutoRepository();
        ProdutoService produtoService = new ProdutoService(produtoRepository);
        ProdutoHttpHandler produtoHandler = new ProdutoHttpHandler(produtoService, new ObjectMapper());

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/clientes", clienteHandler);
        server.createContext("/api/produtos", produtoHandler); // <-- REGISTRAMOS A NOVA ROTA
        server.setExecutor(null);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("\n### DESLIGANDO SERVIÇO DE CADASTROS ###");
            server.stop(1);
            RedisPublisher.close();
            log.info("Recursos liberados. Servidor desligado.");
        }));

        server.start();
        log.info("Servidor rodando em http://localhost:8080");
        log.info("Endpoints disponíveis: /api/clientes, /api/produtos");
        log.info("Pressione Ctrl+C para parar.");
    }
}