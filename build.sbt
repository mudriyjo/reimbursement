val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "reimbursement",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "com.sksamuel.scrimage" % "scrimage-core" % "4.0.32",
    libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"
  )
