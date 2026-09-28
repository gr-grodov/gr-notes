package infrastructure

import com.typesafe.config.{Config, ConfigFactory}
import infrastructure.auth.OidcConfig
import infrastructure.domain.DatabaseConfig
import infrastructure.http.HttpConfig

import scala.jdk.CollectionConverters.ListHasAsScala

final case class AppConfig(http: HttpConfig, database: DatabaseConfig, oidc: OidcConfig) {
}

object AppConfig {
    def apply(config: Config = ConfigFactory.load()): AppConfig = {
        val http = HttpConfig(
            interface = config.getString("http.interface"),
            port = config.getInt("http.port")
        )

        val db = DatabaseConfig(
            url = config.getString("db.url"),
            username = config.getString("db.username"),
            password = config.getString("db.password"),
            driver = config.getString("db.driver"),
            maxPoolSize = config.getInt("db.max_pool_size"),
            migrationPath = config.getString("db.migration_path")
        )

        val oidc = OidcConfig(
            issuer = config.getString("oidc.issuer").stripSuffix("/"),
            clientId = config.getString("oidc.client_id"),
            clientSecret = config.getString("oidc.client_secret"),
            redirectUri = config.getString("oidc.redirect_uri").stripSuffix("/"),
            scopes = config.getStringList("oidc.scopes").asScala.toList
        )

        AppConfig(http, db, oidc)
    }
}
