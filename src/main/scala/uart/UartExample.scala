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
    val dataReg = RegInit(0.U(8.W))
    dataReg := 0xff.U
    val TxModule = Module(new UartTx(freq =125000000, baud = 115200))
    io.tx_in_ready := TxModule.io.in.ready
    io.tx_in_valid := TxModule.io.in.valid
    io.tx_in_bits := TxModule.io.in.bits
    io.tx_state := TxModule.io.state
    io.tx_sub_state := TxModule.io.sub_state
    io.tx_post_data := TxModule.io.post_data
    io.tx_cnt := TxModule.io.cnt
    TxModule.io.in.valid := true.B
    io.out := TxModule.io.txd
    TxModule.io.in.bits := 0xff.U
    when(TxModule.io.in.ready){
        dataReg := dataReg + 1.U
        TxModule.io.in.bits := dataReg + 1.U
    }
}

object Uart_Clock_Generator extends App {
    ChiselStage.emitSystemVerilogFile(
    new Uart_Clock_Generator,
    Array("--target-dir", "build/uart_clock_generator"),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable")
  )
}