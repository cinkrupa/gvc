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

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

public class VersionedEdgeIterable implements CloseableIterable<Edge> {

    Iterable<Edge> baseIterable;

    private final VersionedGraph versionedGraph;

    public VersionedEdgeIterable(Iterable<Edge> baseIterable, VersionedGraph versionedGraph) {
        this.baseIterable = baseIterable;
        this.versionedGraph = versionedGraph;
    }

    @Override
    public void close() {
        if (baseIterable instanceof CloseableIterable) {
            ((CloseableIterable) baseIterable).close();
        }
    }

    @Override
    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private Iterator<Edge> iterator = baseIterable.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Edge next() {
                return new VersionedEdge(iterator.next(), versionedGraph);
            }

            @Override
            public void remove() {
                this.iterator.remove();
            }
        };
    }
}
