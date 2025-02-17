/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a graph containing nodes and edges, used for input with a network-layer
 * Graphical-Output Restrictions!
 * EdgeColors: GraphicalProperties.getColorEdge NodeColors: GraphicalProperties.getColorNode
 *
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalGraph {

    private final List<TopologicalLink> linkList;

    private final List<TopologicalNode> nodeList;

    /**
     * just the constructor to create an empty graph-object
     */
    public TopologicalGraph() {
        linkList = new LinkedList<>();
        nodeList = new LinkedList<>();
    }

    /**
     * adds a link between two topological nodes
     *
     * @param edge the topological link
     */
    public void addLink(TopologicalLink edge) {
        linkList.add(edge);
    }

    /**
     * adds a Topological Node to this graph
     *
     * @param node the topological node to add
     */
    public void addNode(TopologicalNode node) {
        nodeList.add(node);
    }

    /**
     * returns the number of nodes contained inside the topological-graph
     *
     * @return number of nodes
     */
    public int getNumberOfNodes() {
        return nodeList.size();
    }

    /**
     * returns the number of links contained inside the topological-graph
     *
     * @return number of links
     */
    public int getNumberOfLinks() {
        return linkList.size();
    }

    /**
     * return an iterator through all network-graph links
     *
     * @return the iterator through all links
     */
    public Iterator<TopologicalLink> getLinkIterator() {
        return linkList.iterator();
    }

    /**
     * returns an iterator through all network-graph nodes
     *
     * @return the iterator through all nodes
     */
    public Iterator<TopologicalNode> getNodeIterator() {
        return nodeList.iterator();
    }

    /**
     * prints out all internal nodes and link information
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("topological-node-information: \n");

        for (TopologicalNode node : nodeList) {
            buffer.append(node.getNodeID()).append(" | x is: ").append(node.getCoordinateX()).append(" y is: ").append(node.getCoordinateY()).append("\n");
        }

        buffer.append("\n\n node-link-information:\n");

        for (TopologicalLink link : linkList) {
            buffer.append("from: ").append(link.getSrcNodeID()).append(" to: ").append(link.getDestNodeID()).append(" delay: ").append(link.getLinkDelay()).append("\n");
        }
        return buffer.toString();
    }

}
