package infrastructure.domain

import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile

object AppDatabase {

    def migrate(config: DatabaseConfig): Int = {
        val flyway = Flyway
            .configure()
            .dataSource(config.url, config.username, config.password)
            .locations(config.migrationPath)
            .load()
        flyway.migrate().migrationsExecuted
    }

    def byConfig(config: DatabaseConfig): PostgresProfile.backend.Database =
        PostgresProfile.backend.Database.forURL(
            url = config.url,
            user = config.username,
            password = config.password,
            driver = config.driver
        )
}
