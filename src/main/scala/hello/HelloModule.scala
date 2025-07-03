package cache
import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class HelloModule extends Module {
    val io = IO(new Bundle {
        val in = Input(UInt(32.W))
        val out = Output(UInt(32.W))
    })
    io.out := RegNext(io.in)
}

object HelloModule extends App {
    ChiselStage.emitSystemVerilogFile(
        new HelloModule,
        Array("--target-dir", "build/helloModule"),
        firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info", "-default-layer-specialization=enable")
    )
}