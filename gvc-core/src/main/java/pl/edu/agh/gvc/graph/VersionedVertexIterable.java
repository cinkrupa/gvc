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
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

public class VersionedVertexIterable implements CloseableIterable<Vertex> {

    Iterable<Vertex> baseIterable;

    private final VersionedGraph versionedGraph;

    public VersionedVertexIterable(Iterable<Vertex> baseIterable, VersionedGraph versionedGraph) {
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
    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private Iterator<Vertex> iterator = baseIterable.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Vertex next() {
                return new VersionedVertex(iterator.next(), versionedGraph);
            }

            @Override
            public void remove() {
                this.iterator.remove();
            }
        };
    }
}
