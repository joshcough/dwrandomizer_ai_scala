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

  def addWriteListener(address: Address)(f: Int => Event): Unit =
    api.addAccessPointListener(
      (accessPointType: Int, address: Int, newValue: Int) => {
        eventsQueue.put(f(newValue)); newValue
      },
      AccessPointType.PostWrite,
      address.value
    )

  def addExecuteListener(address: Address)(f: Int => Event): Unit =
    api.addAccessPointListener(
      (_accessPointType: Int, _address: Int, newValue: Int) => { eventsQueue.put(f(newValue)); newValue },
      AccessPointType.PostExecute,
      address.value
    )

  def addExecuteListenerM(address: Address)(f: Int => IO[Event]): Unit =
    api.addAccessPointListener(
      (_accessPointType: Int, _address: Int, newValue: Int) => {
        // TODO: we probably have to figure out a better way to do this than just unsafeRunSync
        eventsQueue.put(f(newValue).unsafeRunSync())
        newValue
      },
      AccessPointType.PostExecute,
      address.value
    )

  addWriteListener(Address(0x45))(MapChange)
  addWriteListener(Address(0xd8))(WindowXCursor)
  addWriteListener(Address(0xd9))(WindowYCursor)

  addExecuteListenerM(Address(0xe4df))(_ => getEnemyId.map(enemyId => BattleStarted(Enemy.enemiesMap(enemyId))))

  addExecuteListener(Address(0xefc8))(_ => EnemyRun)
  addExecuteListener(Address(0xe8a4))(_ => PlayerRunSuccess)
  addExecuteListener(Address(0xe89d))(_ => PlayerRunFailed)
  addExecuteListener(Address(0xca83))(_ => EndRepelTimer)
  // LEA90:  LDA #MSC_LEVEL_UP ;Level up music.
  addExecuteListener(Address(0xea90))(_ => LevelUp)
  // LCDE6:  LDA #$00 ;Player is dead. set HP to 0.
  addExecuteListener(Address(0xcdf8))(_ => DeathBySwamp)
  addExecuteListener(Address(0xe98f))(_ => EnemyDefeated)
  // PlayerHasDied: LED9C:  LDA #MSC_DEATH ;Death music.
  addExecuteListener(Address(0xed9c))(_ => PlayerDefeated)
  addExecuteListener(Address(0xcf5a))(_ => OpenCmdWindow)
  addExecuteListener(Address(0xcf6a))(_ => CloseCmdWindow)

  def getFrameCount: IO[Int] = IO(api.getFrameCount)
  def getLocation: IO[Point] = memory.getLocation

  def printGamePad(): IO[Unit] = {
    val buttons = List(A, B, Up, Down, Left, Right, Select, Start)
    Logging.log(buttons.zip(buttons.map { b => api.readGamepad(0, b.underlying) }))
  }

  def advanceOneFrame: StateT[IO, Game, Int] = StateT.liftF(IO(frameQueue.take()))

  def pollEvent: Option[Event] = Option(eventsQueue.poll())

  def getPlayerData: IO[PlayerData] = memory.getPlayerData
  def getLevels: IO[Seq[Level]]     = memory.getLevels
  def getEnemyId: IO[EnemyId]       = memory.getEnemyId
}
