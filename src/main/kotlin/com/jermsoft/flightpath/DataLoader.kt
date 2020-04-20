package com.jermsoft.flightpath

import java.io.File

class DataLoader {

    companion object {

        fun fromFile(file: File): List<Airport> {
            var ret: MutableList<Airport> = mutableListOf()
            file.readLines().map {  }
            file.forEachLine {
                val splits = it.split("\t")
                if (splits.size != 3) {
                    print("ignoring line $it")
                    return@forEachLine
                }

                val airport = Airport(name = splits[0], point = Point(splits[1].toDouble(), splits[2].toDouble()))
                ret.add(airport)
            }

            return ret
        }

    }
}
