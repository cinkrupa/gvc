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

package pl.edu.agh.gvc.rexster;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.rexster.RexsterApplicationGraph;
import com.tinkerpop.rexster.config.GraphConfiguration;
import com.tinkerpop.rexster.config.GraphConfigurationContext;
import com.tinkerpop.rexster.config.GraphConfigurationException;
import pl.edu.agh.gvc.graph.VersionedGraph;

public class VersionedGraphConfiguration implements GraphConfiguration {

    @Override
    public Graph configureGraphInstance(GraphConfigurationContext context) throws GraphConfigurationException {
        return new VersionedGraph<>(getGraph(context, RexsterTokens.GVC_DATA_GRAPH), getGraph(context, RexsterTokens.GVC_VERSION_GRAPH));
    }

    private <T extends IndexableGraph & KeyIndexableGraph & TransactionalGraph> T getGraph(GraphConfigurationContext context, String token) throws GraphConfigurationException {
        final String graphName = context.getProperties().getString(token, null);

        if (graphName == null || graphName.trim().length() == 0) {
            throw new GraphConfigurationException("Check graph configuration. Missing or empty configuration element: " + token);
        }

        RexsterApplicationGraph baseGraph = context.getGraphs().get(graphName);

        if (null == baseGraph) {
            throw new GraphConfigurationException("no such base graph for VersionGraph: " + graphName);
        }

        if (!(baseGraph.getGraph() instanceof IndexableGraph)) {
            throw new GraphConfigurationException("base graph for IdGraph must be an instance of IndexableGraph: " + graphName);
        }

        if (!(baseGraph.getGraph() instanceof KeyIndexableGraph)) {
            throw new GraphConfigurationException("base graph for IdGraph must be an instance of KeyIndexableGraph: " + graphName);
        }

        if (!(baseGraph.getGraph() instanceof TransactionalGraph)) {
            throw new GraphConfigurationException("base graph for IdGraph must be an instance of TransactionalGraph: " + graphName);
        }

        return (T) baseGraph.getGraph();
    }
}
