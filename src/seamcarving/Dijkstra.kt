package seamcarving

import java.util.*

class Edge(val v1: String, val v2: String, val dist: Double)

val path = mutableListOf<String>()

/** One vertex of the graph, complete with mappings to neighbouring vertices */
class Vertex(val name: String) : Comparable<Vertex> {

    var dist = Double.MAX_VALUE  // MAX_VALUE assumed to be infinity
    var previous: Vertex? = null
    val neighbours = HashMap<Vertex, Double>()


    fun printPath() {
        when (previous) {
            this -> {
                print(name)
                path.add(name)

            }
            null -> {
                print("$name(unreached)")
            }
            else -> {
                previous!!.printPath()
                print(" -> $name($dist)")
                path.add(name)

            }
        }
    }

    fun getPathList(): MutableList<String> {
        when (previous) {
            this -> {
                path.add(name)

            }
            null -> {
                path.add("$name(unreached)")
            }
            else -> {
                previous!!.getPathList()
                path.add(name)

            }
        }
        return path
    }

    override fun compareTo(other: Vertex): Int {
        if (dist == other.dist) return name.compareTo(other.name)
        return dist.compareTo(other.dist)
    }

    override fun toString() = "($name, $dist)"
}

class Graph(
    val edges: List<Edge>,
    val directed: Boolean,
    val showAllPaths: Boolean = false
) {
    // mapping of vertex names to Vertex objects, built from a set of Edges
    private val graph = HashMap<String, Vertex>(edges.size)

    init {
        // one pass to find all vertices
        for (e in edges) {
            if (!graph.containsKey(e.v1)) graph.put(e.v1, Vertex(e.v1))
            if (!graph.containsKey(e.v2)) graph.put(e.v2, Vertex(e.v2))
        }

        // another pass to set neighbouring vertices
        for (e in edges) {
            graph[e.v1]!!.neighbours.put(graph[e.v2]!!, e.dist)
            // also do this for an undirected graph if applicable
            if (!directed) graph[e.v2]!!.neighbours.put(graph[e.v1]!!, e.dist)
        }
    }

    /** Runs dijkstra using a specified source vertex */
    fun dijkstra(startName: String) {
        if (!graph.containsKey(startName)) {
            println("Graph doesn't contain start vertex '$startName'")
            return
        }
        val source = graph[startName]
        val q = TreeSet<Vertex>()

        // set-up vertices
        for (v in graph.values) {
            v.previous = if (v == source) source else null
            v.dist = if (v == source) 0.0 else Double.MAX_VALUE
            q.add(v)
        }

        dijkstra(q)
    }

    /** Implementation of dijkstra's algorithm using a binary heap */
    private fun dijkstra(q: TreeSet<Vertex>) {
        while (!q.isEmpty()) {
            // vertex with shortest distance (first iteration will return source)
            val u = q.pollFirst()
            // if distance is infinite we can ignore 'u' (and any other remaining vertices)
            // since they are unreachable
            if (u.dist == Double.MAX_VALUE) break

            //look at distances to each neighbour
            for (a in u.neighbours) {
                val v = a.key // the neighbour in this iteration

                val alternateDist = u.dist + a.value
                if (alternateDist < v.dist) { // shorter path to neighbour found
                    q.remove(v)
                    v.dist = alternateDist
                    v.previous = u
                    q.add(v)
                }
            }
        }
    }

    /** Prints a path from the source to the specified vertex */
    fun printPath(endName: String): MutableList<String> {
        if (!graph.containsKey(endName)) {
            println("Graph doesn't contain end vertex '$endName'")
            return mutableListOf()
        }
        print(if (directed) "Directed   : " else "Undirected : ")
        graph[endName]!!.printPath()
        return graph[endName]!!.getPathList()

    }

    /** returns a path from the source to the specified vertex */
    fun getPath(endName: String): MutableList<String> {
        if (!graph.containsKey(endName)) {
            println("Graph doesn't contain end vertex '$endName'")
            return mutableListOf()
        }
        return graph[endName]!!.getPathList()

    }

    /** Prints the path from the source to every vertex (output order is not guaranteed) */
    private fun printAllPaths() {
        for (v in graph.values) {
            v.printPath()
            println()
        }
        println()
    }
}
