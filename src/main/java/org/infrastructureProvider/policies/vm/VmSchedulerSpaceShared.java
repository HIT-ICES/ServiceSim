/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.infrastructureProvider.policies.vm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.infrastructureProvider.entities.Pe;
import org.infrastructureProvider.entities.Vm;

import java.util.*;

/**
 * VmSchedulerSpaceShared is a VMM allocation policy that allocates one or more Pe to a VM, and
 * doesn't allow sharing of PEs.
 * If there are no free PEs to the VM, allocation fails. 
 * Free PEs are not allocated to VMs
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmSchedulerSpaceShared extends VmScheduler {

    /**
     * Map containing VM ID and a vector of PEs allocated to this VM.
     */
    private Map<String, List<Pe>> peAllocationMap;

    /**
     * The free pes vector.
     */
    private List<Pe> freePes;

    /**
     * Instantiates a new vm scheduler space shared.
     *
     * @param peList the peList
     */
    public VmSchedulerSpaceShared(List<? extends Pe> peList) {
        super(peList);
        setPeAllocationMap(new HashMap<>());
        setFreePes(new ArrayList<>());
        getFreePes().addAll(peList);
    }

    public VmSchedulerSpaceShared(JSONObject config)
    {
        this(new ArrayList<Pe>());

        JSONArray peListArray = config.getJSONArray("peList");
        for (int i = 0; i < peListArray.size(); i++) {
            JSONObject peConfig = peListArray.getJSONObject(i);
            getPeList().add(new Pe(peConfig));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.cloudbus.cloudsim.VmScheduler#allocatePesForVm(org.cloudbus.cloudsim.Vm,
     * java.util.List)
     */
    @Override
    public boolean allocatePesForVm(Vm vm, List<Double> mipsShare) {
        // if there are no enough free PEs, fails
        if (getFreePes().size() < mipsShare.size()) {
            return false;
        }

        List<Pe> selectedPes = new ArrayList<>();
        Iterator<Pe> peIterator = getFreePes().iterator();
        Pe pe = peIterator.next();
        double totalMips = 0;
        for (Double mips : mipsShare) {
            if (mips <= pe.getMips()) {
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

        getFreePes().removeAll(selectedPes);

        getPeAllocationMap().put(vm.getUid(), selectedPes);
        getMipsMap().put(vm.getUid(), mipsShare);
        setAvailableMips(getAvailableMips() - totalMips);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.cloudbus.cloudsim.VmScheduler#deallocatePesForVm(org.cloudbus.cloudsim.Vm)
     */
    @Override
    public void deallocatePesForVm(Vm vm) {
        getFreePes().addAll(getPeAllocationMap().get(vm.getUid()));
        getPeAllocationMap().remove(vm.getUid());

        double totalMips = 0;
        for (double mips : getMipsMap().get(vm.getUid())) {
            totalMips += mips;
        }
        setAvailableMips(getAvailableMips() + totalMips);

        getMipsMap().remove(vm.getUid());
    }

    /**
     * Gets the pe allocation map.
     *
     * @return the pe allocation map
     */
    protected Map<String, List<Pe>> getPeAllocationMap() {
        return peAllocationMap;
    }

    /**
     * Sets the pe allocation map.
     *
     * @param peAllocationMap the pe allocation map
     */
    protected void setPeAllocationMap(Map<String, List<Pe>> peAllocationMap) {
        this.peAllocationMap = peAllocationMap;
    }

    /**
     * Gets the free pes vector.
     *
     * @return the free pes vector
     */
    protected List<Pe> getFreePes() {
        return freePes;
    }

    /**
     * Sets the free pes vector.
     *
     * @param freePes the new free pes vector
     */
    protected void setFreePes(List<Pe> freePes) {
        this.freePes = freePes;
    }

}
