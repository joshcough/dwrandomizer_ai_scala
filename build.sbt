name := "DWR_AI"
version := "0.1"
scalaVersion := "2.13.10"
mainClass := Some("com.joshcough.dwrai.DWRAI")

assembly / assemblyExcludedJars := {
  (assembly / fullClasspath).value filter (_.data.getName == "Nintaco.jar")
}
