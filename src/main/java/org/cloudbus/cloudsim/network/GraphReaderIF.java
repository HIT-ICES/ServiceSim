/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network;

import java.io.IOException;

/**
 * This interface abstracts a reader for different graph-file-formats
 *
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public interface GraphReaderIF {

    /**
     * this method just reads the file and creates a TopologicalGraph object
     *
     * @param filename name of the file to read
     * @return created TopologicalGraph
     * @throws IOException if an error occurs during reading the file
     */
    TopologicalGraph readGraphFile(String filename) throws IOException;

}
