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

package pl.edu.agh.gvc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ElementUtils {

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    public static Map<String, Object> getPropertiesAsMap(Element element) {
        return getPropertiesAsMap(element, null);
    }

    public static Map<String, Object> getPropertiesAsMap(Element element, Set<String> excludedKeys) {
        Builder<String, Object> props = ImmutableMap.builder();

        for (String key : element.getPropertyKeys()) {
            if ((excludedKeys != null) && excludedKeys.contains(key)) {
                continue;
            }

            props.put(key, element.getProperty(key));
        }

        return props.build();
    }

    public static void copyProperties(Element from, Element to, String... excludedKeys) {
        if (excludedKeys == null || excludedKeys.length == 0) {
            copyProperties(from, to, (Set) null);
        } else {
            copyProperties(from, to, Sets.newHashSet(excludedKeys));
        }

    }

    public static void copyProperties(Element from, Element to, Set<String> excludedKeys) {
        for (String k : from.getPropertyKeys()) {
            if (excludedKeys != null && excludedKeys.contains(k)) {
                continue;
            }

            to.setProperty(k, from.getProperty(k));
        }
    }

    public static <E extends Element> E getSingleElement(Graph graph, String key, Object value, Class<E> eClass) {
        Iterable<? extends Element> it = Vertex.class.isAssignableFrom(eClass) ? graph.getVertices(key, value) : graph.getEdges(key, value);
        Iterator<? extends Element> iter = it.iterator();

        E e = null;

        if (iter.hasNext()) {
            e = (E) iter.next();
        }

        if (iter.hasNext()) {
            throw new IllegalStateException(String.format("Multiple elements found."));
        }

        return e;
    }

    public static <E extends Element> E getSingleElement(Iterable<E> it) {
        Iterator<E> iter = it.iterator();
        E e = null;

        if (iter.hasNext()) {
            e = iter.next();
        }

        if (iter.hasNext()) {
            throw new IllegalStateException(String.format("Multiple elements found."));
        }

        return e;
    }

    public static boolean hasMultipleElements(Iterable<?> it) {
        Iterator<?> iter = it.iterator();
        if (iter.hasNext()) {
            iter.next();
            return iter.hasNext();
        }
        return false;
    }

    public static boolean isEmpty(Iterable<?> it) {
        return !it.iterator().hasNext();
    }

    public static boolean hasSingleElement(Iterable<?> it) {
        Iterator<?> iter = it.iterator();
        if (iter.hasNext()) {
            iter.next();
            return !iter.hasNext();
        }
        return false;
    }

    public static boolean isModified(Element e1, Element e2) {
        Set<String> propertyKeys1 = e1.getPropertyKeys();
        Set<String> propertyKeys2 = e2.getPropertyKeys();
        if (propertyKeys1.equals(propertyKeys2)) {
            for (String key : propertyKeys1) {
                if (!ObjectUtils.equals(e1.getProperty(key), e2.getProperty(key))) {
                    return true;
                }
            }
            return false;
        }
        return true;

    }

    public static Edge getSingleEdge(Vertex vertex1, Vertex vertex2, Direction direction, String... labels) {
        for (Edge edge : vertex1.getEdges(direction, labels)) {
            if (edge.getVertex(direction.opposite()).getId() == vertex2.getId()) {
                return edge;
            }
        }
        return null;

    }

    public static Iterable<Edge> filterEdges(Vertex vertex, Direction direction, Set<String> excludedLabels) {
        List<Edge> edges = new ArrayList<Edge>();
        for (Edge edge : vertex.getEdges(direction)) {
            if (!excludedLabels.contains(edge.getLabel())) {
                edges.add(edge);
            }
        }
        return edges;
    }

    public static Vertex getTraceElement(Graph graph, Element element) {
        if (element.getPropertyKeys().contains(PropertyKeys.ID)) {
            Object traceElementId = element.getProperty(PropertyKeys.ID);
            if (traceElementId != null) {
                return ElementUtils.getSingleElement(graph, PropertyKeys.ID, traceElementId, Vertex.class);
            }
        }
        return null;
    }

    public static Edge getRevisionEdge(Vertex versionElement, Vertex revisionElement) {
        return getSingleEdge(versionElement, revisionElement, Direction.IN, EdgeLabels.CONTAINS);
    }

    public static Vertex getPreviousVersion(Vertex versionElement) {
        return ElementUtils.getSingleElement(versionElement.getVertices(Direction.OUT, EdgeLabels.PREVIOUS_VERSION));
    }

    public static Vertex addNewTraceElement(Graph graph, Element element) {
        Vertex traceElement = graph.addVertex(null);
        traceElement.setProperty(PropertyKeys.ID, element.getProperty(PropertyKeys.ID));
        if (element instanceof Edge) {
            traceElement.setProperty(PropertyKeys.EDGE, true);
        } else {
            traceElement.setProperty(PropertyKeys.VERTEX, true);
        }
        return traceElement;
    }

    public static Vertex addNewVersionElement(Graph graph, Element element, Vertex traceElement, Vertex currentRevision) {
        Vertex versionElement = graph.addVertex(null);
        ElementUtils.copyProperties(element, versionElement, PropertyKeys.ID);
        Edge latestVersionEdge = ElementUtils.getSingleElement(traceElement.getEdges(Direction.OUT, EdgeLabels.LATEST_VERSION));
        if (latestVersionEdge != null) {
            Edge currentRevisionEdge = ElementUtils.getSingleEdge(currentRevision, latestVersionEdge.getVertex(Direction.IN), Direction.OUT, EdgeLabels.CONTAINS);
            if (currentRevisionEdge != null) {
                currentRevisionEdge.remove();
            }
            latestVersionEdge.remove();
        }
        traceElement.addEdge(EdgeLabels.LATEST_VERSION, versionElement);
        currentRevision.addEdge(EdgeLabels.CONTAINS, versionElement);
        return versionElement;
    }

    public static Vertex addNewVersionElement(Graph graph, Element element, Vertex traceElement, Vertex currentRevision, Vertex latestVersion) {
        Vertex versionElement = addNewVersionElement(graph, element, traceElement, currentRevision);
        versionElement.addEdge(EdgeLabels.PREVIOUS_VERSION, latestVersion);
        return versionElement;
    }

    public static Vertex getLatestVersion(Vertex traceElement) {
        return ElementUtils.getSingleElement(traceElement.getVertices(Direction.OUT, EdgeLabels.LATEST_VERSION));
    }

    public static boolean isPropertyModified(Vertex versionElement, String key, Object value) {
        return !versionElement.getPropertyKeys().contains(key) || !ObjectUtils.equals(value, versionElement.getProperty(key));

    }

    public static Iterable<?> getRevisionEdges(Vertex versionElement) {
        return versionElement.getEdges(Direction.IN, EdgeLabels.CONTAINS);
    }

    public static void revertIfUnmodified(Vertex versionElement, Vertex traceElement, Vertex revision) {
        Vertex previousVersion = ElementUtils.getPreviousVersion(versionElement);
        if (previousVersion != null && !ElementUtils.isModified(versionElement, previousVersion)) {
            versionElement.remove();
            traceElement.addEdge(EdgeLabels.LATEST_VERSION, previousVersion);
            revision.addEdge(EdgeLabels.CONTAINS, previousVersion);
        }
    }
}
