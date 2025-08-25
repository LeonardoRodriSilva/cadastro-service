package com.config;

import com.exception.DatabaseConfigException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class MongoConfig {

    private static Properties props;
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private MongoConfig() {
    }

    static {
        loadProperties();
        initializeConnection();
    }

    private static void loadProperties() {
        props = new Properties();
        try (InputStream input = MongoConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new DatabaseConfigException("Arquivo database.properties não encontrado");
            }
            props.load(input);
            log.info("Propriedades do MongoDB carregadas com sucesso");
        } catch (IOException e) {
            log.error("Erro ao carregar propriedades do MongoDB", e);
            throw new DatabaseConfigException("Erro ao carregar as propriedades do MongoDB", e);
        }
    }

    private static void initializeConnection() {
        try {
            String mongoUri = props.getProperty("mongo.uri");
            String mongoDatabase = props.getProperty("mongo.database");

            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase(mongoDatabase);

            database.runCommand(new org.bson.Document("ping", 1));
            log.info("Conexão com MongoDB estabelecida com sucesso");
        } catch (Exception e) {
            log.error("Erro ao conectar com MongoDB", e);
            throw new DatabaseConfigException("Erro ao conectar com MongoDB", e);
        }
    }

    public static MongoDatabase getDatabase() {
        try {
            database.runCommand(new Document("ping", 1));
            log.debug("Retornando conexão ativa com MongoDB");
            return database;
        } catch (Exception e) {
            log.warn("Conexão MongoDB perdida, reconectando...");
            initializeConnection();
            return database;
        }
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            log.info("Conexão MongoDB fechada");
        }
    }
}