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
import com.tinkerpop.blueprints.oupls.jung.GraphJung;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;

public class VersionedGraphView {

    public static void main(String[] args) {
        Neo4j2Graph versionGraph = new Neo4j2Graph("D:\\test\\neo4jversion");

        GraphJung g = new GraphJung<>(versionGraph);
        CircleLayout layout = new CircleLayout<Vertex, Edge>(g);
        layout.setSize(new Dimension(300, 300));
        BasicVisualizationServer<Vertex, Edge> viz = new BasicVisualizationServer<Vertex, Edge>(layout);
        viz.setPreferredSize(new Dimension(350, 350));

        Transformer<Vertex, String> vertexLabelTransformer = new Transformer<Vertex, String>() {
            public String transform(Vertex vertex) {
                return (String) vertex.getProperty("name");
            }
        };

        Transformer<Edge, String> edgeLabelTransformer = new Transformer<Edge, String>() {
            public String transform(Edge edge) {
                return edge.getLabel();
            }
        };

        viz.getRenderContext().setEdgeLabelTransformer(edgeLabelTransformer);
        viz.getRenderContext().setVertexLabelTransformer(vertexLabelTransformer);

        JFrame frame = new JFrame("TinkerPop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(viz);
        frame.pack();
        frame.setVisible(true);
    }
}
