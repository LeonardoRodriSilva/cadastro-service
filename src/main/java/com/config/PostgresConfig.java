package com.config;

import com.exception.DatabaseConfigException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class PostgresConfig {

    private static Properties props;

    private PostgresConfig() {
    }

    static {
        loadProperties();
    }

    private static void loadProperties() {
        props = new Properties();
        try (InputStream input = PostgresConfig.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new DatabaseConfigException("Arquivo database.properties não encontrado");
            }
            props.load(input);
            validateRequiredProperties();
            log.info("Propriedades do banco carregadas com sucesso");
        } catch (IOException e) {
            log.error("Erro ao carregar propriedades do banco", e);
            throw new DatabaseConfigException("Erro ao carregar as propriedades do banco", e);
        }
    }

    private static void validateRequiredProperties() {
        String[] requiredProps = {"db.url", "db.username", "db.password"};
        for (String prop : requiredProps) {
            if (props.getProperty(prop) == null || props.getProperty(prop).trim().isEmpty()) {
                throw new DatabaseConfigException("Propriedade obrigatória não encontrada: " + prop);
            }
        }
        log.debug("Todas as propriedades obrigatórias estão presentes");
    }

    public static Connection getConnection() throws SQLException {
        try {
            log.debug("Criando conexão com o banco de dados PostgreSQL");
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.username"),
                    props.getProperty("db.password")
            );
        } catch (SQLException e) {
            log.error("Erro ao conectar com PostgreSQL: {}", e.getMessage(), e);
            throw new SQLException("Erro ao conectar com PostgreSQL: " + e.getMessage(), e);
        }
    }
}