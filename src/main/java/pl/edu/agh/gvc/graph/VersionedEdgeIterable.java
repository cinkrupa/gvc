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
