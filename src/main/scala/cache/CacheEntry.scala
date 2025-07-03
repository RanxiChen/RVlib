package cache

import chisel3._
import chisel3.util._

class DirectedMappedCacheBone(cf:CacheConfig.DirectMappedCacheConfig) extends Module {
    val io = IO(new Bundle{
        val addr = Input(UInt(cf.AddressWidth.W))
        val hit = Output(Bool())
        val rdata = Output(UInt(8.W))
    })
    val byteMask = UIntToOH(io.addr(cf.BlockOffsetWidth-1,0),cf.DataBlockSize)
    val sramIndex = io.addr(cf.BlockOffsetWidth+cf.IndexWidth-1,cf.BlockOffsetWidth)
    val tagdata = io.addr(cf.AddressWidth-1,cf.BlockOffsetWidth+cf.IndexWidth)
    val vbitArray = Vec(cf.CachelineSize, Bool())
    val tagArray = SyncReadMem(cf.CachelineSize, UInt(cf.TagWidth.W))
    val dataArray = SyncReadMem(cf.CachelineSize, Vec(cf.DataBlockSize, UInt(8.W)))
    io.hit := vbitArray(sramIndex) && tagArray.read(sramIndex) === tagdata
    val data = Wire(UInt(8.W))
    data := dataArray.read(sramIndex)(io.addr(cf.BlockOffsetWidth-1,0))
    io.rdata := data
}

class SetAssociativeCacheBone(cf:CacheConfig.SetAssociativeCacheConfig) extends Module {
    val io = IO(new Bundle{
        val addr = Input(UInt(cf.AddressWidth.W))
        val hit = Output(Bool())
        val rdata = Output(UInt(8.W))
    })
    val byteMask = UIntToOH(io.addr(cf.BlockOffsetWidth-1,0),cf.DataBlockSize)
    val sramIndex = io.addr(cf.BlockOffsetWidth+cf.IndexWidth-1,cf.BlockOffsetWidth)
    val tagdata = io.addr(cf.AddressWidth-1,cf.BlockOffsetWidth+cf.IndexWidth)
    val vbitArray = (1 to cf.SetWays).map(_ => Vec(cf.CachelineSize, Bool()))
    val tagArray = (1 to cf.SetWays).map(_ => SyncReadMem(cf.CachelineSize, UInt(cf.TagWidth.W)))
    val dataArray = (1 to cf.SetWays).map(_ => SyncReadMem(cf.CachelineSize, Vec(cf.DataBlockSize, UInt(8.W))))
}