package com.handler;

import com.dto.ClienteDTO;
import com.entity.Cliente;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.ClienteService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
public class ClienteHttpHandler implements HttpHandler {

    private final ClienteService clienteService;
    private final ObjectMapper objectMapper;

    public ClienteHttpHandler(ClienteService clienteService, ObjectMapper objectMapper) {
        this.clienteService = clienteService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("POST".equalsIgnoreCase(method) && path.equals("/api/clientes")) {
                handlePostCliente(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/clientes/")) {
                handleGetClienteById(exchange);
            } else {
                log.warn("Rota não encontrada para o método {} e caminho {}", method, path);
                sendResponse(exchange, 404, "{\"error\":\"Rota não encontrada\"}");
            }
        } catch (Exception e) {
            log.error("Erro inesperado ao processar a requisição: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\":\"Erro interno no servidor\"}");
        }
    }

    private void handlePostCliente(HttpExchange exchange) throws IOException {
        log.info("Recebida requisição POST para /api/clientes");
        InputStream requestBody = exchange.getRequestBody();
        ClienteDTO dto = objectMapper.readValue(requestBody, ClienteDTO.class);

        Cliente novoCliente = clienteService.criarCliente(dto.nome(), dto.email(), dto.telefone(), dto.endereco());
        String responseJson = objectMapper.writeValueAsString(novoCliente);

        log.info("Cliente criado com sucesso. ID: {}", novoCliente.id());
        sendResponse(exchange, 201, responseJson);
    }

    private void handleGetClienteById(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        log.info("Recebida requisição GET para /api/clientes/{}", idStr);

        try {
            Long id = Long.valueOf(idStr);
            Optional<Cliente> clienteOpt = clienteService.buscarPorId(id);

            if (clienteOpt.isPresent()) {
                log.info("Cliente com ID {} encontrado.", id);
                String responseJson = objectMapper.writeValueAsString(clienteOpt.get());
                sendResponse(exchange, 200, responseJson);
            } else {
                log.warn("Cliente com ID {} não foi encontrado.", id);
                sendResponse(exchange, 404, "{\"error\":\"Cliente com ID " + id + " não encontrado\"}");
            }
        } catch (NumberFormatException e) {
            log.warn("ID inválido recebido na requisição: {}", idStr);
            sendResponse(exchange, 400, "{\"error\":\"ID inválido. O ID do cliente deve ser numérico.\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}