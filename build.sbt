import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scalaJsDomV = "0.9.5"
val akkaHttpV = "10.1.1"
val akkaV = "2.5.12"
val scalaV = "2.12.6"

lazy val commonSettings = Seq(version := "0.1", scalaVersion := scalaV)

resolvers += Resolver.sonatypeRepo("snapshots")

lazy val frontend = project.in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomV,
      "org.querki" %%% "jquery-facade" % "1.2",
      "com.lihaoyi" %% "upickle" % "0.5.1"
    ),
    scalaJSUseMainModuleInitializer := true
  ).dependsOn(sharedJS)

lazy val backend = project.in(file("backend"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-stream" % akkaV,
      "com.lihaoyi" %% "upickle" % "0.5.1"
    ),
    resourceGenerators in Compile += Def.task {
      val f1 = (fastOptJS in Compile in frontend).value
      val f1SourceMap = f1.data.getParentFile / (f1.data.getName + ".map")
      Seq(f1.data, f1SourceMap)
    }.taskValue,
    watchSources ++= (watchSources in frontend).value
  ).dependsOn(sharedJVM)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.5.1"
    )
  )

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js