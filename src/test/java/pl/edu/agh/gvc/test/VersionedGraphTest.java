package pl.edu.agh.gvc.test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import pl.edu.agh.gvc.graph.VersionedGraph;

public class VersionedGraphTest {

    public static void main(String[] args) {
        Neo4j2Graph dataGraph = new Neo4j2Graph("D:\\test\\neo4jdata");
        Neo4j2Graph versionGraph = new Neo4j2Graph("D:\\test\\neo4jversion");
        VersionedGraph<Neo4j2Graph> graph = new VersionedGraph<Neo4j2Graph>(dataGraph, versionGraph);

        try {
            Vertex v1 = graph.addVertex(null);
            Vertex v2 = graph.addVertex(null);
            Edge e1 = graph.addEdge(null, v1, v2, "relation");

            graph.commit();

            Vertex v3 = graph.addVertex(null);

            graph.commit();

            v2.remove();

            graph.commit();
        } finally {
            graph.shutdown();
        }
    }
}
