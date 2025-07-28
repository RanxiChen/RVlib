package uart

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import dataclass.data

class Uart_Clock_Generator extends Module {
    val io = IO(new Bundle{
        val out = Output(Bool())
    })
    val dataReg = RegInit(0.U(8.W))
    dataReg := 0xf1.U
    val TxModule = Module(new UartTx(freq =125000000, baud = 115200))
    TxModule.io.in.valid := true.B
    TxModule.io.in.bits := dataReg
    io.out := TxModule.io.txd
}

object Uart_Clock_Generator extends App {
    ChiselStage.emitSystemVerilogFile(
    new Uart_Clock_Generator,
    Array("--target-dir", "build/uart_clock_generator"),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable")
  )
}