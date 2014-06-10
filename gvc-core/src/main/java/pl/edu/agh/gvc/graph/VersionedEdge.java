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

package pl.edu.agh.gvc.graph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import pl.edu.agh.gvc.PropertyKeys;

import java.util.Set;

public class VersionedEdge implements Edge {

    private final Edge baseEdge;

    private final VersionedGraph versionedGraph;

    protected VersionedEdge(Edge baseEdge, VersionedGraph versionedGraph) {
        this.baseEdge = baseEdge;
        this.versionedGraph = versionedGraph;
    }

    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        return baseEdge.getVertex(direction);
    }

    @Override
    public String getLabel() {
        return baseEdge.getLabel();
    }

    @Override
    public <T> T getProperty(String key) {
        return baseEdge.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return baseEdge.getPropertyKeys();
    }

    @Override
    public void setProperty(String key, Object value) {
        versionedGraph.getVersionGraph().setProperty(baseEdge, key, value);
        baseEdge.setProperty(key, value);
    }

    @Override
    public <T> T removeProperty(String key) {
        versionedGraph.getVersionGraph().removeProperty(baseEdge, key);
        return baseEdge.removeProperty(key);
    }

    @Override
    public void remove() {
        versionedGraph.getVersionGraph().removeEdge(baseEdge);
        baseEdge.remove();
    }

    @Override
    public Object getId() {
        return baseEdge.getProperty(PropertyKeys.ID);
    }
}
