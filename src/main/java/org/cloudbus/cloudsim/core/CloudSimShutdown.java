/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.CloudSimTags;

import java.util.Calendar;

/**
 * CloudSimShutdown waits for the termination of all CloudSim user entities to determine the end of simulation.
 * CloudSim will create this class upon initialization of the simulation,
 * i.e., done via <tt>CloudSim.init()</tt> method.
 * Hence, do not need to worry about creating an object of this class.
 * This object signals the end of simulation to CloudInformationService (GIS) entity.
 *
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class CloudSimShutdown extends SimEntity {

    /**
     * The num user.
     */
    private int numUser;

    /**
     * Allocates a new CloudSimShutdown object.
     * <p>
     * The total number of grid user entities plays an important role to determine whether all hostList should be shut
     * down or not.
     * If one or more users are still not finish, then the hostList will not be shut down.
     * Therefore, it is important to give a correct number of total grid user entities.
     * Otherwise, CloudSim program will freeze or encounter a unique behavior.
     *
     * @param name    the name to be associated with this entity (as required by SimEntity class)
     * @param numUser total number of grid user entities
     * @pre name != null
     * @pre numUser >= 0
     * @post $none
     * @see gridsim.CloudSim#init(int, Calendar, boolean)
     */
    public CloudSimShutdown(String name, int numUser) {
        // NOTE: This entity doesn't use any I/O port.
        // super (name, CloudSimTags.DEFAULT_BAUD_RATE);
        super(name);
        this.numUser = numUser;
    }

    /**
     * The main method that shuts down hostList and Cloud Information Service (GIS).
     * In addition, this method writes down a report at the end of a simulation based on <tt>reportWriterName</tt>
     * defined in the Constructor. <br> <b>NOTE:</b> This method shuts down grid hostList and GIS entities either
     * <tt>AFTER</tt> all grid users have been shut down
     * or an entity requires an abrupt end of the whole simulation.
     * In the first case, the number of grid users given in the Constructor <tt>must</tt> be correct.
     * Otherwise, CloudSim package hangs forever, or it does not terminate properly.
     *
     * @param ev the ev
     * @pre $none
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        numUser--;
        if (numUser == 0 || ev.getTag() == CloudSimTags.ABRUPT_END_OF_SIMULATION) {
            CloudSim.abruptlyTerminate();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.cloudbus.cloudsim.core.SimEntity#startEntity()
     */
    @Override
    public void startEntity() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * @see org.cloudbus.cloudsim.core.SimEntity#shutdownEntity()
     */
    @Override
    public void shutdownEntity() {
        // do nothing
    }

}
