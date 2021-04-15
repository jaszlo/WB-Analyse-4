package app.analysis;

import app.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class TestCentralization {

    @Test
    void getCentralization() {
    	double err = 0.001;
//
    	Graph graph = TestCentrality.getTestGraph("star");
    	assertEquals(1.0,Centralization.getCentralization(graph, Centrality::getBetweennessCentralities),err);
    	assertEquals(1.0,Centralization.getCentralization(graph, Centrality::getClosenessCentralities),err);
    	assertEquals(1.0000000000000002,Centralization.getCentralization(graph, Centrality::getEigenvectorCentralities),err);
    	assertEquals(1.0,Centralization.getCentralization(graph, Centrality::getHarmonicCentralities),err);
    	assertEquals(1.0,Centralization.getCentralization(graph, Centrality::getDegreeCentralities),err);
    	assertEquals(1.0,Centralization.getCentralization(graph, Centrality::getWeightedDegreeCentralities),err);
        
    	graph = TestCentrality.getTestGraph("bin_tree");
    	assertEquals(0.3999,Centralization.getCentralization(graph, Centrality::getBetweennessCentralities),err);
    	assertEquals(0.3699,Centralization.getCentralization(graph, Centrality::getClosenessCentralities),err);
    	assertEquals(0.3983,Centralization.getCentralization(graph, Centrality::getEigenvectorCentralities),err);
    	assertEquals(0.3666,Centralization.getCentralization(graph, Centrality::getHarmonicCentralities),err);
    	assertEquals(0.6000,Centralization.getCentralization(graph, Centrality::getDegreeCentralities),err);
    	assertEquals(0.6000,Centralization.getCentralization(graph, Centrality::getWeightedDegreeCentralities),err);
    	
    	graph = TestCentrality.getTestGraph("spezifikationsgraph");
    	assertEquals(0.5133,Centralization.getCentralization(graph, Centrality::getBetweennessCentralities),err);
    	assertEquals(0.6695,Centralization.getCentralization(graph, Centrality::getClosenessCentralities),err);
    	assertEquals(0.4703,Centralization.getCentralization(graph, Centrality::getEigenvectorCentralities),err);
    	assertEquals(1.5200,Centralization.getCentralization(graph, Centrality::getHarmonicCentralities),err);
    	assertEquals(0.4285,Centralization.getCentralization(graph, Centrality::getDegreeCentralities),err);
    	assertEquals(0.4957,Centralization.getCentralization(graph, Centrality::getWeightedDegreeCentralities),err);

    	graph = TestCentrality.getTestGraph("random1");
    	assertEquals(0.5416,Centralization.getCentralization(graph, Centrality::getBetweennessCentralities),err);
    	assertEquals(0.5518,Centralization.getCentralization(graph, Centrality::getClosenessCentralities),err);
    	assertEquals(0.6529,Centralization.getCentralization(graph, Centrality::getEigenvectorCentralities),err);
    	assertEquals(0.5277,Centralization.getCentralization(graph, Centrality::getHarmonicCentralities),err);
    	assertEquals(0.5555,Centralization.getCentralization(graph, Centrality::getDegreeCentralities),err);
    	assertEquals(0.5555,Centralization.getCentralization(graph, Centrality::getWeightedDegreeCentralities),err);
    }
}