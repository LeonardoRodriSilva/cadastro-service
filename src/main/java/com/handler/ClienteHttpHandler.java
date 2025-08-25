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

    private static final String API_CLIENTES_PATH = "/api/clientes";
    private static final String API_CLIENTES_PREFIX = "/api/clientes/";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";

    private final ClienteService clienteService;
    private final ObjectMapper objectMapper;

    public ClienteHttpHandler(ClienteService clienteService, ObjectMapper objectMapper) {
        this.clienteService = clienteService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case METHOD_POST:
                    if (path.equals(API_CLIENTES_PATH)) {
                        handlePostCliente(exchange);
                    }
                    break;
                case METHOD_GET:
                    if (path.equals(API_CLIENTES_PATH)) {
                        handleGetTodosClientes(exchange);
                    } else if (path.startsWith(API_CLIENTES_PREFIX)) {
                        handleGetClienteById(exchange);
                    }
                    break;
                case METHOD_PUT:
                    if (path.startsWith(API_CLIENTES_PREFIX)) {
                        handlePutCliente(exchange);
                    }
                    break;
                case METHOD_DELETE:
                    if (path.startsWith(API_CLIENTES_PREFIX)) {
                        handleDeleteCliente(exchange);
                    }
                    break;
                default:
                    log.warn("Método não suportado: {}", method);
                    sendResponse(exchange, 405, createErrorResponse("Método não suportado"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Erro de validação ou recurso não encontrado: {}", e.getMessage());
            sendResponse(exchange, 400, createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Erro inesperado ao processar a requisição: {}", e.getMessage(), e);
            sendResponse(exchange, 500, createErrorResponse("Erro interno no servidor"));
        }
    }

    private void handleGetTodosClientes(HttpExchange exchange) throws IOException {
        log.info("Recebida requisição GET para {} (listar todos)", API_CLIENTES_PATH);
        List<Cliente> clientes = clienteService.listarTodos();
        String responseJson = objectMapper.writeValueAsString(clientes);
        log.info("Retornando {} clientes.", clientes.size());
        sendResponse(exchange, 200, responseJson);
    }

    private void handlePostCliente(HttpExchange exchange) throws IOException {
        log.info("Recebida requisição POST para {}", API_CLIENTES_PATH);
        InputStream requestBody = exchange.getRequestBody();
        ClienteDTO dto = objectMapper.readValue(requestBody, ClienteDTO.class);
        Cliente novoCliente = clienteService.criarCliente(dto.nome(), dto.email(), dto.telefone(), dto.endereco());
        String responseJson = objectMapper.writeValueAsString(novoCliente);
        log.info("Cliente criado com sucesso. ID: {}", novoCliente.id());
        sendResponse(exchange, 201, responseJson);
    }

    private void handleGetClienteById(HttpExchange exchange) throws IOException {
        try {
            Long id = extractIdFromPath(exchange);
            log.info("Recebida requisição GET para {}/{}", API_CLIENTES_PATH, id);
            Optional<Cliente> clienteOpt = clienteService.buscarPorId(id);
            if (clienteOpt.isPresent()) {
                log.info("Cliente com ID {} encontrado.", id);
                String responseJson = objectMapper.writeValueAsString(clienteOpt.get());
                sendResponse(exchange, 200, responseJson);
            } else {
                log.warn("Cliente com ID {} não foi encontrado.", id);
                sendResponse(exchange, 404, createErrorResponse("Cliente com ID " + id + " não encontrado"));
            }
        } catch (NumberFormatException e) {
            log.warn("ID inválido na requisição: {}", exchange.getRequestURI().getPath());
            sendResponse(exchange, 400, createErrorResponse("ID do cliente deve ser numérico."));
        }
    }

    private void handlePutCliente(HttpExchange exchange) throws IOException {
        try {
            Long id = extractIdFromPath(exchange);
            log.info("Recebida requisição PUT para {}/{}", API_CLIENTES_PATH, id);
            InputStream requestBody = exchange.getRequestBody();
            ClienteDTO dto = objectMapper.readValue(requestBody, ClienteDTO.class);
            Cliente clienteAtualizado = clienteService.atualizarCliente(id, dto.nome(), dto.email(), dto.telefone(), dto.endereco());
            String responseJson = objectMapper.writeValueAsString(clienteAtualizado);
            log.info("Cliente com ID {} atualizado com sucesso.", id);
            sendResponse(exchange, 200, responseJson);
        } catch (NumberFormatException e) {
            log.warn("ID inválido na requisição: {}", exchange.getRequestURI().getPath());
            sendResponse(exchange, 400, createErrorResponse("ID do cliente deve ser numérico."));
        }
    }

    private void handleDeleteCliente(HttpExchange exchange) throws IOException {
        try {
            Long id = extractIdFromPath(exchange);
            log.info("Recebida requisição DELETE para {}/{}", API_CLIENTES_PATH, id);
            boolean deletado = clienteService.deletarCliente(id);
            if (deletado) {
                log.info("Cliente com ID {} deletado com sucesso.", id);
                sendResponse(exchange, 204, "");
            } else {
                log.warn("Falha ao deletar cliente com ID {}. Não foi encontrado.", id);
                sendResponse(exchange, 404, createErrorResponse("Cliente com ID " + id + " não encontrado para deleção"));
            }
        } catch (NumberFormatException e) {
            log.warn("ID inválido na requisição: {}", exchange.getRequestURI().getPath());
            sendResponse(exchange, 400, createErrorResponse("ID do cliente deve ser numérico."));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.getResponseHeaders().set(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private Long extractIdFromPath(HttpExchange exchange) {
        String idStr = exchange.getRequestURI().getPath().substring(API_CLIENTES_PREFIX.length());
        return Long.valueOf(idStr);
    }

    private String createErrorResponse(String message) {
        return "{\"error\":\"" + message + "\"}";
    }
}