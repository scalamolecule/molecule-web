
val moleculeVersion = "0.26.0-SNAPSHOT"

inThisBuild(
  List(
    organization := "org.scalamolecule",
    organizationName := "ScalaMolecule",
    organizationHomepage := Some(url("http://www.scalamolecule.org")),
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.7.3",

    // Run tests for all systems sequentially to avoid data locks with db
    // Only applies on JVM. On JS platform there's no parallelism anyway.
    Test / parallelExecution := false,
  )
)

lazy val code = project.in(file("code"))
  .enablePlugins(MoleculePlugin)
  .settings(
    name := "code",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.8.4",
      //      "com.outr" %% "scribe" % "3.13.0", // Logging
      //      "org.scalactic" %% "scalactic" % "3.2.18", // Tolerant roundings with triple equal on js platform
      //      "io.github.cquiroz" %% "scala-java-time" % "2.5.0",

      //      "dev.zio" %% "zio" % zioVersion,
      //      "dev.zio" %% "zio-streams" % zioVersion,
      //      "dev.zio" %% "zio-test" % zioVersion % Test,
      //      "dev.zio" %% "zio-test-sbt" % zioVersion, // % Test // todo: why does this collide?

      //      "com.typesafe.akka" %% "akka-stream" % akkaVersion cross CrossVersion.for3Use2_13,
      //      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion cross CrossVersion.for3Use2_13,
      //      "ch.megard" %% "akka-http-cors" % "1.2.0",

      "org.scalamolecule" %% "molecule-db-h2" % moleculeVersion,
      "org.scalamolecule" %% "molecule-db-mariadb" % moleculeVersion,
      "org.scalamolecule" %% "molecule-db-mysql" % moleculeVersion,
      "org.scalamolecule" %% "molecule-db-postgresql" % moleculeVersion,
      "org.scalamolecule" %% "molecule-db-sqlite" % moleculeVersion,


      //      "com.h2database" % "h2" % "2.3.232",

      "com.dimafeng" %% "testcontainers-scala-mariadb" % "0.41.3",
      "org.mariadb.jdbc" % "mariadb-java-client" % "3.4.0",

      "org.testcontainers" % "mysql" % "1.19.8",
      "mysql" % "mysql-connector-java" % "8.0.33",

      "org.testcontainers" % "postgresql" % "1.19.8",
      "org.postgresql" % "postgresql" % "42.7.2",

      "org.xerial" % "sqlite-jdbc" % "3.46.0.0",

      "org.tpolecat" %% "doobie-core" % "1.0.0-RC8",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC8",

    ),

    Test / fork := true, // necessary for sbt testing

    testFrameworks += new TestFramework("utest.runner.Framework"),
    //    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )