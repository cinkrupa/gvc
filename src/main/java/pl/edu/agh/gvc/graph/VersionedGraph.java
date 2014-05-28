package pl.edu.agh.gvc.graph;

import com.tinkerpop.blueprints.*;
import pl.edu.agh.gvc.ElementUtils;
import pl.edu.agh.gvc.Properties;

import java.util.Set;

public class VersionedGraph<T extends KeyIndexableGraph & IndexableGraph & TransactionalGraph> implements KeyIndexableGraph, IndexableGraph, TransactionalGraph {

    private final T dataGraph;

    private final VersionGraph<T> versionGraph;

    public VersionedGraph(T dataGraph, T versionGraph) {
        this.dataGraph = dataGraph;
        this.versionGraph = new VersionGraph<T>(versionGraph);
        init();
    }

    private void init() {

        if (!dataGraph.getIndexedKeys(Vertex.class).contains(Properties.ID)) {
            dataGraph.createKeyIndex(Properties.ID, Vertex.class);
        }

        if (!dataGraph.getIndexedKeys(Edge.class).contains(Properties.ID)) {
            dataGraph.createKeyIndex(Properties.ID, Edge.class);
        }

        dataGraph.commit();
    }

    @Override
    public <E extends Element> Index<E> createIndex(String s, Class<E> eClass, Parameter... parameters) {
        return dataGraph.createIndex(s, eClass, parameters);
    }

    @Override
    public <E extends Element> Index<E> getIndex(String s, Class<E> eClass) {
        return dataGraph.getIndex(s, eClass);
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return dataGraph.getIndices();
    }

    @Override
    public void dropIndex(String s) {
        dataGraph.dropIndex(s);
    }

    @Override
    public <E extends Element> void dropKeyIndex(String s, Class<E> eClass) {
        dataGraph.dropKeyIndex(s, eClass);
    }

    @Override
    public <E extends Element> void createKeyIndex(String s, Class<E> eClass, Parameter... parameters) {
        dataGraph.createKeyIndex(s, eClass, parameters);
    }

    @Override
    public <E extends Element> Set<String> getIndexedKeys(Class<E> eClass) {
        return dataGraph.getIndexedKeys(eClass);
    }

    @Override
    public Features getFeatures() {
        return dataGraph.getFeatures();
    }

    @Override
    public VersionedVertex addVertex(Object o) {
        Vertex vertex = dataGraph.addVertex(o);
        vertex.setProperty(Properties.ID, ElementUtils.generateId());
        versionGraph.addVertex(vertex);
        return new VersionedVertex(vertex, this);
    }

    @Override
    public VersionedVertex getVertex(Object o) {
        Vertex vertex = dataGraph.getVertex(o);
        if (vertex != null) {
            return new VersionedVertex(dataGraph.getVertex(o), this);
        }
        return null;
    }

    @Override
    public void removeVertex(Vertex vertex) {
        versionGraph.removeVertex(vertex);
        dataGraph.removeVertex(vertex);
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return new VersionedVertexIterable(dataGraph.getVertices(), this);
    }

    @Override
    public Iterable<Vertex> getVertices(String s, Object o) {
        return new VersionedVertexIterable(dataGraph.getVertices(s, o), this);
    }

    @Override
    public VersionedEdge addEdge(Object o, Vertex v1, Vertex v2, String s) {
        Vertex baseV1 = v1;
        if (v1 instanceof VersionedVertex) {
            baseV1 = ((VersionedVertex) v1).getBaseVertex();
        }
        Vertex baseV2 = v2;
        if (v2 instanceof VersionedVertex) {
            baseV2 = ((VersionedVertex) v2).getBaseVertex();
        }
        Edge edge = dataGraph.addEdge(o, baseV1, baseV2, s);
        edge.setProperty(Properties.ID, ElementUtils.generateId());
        versionGraph.addEdge(edge);
        return new VersionedEdge(edge, this);
    }

    @Override
    public VersionedEdge getEdge(Object o) {
        Edge edge = dataGraph.getEdge(o);
        if (edge != null) {
            return new VersionedEdge(dataGraph.getEdge(o), this);
        }
        return null;
    }

    @Override
    public void removeEdge(Edge edge) {
        versionGraph.removeEdge(edge);
        dataGraph.removeEdge(edge);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return new VersionedEdgeIterable(dataGraph.getEdges(), this);
    }

    @Override
    public Iterable<Edge> getEdges(String s, Object o) {
        return new VersionedEdgeIterable(dataGraph.getEdges(s, o), this);
    }

    @Override
    public GraphQuery query() {
        return dataGraph.query();
    }

    @Override
    public void shutdown() {
        dataGraph.shutdown();
        versionGraph.shutdown();
    }

    @Deprecated
    @Override
    public void stopTransaction(Conclusion conclusion) {
        dataGraph.stopTransaction(conclusion);
        versionGraph.stopTransaction(conclusion);
    }

    @Override
    public void commit() {
        dataGraph.commit();
        versionGraph.commit();
    }

    @Override
    public void rollback() {
        dataGraph.rollback();
        versionGraph.rollback();
    }

    public VersionGraph<T> getVersionGraph() {
        return versionGraph;
    }
}
