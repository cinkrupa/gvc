package pl.edu.agh.gvc.graph;

import com.tinkerpop.blueprints.*;
import org.joda.time.DateTime;
import pl.edu.agh.gvc.EdgeLabels;
import pl.edu.agh.gvc.ElementUtils;
import pl.edu.agh.gvc.Properties;

import java.util.Set;

public class VersionGraph<T extends KeyIndexableGraph & IndexableGraph & TransactionalGraph> implements KeyIndexableGraph, IndexableGraph, TransactionalGraph {

    private final T baseGraph;

    private Vertex currentRevision;

    public VersionGraph(T baseGraph) {
        this.baseGraph = baseGraph;
        init();

    }

    private void init() {

        if (!baseGraph.getIndexedKeys(Vertex.class).contains(Properties.HEAD)) {
            baseGraph.createIndex(Properties.HEAD.toString(), Vertex.class);
        }

//        if (!baseGraph.getIndexedKeys(Vertex.class).contains(Properties.REVISION_NUMBER)) {
//            baseGraph.createIndex(Properties.REVISION_NUMBER, Vertex.class);
//        }

        if (!baseGraph.getIndexedKeys(Vertex.class).contains(Properties.ID)) {
            baseGraph.createKeyIndex(Properties.ID, Vertex.class);
        }

        if (!baseGraph.getIndexedKeys(Edge.class).contains(Properties.ID)) {
            baseGraph.createKeyIndex(Properties.ID, Edge.class);
        }

        currentRevision = ElementUtils.getSingleElement(baseGraph, Properties.HEAD, true, Vertex.class);

        if (currentRevision == null) {
            currentRevision = baseGraph.addVertex(null);
            currentRevision.setProperty(Properties.HEAD, true);
            //baseGraph.getIndex(Properties.HEAD, Vertex.class).put(Properties.HEAD, true, currentRevision);
            currentRevision.setProperty(Properties.REVISION_NUMBER, ElementUtils.generateId());
        }

        baseGraph.commit();
    }

    @Override
    public <E extends Element> Index<E> createIndex(String s, Class<E> eClass, Parameter... parameters) {
        return baseGraph.createIndex(s, eClass, parameters);
    }

    @Override
    public <E extends Element> Index<E> getIndex(String s, Class<E> eClass) {
        return baseGraph.getIndex(s, eClass);
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        return baseGraph.getIndices();
    }

    @Override
    public void dropIndex(String s) {
        baseGraph.dropIndex(s);
    }

    @Override
    public <E extends Element> void dropKeyIndex(String s, Class<E> eClass) {
        baseGraph.dropKeyIndex(s, eClass);
    }

    @Override
    public <E extends Element> void createKeyIndex(String s, Class<E> eClass, Parameter... parameters) {
        baseGraph.createKeyIndex(s, eClass, parameters);
    }

    @Override
    public <E extends Element> Set<String> getIndexedKeys(Class<E> eClass) {
        return baseGraph.getIndexedKeys(eClass);
    }

    @Override
    public Features getFeatures() {
        return baseGraph.getFeatures();
    }

    @Override
    public Vertex addVertex(Object o) {
        return baseGraph.addVertex(o);
    }

    @Override
    public Vertex getVertex(Object o) {
        return baseGraph.getVertex(o);
    }


    @Override
    public Iterable<Vertex> getVertices() {
        return baseGraph.getVertices();
    }

    @Override
    public Iterable<Vertex> getVertices(String s, Object o) {
        return baseGraph.getVertices(s, o);
    }

    @Override
    public Edge getEdge(Object o) {
        return baseGraph.getEdge(o);
    }


    @Override
    public Iterable<Edge> getEdges() {
        return baseGraph.getEdges();
    }

    @Override
    public Iterable<Edge> getEdges(String s, Object o) {
        return baseGraph.getEdges(s, o);
    }

    @Override
    public GraphQuery query() {
        return baseGraph.query();
    }

    @Override
    public void shutdown() {
        baseGraph.shutdown();
    }

    @Deprecated
    @Override
    public void stopTransaction(Conclusion conclusion) {
        baseGraph.stopTransaction(conclusion);
    }

    @Override
    public void commit() {
        Vertex headRevision = baseGraph.addVertex(null);
        headRevision.setProperty(Properties.HEAD, true);
        currentRevision.removeProperty(Properties.HEAD);
        headRevision.addEdge(EdgeLabels.PREVIOUS_REVISION, currentRevision);
        for (Vertex versionVertex : currentRevision.getVertices(Direction.OUT, EdgeLabels.CONTAINS)) {
            headRevision.addEdge(EdgeLabels.CONTAINS, versionVertex);
        }
        currentRevision.setProperty(Properties.REVISION_NUMBER, ElementUtils.generateId());
        currentRevision.setProperty(Properties.COMMIT_DATE, new DateTime().toString());
        currentRevision = headRevision;
        baseGraph.commit();
    }

    @Override
    public void rollback() {
        baseGraph.rollback();
    }

    @Override
    public Edge addEdge(Object o, Vertex vertex, Vertex vertex2, String label) {
        return baseGraph.addEdge(o, vertex, vertex2, label);
    }

