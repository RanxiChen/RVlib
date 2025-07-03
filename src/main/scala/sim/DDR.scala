package sim
import common.NumAndUnits._
import scala.language.postfixOps


object DDR {
    val ddrsize:Int = 512 MB
    val datablocksize:Int = 16 B
    val ddrlinessize:Int = ddrsize / datablocksize
    val ddraddrwidth:Int = Math.ceil(math.log(ddrlinessize)/math.log(2)) toInt
    val ddrinneraddrwidth:Int = ddraddrwidth - 4
    val info:String = s"""
    ddrsize: 0x${ddrsize.toHexString} bytes
    datablocksize: 0x${datablocksize.toHexString} bytes
    ddrlinessize: 0x${ddrlinessize.toHexString}
    ddraddrwidth: $ddraddrwidth
    """.stripMargin
}

object SimBUS{
    val mem_Map = Map(
        0x8000_0000L -> DDR
    )
}

object Console extends App {
    println(s"ddr load at 0x8000_0000 with size ${SimBUS.mem_Map(0x8000_0000L).ddrsize}")
    println(SimBUS.mem_Map(0x8000_0000L).info)
}