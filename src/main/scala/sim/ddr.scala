package sim
import common.NumAndUnits._
import scala.language.postfixOps


object DDR {
    val ddrsize:Int = 512 MB
}

object SimBUS{
    val mem_Map = Map(
        0x8000_0000L -> DDR
    )
}

object Console extends App {
    println(SimBUS.mem_Map)
    println(s"ddr load at 0x8000_0000 with size ${SimBUS.mem_Map(0x8000_0000L).ddrsize}")
}