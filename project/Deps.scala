import sbt._

object Deps {
  val MunitCatsEffectVersion = "1.0.7"
  val CatsEffectVersion      = "3.3.11"
  val RefinedVersion         = "0.9.29"

  val deps = Seq(
    "eu.timepit"    %% "refined"            % RefinedVersion,
    "eu.timepit"    %% "refined-pureconfig" % RefinedVersion,
    "org.typelevel" %% "cats-effect"        % CatsEffectVersion
  )
}
