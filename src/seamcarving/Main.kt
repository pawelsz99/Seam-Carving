package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main(args: Array<String>) {

    var nameIn = ""
    var nameOut = ""

    for (i in args.indices) {
        when (args[i]) {
            "-in" -> try {
                nameIn = args[i + 1]
            } catch (e: ArrayIndexOutOfBoundsException) {
                println("No name in defined!")
                return
            }
            "-out" -> try {
                nameOut = args[i + 1]
            } catch (e: ArrayIndexOutOfBoundsException) {
                println("No name out defined!")
                return
            }
        }
    }

//    val img = invertImg(nameIn, nameOut)
//    val img = energyImg(nameIn, nameOut)

    val img = SeamImg(nameIn)

    val energyArray = img.energyArray()

    img.findSeam()


    // just for testing, for solution use the line below
//    ImageIO.write(img, "png", File("C:\\AndroidProjects\\Seam Carving\\Seam Carving\\task\\src\\seamcarving\\test.png"))
    ImageIO.write(img.colourSeamRed(), "png", File(nameOut))

}

class SeamImg(nameIn: String) {
    private val img: BufferedImage = ImageIO.read(File(nameIn))
    private var maxEnergyValue = 0.0
    private val energyArray = Array(img.height) { DoubleArray(img.width) }
    private val seamPixelCords = mutableListOf<Array<Int>>()

//    init {
//        println("img.width = ${img.width}")
//        println("img.height = ${img.height}")
//    }

    fun getBufferedImage(): BufferedImage {
        return img
    }

    fun energyArray(): Array<DoubleArray> {
        maxEnergyValue = 0.0

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {

                val pixelColorXL: Color = if (x == 0) {
                    Color(img.getRGB(x, y))
                } else {
                    Color(img.getRGB(x - 1, y))
                }

                val pixelColorXR: Color = if (x == img.width - 1) {
                    Color(img.getRGB(x, y))
                } else {
                    Color(img.getRGB(x + 1, y))
                }

                val pixelColorYU: Color = if (y == 0) {
                    Color(img.getRGB(x, y))
                } else {
                    Color(img.getRGB(x, y - 1))
                }

                val pixelColorYD: Color = if (y == img.height - 1) {
                    Color(img.getRGB(x, y))
                } else {
                    Color(img.getRGB(x, y + 1))
                }

                val xGradient = (pixelColorXR.red - pixelColorXL.red) * (pixelColorXR.red - pixelColorXL.red) +
                        (pixelColorXR.green - pixelColorXL.green) * (pixelColorXR.green - pixelColorXL.green) +
                        (pixelColorXR.blue - pixelColorXL.blue) * (pixelColorXR.blue - pixelColorXL.blue)

                val yGradient = (pixelColorYU.red - pixelColorYD.red) * (pixelColorYU.red - pixelColorYD.red) +
                        (pixelColorYU.green - pixelColorYD.green) * (pixelColorYU.green - pixelColorYD.green) +
                        (pixelColorYU.blue - pixelColorYD.blue) * (pixelColorYU.blue - pixelColorYD.blue)

                val energy = sqrt((xGradient + yGradient).toDouble())
                if (energy > maxEnergyValue) {
                    maxEnergyValue = energy
                }
                energyArray[y][x] = energy
            }
        }
//        for (element in energyArray) {
//            println(element.toMutableList())
//        }
        return energyArray
    }

    fun createIntensity(energyArray: Array<DoubleArray>): BufferedImage {
        val imgCopy = img
        // intensity
        for (x in 0 until imgCopy.width) {
            for (y in 0 until imgCopy.height) {
                val intensity = ((255.0 * energyArray[x][y]) / maxEnergyValue).toInt()
                imgCopy.setRGB(x, y, Color(intensity, intensity, intensity).rgb)
            }
        }
        return imgCopy
    }

    fun findSeam() {
        val seamEnergyArray: MutableList<MutableList<Double>> = ArrayList()
        seamEnergyArray.add(MutableList(img.width) { 0.0 })
        for (i in 0 until img.height) {
            seamEnergyArray.add(energyArray[i].toMutableList())
        }
        seamEnergyArray.add(MutableList(img.width) { 0.0 })

//        println("energy array= $energyArray")
//        for (element in energyArray) {
//            println(element.toMutableList())
//        }
//        println("seam array= $seamEnergyArray")
//        for (element in seamEnergyArray) {
//            println(element)
//        }

//        search for the shortest path between top-left and bottom-right corner (0, 0) (img.width-1, img.height+1)

//        Edge( pixelStart:"x,y", pixelDestination:"x,y", DestinationEnergy:Double)
        val edges = mutableListOf<Edge>()
//
//        println("size ${seamEnergyArray.size}")
//        println("size0 ${seamEnergyArray[0].size}")
//        println("size1 ${seamEnergyArray[1].size}")
//        println("size2 ${seamEnergyArray[2].size}")


        for (y in 0 until seamEnergyArray.size) {
            for (x in 0 until seamEnergyArray[y].size) {

                if (y == 0) {
                    if (x < seamEnergyArray[y].size - 1) {
                        edges.add(Edge("$x,$y", "$x,${y + 1}", seamEnergyArray[y + 1][x]))
                        edges.add(Edge("$x,$y", "${x + 1},${y + 1}", seamEnergyArray[y + 1][x + 1]))
                    }
                } else if (y == seamEnergyArray.size - 1) {
                    if (x < seamEnergyArray[y].size - 1) {
                        edges.add(Edge("$x,$y", "${x + 1},$y", seamEnergyArray[y][x + 1]))
                    }
                } else {
                    edges.add(Edge("$x,$y", "$x,${y + 1}", seamEnergyArray[y + 1][x]))
                    if (x < seamEnergyArray[y].size - 1) {
                        edges.add(Edge("$x,$y", "${x + 1},${y + 1}", seamEnergyArray[y + 1][x + 1]))
                    }
                    if (x > 0) {
                        edges.add(Edge("$x,$y", "${x - 1},${y + 1}", seamEnergyArray[y + 1][x - 1]))
                    }
                }
            }
//            println("$y TIMES")
//            for (e in edges) {
//                println("Start: ${e.v1}, Dest: ${e.v2}, Energy: ${e.dist}")
//            }
        }
//
//        for (e in edges) {
//
//            println("Start: ${e.v1}, Dest: ${e.v2}, Energy: ${e.dist}")
//        }


        val path: MutableList<String>


        with(Graph(edges, true)) {   // directed
            dijkstra("0,0")
            path = getPath("${seamEnergyArray[seamEnergyArray.size - 1].size - 1},${seamEnergyArray.size - 1}")
        }

        // removing the top and bottom line from the pixel coordinates
        for (element in path) {
            val x = element.substringBefore(",", "-1").toInt()
            val y = element.substringAfter(",", "-1").toInt()
            if (y > 0 && y <= img.height) {
                seamPixelCords.add(arrayOf(x, y - 1))
            }

        }


    }

    fun colourSeamRed(): BufferedImage{
        val imgCopy = img
        var energy = 0.0
    for (element in seamPixelCords){
        imgCopy.setRGB(element[0], element[1], Color(255, 0, 0).rgb)
        energy += energyArray[element[1]][element[0]]
    }

        println("energy = $energy")
        return imgCopy
    }


}


