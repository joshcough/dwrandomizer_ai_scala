package com.joshcough.dwrai

object Locations {

  sealed trait ImportantLocationType

  object ImportantLocationType {
    case object CHARLOCK    extends ImportantLocationType
    case object TANTEGEL    extends ImportantLocationType
    case object TOWN        extends ImportantLocationType
    case object CAVE        extends ImportantLocationType
    case object CHEST       extends ImportantLocationType
    case object SPIKE       extends ImportantLocationType
    case object COORDINATES extends ImportantLocationType
    case object BASEMENT    extends ImportantLocationType
  }
}
