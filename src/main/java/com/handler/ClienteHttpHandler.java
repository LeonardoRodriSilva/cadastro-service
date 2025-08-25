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
import java.util.List;
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
            } else if ("GET".equalsIgnoreCase(method) && path.matches("/api/clientes/\\d+")) {
                handleGetClienteById(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/clientes")) {
                handleGetTodosClientes(exchange);
            } else if ("PUT".equalsIgnoreCase(method) && path.matches("/api/clientes/\\d+")) {
                handlePutCliente(exchange);
            } else if ("DELETE".equalsIgnoreCase(method) && path.matches("/api/clientes/\\d+")) {
                handleDeleteCliente(exchange);
            } else {
                log.warn("Rota não encontrada para o método {} e caminho {}", method, path);
                sendResponse(exchange, 404, "{\"error\":\"Rota não encontrada\"}");
            }
        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação ou recurso não encontrado: {}", e.getMessage());
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            log.error("Erro inesperado ao processar a requisição: {}", e.getMessage(), e);
            sendResponse(exchange, 500, "{\"error\":\"Erro interno no servidor\"}");
        }
    }

    private void handleGetTodosClientes(HttpExchange exchange) throws IOException {
        log.info("Recebida requisição GET para /api/clientes (listar todos)");
        List<Cliente> clientes = clienteService.listarTodos();
        String responseJson = objectMapper.writeValueAsString(clientes);
        log.info("Retornando {} clientes.", clientes.size());
        sendResponse(exchange, 200, responseJson);
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
        String idStr = exchange.getRequestURI().getPath().substring(exchange.getRequestURI().getPath().lastIndexOf('/') + 1);
        log.info("Recebida requisição GET para /api/clientes/{}", idStr);
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
    }

    private void handlePutCliente(HttpExchange exchange) throws IOException {
        String idStr = exchange.getRequestURI().getPath().substring(exchange.getRequestURI().getPath().lastIndexOf('/') + 1);
        log.info("Recebida requisição PUT para /api/clientes/{}", idStr);
        Long id = Long.valueOf(idStr);
        InputStream requestBody = exchange.getRequestBody();
        ClienteDTO dto = objectMapper.readValue(requestBody, ClienteDTO.class);
        Cliente clienteAtualizado = clienteService.atualizarCliente(id, dto.nome(), dto.email(), dto.telefone(), dto.endereco());
        String responseJson = objectMapper.writeValueAsString(clienteAtualizado);
        log.info("Cliente com ID {} atualizado com sucesso.", id);
        sendResponse(exchange, 200, responseJson);
    }

    private void handleDeleteCliente(HttpExchange exchange) throws IOException {
        String idStr = exchange.getRequestURI().getPath().substring(exchange.getRequestURI().getPath().lastIndexOf('/') + 1);
        log.info("Recebida requisição DELETE para /api/clientes/{}", idStr);
        Long id = Long.valueOf(idStr);
        boolean deletado = clienteService.deletarCliente(id);
        if (deletado) {
            log.info("Cliente com ID {} deletado com sucesso.", id);
            sendResponse(exchange, 204, "");
        } else {
            log.warn("Falha ao deletar cliente com ID {}. Pode já ter sido removido.", id);
            sendResponse(exchange, 404, "{\"error\":\"Cliente com ID " + id + " não encontrado para deleção\"}");
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