package com.joshcough.dwrai

import cats.data.StateT
import cats.effect.IO
import com.joshcough.dwrai.Button.{A, B, Down, Left, Right, Select, Start, Up}
import com.joshcough.dwrai.Event._
import nintaco.api.{API, AccessPointType}

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

case class Machine(private val api: API) {

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

  def addExecuteListener(address: Address)(e: Event): Unit =
    api.addAccessPointListener(
      (_accessPointType: Int, _address: Int, newValue: Int) => { eventsQueue.put(e); newValue },
      AccessPointType.PostExecute,
      address.value
    )

  addWriteListener(Address(0x45))(MapChange)
  addWriteListener(Address(0xd8))(WindowXCursor)
  addWriteListener(Address(0xd9))(WindowYCursor)

  addExecuteListener(Address(0xe4df))(BattleStarted)
  addExecuteListener(Address(0xefc8))(EnemyRun)
  addExecuteListener(Address(0xe8a4))(PlayerRunSuccess)
  addExecuteListener(Address(0xe89d))(PlayerRunFailed)
  addExecuteListener(Address(0xca83))(EndRepelTimer)
  // LEA90:  LDA #MSC_LEVEL_UP ;Level up music.
  addExecuteListener(Address(0xea90))(LevelUp)
  // LCDE6:  LDA #$00 ;Player is dead. set HP to 0.
  addExecuteListener(Address(0xcdf8))(DeathBySwamp)
  addExecuteListener(Address(0xe98f))(EnemyDefeated)
  // PlayerHasDied: LED9C:  LDA #MSC_DEATH ;Death music.
  addExecuteListener(Address(0xed9c))(PlayerDefeated)
  addExecuteListener(Address(0xcf5a))(OpenCmdWindow)
  addExecuteListener(Address(0xcf6a))(CloseCmdWindow)

  val memory: Memory         = Memory(api)
  val controller: Controller = Controller(api)
  def getFrameCount: IO[Int] = IO(api.getFrameCount)
  def getLocation: IO[Point] = memory.getLocation

  def printGamePad(): IO[Unit] = {
    val buttons = List(A, B, Up, Down, Left, Right, Select, Start)
    Logging.log(buttons.zip(buttons.map { b => api.readGamepad(0, b.underlying) }))
  }

  def advanceOneFrame: StateT[IO, Game, Int] = StateT.liftF(IO(frameQueue.take()))

  def pollEvent: Option[Event] = Option(eventsQueue.poll())
}
