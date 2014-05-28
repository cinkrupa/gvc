package pl.edu.agh.gvc.graph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import pl.edu.agh.gvc.Properties;

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
        return baseEdge.getProperty(Properties.ID);
    }
}
