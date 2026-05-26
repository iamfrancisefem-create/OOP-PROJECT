package com.pms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && !databaseUrl.isBlank()) {
            URI dbUri = new URI(databaseUrl);
            String[] userInfo = dbUri.getUserInfo().split(":");
            String username = userInfo[0];
            String password = userInfo.length > 1 ? userInfo[1] : "";
            String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

            return DataSourceBuilder.create()
                    .type(HikariDataSource.class)
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        // Fall back to environment vars or defaults matching application.yml
        String url = env("SPRING_DATASOURCE_URL", "jdbc:postgresql://localhost:5432/project_management");
        String username = env("SPRING_DATASOURCE_USERNAME", "DB_USERNAME", "postgres");
        String password = env("SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD", "postgres");

        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }

    private static String env(String... keys) {
        for (int i = 0; i < keys.length - 1; i++) {
            String val = System.getenv(keys[i]);
            if (val != null && !val.isBlank()) return val;
        }
        return keys[keys.length - 1];
    }
}
