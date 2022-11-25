import sbt._

object Deps {
  val MunitCatsEffectVersion = "1.0.7"
  val CatsEffectVersion      = "3.3.11"
  val RefinedVersion         = "0.9.29"

  val deps = Seq(
    "eu.timepit"    %% "refined"                 % RefinedVersion,
    "eu.timepit"    %% "refined-pureconfig"      % RefinedVersion,
    "org.typelevel" %% "cats-effect"             % CatsEffectVersion,
    "org.typelevel" %% "cats-laws"               % "2.7.0"                % "test,it",
    "org.typelevel" %% "kittens"                 % "2.3.2",
    "org.typelevel" %% "munit-cats-effect-3"     % MunitCatsEffectVersion % "test,it",
    "org.typelevel" %% "scalacheck-effect-munit" % "1.0.3"                % "test,it"
  )
}
