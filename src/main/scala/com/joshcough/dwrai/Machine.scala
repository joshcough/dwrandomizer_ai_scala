package com.joshcough.dwrai

import cats.data.StateT
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.joshcough.dwrai.Button.{A, B, Down, Left, Right, Select, Start, Up}
import com.joshcough.dwrai.Event._
import nintaco.api.{API, AccessPointType}

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

case class Machine(private val api: API) {

  val memory: Memory         = Memory(api)
  val controller: Controller = Controller(api)

  val frameQueue: BlockingQueue[Int] = new ArrayBlockingQueue[Int](10000)
  api.addFrameListener(() => frameQueue.put(api.getFrameCount))

  val eventsQueue: BlockingQueue[Event] = new ArrayBlockingQueue[Event](10000)

  def cheat: IO[Unit] = memory.writeRAM(RamAddress(0xbe), 255.toByte)

  def addWriteListener(address: RamAddress)(f: Int => Event): Unit =
    api.addAccessPointListener(
      (accessPointType: Int, address: Int, newValue: Int) => {
        eventsQueue.put(f(newValue)); newValue
      },
      AccessPointType.PostWrite,
      address.value
    )

  def addExecuteListener(address: RamAddress)(f: Int => Event): Unit =
    api.addAccessPointListener(
      (_accessPointType: Int, _address: Int, newValue: Int) => {
        eventsQueue.put(f(newValue)); newValue
      },
      AccessPointType.PostExecute,
      address.value
    )

  def addExecuteListenerM(address: RamAddress, apt: Int = AccessPointType.PostExecute)(
      f: Int => IO[Event]
  ): Unit =
    api.addAccessPointListener(
      (_accessPointType: Int, _address: Int, newValue: Int) => {
        // TODO: we probably have to figure out a better way to do this than just unsafeRunSync
        eventsQueue.put(f(newValue).unsafeRunSync())
        newValue
      },
      apt,
      address.value
    )

  addWriteListener(RamAddress(0x45))(MapChange)
  addWriteListener(RamAddress(0xd8))(WindowXCursor)
  addWriteListener(RamAddress(0xd9))(WindowYCursor)

  addExecuteListenerM(RamAddress(0xe4df))(_ =>
    getEnemyId.map(enemyId => BattleStarted(Enemy.enemiesMap(enemyId)))
  )

  addExecuteListener(RamAddress(0xefc8))(_ => EnemyRun)
  addExecuteListener(RamAddress(0xe8a4))(_ => PlayerRunSuccess)
  addExecuteListener(RamAddress(0xe89d))(_ => PlayerRunFailed)
  addExecuteListener(RamAddress(0xca83))(_ => EndRepelTimer)
  // LEA90:  LDA #MSC_LEVEL_UP ;Level up music.
  addExecuteListener(RamAddress(0xea90))(_ => LevelUp)
  addExecuteListener(RamAddress(0xeb18))(_ => DoneLevelingUp)
  // LCDE6:  LDA #$00 ;Player is dead. set HP to 0.
  addExecuteListener(RamAddress(0xcdf8))(_ => DeathBySwamp)
  addExecuteListener(RamAddress(0xe98f))(_ => EnemyDefeated)
  // PlayerHasDied: LED9C:  LDA #MSC_DEATH ;Death music.
  addExecuteListener(RamAddress(0xed9c))(_ => PlayerDefeated)
  addExecuteListener(RamAddress(0xcf5a))(_ => OpenCmdWindow)
  addExecuteListener(RamAddress(0xcf6a))(_ => CloseCmdWindow)
  addExecuteListener(RamAddress(0xee90))(_ => FightEnded)
  addExecuteListener(RamAddress(0xc6f0))(_ => WindowOpened)
  addExecuteListener(RamAddress(0xa7a2))(_ => WindowRemoved)

  def getFrameCount: IO[Int] = IO(api.getFrameCount)
  def getLocation: IO[Point] = memory.getLocation

  def printGamePad(): IO[Unit] = {
    val buttons = List(A, B, Up, Down, Left, Right, Select, Start)
    Logging.log(buttons.zip(buttons.map { b => api.readGamepad(0, b.underlying) }))
  }

  def advanceOneFrame: StateT[IO, Game, Int] = StateT.liftF(IO(frameQueue.take()))

  def pollEvent: Option[Event] = Option(eventsQueue.poll())

  def getPlayerData: IO[PlayerData]          = memory.getPlayerData
  def getLevels: IO[Seq[Level]]              = memory.getLevels
  def getEnemyId: IO[EnemyId]                = memory.getEnemyId
  def setEnemyId(enemyId: EnemyId): IO[Unit] = memory.setEnemyId(enemyId.value)
}
