package common
import scala.language.postfixOps

object NumAndUnits {
    implicit class RichInt4Unit(m:Int) {
        def B:Int = {
            require(m >= 0, "Number of bytes should be non-negative")
            m
        }
        def KB:Int = {
            require(m >= 0, "Number of kilobytes should be non-negative")
            m * 1024
        }
        def MB:Int = {
            require(m >= 0, "Number of gigabytes should be non-negative")
            m * 1024 * 1024
        }
        def GB:Int = {
            require(m >= 0, "Number of gigabytes should be non-negative")
            m * 1024 * 1024 * 1024
        }
    }
}
