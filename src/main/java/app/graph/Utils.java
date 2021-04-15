package app.graph;

import app.data.AdjacencyList;
import app.data.SVGRequest;
import app.http.logger.Logger;
import app.http.logger.LoggerLevel;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Engine;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class Utils {

    private static String getColor(double weight, Map<String, String> colors) {

        java.awt.Color minColor = java.awt.Color.decode(colors.get("node_color_min"));
        java.awt.Color maxColor = java.awt.Color.decode(colors.get("node_color_max"));

        float[] hsbMinColor = new float[3];
        float[] hsbMaxColor = new float[3];
        float[] hsbResult   = new float[3];

        java.awt.Color.RGBtoHSB(minColor.getRed(), minColor.getGreen(), minColor.getBlue(), hsbMinColor);
        java.awt.Color.RGBtoHSB(maxColor.getRed(), maxColor.getGreen(), maxColor.getBlue(), hsbMaxColor);

        float diff = (Math.abs(hsbMaxColor[0] - hsbMinColor[0]) < 0.5) ? (hsbMaxColor[0] - hsbMinColor[0]) : hsbMaxColor[0] - hsbMinColor[0] - Math.signum(hsbMaxColor[0] - hsbMinColor[0]);
        hsbResult[0] = (float) (hsbMinColor[0] + (diff * weight) + 1) % 1;

        for(int i = 1; i < 3; i++) {
            diff = hsbMaxColor[i] - hsbMinColor[i];
            hsbResult[i] = (float) (hsbMinColor[i] + (diff * weight));
        }


        return String.format("#%06X", java.awt.Color.HSBtoRGB(hsbResult[0], hsbResult[1], hsbResult[2]) & 0x00FFFFFF);
    }

    private static int clamp(double x) {
        return (int) Math.max(0, Math.min(255, x));
    }

    @Deprecated
    public static String graphToSvg(SVGRequest svgRequest) {

        Graph graph = svgRequest.getGraph();
        Map<String, String> colors = svgRequest.getColors();

        Map<String, Double> measure = svgRequest.getCentralityFunction().apply(graph);

        String display = svgRequest.getDisplay();
        int distance = svgRequest.getDistance();
        DoubleSummaryStatistics stats = measure.values().stream().mapToDouble(Double::doubleValue).summaryStatistics();

        MutableGraph g = mutGraph("example1").setDirected(false).use((gr, ctx) -> {
            graphAttrs().add("overlap", false);
            graphAttrs().add("splines", true);
            graphAttrs().add("bgcolor", colors.get("graph_bgcolor"));
            graphAttrs().add("fontcolor", colors.get("graph_fontcolor"));

            nodeAttrs().add("shape", "circle");
            nodeAttrs().add("fixedsize", true);
            nodeAttrs().add("width", 1);
            nodeAttrs().add("fontcolor", colors.get("node_fontcolor"));

            linkAttrs().add("color", colors.get("edge_color"));
            linkAttrs().add("fontcolor", colors.get("edge_fontcolor"));


            Set<Vertex> toDraw = null;
            if (!display.equals("")) {
                toDraw = graph.getVertex(display).getReachableWithin(distance);
            }

            Map<String, MutableNode> graphvizVertices= new HashMap<>();
            for(Vertex vertex : (toDraw == null ? graph.getVertices() : toDraw)) {
                graphvizVertices.put(vertex.getIdentifier(), mutNode(vertex.getIdentifier()));
                // Don't worry about it
                double offset = measure.get(vertex.getIdentifier()) - stats.getMin();
                double range = stats.getMax() - stats.getMin();

                int color_r = 255;
                int color_g = 255;

                if(range != 0) {
                    color_r = clamp(511 * (1 - offset / range));
                    color_g = clamp(511 * offset / range);
                }

                Color nodeColor = Color.rgb(color_r, color_g, 0);
                graphvizVertices.get(vertex.getIdentifier()).add(Style.FILLED, nodeColor).add("xlabel", String.format("%.2f", measure.get(vertex.getIdentifier())));
            }

            for (Edge edge : graph.getEdges()) {
                MutableNode v1, v2;
                v1 = graphvizVertices.get(edge.getVertices()[0].getIdentifier());
                v2 = graphvizVertices.get(edge.getVertices()[1].getIdentifier());

                if (toDraw == null || (toDraw.contains(v1) && toDraw.contains(v2))) {
                    v1.links().add(v1.linkTo(v2).with(attr("penwidth", edge.getWeight() * 10)).with("label", edge.getWeight() + ""));
                }
            }
        });

        return Graphviz.fromGraph(g).engine(Engine.NEATO).render(Format.SVG).toString();
    }

    public static String graphToDot(SVGRequest svgRequest) {

        Graph graph = svgRequest.getGraph();
        Graph drawnWeights = new AdjacencyList(graph).asGraph(); // Dirty trick :D
        drawnWeights.invertEdgeWeights();
        drawnWeights.normalizeEdgeWeights();
        Map<String, String> colors = svgRequest.getColors();
        String display = svgRequest.getDisplay();
        int distance = svgRequest.getDistance();

        Map<String, Double> measure = svgRequest.getCentralityFunction().apply(graph);

        DoubleSummaryStatistics stats = measure.values().stream().mapToDouble(Double::doubleValue).summaryStatistics();

        StringBuilder result = new StringBuilder();

        result.append(String.format("""
                graph G {
                graph [overlap = false, splines=true, bgcolor="%s", fontcolor="%s"];
                node [shape="circle", fixedsize=true, width=1, fontcolor="%s"];
                edge [color="%s", fontcolor="%s"];
                """, colors.get("graph_bgcolor"), colors.get("graph_fontcolor"), colors.get("node_fontcolor"), colors.get("edge_color"), colors.get("edge_fontcolor")));

        Set<Vertex> toDraw = null;
        if (!display.equals("")) {
            toDraw = graph.getVertex(display).getReachableWithin(distance);
        }

        for(Vertex vertex: (toDraw == null ? graph.getVertices() : toDraw)) {
            double offset = measure.get(vertex.getIdentifier()) - stats.getMin();
            double range = stats.getMax() - stats.getMin();
            double normalized = 0.5;

            if(range != 0) {
                normalized = offset / range;
            }

            result.append(String.format("\"%s\" [fillcolor = \"%s\", style = \"filled\", xlabel = \"%.2f\"];%n", vertex.getIdentifier().replace("\"", "\\\""), getColor(normalized, colors), measure.get(vertex.getIdentifier())));
        }

        for(Edge edge: drawnWeights.getEdges()) {
            if (toDraw == null || (toDraw.contains(edge.getVertices()[0]) && toDraw.contains(edge.getVertices()[1]))) {
                result.append(String.format("\"%s\" -- \"%s\" [penwidth=%d, label=\"%.2f\"];%n", edge.getVertices()[0].getIdentifier().replace("\"", "\\\""), edge.getVertices()[1].getIdentifier().replace("\"", "\\\""), (int) (edge.getWeight() * 10), edge.getWeight()));
            }
        }

        result.append("}");
        return result.toString();
    }


    public static String graphToSvgWithExternalGraphviz(SVGRequest svgRequest) throws IOException{

        Runtime rt = Runtime.getRuntime();
        List<String> args = new ArrayList<>(List.of("dot", "-Tsvg", "-Kneato"));
        Process p;
        try {
            p = rt.exec(args.toArray(new String[0]));
        } catch(IOException e) {
            Logger.log("Error", "Could not start dot. Is GraphVIZ installed? Trying to use dot from wsl", LoggerLevel.BASIC);
            Logger.log(e);
            args.add(0, "wsl");
            p = rt.exec(args.toArray(new String[0]));
        }

        p.getOutputStream().write(graphToDot(svgRequest).getBytes(StandardCharsets.UTF_8));
        p.getOutputStream().close();

        try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining());
        }
    }
}
