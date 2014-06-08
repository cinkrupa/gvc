/*
 * Copyright (C) 2014  Marcin Krupa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
            v1.setProperty("p1", 0);
            Vertex v2 = graph.addVertex(null);
            Edge e1 = graph.addEdge(null, v1, v2, "relation");

            graph.commit();

            Vertex v3 = graph.addVertex(null);
            v3.setProperty("p2", 0);

            graph.commit();

            v1.setProperty("p1", 1);
            v2.remove();

            graph.commit();

            v1.setProperty("p1", 2);
            v1.setProperty("p1", 1);
            v3.removeProperty("p2");

            graph.commit();
        } finally {
            graph.shutdown();
        }
    }
}
