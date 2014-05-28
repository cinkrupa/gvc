package pl.edu.agh.gvc.test;

import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import org.apache.commons.io.FileUtils;
import pl.edu.agh.gvc.graph.VersionedGraph;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VersionedGraphTestSuite extends GraphTest {

    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexTestSuite(this));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new EdgeTestSuite(this));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphTestSuite(this));
        printTestPerformance("GraphTestSuite", this.stopWatch());
    }

    public void testKeyIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new KeyIndexableGraphTestSuite(this));
        printTestPerformance("KeyIndexableGraphTestSuite", this.stopWatch());
    }

    public void testIndexableGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new IndexableGraphTestSuite(this));
        printTestPerformance("IndexableGraphTestSuite", this.stopWatch());
    }

    public void testIndexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new IndexTestSuite(this));
        printTestPerformance("IndexTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    @Override
    public Graph generateGraph() {
        try {
            FileUtils.deleteDirectory(new File("D:\\test\\neo4jdata"));
            FileUtils.deleteDirectory(new File("D:\\test\\neo4jversion"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Neo4j2Graph dataGraph = new Neo4j2Graph("D:\\test\\neo4jdata");
        Neo4j2Graph versionGraph = new Neo4j2Graph("D:\\test\\neo4jversion");
        return new VersionedGraph<Neo4j2Graph>(dataGraph, versionGraph);
    }

    @Override
    public Graph generateGraph(String s) {
        return new TinkerGraph();
    }

    @Override
    public void doTestSuite(final TestSuite testSuite) {
        String doTest = System.getProperty("testTinkerGraph");
        if (doTest == null || doTest.equals("true")) {
            for (Method method : testSuite.getClass().getDeclaredMethods()) {
                if (method.getName().startsWith("test")) {
                    System.out.println("Testing " + method.getName() + "...");
                    try {
                        method.invoke(testSuite);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
