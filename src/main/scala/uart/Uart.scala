package uart

import chisel3._
import chisel3.util._

class UartTx(freq:Int = 125000000, baud:Int = 1500000 ) extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val in = Flipped(Decoupled(UInt(8.W)))
  })
  val state = RegInit(false.B) 
  //false -> idle
  //true -> sending
  io.in.ready := false.B
  io.txd := true.B // idle state is high
  val sub_state = RegInit(0.U(4.W)) // 0 -> start, 1-8 -> data bits, 9 -> stop
  val max_cycle = freq/baud
  val cnt = RegInit(0.U(log2Ceil(freq/baud).W))
  val postReg = RegInit(0xff.U(10.W))
  when(state === false.B) {
    //IDLE
    io.in.ready := true.B
    io.txd := true.B // idle state is high
    when(io.in.valid){
      //get data, and will post
      postReg := true.B ## io.in.bits ## false.B // start bit + data bits + stop bit
      state := true.B
      sub_state := 0.U
      //start count clock
      cnt := 0.U
    }.otherwise {
      state := false.B
      sub_state := 0xff.U
      cnt := 0.U
    } 
  }.elsewhen(state === true.B) {
    //SENDING
    io.txd := postReg(sub_state)
    when(cnt === (max_cycle-1).U) {
      //state changes
      when(sub_state === 9.U) {
        //can get data
        io.in.ready := true.B
        when(io.in.valid) {
          postReg := true.B ## io.in.bits ## false.B // start bit + data bits + stop bit
          sub_state := 0.U
          state := true.B
        }.otherwise{
          //switch to idle
          state := false.B
          sub_state := 0xff.U
        }
      }.elsewhen(sub_state <= 8.U) {
        io.txd := postReg(sub_state)
        sub_state := sub_state + 1.U
        io.in.ready := false.B
      }
      cnt := 0.U
    }.otherwise{
      cnt := cnt + 1.U
      io.in.ready := false.B
    }
  }
}

class UartRx(freq: Int, baud: Int) extends Module {
  val io = IO(new Bundle {
    val rxd = Input(Bool())
    val out_bits = Output(UInt(8.W))
    val out_valid = Output(Bool())
  })

  object State extends ChiselEnum {
    val idle,start,data,stop,done = Value
  }
  import State._
  val stateReg = RegInit(idle)
  val max_cycle = freq/baud
  val cnt = RegInit(0.U(log2Ceil(freq/baud).W))
  val dataReg = RegInit(0.U(8.W))
  val dataCnt = RegInit(0.U(8.W))
  val Rxin = RegNext(RegNext(io.rxd))
  val trigger = Wire(Bool())
  val sample = Vec(3,Bool())
  val bit_after_vote = Wire(Bool())
  val num_of_high = Wire(UInt(2.W))
  bit_after_vote := true.B
  sample(0) := true.B
  sample(1) := true.B
  sample(2) := true.B
  trigger := false.B
  trigger := Rxin === false.B
  when(stateReg === idle) {
    when(trigger) {
      stateReg := start
    }.otherwise{
      stateReg := idle
    }
    cnt := 0.U
    dataCnt := 0.U
    sample(0) := true.B
    sample(1) := true.B
    sample(2) := true.B
    bit_after_vote := true.B
    num_of_high := 3.U
  }.elsewhen(stateReg === start || stateReg === data) {
    switch(cnt) {
      is((max_cycle/4).U){
        sample(0) := Rxin
      }
      is((max_cycle/2).U){
        sample(1) := Rxin
      }
      is((3*max_cycle/4).U){
        sample(2) := Rxin
      }
      is((max_cycle-2).U){
        //vote
        num_of_high := (false.B ## sample(0)).asUInt + (false.B ## sample(1)).asUInt + (false.B ## sample(2)).asUInt
        when(num_of_high >= 2.U) {
          bit_after_vote := true.B
        }.otherwise {
          bit_after_vote := false.B
        }
      }
    }
    when(cnt === (max_cycle-1).U) {
      cnt := 0.U
      when(stateReg === start){
        when(bit_after_vote === false.B) {
            //indeedly start bit
          stateReg := data
          dataCnt := 1.U
          bit_after_vote := true.B
          num_of_high := 3.U
        }.otherwise(
          //switch back to idle
          stateReg := idle
        )
      }.elsewhen(stateReg === data){
        switch(dataCnt) {
          is(0x1.U){
            dataReg := false.B ## bit_after_vote
            dataCnt := 0x2.U
          }
          is(0x2.U) {
            dataReg := false.B ## bit_after_vote ## dataReg(0)
            dataCnt := 0x4.U 
          }
          is(0x4.U) {
            dataReg := false.B ## bit_after_vote ## dataReg(1,0)
            dataCnt := 0x8.U
          }
          is(0x8.U) {
            dataReg := false.B ## bit_after_vote ## dataReg(2,0)
            dataCnt := 0x10.U
          }
          is(0x10.U) {
            dataReg := false.B ## bit_after_vote ## dataReg(3,0)
            dataCnt := 0x20.U
          }
          is(0x20.U) {
            dataReg := false.B ## bit_after_vote ## dataReg(4,0)
            dataCnt := 0x40.U
          }
          is(0x40.U) {
            dataReg := false.B ## bit_after_vote ## dataReg(5,0)
            dataCnt := 0x80.U
          }
          is(0x80.U) {
            dataReg := bit_after_vote ## dataReg(6,0)
            dataCnt := 0x0.U
            stateReg := stop
          }
        }
      }
    }.otherwise{
      cnt := cnt + 1.U
    }
  }.elsewhen(stateReg === stop) {
    switch(cnt){
      is((max_cycle/4).U){
        sample(0) := Rxin
      }
      is((max_cycle/2).U){
        sample(1) := Rxin
      }
      is((3*max_cycle/4).U){
        sample(2) := Rxin
        num_of_high := (false.B ## sample(0)).asUInt + (false.B ## sample(1)).asUInt + (false.B ## sample(2)).asUInt
        when(num_of_high >= 2.U) {
          bit_after_vote := true.B
        }.otherwise {
          bit_after_vote := false.B
        }
      }      
    }
    when(cnt === (3*max_cycle/4 + 1).U) {
      when(bit_after_vote === true.B) {
        stateReg := done
      }.otherwise {
        stateReg := idle
      }
    }.otherwise(
      cnt := cnt + 1.U
    )
  }.elsewhen(stateReg === done) {
    stateReg := idle
  }
  io.out_bits := dataReg
  io.out_valid := stateReg === done
}