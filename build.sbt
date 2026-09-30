ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.18"

val akkaVersion     = "2.8.8"
val akkaHttpVersion = "10.5.3"
val circeVersion    = "0.14.16"

val slickVersion    = "3.6.1"
val postgresqlVersion = "42.7.13"
val flywayVersion = "13.8.0"

lazy val root = (project in file("."))
  .settings(
    name := "gr-notes",

    libraryDependencies ++= Seq(
      // Akka
      "com.typesafe.akka"   %% "akka-actor-typed"             % akkaVersion,
      "com.typesafe.akka"   %% "akka-stream"                  % akkaVersion,
      "com.typesafe.akka"   %% "akka-http"                    % akkaHttpVersion,
      "com.typesafe.akka"   %% "akka-http-spray-json"         % akkaHttpVersion,
      "de.heikoseeberger"   %% "akka-http-circe"              % "1.39.2",

      // Json
      "io.circe"            %% "circe-core"                   % circeVersion,
      "io.circe"            %% "circe-generic"                % circeVersion,
      "io.circe"            %% "circe-parser"                 % circeVersion,

      // Database
      "com.typesafe.slick"  %% "slick"                        % slickVersion,
      "com.typesafe.slick"  %% "slick-hikaricp"               % slickVersion,
      "org.postgresql"       % "postgresql"                   % postgresqlVersion,
      // Migrations
      "org.flywaydb"         % "flyway-core"                  % "13.8.0",
      "org.flywaydb"         % "flyway-database-postgresql"   % "13.8.0",

      // JWT (OIDC & OAUTH)
      "com.nimbusds"         % "nimbus-jose-jwt"              % "10.10",
      "com.nimbusds"         % "oauth2-oidc-sdk"              % "10.10",
      // Logging
      "ch.qos.logback"       % "logback-classic"              % "1.6.4",

      // Testing
      "com.typesafe.akka"   %% "akka-actor-testkit-typed"     % akkaVersion       % Test,
      "com.typesafe.akka"   %% "akka-http-testkit"            % akkaHttpVersion   % Test,
      "org.scalatest"       %% "scalatest"                    % "3.2.20"          % Test
    )
  )
