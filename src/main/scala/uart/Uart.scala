package uart

import chisel3._
import chisel3.util._

class UartTx(freq:Int = 125000000, baud:Int = 1500000 ) extends Module {
  val io = IO(new Bundle {
    val txd = Output(Bool())
    val in = Flipped(Decoupled(UInt(8.W)))
    val state = Output(Bool())
    val cnt = Output(UInt(log2Ceil(freq/baud).W))
    val sub_state = Output(UInt(4.W))
    val post_data = Output(UInt(10.W))
  })
  val state = RegInit(false.B) 
  io.state := state 
  //false -> idle
  //true -> sending
  io.in.ready := false.B
  io.txd := true.B // idle state is high
  val sub_state = RegInit(0.U(4.W)) // 0 -> start, 1-8 -> data bits, 9 -> stop
  io.sub_state := sub_state
  val max_cycle = freq/baud
  val cnt = RegInit(0.U(log2Ceil(freq/baud).W))
  io.cnt := cnt
  val postReg = RegInit(0xff.U(10.W))
  io.post_data:=postReg
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
    val rx_state = Output(UInt(4.W))
    val rx_cnt = Output(UInt(log2Ceil(freq/baud).W))
    val rx_bit_cnt = Output(UInt(3.W))
  })
  object state_t extends ChiselEnum {
    val idle, start, data, stop,done = Value
  }
  import state_t._
  val max_cycle = freq/baud
  val state = RegInit(idle)
  val bit_cnt = RegInit(0.U(3.W)) 
  val cnt = RegInit(0.U(log2Ceil(freq/baud).W))
  //val post_data = RegInit(0xff.U(8.W))
  val data_bar = RegInit(VecInit(Seq.fill(8)(true.B)))
  val vote0 = RegInit(true.B)
  val vote1 = RegInit(true.B)
  val vote2 = RegInit(true.B)
  val bit_valid = Wire(Bool())
  val bit_after_vote = Wire(Bool())
  bit_valid := (cnt > (3*max_cycle/4).U)
  bit_after_vote := ( (false.B ## vote0).asUInt +
    (false.B ## vote1).asUInt +
    (false.B ## vote2).asUInt ).asUInt   >= 2.U
  val rxd_after_sample = RegNext(RegNext(io.rxd)) // 2 cycle delay to detect negedge
  io.out_valid := false.B
  io.out_bits := 0.U
  io.rx_bit_cnt := bit_cnt
  io.rx_cnt := cnt
  io.rx_state := state.asUInt
  when(state === idle) {
    when(rxd_after_sample === false.B) {
      state := start
    }.otherwise {
      state := idle
    }
    cnt := 0.U
    vote0 := true.B
    vote1 := true.B
    vote2 := true.B
  }.elsewhen(state === start) {
    when(cnt === (max_cycle>>1).U){
      when(rxd_after_sample === false.B) {
        //really start
        bit_cnt := 0.U
        data_bar := VecInit(Seq.fill(8)(false.B)) 
        cnt := cnt + 1.U
      }.otherwise{
        state := idle
        bit_cnt := 0.U
        data_bar := VecInit(Seq.fill(8)(false.B))
        cnt := 0.U
      }
    }.elsewhen(cnt === (max_cycle-1).U) {
      cnt := 0.U
      state := data
    }.otherwise{
      cnt := cnt + 1.U
    }
  }.elsewhen(state === data || state === stop) {
    when(cnt === (max_cycle/4).U) {
      vote0 := rxd_after_sample
      cnt := cnt + 1.U
    }.elsewhen(cnt === (max_cycle/2).U) {
      vote1 := rxd_after_sample
      cnt := cnt + 1.U
    }.elsewhen(cnt === (3*max_cycle/4).U) {
      vote2 := rxd_after_sample
      cnt := cnt + 1.U
    }.elsewhen(cnt === (max_cycle-1).U) {
      cnt := 0.U
      when(bit_valid){
        when(state === data) {
          //post_data(bit_cnt) := bit_after_vote
          data_bar(bit_cnt) := bit_after_vote
          when(bit_cnt === 7.U) {
            //data bits done
            state := stop
            bit_cnt := 0.U
          }.otherwise {
            bit_cnt := bit_cnt + 1.U
          }
        }.elsewhen(state === stop) {
          when(bit_after_vote === true.B) {
            //really stop
            state := done
          }.otherwise{
            state := idle
          }
        }
      }.otherwise{
        state := idle
        bit_cnt := 0.U
        data_bar := VecInit(Seq.fill(8)(false.B))
        cnt := 0.U
      }
    }.otherwise{
      cnt := cnt + 1.U
    }
  }.elsewhen(state === done) {
    state := idle
    cnt := 0.U
    bit_cnt := 0.U
    data_bar := VecInit(Seq.fill(8)(false.B))
  }.otherwise{
    state := idle
    cnt := 0.U
    bit_cnt := 0.U
  }
  //output logic
  io.out_bits := data_bar.asUInt
  io.out_valid := state === done
}