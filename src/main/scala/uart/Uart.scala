package uart

import chisel3._
import chisel3.util._

class UartTx(freq:Int = 125000000, baud:Int = 1500000 ) extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val in = Decoupled(UInt(8.W))
  })
  object State extends ChiselEnum {
    val IDLE,DATA = Value
  }
  import State._
  val max_cycle = freq/baud

  val cnt = RegInit(0.U(log2Ceil(max_cycle).W))
  val state = RegInit(IDLE)
  val data = RegInit(0.U(8.W))
  io.txd := false.B

  when(state === IDLE) {
    when(io.in.fire) {
      // switch to data state
      state := DATA
      data := io.in.bits
    }.otherwise {
      state := IDLE
    }
    cnt := 0.U
  }.elsewhen(state === DATA) {
    ???
  }
}
