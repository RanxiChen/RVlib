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
    dataReg := 0xff.U
    val TxModule = Module(new UartTx(freq =125000000, baud = 115200))
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