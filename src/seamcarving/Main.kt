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
    ImageIO.write(img.getBufferedImage(), "png", File(nameOut))

}

class SeamImg(nameIn: String) {
    private val img: BufferedImage = ImageIO.read(File(nameIn))
    private var maxEnergyValue = 0.0
    private val energyArray = Array(img.width) { DoubleArray(img.height) }

    init {
        println("img.width = ${img.width}")
        println("img.height = ${img.height}")
    }

    fun getBufferedImage(): BufferedImage {
        return img
    }

    fun energyArray(): Array<DoubleArray> {
        maxEnergyValue = 0.0

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
        for (element in energyArray) {
            println(element.toMutableList())
        }
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
        for (i in 0..img.height) {
            seamEnergyArray.add(energyArray[i].toMutableList())
        }
        seamEnergyArray.add(MutableList(img.width) { 0.0 })
        println("energy array= $energyArray")
        for (element in energyArray) {
            println(element.toMutableList())
        }
        println("seam array= $seamEnergyArray")
        for (element in seamEnergyArray) {
            println(element)
        }

//        search for the shortest path between top-left and bottom-right corner (0, 0) (img.width-1, img.height+1)

//        Edge( pixelStart:"x,y", pixelDestination:"x,y", DestinationEnergy:Double)
        val edges = mutableListOf<Edge>()

        println("size ${seamEnergyArray.size}")
        println("size0 ${seamEnergyArray[0].size}")
        println("size1 ${seamEnergyArray[1].size}")
        println("size2 ${seamEnergyArray[2].size}")

        for (x in 0 until seamEnergyArray.size) {


            for (y in 0 until seamEnergyArray[x].size) {
                if (y == 0) {
                    if (x < seamEnergyArray.size - 1) {
                        edges.add(Edge("$x,$y", "$x,${y + 1}", seamEnergyArray[x][y + 1]))
                        edges.add(Edge("$x,$y", "${x + 1},${y + 1}", seamEnergyArray[x + 1][y + 1]))
                    }
                } else if (y == seamEnergyArray[x].size - 1) {
                    edges.add(Edge("$x,$y", "${x + 1},$y", seamEnergyArray[x + 1][y]))
                } else {
                    edges.add(Edge("$x,$y", "$x,${y + 1}", seamEnergyArray[x][y + 1]))
                    if (x < seamEnergyArray.size - 1) {
                        edges.add(Edge("$x,$y", "${x + 1},${y + 1}", seamEnergyArray[x + 1][y + 1]))
                    }
                    if (x > 0) {
                        edges.add(Edge("$x,$y", "${x - 1},${y + 1}", seamEnergyArray[x - 1][y + 1]))
                    }
                }
            }
            println("$x TIMES")
            for (e in edges) {
                println("Start: ${e.v1}, Dest: ${e.v2}, Energy: ${e.dist}")
            }
        }
//
//        for (e in edges) {
//
//            println("Start: ${e.v1}, Dest: ${e.v2}, Energy: ${e.dist}")
//        }


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
