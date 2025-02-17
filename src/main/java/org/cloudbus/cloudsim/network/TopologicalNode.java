/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

/**
 * Just represents a topological network node retrieves its information from a
 * topological-generated file (e.g., topology-generator)
 *
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalNode {

    /**
     * its nodes-ID inside this network
     */
    private final int nodeID;

    /**
     * describes the nodes-name inside the network
     */
    private final String nodeName;

    /**
     * representing the x a y world-coordinates
     */
    private int worldX = 0;

    private int worldY = 0;

    /**
     * constructs a new node
     */
    public TopologicalNode(int nodeID) {
        // let's initialize all private class attributes
        this.nodeID = nodeID;
        nodeName = String.valueOf(nodeID);
    }

    /**
     * constructs a new node including world-coordinates
     */
    public TopologicalNode(int nodeID, int x, int y) {
        // let's initialize all private class attributes
        this.nodeID = nodeID;
        nodeName = String.valueOf(nodeID);
        worldX = x;
        worldY = y;
    }

    /**
     * constructs a new node including world-coordinates and the nodeName
     */
    public TopologicalNode(int nodeID, String nodeName, int x, int y) {
        // let's initialize all private class attributes
        this.nodeID = nodeID;
        this.nodeName = nodeName;
        worldX = x;
        worldY = y;
    }

    /**
     * delivers the nodes id
     *
     * @return just the nodeID
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * delivers the name of the node
     *
     * @return name of the node
     */
    public String getNodeLabel() {
        return nodeName;
    }

    /**
     * returns the x coordinate of this network-node
     *
     * @return the x coordinate
     */
    public int getCoordinateX() {
        return worldX;
    }

    /**
     * returns the y coordinate of this network-node
     *
     * @return the y coordinate
     */
    public int getCoordinateY() {
        return worldY;
    }

}
