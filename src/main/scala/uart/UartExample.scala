package uart

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import dataclass.data

class Uart_Clock_Generator extends Module {
    val io = IO(new Bundle{
        val out = Output(Bool())
        val rx_data = Output(UInt(8.W))
        val rx_valid = Output(Bool())
        val rx_state = Output(UInt(4.W))
        val rx_cnt = Output(UInt(log2Ceil(125000000/115200).W))
        val rx_bit_cnt = Output(UInt(3.W))
    })
    io.rx_bit_cnt := 0.U
    io.rx_cnt := 0.U
    io.rx_state := 0.U
    //after shakehands, switch to next
    val dataReg = RegInit(0.U(8.W))
    val next_sig = Wire(Bool())
    when(next_sig){
        dataReg := dataReg + 1.U
    }
    val tx_dut = Module(new UartTx(125000000,115200))
    //expose debug signals
    tx_dut.io.in.valid := true.B
    tx_dut.io.in.bits := dataReg
    next_sig := tx_dut.io.in.ready
    //for rx
    val rx_dut = Module(new UartRx(125000000,115200))
    rx_dut.io.rxd := tx_dut.io.txd
    io.out := tx_dut.io.txd
    io.rx_data := rx_dut.io.out_bits
    io.rx_valid := rx_dut.io.out_valid
    io.rx_state := rx_dut.io.rx_state
    io.rx_cnt := rx_dut.io.rx_cnt
    io.rx_bit_cnt := rx_dut.io.rx_bit_cnt
}

object Uart_Clock_Generator extends App {
    ChiselStage.emitSystemVerilogFile(
    new Uart_Clock_Generator,
    Array("--target-dir", "build/uart_clock_generator"),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable")
  )
}