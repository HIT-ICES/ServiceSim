/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.util.Iterator;

/**
 * This class represents a delay-topology storing every distance between connected nodes
 *
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class DelayMatrix_Float {

    /**
     * matrix holding delay information between any two nodes
     */
    protected float[][] mDelayMatrix = null;

    /**
     * number of nodes in the distance-aware-topology
     */
    protected int mTotalNodeNum = 0;

    /**
     * private constructor to ensure that only a correct initialized delay-matrix could be created
     */
    @SuppressWarnings("unused")
    private DelayMatrix_Float() {
    }

    /**
     * this constructor creates and correct initialized Float-Delay-Matrix
     *
     * @param graph    the topological graph as source-information
     * @param directed true if a directed matrix should be computed, false otherwise
     */
    public DelayMatrix_Float(TopologicalGraph graph, boolean directed) {

        // lets preInitialize the Delay-Matrix
        createDelayMatrix(graph, directed);

        // now it's time to calculate all possible connection-delays
        calculateShortestPath();
    }

    /**
     * @param srcID  the id of the source-node
     * @param destID the id of the destination-node
     * @return the delay-count between the given two nodes
     */
    public float getDelay(int srcID, int destID) {
        // check the nodeIDs against internal array-boundary's
        if (srcID > mTotalNodeNum || destID > mTotalNodeNum) {
            throw new ArrayIndexOutOfBoundsException("srcID or destID is higher than highest stored node-ID!");
        }

        return mDelayMatrix[srcID][destID];
    }

    /**
     * creates all internal necessary network-distance structures from the given graph for
     * similarity we assume all communication-distances are symmetrical thus leads to an undirected
     * network
     *
     * @param graph    this graph contains all node and link information
     * @param directed defines to preInitialize a directed or undirected Delay-Matrix!
     */
    private void createDelayMatrix(TopologicalGraph graph, boolean directed) {

        // number of nodes inside the network
        mTotalNodeNum = graph.getNumberOfNodes();

        mDelayMatrix = new float[mTotalNodeNum][mTotalNodeNum];

        // cleanup the complete distance-matrix with "0"s
        for (int row = 0; row < mTotalNodeNum; ++row) {
            for (int col = 0; col < mTotalNodeNum; ++col) {
                mDelayMatrix[row][col] = Float.MAX_VALUE;
            }
        }

        Iterator<TopologicalLink> itr = graph.getLinkIterator();

        TopologicalLink edge;
        while (itr.hasNext()) {
            edge = itr.next();

            mDelayMatrix[edge.getSrcNodeID()][edge.getDestNodeID()] = edge.getLinkDelay();

            if (!directed) {
                // according to approximate of symmetry to all communication-paths
                mDelayMatrix[edge.getDestNodeID()][edge.getSrcNodeID()] = edge.getLinkDelay();
            }

        }
    }

    /**
     * just calculates all pairs shortest paths
     */
    private void calculateShortestPath() {
        FloydWarshall_Float floyd = new FloydWarshall_Float();

        floyd.initialize(mTotalNodeNum);
        mDelayMatrix = floyd.allPairsShortestPaths(mDelayMatrix);
    }

    /**
     * this method just creates a string-output from the internal structures... e.g., prints out the
     * delay-matrix...
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("just a simple printout of the distance-aware-topology-class\n");
        buffer.append("delay-matrix is:\n");

        for (int column = 0; column < mTotalNodeNum; ++column) {
            buffer.append("\t").append(column);
        }

        for (int row = 0; row < mTotalNodeNum; ++row) {
            buffer.append("\n").append(row);

            for (int col = 0; col < mTotalNodeNum; ++col) {
                if (mDelayMatrix[row][col] == Float.MAX_VALUE) {
                    buffer.append("\t" + "-");
                } else {
                    buffer.append("\t").append(mDelayMatrix[row][col]);
                }
            }
        }

        return buffer.toString();
    }
}