    //TODO: update incoming and outgoing vertices
    public Vertex addEdge(Edge edge) {
        return addElement(edge);
    }

    @Override
    public void removeEdge(Edge edge) {
        removeElement(edge);
    }

    public Vertex addVertex(Vertex vertex) {
        return addElement(vertex);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        for (Edge edge : ElementUtils.filterEdges(vertex, Direction.BOTH, EdgeLabels.GVC_LABELS)) {
            removeElement(edge);
        }
        removeElement(vertex);
    }

    public void setProperty(Element element, String key, Object value) {
        //TODO: implement
    }

    public void removeProperty(Element element, String key) {
        //TODO: implement
    }

    private void removeElement(Element element) {
        Object traceElementId = element.getProperty(Properties.ID);
        Vertex traceElement = ElementUtils.getSingleElement(baseGraph, Properties.ID, traceElementId, Vertex.class);
        if (traceElement != null) {
            Vertex latestVersion = ElementUtils.getSingleElement(traceElement.getVertices(Direction.OUT, EdgeLabels.LATEST_VERSION));
            if (latestVersion != null) {

                Edge currentRevisionEdge = ElementUtils.getSingleEdge(latestVersion, currentRevision, Direction.IN, EdgeLabels.CONTAINS);
                if (currentRevisionEdge != null) {
                    currentRevisionEdge.remove();
                }
                if (ElementUtils.isEmpty(latestVersion.getEdges(Direction.IN, EdgeLabels.CONTAINS))) {
                    Vertex previousVersion = ElementUtils.getSingleElement(latestVersion.getVertices(Direction.OUT, EdgeLabels.PREVIOUS_VERSION));
                    if (previousVersion != null) {
                        traceElement.addEdge(EdgeLabels.LATEST_VERSION, previousVersion);
                        latestVersion.remove();
                    } else {
                        latestVersion.remove();
                        traceElement.remove();
                    }
                }
            } else {
                traceElement.remove();
            }
        }
    }

    private Vertex addElement(Element element) {
        Object traceElementId = element.getProperty(Properties.ID);
        Vertex traceElement = ElementUtils.getSingleElement(baseGraph, Properties.ID, traceElementId, Vertex.class);
        if (traceElement != null) {
            Vertex latestVersion = ElementUtils.getSingleElement(traceElement.getVertices(Direction.OUT, EdgeLabels.LATEST_VERSION));
            if (latestVersion != null) {
                Edge currentRevisionEdge = ElementUtils.getSingleEdge(latestVersion, currentRevision, Direction.IN, EdgeLabels.CONTAINS);
                if (!ElementUtils.isModified(element, latestVersion)) {
                    if (currentRevisionEdge == null) currentRevision.addEdge(EdgeLabels.CONTAINS, latestVersion);
                } else if (currentRevisionEdge != null) {
                    currentRevisionEdge.remove();
                    Vertex versionElement = baseGraph.addVertex(null);
                    ElementUtils.copyProperties(element, versionElement, Properties.ID);
                    traceElement.addEdge(EdgeLabels.LATEST_VERSION, versionElement);
                    currentRevision.addEdge(EdgeLabels.CONTAINS, versionElement);
                    if (ElementUtils.isEmpty(latestVersion.getEdges(Direction.IN, EdgeLabels.CONTAINS))) {
                        Vertex previousVersion = ElementUtils.getSingleElement(latestVersion.getVertices(Direction.OUT, EdgeLabels.PREVIOUS_VERSION));
                        if (previousVersion != null) {
                            versionElement.addEdge(EdgeLabels.PREVIOUS_REVISION, previousVersion);
                        }
                        latestVersion.remove();
                    } else {
                        versionElement.addEdge(EdgeLabels.PREVIOUS_REVISION, latestVersion);
                    }
                    return versionElement;
                } else {
                    Vertex versionElement = baseGraph.addVertex(null);
                    ElementUtils.copyProperties(element, versionElement, Properties.ID);
                    versionElement.addEdge(EdgeLabels.PREVIOUS_REVISION, latestVersion);
                    traceElement.addEdge(EdgeLabels.LATEST_VERSION, versionElement);
                    currentRevision.addEdge(EdgeLabels.CONTAINS, versionElement);
                    return versionElement;
                }

            } else {
                Vertex versionElement = baseGraph.addVertex(null);
                ElementUtils.copyProperties(element, versionElement, Properties.ID);
                traceElement.addEdge(EdgeLabels.LATEST_VERSION, versionElement);
                currentRevision.addEdge(EdgeLabels.CONTAINS, versionElement);
                return versionElement;
            }

        } else {
            traceElement = baseGraph.addVertex(null);
            traceElement.setProperty(Properties.ID, traceElementId);
            if (element instanceof Edge) {
                traceElement.setProperty(Properties.EDGE, true);
            } else {
                traceElement.setProperty(Properties.VERTEX, true);
            }
            Vertex versionElement = baseGraph.addVertex(null);
            ElementUtils.copyProperties(element, versionElement, Properties.ID);
            traceElement.addEdge(EdgeLabels.LATEST_VERSION, versionElement);
            currentRevision.addEdge(EdgeLabels.CONTAINS, versionElement);
            return versionElement;
        }

        return null;
    }
}
