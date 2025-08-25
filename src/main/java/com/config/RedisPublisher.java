package com.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class RedisPublisher {

    private static JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    static {
        try {
            Properties props = new Properties();
            try (InputStream input = RedisPublisher.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                props.load(input);
            }

            String host = props.getProperty("redis.host");
            int port = Integer.parseInt(props.getProperty("redis.port"));

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            jedisPool = new JedisPool(poolConfig, host, port);
            log.info("Pool de conexões com Redis inicializado com sucesso em {}:{}", host, port);

        } catch (IOException | NumberFormatException e) {
            log.error("Falha ao inicializar o publisher do Redis", e);
            throw new RuntimeException("Não foi possível configurar a conexão com o Redis", e);
        }
    }

    public RedisPublisher() {
        this.objectMapper = new ObjectMapper();
    }
    public void publish(String topic, Object messageObject) {
        try (Jedis jedis = jedisPool.getResource()) {
            String messageJson = objectMapper.writeValueAsString(messageObject);

            jedis.publish(topic, messageJson);
            log.info("Mensagem publicada no tópico '{}': {}", topic, messageJson);

        } catch (Exception e) {
            log.error("Falha ao publicar mensagem no Redis no tópico {}", topic, e);
        }
    }

    public static void close() {
        if (jedisPool != null) {
            jedisPool.close();
            log.info("Pool de conexões com Redis fechado.");
        }
    }

}
