/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.infrastructureProvider.policies;

import io.github.hit_ices.serviceSim.service.VmCloudletSchedulerManagerService;
import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Vm;

import java.util.List;
import java.util.Map;

/**
 * VmAllocationPolicy is an class that represents the provisioning policy of hosts to
 * virtual machines in a Datacentre. It supports two-stage commit of reservation of hosts: first, we
 * reserve the host and, once commited by the user, it is effectivelly allocated to he/she
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public interface VmAllocationPolicy {

	/**
	 * Allocates a host for a given VM. The host to be allocated is the one that was already
	 * reserved.
	 * 
	 * @param vm virtual machine which the host is reserved to
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocateHostForVm(Vm vm);

	/**
	 * Allocates a specified host for a given VM.
	 * 
	 * @param vm virtual machine which the host is reserved to
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocateHostForVm(Vm vm, Host host);

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * @param utilizationBound the utilization bound
	 * @param time the time
	 * @return the array list< hash map< string, object>>
	 */
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList);

	/**
	 * Releases the host used by a VM.
	 * 
	 * @param vm the vm
	 * @pre $none
	 * @post $none
	 */
	public void deallocateHostForVm(Vm vm);

	/**
	 * Get the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vm the vm
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	public Host getHost(Vm vm);

	/**
	 * Get the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vmId the vm id
	 * @param userId the user id
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	public Host getHost(int vmId, int userId);


	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList();

}
