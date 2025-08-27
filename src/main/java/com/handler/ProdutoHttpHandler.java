package com.handler;

import com.dto.ProdutoDTO;
import com.entity.Produto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.ProdutoService;
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
public class ProdutoHttpHandler implements HttpHandler {

    private static final String API_PRODUTOS_PATH = "/api/produtos";
    private static final String API_PRODUTOS_PREFIX = "/api/produtos/";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_DELETE = "DELETE";

    private static final String ID_INVALIDO = "Produto com ID";

    private final ProdutoService produtoService;
    private final ObjectMapper objectMapper;

    public ProdutoHttpHandler(ProdutoService produtoService, ObjectMapper objectMapper) {
        this.produtoService = produtoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case METHOD_POST:
                    if (path.equals(API_PRODUTOS_PATH)) {
                        handlePostProduto(exchange);
                    }
                    break;
                case METHOD_GET:
                    if (path.equals(API_PRODUTOS_PATH)) {
                        handleGetTodosProdutos(exchange);
                    } else if (path.startsWith(API_PRODUTOS_PREFIX)) {
                        handleGetProdutoById(exchange);
                    }
                    break;
                case METHOD_PUT:
                    if (path.startsWith(API_PRODUTOS_PREFIX)) {
                        handlePutProduto(exchange);
                    }
                    break;
                case METHOD_DELETE:
                    if (path.startsWith(API_PRODUTOS_PREFIX)) {
                        handleDeleteProduto(exchange);
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

    private void handleGetTodosProdutos(HttpExchange exchange) throws IOException {
        log.info("Recebida requisição GET para {} (listar todos)", API_PRODUTOS_PATH);
        List<Produto> produtos = produtoService.listarTodosProdutos();
        String responseJson = objectMapper.writeValueAsString(produtos);
        log.info("Retornando {} produtos.", produtos.size());
        sendResponse(exchange, 200, responseJson);
    }

    private void handlePostProduto(HttpExchange exchange) throws IOException {
        log.info("Recebida requisição POST para {}", API_PRODUTOS_PATH);
        InputStream requestBody = exchange.getRequestBody();
        ProdutoDTO dto = objectMapper.readValue(requestBody, ProdutoDTO.class);
        Produto novoProduto = produtoService.criarProduto(dto.nome(), dto.preco(), dto.descricao());
        String responseJson = objectMapper.writeValueAsString(novoProduto);
        log.info("Produto criado com sucesso. ID: {}", novoProduto.id());
        sendResponse(exchange, 201, responseJson);
    }

    private void handleGetProdutoById(HttpExchange exchange) throws IOException {
        String id = extractIdFromPath(exchange);
        log.info("Recebida requisição GET para {}/{}", API_PRODUTOS_PATH, id);
        Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
        if (produtoOpt.isPresent()) {
            log.info("Produto com ID {} encontrado.", id);
            String responseJson = objectMapper.writeValueAsString(produtoOpt.get());
            sendResponse(exchange, 200, responseJson);
        } else {
            log.warn("Produto com ID {} não foi encontrado.", id);
            sendResponse(exchange, 404, createErrorResponse(ID_INVALIDO + id + " não encontrado"));
        }
    }

    private void handlePutProduto(HttpExchange exchange) throws IOException {
        String id = extractIdFromPath(exchange);
        log.info("Recebida requisição PUT para {}/{}", API_PRODUTOS_PATH, id);
        InputStream requestBody = exchange.getRequestBody();
        ProdutoDTO dto = objectMapper.readValue(requestBody, ProdutoDTO.class);
        Produto produtoParaAtualizar = new Produto(id, dto.nome(), dto.preco(), dto.descricao());
        boolean atualizado = produtoService.atualizarProduto(produtoParaAtualizar);
        if (atualizado) {
            log.info("Produto com ID {} atualizado com sucesso.", id);
            String responseJson = objectMapper.writeValueAsString(produtoParaAtualizar);
            sendResponse(exchange, 200, responseJson);
        } else {
            log.warn("Produto com ID {} não foi encontrado para atualização.", id);
            sendResponse(exchange, 404, createErrorResponse(ID_INVALIDO + id + " não encontrado para atualização"));
        }
    }

    private void handleDeleteProduto(HttpExchange exchange) throws IOException {
        String id = extractIdFromPath(exchange);
        log.info("Recebida requisição DELETE para {}/{}", API_PRODUTOS_PATH, id);
        boolean deletado = produtoService.deletarProduto(id);
        if (deletado) {
            log.info("Produto com ID {} deletado com sucesso.", id);
            sendResponse(exchange, 204, "");
        } else {
            log.warn("Falha ao deletar produto com ID {}. Não foi encontrado.", id);
            sendResponse(exchange, 404, createErrorResponse(ID_INVALIDO + id + " não encontrado para deleção"));
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

    private String extractIdFromPath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath().substring(API_PRODUTOS_PREFIX.length());
    }

    private String createErrorResponse(String message) {
        return "{\"error\":\"" + message + "\"}";
    }
}