/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.infrastructureProvider.policies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.lists.PeList;
import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VmSchedulerBase is an abstract class that represents the policy used by a VMM to share processing
 * power among VMs running in a host.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class VmSchedulerBase implements VmScheduler {

	protected final VmResourceProvisioner<Pe, Double> provisioner;

	/** The map of VMs to PEs. */
	protected final Map<Host, Map<String, List<Pe>>> peMap = new HashMap<>();
	/** The MIPS that are currently allocated to the VMs. */
	protected final Map<Host, Map<String, List<Double>>> mipsMap = new HashMap<>();
	/** The total available mips. */
	protected final Map<Host, Double> availableMipsMap = new HashMap<>();
	/** The VMs migrating in. */
	protected final Map<Host, List<String>> vmsMigratingInMap = new HashMap<>();
	/** The VMs migrating out. */
	protected final Map<Host, List<String>> vmsMigratingOutMap = new HashMap<>();
	@Override
	public void manage(Host host){
		peMap.put(host,new HashMap<>());
		mipsMap.put(host,new HashMap<>());
		availableMipsMap.put(host,0.0);
		vmsMigratingInMap.put(host,new ArrayList<>());
		vmsMigratingOutMap.put(host,new ArrayList<>());
	}
	/**
	 * Creates a new HostAllocationPolicy.
	 *
	 * @pre peList != $null
	 * @post $none
	 */
	public VmSchedulerBase(VmResourceProvisioner<Pe, Double> provisioner) {
		this.provisioner = provisioner;
	}

	/**
	 * Releases PEs allocated to all the VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	public void deallocatePesForAllVms(Host host) {
		mipsMap.getOrDefault(host, new HashMap<>()).clear();
		availableMipsMap.put(host, PeList.getTotalMips(getPeList(host)));
		for (Pe pe : getPeList(host)) {
			provisioner.deallocateForAllVms(pe);
		}
	}

	/**
	 * Gets the pes allocated for vm.
	 * 
	 * @param vm the vm
	 * @return the pes allocated for vm
	 */
	@Override
	public List<Pe> getPesAllocatedForVM(Host host, Vm vm) {
		return peMap.get(host).get(vm.getUid());
	}

	/**
	 * Returns the MIPS share of each Pe that is allocated to a given VM.
	 * 
	 * @param vm the vm
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	@Override
	public List<Double> getAllocatedMipsForVm(Host host, Vm vm) {
		return mipsMap.get(host).get(vm.getUid());
	}

	/**
	 * Gets the total allocated MIPS for a VM over all the PEs.
	 * 
	 * @param vm the vm
	 * @return the allocated mips for vm
	 */
	@Override
	public double getTotalAllocatedMipsForVm(Host host, Vm vm) {
		double allocated = 0;
		List<Double> mipsMap = getAllocatedMipsForVm(host, vm);
		if (mipsMap != null) {
			for (double mips : mipsMap) {
				allocated += mips;
			}
		}
		return allocated;
	}

	/**
	 * Returns maximum available MIPS among all the PEs.
	 * 
	 * @return max mips
	 */
	@Override
	public double getMaxAvailableMips(Host host) {
		if (host.getPeList().isEmpty()) {
			Log.printLine("Pe list is empty");
			return 0;
		}

		double max = 0.0;
		for (Pe pe : host.getPeList()) {
			double tmp = pe.getAvailableMips();
			if (tmp > max) {
				max = tmp;
			}
		}

		return max;
	}

	/**
	 * Returns PE capacity in MIPS.
	 *
	 * @return mips
	 */
	@Override
	public double getPeCapacity(Host host) {
		if (host.getPeList().isEmpty()) {
			Log.printLine("Pe list is empty");
			return 0;
		}
		return host.getPeList().getFirst().getTotalMips();
	}

	/**
	 * Gets the vm list.
	 *
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Pe> List<T> getPeList(Host host) {
		return (List<T>) host.getPeList();
	}


	/**
	 * Gets the free mips.
	 *
	 * @return the free mips
	 */
	@Override
	public double getAvailableMips(Host host) {
		return availableMipsMap.get(host);
	}

	/**
	 * Gets the vms in migration.
	 *
	 * @return the vms in migration
	 */
	@Override
	public List<String> getVmsMigratingOut(Host host) {
		return vmsMigratingOutMap.get(host);
	}


	/**
	 * Gets the vms migrating in.
	 *
	 * @return the vms migrating in
	 */
	@Override
	public List<String> getVmsMigratingIn(Host host) {
		return vmsMigratingInMap.get(host);
	}


	/**
	 * Gets the pe map.
	 *
	 * @return the pe map
	 */
	@Override
	public Map<String, List<Pe>> getPeMap(Host host) {
		return peMap.get(host);
	}


}
