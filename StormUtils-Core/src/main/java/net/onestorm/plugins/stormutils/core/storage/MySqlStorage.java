package net.onestorm.plugins.stormutils.core.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.onestorm.library.configuration.Configuration;
import net.onestorm.plugins.stormutils.api.UtilPlugin;

public class MySqlStorage extends SqlStorage {

    public MySqlStorage(UtilPlugin plugin) {
        Configuration configuration = plugin.getConfiguration();

        String host = configuration.getString("database.host").orElse("127.0.0.1");
        String port = configuration.getString("database.port").orElse("3306");
        String name = configuration.getString("database.name").orElse("storm");
        String username = configuration.getString("database.username").orElse("root");
        String password = configuration.getString("database.password").orElse("");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8",
                host, port, name));
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikari = new HikariDataSource(hikariConfig);

        createTables();
    }


    @Override
    public void close() {
        hikari.close();
        super.close(); // closes executor
    }
}
