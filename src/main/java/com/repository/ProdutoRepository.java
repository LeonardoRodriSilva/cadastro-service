package com.repository;

import com.config.MongoConfig;
import com.entity.Produto;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ProdutoRepository {

    private static final String ID = "_id";
    private static final String NOME = "nome";
    private static final String PRECO = "preco";
    private static final String DESCRICAO = "descricao";
    private final MongoCollection<Document> collection;

    public ProdutoRepository() {
        this.collection = MongoConfig.getDatabase().getCollection("produtos");
    }

    public Produto criar(Produto produto) {
        Document doc = new Document()
                .append(NOME, produto.nome())
                .append(PRECO, new Decimal128(produto.preco())) // Salva como Decimal128
                .append(DESCRICAO, produto.descricao());

        collection.insertOne(doc);
        String id = doc.getObjectId(ID).toString();

        log.info("Produto criado no MongoDB com ID: {}", id);
        return new Produto(id, produto.nome(), produto.preco(), produto.descricao());
    }

    public Optional<Produto> buscarPorId(String id) {
        if (!ObjectId.isValid(id)) {
            log.warn("Tentativa de busca com ID inválido: {}", id);
            return Optional.empty();
        }

        try {
            Document doc = collection.find(Filters.eq(ID, new ObjectId(id))).first();
            return Optional.ofNullable(doc).map(this::documentToProduto);
        } catch (Exception e) {
            log.error("Erro ao buscar produto por ID: {}", id, e);
            return Optional.empty();
        }
    }

    public List<Produto> listarTodos() {
        List<Produto> produtos = new ArrayList<>();
        for (Document doc : collection.find()) {
            produtos.add(documentToProduto(doc));
        }
        return produtos;
    }

    public boolean atualizar(Produto produto) {
        if (produto.id() == null || !ObjectId.isValid(produto.id())) {
            log.error("ID inválido ou nulo para atualização: {}", produto.id());
            return false;
        }

        Document filtro = new Document(ID, new ObjectId(produto.id()));
        Document update = new Document("$set", new Document()
                .append(NOME, produto.nome())
                .append(PRECO, new Decimal128(produto.preco())) // Atualiza como Decimal128
                .append(DESCRICAO, produto.descricao()));

        UpdateResult result = collection.updateOne(filtro, update);
        boolean foiAtualizado = result.getModifiedCount() > 0;

        if (foiAtualizado) {
            log.info("Produto atualizado no MongoDB: {}", produto.id());
        } else {
            log.warn("Nenhum produto foi atualizado com o ID: {}", produto.id());
        }
        return foiAtualizado;
    }

    public boolean deletar(String id) {
        if (!ObjectId.isValid(id)) {
            log.warn("Tentativa de deleção com ID inválido: {}", id);
            return false;
        }

        try {
            DeleteResult result = collection.deleteOne(Filters.eq(ID, new ObjectId(id)));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            log.error("Erro ao deletar produto: {}", id, e);
            return false;
        }
    }

    private Produto documentToProduto(Document doc) {
        Decimal128 precoDecimal = doc.get(PRECO, Decimal128.class);

        return new Produto(
                doc.getObjectId(ID).toString(),
                doc.getString(NOME),
                precoDecimal != null ? precoDecimal.bigDecimalValue() : BigDecimal.ZERO,
                doc.getString(DESCRICAO)
        );
    }
}