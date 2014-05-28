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
