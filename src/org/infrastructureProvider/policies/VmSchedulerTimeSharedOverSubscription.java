/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.infrastructureProvider.policies;

import org.infrastructureProvider.entities.Host;
import org.infrastructureProvider.entities.Pe;
import org.cloudbus.cloudsim.lists.PeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is a Time-Shared VM Scheduler, which allows over-subscription. In other words, the scheduler
 * still allows the allocation of VMs that require more CPU capacity that is available.
 * Oversubscription results in performance degradation.
 * 
 * @author Anton Beloglazov
 * @author Rodrigo N. Calheiros
 * @since CloudSim Toolkit 3.0
 */
public class VmSchedulerTimeSharedOverSubscription extends VmSchedulerTimeShared {

	/**
	 * Instantiates a new vm scheduler time shared over subscription.
	 * 

	 */
	public VmSchedulerTimeSharedOverSubscription(VmResourceProvisioner<Pe, Double> provisioner) {
		super(provisioner);
	}
	/**
	 * Allocate pes for vm. The policy allows over-subscription. In other words, the policy still
	 * allows the allocation of VMs that require more CPU capacity that is available.
	 * Oversubscription results in performance degradation. Each virtual PE cannot be allocated more
	 * CPU capacity than MIPS of a single PE.
	 * 
	 * @param vmUid the vm uid
	 * @param mipsShareRequested the mips share requested
	 * @return true, if successful
	 */
	@Override
	protected boolean allocatePesForVm(Host host, String vmUid, List<Double> mipsShareRequested) {
		double totalRequestedMips = 0;

		// 获取当前主机的 PE 容量
		double peMips = getPeCapacity(host);
		List<Double> mipsShareRequestedCapped = new ArrayList<>();

		for (Double mips : mipsShareRequested) {
			if (mips > peMips) {
				mipsShareRequestedCapped.add(peMips);
				totalRequestedMips += peMips;
			} else {
				mipsShareRequestedCapped.add(mips);
				totalRequestedMips += mips;
			}
		}

		mipsMapRequestedMap.get(host).put(vmUid, mipsShareRequested);
		setPesInUse(host, getPesInUse(host) + mipsShareRequested.size());

		if (getVmsMigratingIn(host).contains(vmUid)) {
			totalRequestedMips *= 0.1;
		}

		if (getAvailableMips(host) >= totalRequestedMips) {
			List<Double> mipsShareAllocated = new ArrayList<>();
			for (Double mipsRequested : mipsShareRequestedCapped) {
				if (getVmsMigratingOut(host).contains(vmUid)) {
					mipsRequested *= 0.9;
				} else if (getVmsMigratingIn(host).contains(vmUid)) {
					mipsRequested *= 0.1;
				}
				mipsShareAllocated.add(mipsRequested);
			}

			mipsMap.get(host).put(vmUid, mipsShareAllocated);
			availableMipsMap.put(host, getAvailableMips(host) - totalRequestedMips);
		} else {
			redistributeMipsDueToOverSubscription(host);
		}

		return true;
	}

	/**
	 * This method recalculates distribution of MIPs among VMs considering eventual shortage of MIPS
	 * compared to the amount requested by VMs.
	 */
	protected void redistributeMipsDueToOverSubscription(Host host) {
		// First, we calculate the scaling factor - the MIPS allocation for all VMs will be scaled
		// proportionally
		double totalRequiredMipsByAllVms = 0;

		Map<String, List<Double>> mipsMapCapped = new HashMap<String, List<Double>>();
		for (Entry<String, List<Double>> entry : getMipsMapRequested(host).entrySet()) {

			double requiredMipsByThisVm = 0.0;
			String vmId = entry.getKey();
			List<Double> mipsShareRequested = entry.getValue();
			List<Double> mipsShareRequestedCapped = new ArrayList<Double>();
			double peMips = getPeCapacity(host);
			for (Double mips : mipsShareRequested) {
				if (mips > peMips) {
					mipsShareRequestedCapped.add(peMips);
					requiredMipsByThisVm += peMips;
				} else {
					mipsShareRequestedCapped.add(mips);
					requiredMipsByThisVm += mips;
				}
			}

			mipsMapCapped.put(vmId, mipsShareRequestedCapped);

			if (getVmsMigratingIn(host).contains(entry.getKey())) {
				// the destination host only experience 10% of the migrating VM's MIPS
				requiredMipsByThisVm *= 0.1;
			}
			totalRequiredMipsByAllVms += requiredMipsByThisVm;
		}

		double totalAvailableMips = PeList.getTotalMips(getPeList(host));

		double scalingFactor = totalAvailableMips / totalRequiredMipsByAllVms;

		// Clear the old MIPS allocation
		mipsMap.get(host).clear();

		// Update the actual MIPS allocated to the VMs
		for (Entry<String, List<Double>> entry : mipsMapCapped.entrySet()) {
			String vmUid = entry.getKey();
			List<Double> requestedMips = entry.getValue();
			List<Double> updatedMipsAllocation = new ArrayList<Double>();
			for (Double mips : requestedMips) {
				if (getVmsMigratingOut(host).contains(vmUid)) {
					// the original amount is scaled
					mips *= scalingFactor;
					// performance degradation due to migration = 10% MIPS
					mips *= 0.9;
				} else if (getVmsMigratingIn(host).contains(vmUid)) {
					// the destination host only experiences 10% of the migrating VM's MIPS
					mips *= 0.1;
					// the final 10% of the requested MIPS are scaled
					mips *= scalingFactor;
				} else {
					mips *= scalingFactor;
				}

				updatedMipsAllocation.add(Math.floor(mips));
			}

			// add in the new map
			//System.out.println("Setting MIPS of "+vmUid+" to "+updatedMipsAllocation);
			mipsMap.get(host).put(vmUid, updatedMipsAllocation);

		}

		// As the host is oversubscribed, there no more available MIPS
		availableMipsMap.put(host,0.0);
	}

}
