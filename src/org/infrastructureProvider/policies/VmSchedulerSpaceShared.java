/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.infrastructureProvider.policies;

import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;

import java.util.*;

/**
 * VmSchedulerSpaceShared is a VMM allocation policy that allocates one or more Pe to a VM, and
 * doesn't allow sharing of PEs. If there is no free PEs to the VM, allocation fails. Free PEs are
 * not allocated to VMs
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerSpaceShared extends VmSchedulerBase {

	/** Map containing VM ID and a vector of PEs allocated to this VM. */
	protected final Map<Host, Map<String, List<Pe>>> peAllocationMap = new HashMap<>();

	/** The free pes vector. */
	protected final Map<Host, List<Pe>> freePesMap = new HashMap<>();

	@Override
	public void manage(Host host) {
		super.manage(host);
		peAllocationMap.put(host, new HashMap<>());
		freePesMap.put(host, host.getPeList().stream().toList());
	}
	/**
	 * Instantiates a new vm scheduler space shared.
	 *
	 */
	public VmSchedulerSpaceShared(VmResourceProvisioner<Pe, Double> provisioner) {
		super(provisioner);
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmSchedulerBase#allocatePesForVm(org.cloudbus.cloudsim.Vm,
	 * java.util.List)
	 */
	@Override
	public boolean allocatePesForVm(Host host,Vm vm, List<Double> mipsShare) {
		// if there is no enough free PEs, fails
		if (getFreePes(host).size() < mipsShare.size()) {
			return false;
		}

		List<Pe> selectedPes = new ArrayList<Pe>();
		Iterator<Pe> peIterator = getFreePes(host).iterator();
		Pe pe = peIterator.next();
		double totalMips = 0;
		for (Double mips : mipsShare) {
			if (mips <= pe.getTotalMips()) {
				selectedPes.add(pe);
				if (!peIterator.hasNext()) {
					break;
				}
				pe = peIterator.next();
				totalMips += mips;
			}
		}
		if (mipsShare.size() > selectedPes.size()) {
			return false;
		}

		getFreePes(host).removeAll(selectedPes);

		getPeAllocationMap(host).put(vm.getUid(), selectedPes);
		mipsMap.get(host).put(vm.getUid(), mipsShare);
		availableMipsMap.put(host,getAvailableMips(host) - totalMips);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmSchedulerBase#deallocatePesForVm(org.cloudbus.cloudsim.Vm)
	 */
	@Override
	public void deallocatePesForVm(Host host,Vm vm) {
		getFreePes(host).addAll(getPeAllocationMap(host).get(vm.getUid()));
		getPeAllocationMap(host).remove(vm.getUid());

		double totalMips = 0;
		for (double mips : mipsMap.get(host).get(vm.getUid())) {
			totalMips += mips;
		}
		availableMipsMap.put(host,getAvailableMips(host) + totalMips);

		mipsMap.get(host).remove(vm.getUid());
	}

	/**
	 * Sets the pe allocation map.
	 * 
	 * @param peAllocationMap the pe allocation map
	 */
	protected void setPeAllocationMap(Host host,Map<String, List<Pe>> peAllocationMap) {
		this.peAllocationMap.put(host, peAllocationMap);
	}

	/**
	 * Gets the pe allocation map.
	 * 
	 * @return the pe allocation map
	 */
	protected Map<String, List<Pe>> getPeAllocationMap(Host host) {
		return peAllocationMap.get(host);
	}

	/**
	 * Sets the free pes vector.
	 * 
	 * @param freePes the new free pes vector
	 */
	protected void setFreePes(Host host,List<Pe> freePes) {
		this.freePesMap.put(host,freePes);
	}

	/**
	 * Gets the free pes vector.
	 * 
	 * @return the free pes vector
	 */
	protected List<Pe> getFreePes(Host host) {
		return freePesMap.get(host);
	}

}
