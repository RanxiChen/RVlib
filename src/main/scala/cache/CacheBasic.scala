package cache


object CacheConfig {
    case class DirectMappedCacheConfig(AddressWidth:Int = 32,IndexWidth:Int =6,BlockOffsetWidth:Int = 5,TagWidth:Int = 21){
        require(AddressWidth == IndexWidth + BlockOffsetWidth + TagWidth,
            s"AddressWidth($AddressWidth) should be equal to IndexWidth($IndexWidth) + BlockOffsetWidth($BlockOffsetWidth) + TagWidth($TagWidth)"
        )
        val DataBlockSize:Int = 1 << BlockOffsetWidth
        val CachelineSize:Int = 1 << IndexWidth
        val CacheSetSize:Int = 1 << IndexWidth
        val CacheSize:Int = CachelineSize*DataBlockSize
        val CacheSizeHumanReadable = {
            if(CacheSize < 1024){
                f"${CacheSize} B"
            }else if(CacheSize < 1024*1024){
                f"${CacheSize/1024}KB"
            }else if(CacheSize < 1024*1024*1024){
                f"${CacheSize/1024/1024}MB"
            }else{
                f"Too Big Size"
            }
        }
        override def toString(): String = {
            s"""DirectedMappedCache:
                |AddressWidth: $AddressWidth
                |DataBlockSize: $DataBlockSize bytes
                |CachelineSize: $CachelineSize
                |CacheSize: $CacheSizeHumanReadable""".stripMargin
        }        
    }
    val exampleDirectedMappedCacheConfig = DirectMappedCacheConfig(AddressWidth = 32,IndexWidth = 6,BlockOffsetWidth = 5,TagWidth = 21)
    case class SetAssociativeCacheConfig(AddressWidth:Int = 32,SetWays:Int = 2,IndexWidth:Int =6,BlockOffsetWidth:Int = 5,TagWidth:Int = 21){
        require(AddressWidth == IndexWidth + BlockOffsetWidth + TagWidth,
            s"AddressWidth($AddressWidth) should be equal to IndexWidth($IndexWidth) + BlockOffsetWidth($BlockOffsetWidth) + TagWidth($TagWidth)"
        )
        require(SetWays > 1,s"SetWays($SetWays) should be greater than 1")
        val DataBlockSize:Int = 1 << BlockOffsetWidth
        val CacheSetSize:Int = 1 << IndexWidth
        val CachelineSize:Int = CacheSetSize*SetWays
        val CacheSize:Int = CachelineSize*DataBlockSize
        val CacheSizeHumanReadable = {
            if(CacheSize < 1024){
                f"${CacheSize} B"
            }else if(CacheSize < 1024*1024){
                f"${CacheSize/1024}KB"
            }else if(CacheSize < 1024*1024*1024){
                f"${CacheSize/1024/1024}MB"
            }else{
                f"Too Big Size"
            }
        }
        override def toString(): String = {
            s"""SetAssociativeCache:
                |AddressWidth: $AddressWidth
                |SetWays: $SetWays
                |DataBlockSize: $DataBlockSize bytes
                |CachelineSize: $CachelineSize
                |CacheSize: $CacheSizeHumanReadable""".stripMargin
        }        
    }

    val exampleSetAssociativeCacheConfig = SetAssociativeCacheConfig(AddressWidth = 32,SetWays = 2,IndexWidth = 6,BlockOffsetWidth = 5,TagWidth = 21)

}


object CacheBasic extends App {
    println("Hello World")
    println("There is one directed mapped cache")
    println(CacheConfig.exampleDirectedMappedCacheConfig)
    println("There is one set associative cache")
    println(CacheConfig.exampleSetAssociativeCacheConfig)
}