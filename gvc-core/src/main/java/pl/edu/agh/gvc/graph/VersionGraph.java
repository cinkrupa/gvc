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
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import org.joda.time.DateTime;
import pl.edu.agh.gvc.EdgeLabels;
import pl.edu.agh.gvc.ElementUtils;
import pl.edu.agh.gvc.PropertyKeys;
import pl.edu.agh.gvc.exception.ElementNotInCurrentRevisionException;
import pl.edu.agh.gvc.exception.TransientElementException;

import java.util.Set;

public class VersionGraph<T extends KeyIndexableGraph & IndexableGraph & TransactionalGraph> implements KeyIndexableGraph, IndexableGraph, TransactionalGraph {

    private final T baseGraph;

    private Vertex currentRevision;

    public VersionGraph(T baseGraph) {
        this.baseGraph = baseGraph;
        init();

    }

    private void init() {
        if (!baseGraph.getIndexedKeys(Vertex.class).contains(PropertyKeys.HEAD)) {
            baseGraph.createKeyIndex(PropertyKeys.HEAD, Vertex.class);
        }

        //        if (!baseGraph.getIndexedKeys(Vertex.class).contains(Properties.REVISION_NUMBER)) {
        //            baseGraph.createIndex(Properties.REVISION_NUMBER, Vertex.class);
        //        }

        if (!baseGraph.getIndexedKeys(Vertex.class).contains(PropertyKeys.ID)) {
            baseGraph.createKeyIndex(PropertyKeys.ID, Vertex.class);
        }

        //        if (!baseGraph.getIndexedKeys(Edge.class).contains(PropertyKeys.ID)) {
        //            baseGraph.createKeyIndex(PropertyKeys.ID, Edge.class);
        //        }

        currentRevision = ElementUtils.getSingleElement(baseGraph, PropertyKeys.HEAD, true, Vertex.class);

        if (currentRevision == null) {
            currentRevision = baseGraph.addVertex(null);
            currentRevision.setProperty(PropertyKeys.HEAD, true);
            //baseGraph.getIndex(Properties.HEAD, Vertex.class).put(Properties.HEAD, true, currentRevision);
            currentRevision.setProperty(PropertyKeys.REVISION_NUMBER, ElementUtils.generateId());
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
        headRevision.setProperty(PropertyKeys.HEAD, true);
        currentRevision.removeProperty(PropertyKeys.HEAD);
        headRevision.addEdge(EdgeLabels.PREVIOUS_REVISION, currentRevision);
        for (Vertex versionVertex : currentRevision.getVertices(Direction.OUT, EdgeLabels.CONTAINS)) {
            headRevision.addEdge(EdgeLabels.CONTAINS, versionVertex);
        }
        currentRevision.setProperty(PropertyKeys.REVISION_NUMBER, ElementUtils.generateId());
        currentRevision.setProperty(PropertyKeys.COMMIT_DATE, new DateTime().toString());
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
        Vertex traceElement = ElementUtils.getTraceElement(baseGraph, element);
        if (traceElement == null) {
            throw new TransientElementException();
        }
        Vertex latestVersion = ElementUtils.getLatestVersion(traceElement);
        if (latestVersion == null) {
            throw new TransientElementException();
        }
        Edge currentRevisionEdge = ElementUtils.getRevisionEdge(latestVersion, currentRevision);
        if (currentRevisionEdge == null) {
            throw new ElementNotInCurrentRevisionException();
        }

        if (!ElementUtils.isPropertyModified(latestVersion, key, value)) {
            return;
        }

        if (ElementUtils.hasMultipleElements(ElementUtils.getRevisionEdges(latestVersion))) {
            latestVersion = ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision, latestVersion);
            latestVersion.setProperty(key, value);
            return;
        }

        latestVersion.setProperty(key, value);

        ElementUtils.revertIfUnmodified(latestVersion, traceElement, currentRevision);
    }

    public void removeProperty(Element element, String key) {
        Vertex traceElement = ElementUtils.getTraceElement(baseGraph, element);
        if (traceElement == null) {
            throw new TransientElementException();
        }
        Vertex latestVersion = ElementUtils.getLatestVersion(traceElement);
        if (latestVersion == null) {
            throw new TransientElementException();
        }
        Edge currentRevisionEdge = ElementUtils.getRevisionEdge(latestVersion, currentRevision);
        if (currentRevisionEdge == null) {
            throw new ElementNotInCurrentRevisionException();
        }

        if (!latestVersion.getPropertyKeys().contains(key)) {
            return;
        }

        if (ElementUtils.hasMultipleElements(ElementUtils.getRevisionEdges(latestVersion))) {
            latestVersion = ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision, latestVersion);
            latestVersion.removeProperty(key);
            return;
        }

        latestVersion.removeProperty(key);

        ElementUtils.revertIfUnmodified(latestVersion, traceElement, currentRevision);
    }

    private void removeElement(Element element) {
        Vertex traceElement = ElementUtils.getTraceElement(baseGraph, element);
        if (traceElement == null) {
            throw new TransientElementException();
        }
        Vertex latestVersion = ElementUtils.getLatestVersion(traceElement);
        if (latestVersion == null) {
            throw new TransientElementException();
        }
        Edge currentRevisionEdge = ElementUtils.getRevisionEdge(latestVersion, currentRevision);
        if (currentRevisionEdge != null) {
            currentRevisionEdge.remove();
        }
        if (ElementUtils.isEmpty(ElementUtils.getRevisionEdges(latestVersion))) {
            Vertex previousVersion = ElementUtils.getPreviousVersion(latestVersion);
            latestVersion.remove();
            if (previousVersion != null) {
                traceElement.addEdge(EdgeLabels.LATEST_VERSION, previousVersion);
            } else {
                traceElement.remove();
            }
        }

    }

    private Vertex addElement(Element element) {
        Vertex traceElement = ElementUtils.getTraceElement(baseGraph, element);
        if (traceElement == null) {
            traceElement = ElementUtils.addNewTraceElement(baseGraph, element);
            return ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision);
        }
        Vertex latestVersion = ElementUtils.getLatestVersion(traceElement);
        if (latestVersion == null) {
            return ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision);
        }
        Edge currentRevisionEdge = ElementUtils.getRevisionEdge(latestVersion, currentRevision);
        if (currentRevisionEdge == null) {
            if (ElementUtils.isModified(element, latestVersion)) {
                return ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision, latestVersion);
            } else {
                currentRevision.addEdge(EdgeLabels.CONTAINS, latestVersion);
                return latestVersion;
            }
        }
        currentRevisionEdge.remove();
        if (!ElementUtils.isEmpty(ElementUtils.getRevisionEdges(latestVersion))) {
            return ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision, latestVersion);
        }

        latestVersion.remove();

        Vertex previousVersion = ElementUtils.getPreviousVersion(latestVersion);

        if (previousVersion == null) {
            return ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision);
        }

        return ElementUtils.addNewVersionElement(baseGraph, element, traceElement, currentRevision, previousVersion);
    }
}
