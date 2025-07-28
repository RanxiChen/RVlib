package uart

import chisel3._
import chisel3.util._

class UartTx(freq:Int = 125000000, baud:Int = 1500000 ) extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val in = Flipped(Decoupled(UInt(8.W)))
  })
  object State extends ChiselEnum {
    val IDLE,DATA = Value
  }
  import State._
  val max_cycle = freq/baud

  val cnt = RegInit(0.U(log2Ceil(max_cycle).W))
  val state = RegInit(IDLE)
  val data = RegInit(0.U(8.W))
  io.txd := true.B
  val data_cnt = RegInit(0xff.U(8.W))
  val trigger = Wire(Bool())
  //0xff for stop bits
  //0x00 for start bits
  //other is one-hot

  when(state === IDLE) {
    when(io.in.fire) {
      // switch to data state
      state := DATA
      data := io.in.bits
      data_cnt := 0x00.U
    }.otherwise {
      state := IDLE
    }
    cnt := 0.U
  }.elsewhen(state === DATA) {
    def next_data_cnt(cnt4data:UInt):UInt = {
      val res = Wire(UInt(8.W))
      res := 0xff.U
      switch(cnt4data) {
        is(0x00.U) { res := 0x01.U }
        is(0x01.U) { res := 0x02.U }
        is(0x02.U) { res := 0x04.U }
        is(0x04.U) { res := 0x08.U }
        is(0x08.U) { res := 0x10.U }
        is(0x10.U) { res := 0x20.U }
        is(0x20.U) { res := 0x40.U }
        is(0x40.U) { res := 0x80.U }
        is(0x80.U) { res := 0xff.U }
      }
      res
    }
    when(cnt === (max_cycle-1).U) {
      //sub state changes
      cnt := 0.U
      when(data_cnt =/= 0xff.U) {
        data_cnt := next_data_cnt(data_cnt)
      }.otherwise{
        when(trigger){
          state := DATA
          data_cnt := 0x00.U
        }.otherwise{
          state := IDLE
          data_cnt := 0xff.U
        }
      }
    }.otherwise{
      cnt := cnt + 1.U
    }
  }
  //Tx output logic
  when(state === IDLE){
    io.txd := true.B
  }.elsewhen(state === DATA){
    when(data_cnt === 0xff.U){
      //stop bits
      io.txd := true.B
    }.elsewhen(data_cnt === 0x00.U){
      //start bits
      io.txd := false.B
    }.elsewhen(data_cnt === 0x01.U){
      io.txd := data(0)
    }.elsewhen(data_cnt === 0x02.U){
      io.txd := data(1)
    }.elsewhen(data_cnt === 0x04.U){
      io.txd := data(2)
    }.elsewhen(data_cnt === 0x08.U){
      io.txd := data(3)
    }.elsewhen(data_cnt === 0x10.U){
      io.txd := data(4)
    }.elsewhen(data_cnt === 0x20.U){
      io.txd := data(5)
    }.elsewhen(data_cnt === 0x40.U){
      io.txd := data(6)
    }.elsewhen(data_cnt === 0x80.U){
      io.txd := data(7)
    }.otherwise{
      io.txd := true.B
    }
  }
  //hands shake logic
  val busy = Wire(Bool())
  busy := state === DATA && (data_cnt =/= 0xff.U || data_cnt === 0xff.U && cnt =/= (max_cycle-1).U)
  io.in.ready := !busy
  trigger := io.in.fire
}
