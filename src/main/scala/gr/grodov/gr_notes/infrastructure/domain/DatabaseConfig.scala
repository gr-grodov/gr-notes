package gr.grodov.gr_notes.infrastructure.domain

final case class DatabaseConfig(
   url: String,
   username: String,
   password: String,
   driver: String,
   maxPoolSize: Int,
   migrationPath: String
)