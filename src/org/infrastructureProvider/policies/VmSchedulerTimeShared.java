/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.infrastructureProvider.policies;

import org.cloudbus.cloudsim.Log;
import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Pe;
import org.cloudbus.cloudsim.lists.PeList;
import org.infrastructureProvider.entities.Vm;

import java.util.*;

/**
 * VmSchedulerTimeShared is a VMM allocation policy that allocates one or more Pe to a VM, and
 * allows sharing of PEs by multiple VMs. This class also implements 10% performance degration due
 * to VM migration. This scheduler does not support over-subscription.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerTimeShared extends VmSchedulerBase {

	/** The mips map requested. */
	protected final Map<Host, Map<String, List<Double>>> mipsMapRequestedMap =new HashMap<>();

	/** The pes in use. */
	protected final Map<Host, Integer> pesInUseMap=new HashMap<>();
	@Override
	public void manage(Host host){
		super.manage(host);
		mipsMapRequestedMap.put(host, new HashMap<>());
		pesInUseMap.put(host, 0);
	}
	/**
	 * Instantiates a new vm scheduler time shared.
	 *
	 */
	public VmSchedulerTimeShared(VmResourceProvisioner<Pe,Double> provisioner) {
		super(provisioner);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.VmSchedulerBase#allocatePesForVm(cloudsim.Vm, java.util.List)
	 */
	@Override
	public boolean allocatePesForVm(Host host, Vm vm, List<Double> mipsShareRequested) {
		/**
		 * TODO: add the same to RAM and BW provisioners
		 */
		if (vm.isInMigration()) {
			if (!getVmsMigratingIn(host).contains(vm.getUid()) && !getVmsMigratingOut(host).contains(vm.getUid())) {
				getVmsMigratingOut(host).add(vm.getUid());
			}
		} else {
			if (getVmsMigratingOut(host).contains(vm.getUid())) {
				getVmsMigratingOut(host).remove(vm.getUid());
			}
		}
		boolean result = allocatePesForVm(host,vm.getUid(), mipsShareRequested);
		updatePeProvisioning(host);
		return result;
	}

	/**
	 * Allocate pes for vm.
	 *
	 * @param vmUid the vm uid
	 * @param mipsShareRequested the mips share requested
	 * @return true, if successful
	 */
	protected boolean allocatePesForVm(Host host, String vmUid, List<Double> mipsShareRequested) {
		double totalRequestedMips = 0;
		double peMips = getPeCapacity(host);
		for (Double mips : mipsShareRequested) {
			// each virtual PE of a VM must require not more than the capacity of a physical PE
			if (mips > peMips) {
				return false;
			}
			totalRequestedMips += mips;
		}

		// This scheduler does not allow over-subscription
		if (getAvailableMips(host) < totalRequestedMips) {
			return false;
		}

		getMipsMapRequested(host).put(vmUid, mipsShareRequested);
		setPesInUse(host,getPesInUse(host) + mipsShareRequested.size());

		if (getVmsMigratingIn(host).contains(vmUid)) {
			// the destination host only experience 10% of the migrating VM's MIPS
			totalRequestedMips *= 0.1;
		}

		List<Double> mipsShareAllocated = new ArrayList<Double>();
		for (Double mipsRequested : mipsShareRequested) {
			if (getVmsMigratingOut(host).contains(vmUid)) {
				// performance degradation due to migration = 10% MIPS
				mipsRequested *= 0.9;
			} else if (getVmsMigratingIn(host).contains(vmUid)) {
				// the destination host only experience 10% of the migrating VM's MIPS
				mipsRequested *= 0.1;
			}
			mipsShareAllocated.add(mipsRequested);
		}

		mipsMap.get(host).put(vmUid, mipsShareAllocated);
		availableMipsMap.put(host,getAvailableMips(host) - totalRequestedMips);

		return true;
	}

	/**
	 * Update allocation of VMs on PEs.
	 */
	protected void updatePeProvisioning(Host host) {
		getPeMap(host).clear();
		for (Pe pe : host.getPeList()) {
			provisioner.deallocateForAllVms(pe);
		}

		Iterator<Pe> peIterator = host.getPeList().iterator();
		Pe pe = peIterator.next();
		double availableMips = pe.getAvailableMips();

		for (Map.Entry<String, List<Double>> entry : mipsMap.get(host).entrySet()) {
			String vmUid = entry.getKey();
			getPeMap(host).put(vmUid, new LinkedList<Pe>());

			for (double mips : entry.getValue()) {
				while (mips >= 0.1) {
					var vm=host.getVmList().stream().filter(v->v.getUid().equals(vmUid)).findFirst().get();
					if (availableMips >= mips) {
						provisioner.allocateForVm(pe,vm, mips);
						getPeMap(host).get(vmUid).add(pe);
						availableMips -= mips;
						break;
					} else {
						provisioner.allocateForVm(pe,vm, availableMips);
						getPeMap(host).get(vmUid).add(pe);
						mips -= availableMips;
						if (mips <= 0.1) {
							break;
						}
						if (!peIterator.hasNext()) {
							Log.printLine("There is no enough MIPS (" + mips + ") to accommodate VM " + vmUid);
						}
						pe = peIterator.next();
						availableMips = pe.getAvailableMips();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.VmSchedulerBase#deallocatePesForVm(cloudsim.Vm)
	 */
	@Override
	public void deallocatePesForVm(Host host, Vm vm) {
		getMipsMapRequested(host).remove(vm.getUid());
		setPesInUse(host,0);
		mipsMap.get(host).clear();
		availableMipsMap.put(host,PeList.getTotalMips(host.getPeList()));

		for (Pe pe : host.getPeList()) {
			provisioner.deallocateForVm(pe,vm);
		}

		for (Map.Entry<String, List<Double>> entry : getMipsMapRequested(host).entrySet()) {
			allocatePesForVm(host,entry.getKey(), entry.getValue());
		}

		updatePeProvisioning(host);
	}


	/**
	 * Releases PEs allocated to all the VMs.
	 *
	 * @pre $none
	 * @post $none
	 */
	@Override
	public void deallocatePesForAllVms(Host host) {
		super.deallocatePesForAllVms(host);
		getMipsMapRequested(host).clear();
		setPesInUse(host,0);
	}

	/**
	 * Returns maximum available MIPS among all the PEs. For the time shared policy it is just all
	 * the avaiable MIPS.
	 *
	 * @return max mips
	 */
	@Override
	public double getMaxAvailableMips(Host host) {
		return getAvailableMips(host);
	}

	/**
	 * Sets the pes in use.
	 *
	 * @param pesInUse the new pes in use
	 */
	protected void setPesInUse(Host host, int pesInUse) {
		this.pesInUseMap.put(host,pesInUse);
	}

	/**
	 * Gets the pes in use.
	 *
	 * @return the pes in use
	 */
	protected int getPesInUse(Host host) {
		return pesInUseMap.get(host);
	}

	/**
	 * Gets the mips map requested.
	 *
	 * @return the mips map requested
	 */
	protected Map<String, List<Double>> getMipsMapRequested(Host host) {
		return mipsMapRequestedMap.get(host); //
	}

	/**
	 * Sets the mips map requested.
	 *
	 * @param mipsMapRequested the mips map requested
	 */
	protected void setMipsMapRequested(Host host, Map<String, List<Double>> mipsMapRequested) {
		this.mipsMapRequestedMap.put(host, mipsMapRequested);
	}

}
