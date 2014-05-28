package pl.edu.agh.gvc.graph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;
import pl.edu.agh.gvc.Properties;

import java.util.Set;

/**
 * A {@link com.tinkerpop.blueprints.Vertex} that represents an up-to-date vertex queried from the data graph.
 * <p/>
 * Modifications to properties will create a new vertex instance in the {@link VersionGraph}.
 */
public class VersionedVertex implements Vertex {

    private final Vertex baseVertex;

    private final VersionedGraph versionedGraph;

    protected VersionedVertex(Vertex baseVertex, VersionedGraph versionedGraph) {
        this.baseVertex = baseVertex;
        this.versionedGraph = versionedGraph;
    }

    @Override
    public Iterable<Edge> getEdges(Direction direction, String... labels) {
        return new VersionedEdgeIterable(baseVertex.getEdges(direction, labels), versionedGraph);
    }

    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        return new VersionedVertexIterable(baseVertex.getVertices(direction, labels), versionedGraph);
    }

    @Override
    public VertexQuery query() {
        return new WrapperVertexQuery((baseVertex).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new VersionedVertexIterable(this.query.vertices(), versionedGraph);
            }

            @Override
            public Iterable<Edge> edges() {
                return new VersionedEdgeIterable(this.query.edges(), versionedGraph);
            }
        };
    }

    @Override
    public Edge addEdge(String label, Vertex inVertex) {
        Edge edge = baseVertex.addEdge(label, inVertex);
        versionedGraph.getVersionGraph().addEdge(edge);
        return edge;
    }

    @Override
    public <T> T getProperty(String key) {
        return baseVertex.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return baseVertex.getPropertyKeys();
    }

    @Override
    public void setProperty(String key, Object value) {
        versionedGraph.getVersionGraph().setProperty(baseVertex, key, value);
        baseVertex.setProperty(key, value);
    }

    @Override
    public <T> T removeProperty(String key) {
        versionedGraph.getVersionGraph().removeProperty(baseVertex, key);
        return baseVertex.removeProperty(key);
    }

    @Override
    public void remove() {
        versionedGraph.getVersionGraph().removeVertex(baseVertex);
        baseVertex.remove();
    }

    @Override
    public Object getId() {
        return baseVertex.getProperty(Properties.ID);
    }

    public Vertex getBaseVertex() {
        return baseVertex;
    }
}
