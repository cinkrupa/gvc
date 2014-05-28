package pl.edu.agh.gvc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.*;

import java.util.*;

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
        Iterable<? extends Element> it =
                Vertex.class.isAssignableFrom(eClass) ? graph.getVertices(key, value) : graph.getEdges(key, value);
        Iterator<? extends Element> iter = it.iterator();

        E e = null;

        if (iter.hasNext()) {
            e = (E) iter.next();
        }

        if (iter.hasNext()) throw new IllegalStateException(String.format("Multiple elements found."));

        return e;
    }

    public static <E extends Element> E getSingleElement(Iterable<E> it) {
        Iterator<E> iter = it.iterator();
        E e = null;

        if (iter.hasNext()) {
            e = iter.next();
        }

        if (iter.hasNext()) throw new IllegalStateException(String.format("Multiple elements found."));

        return e;
    }

    public static boolean hasMultipleElements(Iterable<? extends Element> it) {
        Iterator<? extends Element> iter = it.iterator();
        if (iter.hasNext()) {
            iter.next();
            if (iter.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEmpty(Iterable<?> it) {
        return !it.iterator().hasNext();
    }

    public static boolean isModified(Element e1, Element e2) {
        Set<String> propertyKeys1 = e1.getPropertyKeys();
        Set<String> propertyKeys2 = e2.getPropertyKeys();
        if (propertyKeys1.equals(propertyKeys2)) {
            for (String key : propertyKeys1) {
                Object property1 = e1.getProperty(key);
                Object property2 = e2.getProperty(key);
                if (property1 == null) {
                    if (property2 != null) {
                        return true;
                    }
                } else if (!property1.equals(property2)) {
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
}