private fun energyImg(nameIn: String, nameOut: String): BufferedImage? {
//    not in use, can be deleted
    val img = ImageIO.read(File(nameIn))

    val energyArray = Array(img.width) { DoubleArray(img.height) }
    var maxEnergyValue = 0.0

    for (x in 0 until img.width) {
        for (y in 0 until img.height) {

            val pixelColorXL: Color = if (x == 0) {
                Color(img.getRGB(x, y))
            } else {
                Color(img.getRGB(x - 1, y))
            }

            val pixelColorXR: Color = if (x == img.width - 1) {
                Color(img.getRGB(x, y))
            } else {
                Color(img.getRGB(x + 1, y))
            }

            val pixelColorYU: Color = if (y == 0) {
                Color(img.getRGB(x, y))
            } else {
                Color(img.getRGB(x, y - 1))
            }

            val pixelColorYD: Color = if (y == img.height - 1) {
                Color(img.getRGB(x, y))
            } else {
                Color(img.getRGB(x, y + 1))
            }

            val xGradient = (pixelColorXR.red - pixelColorXL.red) * (pixelColorXR.red - pixelColorXL.red) +
                    (pixelColorXR.green - pixelColorXL.green) * (pixelColorXR.green - pixelColorXL.green) +
                    (pixelColorXR.blue - pixelColorXL.blue) * (pixelColorXR.blue - pixelColorXL.blue)

            val yGradient = (pixelColorYU.red - pixelColorYD.red) * (pixelColorYU.red - pixelColorYD.red) +
                    (pixelColorYU.green - pixelColorYD.green) * (pixelColorYU.green - pixelColorYD.green) +
                    (pixelColorYU.blue - pixelColorYD.blue) * (pixelColorYU.blue - pixelColorYD.blue)

            val energy = sqrt((xGradient + yGradient).toDouble())
            if (energy > maxEnergyValue) {
                maxEnergyValue = energy
            }
            energyArray[x][y] = energy

        }


    }
//        for (i in energyArray.indices) {
//        println(energyArray[i].contentToString())
//    }

    // intensity
    for (x in 0 until img.width) {
        for (y in 0 until img.height) {
            val intensity = ((255.0 * energyArray[x][y]) / maxEnergyValue).toInt()
            img.setRGB(x, y, Color(intensity, intensity, intensity).rgb)
        }
    }


    return img

}

private fun invertImg(nameIn: String, nameOut: String): BufferedImage? {
//    not in use, can be deleted

    val img = ImageIO.read(File((nameIn)))

    print(img)
    try {
        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                val pixelRGB = Color(img.getRGB(x, y))
                val r = 255 - pixelRGB.red
                val g = 255 - pixelRGB.green
                val b = 255 - pixelRGB.blue
                val pixelInvertedRGB = Color(r, g, b).rgb
                img.setRGB(x, y, pixelInvertedRGB)
            }
        }
        return img
    } catch (e: NullPointerException) {
        println("img must not be null")
        return img
    }
}
