package uart

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import dataclass.data

class Uart_Clock_Generator extends Module {
    val io = IO(new Bundle{
        val out = Output(Bool())
        val tx_state = Output(Bool())
        val tx_cnt = Output(UInt(32.W))
        val tx_sub_state = Output(UInt(4.W))
        val tx_post_data = Output(UInt(10.W))
        val tx_in_ready = Output(Bool())
        val tx_in_valid = Output(Bool())
        val tx_in_bits = Output(UInt(8.W))
    })
    //after shakehands, switch to next
    val dataReg = RegInit(0.U(8.W))
    val next_sig = Wire(Bool())
    when(next_sig){
        dataReg := dataReg + 1.U
    }
    val tx_dut = Module(new UartTx(125000000,115200))
    //expose debug signals
    io.tx_state := tx_dut.io.state
    io.tx_cnt := tx_dut.io.cnt
    io.tx_sub_state := tx_dut.io.sub_state
    io.tx_post_data := tx_dut.io.post_data
    io.tx_in_ready := tx_dut.io.in.ready
    io.tx_in_valid := tx_dut.io.in.valid
    io.tx_in_bits := tx_dut.io.in.bits
    io.out := tx_dut.io.txd
    tx_dut.io.in.valid := true.B
    tx_dut.io.in.bits := dataReg
    next_sig := tx_dut.io.in.ready
}

object Uart_Clock_Generator extends App {
    ChiselStage.emitSystemVerilogFile(
    new Uart_Clock_Generator,
    Array("--target-dir", "build/uart_clock_generator"),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable")
  )
}