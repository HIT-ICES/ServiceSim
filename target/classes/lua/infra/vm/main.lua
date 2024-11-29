PeList = java.import('org.cloudbus.cloudsim.lists.PeList')
Log = java.import('org.cloudbus.cloudsim.Log')

VmScheduler = {
    peList = {},            -- The peList.
    peMap = {},             -- The map of VMs to PEs.
    mipsMap = {},           -- The MIPS that are currently allocated to the VMs.
    availableMips = 0,      -- The total available mips.
    vmsMigratingIn = {},    -- The VMs migrating in.
    vmsMigratingOut = {}    -- The VMs migrating out.
}

--[[
* Creates a new HostAllocationPolicy.
*
* @param peList the peList
* @pre peList != $null
* @post $none
--]]
function VmScheduler:new(o, peList)
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    self.peList = peList or {}
    self.peMap = {}
    self.mipsMap = {}
    self.availableMips = PeList:getTotalMips(self.peList)
    self.vmsMigratingIn = {}
    self.vmsMigratingOut = {}
    return o
end

--[[
* Allocates PEs for a VM.
*
* @param vm        the vm
* @param mipsShare the mips share
* @return $true if this policy allows a new VM in the host, $false otherwise
* @pre $none
* @post $none
--]]
function VmScheduler:allocatePesForVm(vm, mipsShare)
    -- Abstract method, should be implemented in derived classes
end

--[[
* Releases PEs allocated to a VM.
*
* @param vm the vm
* @pre $none
* @post $none
--]]
function VmScheduler:deallocatePesForVm(vm)
    -- Abstract method, should be implemented in derived classes
end

--[[
* Releases PEs allocated to all the VMs.
*
* @pre $none
* @post $none
--]]
function VmScheduler:deallocatePesForAllVms()
    self.mipsMap = {}
    self.availableMips = PeList:getTotalMips(self.peList)
    for _, pe in ipairs(self.peList) do
        pe:getPeProvisioner():deallocateMipsForAllVms()
    end
end

--[[
* Gets the pes allocated for vm.
*
* @param vm the vm
* @return the pes allocated for vm
--]]
function VmScheduler:getPesAllocatedForVM(vm)
    return self.peMap[vm:getUid()]
end

--[[
* Returns the MIPS share of each Pe that is allocated to a given VM.
*
* @param vm the vm
* @return a list containing the amount of MIPS in each pe that is available to the VM
* @pre $none
* @post $none
--]]
function VmScheduler:getAllocatedMipsForVm(vm)
    return self.mipsMap[vm:getUid()]
end

--[[
* Gets the total allocated MIPS for a VM over all the PEs.
*
* @param vm the vm
* @return the allocated mips for vm
--]]
function VmScheduler:getTotalAllocatedMipsForVm(vm)
    local allocated = 0
    local mipsMap = self:getAllocatedMipsForVm(vm)
    if mipsMap then
        for _, mips in ipairs(mipsMap) do
            allocated = allocated + mips
        end
    end
    return allocated
end

--[[
* Returns maximum available MIPS among all the PEs.
*
* @return max mips
--]]
function VmScheduler:getMaxAvailableMips()
    if not self.peList then
        Log:printLine("Pe list is empty")
        return 0
    end

    local max = 0.0

    for i = 0, self.peList:size() - 1 do
        local pe = self.peList:get(i)
        local tmp = pe:getPeProvisioner():getAvailableMips()
        if tmp > max then
            max = tmp
        end
    end

    return max
end

--[[
* Returns PE capacity in MIPS.
*
* @return mips
--]]
function VmScheduler:getPeCapacity()
    if not self.peList then
        Log:printLine("Pe list is empty")
        return 0
    end
    return self.peList:get(0):getMips()
end

--[[
* Gets the pe list.
*
* @return the pe list
--]]
function VmScheduler:getPeList()
    return self.peList
end

--[[
* Sets the pe list.
*
* @param peList the pe list
--]]
function VmScheduler:setPeList(peList)
    self.peList = peList
end

--[[
* Gets the mips map.
*
* @return the mips map
--]]
function VmScheduler:getMipsMap()
    return self.mipsMap
end

--[[
* Sets the mips map.
*
* @param mipsMap the mips map
--]]
function VmScheduler:setMipsMap(mipsMap)
    self.mipsMap = mipsMap
end

--[[
* Gets the available mips.
*
* @return the available mips
--]]
function VmScheduler:getAvailableMips()
    return self.availableMips
end

--[[
* Sets the available mips.
*
* @param availableMips the new available mips
--]]
function VmScheduler:setAvailableMips(availableMips)
    self.availableMips = availableMips
end

--[[
* Gets the vms migrating out.
*
* @return the vms migrating out
--]]
function VmScheduler:getVmsMigratingOut()
    return self.vmsMigratingOut
end

--[[
* Sets the vms migrating out.
*
* @param vmsMigratingOut the new vms migrating out
--]]
function VmScheduler:setVmsMigratingOut(vmsMigratingOut)
    self.vmsMigratingOut = vmsMigratingOut
end

--[[
* Gets the vms migrating in.
*
* @return the vms migrating in
--]]
function VmScheduler:getVmsMigratingIn()
    return self.vmsMigratingIn
end

--[[
* Sets the vms migrating in.
*
* @param vmsMigratingIn the new vms migrating in
--]]
function VmScheduler:setVmsMigratingIn(vmsMigratingIn)
    self.vmsMigratingIn = vmsMigratingIn
end

--[[
* Gets the pe map.
*
* @return the pe map
--]]
function VmScheduler:getPeMap()
    return self.peMap
end

--[[
* Sets the pe map.
*
* @param peMap the pe map
--]]
function VmScheduler:setPeMap(peMap)
    self.peMap = peMap
end