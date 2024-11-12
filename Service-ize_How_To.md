# 如何对当前的Service Sim进行服务化？

## 什么是服务化的应用

首先，我们需要知道一个服务化的应用是什么样的。服务化的应用里有且只有两种对象：

1. **记录。** 这是一些实体，他们是不可变类型，所有字段（如果字段是集合或对象，那么也应该是只读集合或不可变对象），不应该有 **setter**。它们会在不同的服务间传递。
2. **服务。** 这是一些逻辑，其中的代码一般是对实体的操作，服务可以有状态，如现在的VmScheduler就是服务。其中的各个方法如果传入了实体，
   1. 要么，未对实体做任何修改
   2. 要么，返回修改了的实体

此外，推荐把服务定义成接口，而不是抽象类。把实现和定义区分开，以便后续的微服务化。

**服务化的应用可以无缝切换到微服务架构。** 这是显然的。

## 如何进行服务化

需要对现有ServiceSim的代码进行改造，使其中的类满足上节所述的两个条件。具体而言，是要讲前实体类变成记录，前服务类变成服务。

### Step 1: 控制反转

此步骤是要将实体中的逻辑全部移动到服务，即，将前实体类中的非属性方法全部移除，移动到相关的服务中。

例如，将Host的Provisioner们相关的内容悉数移除，转移到Provisioner中，Provisioner成为服务，而Host变成实体。
见：`src/org/infrastructureProvider/policies/VmResourceProvisioner.java`
```java
public interface VmResourceProvisioner<THost, TResource> {
    /**
     * Allocates RAM for a given VM on the specified host.
     *
     * @param host the host on which the RAM is being allocated
     * @param vm   the virtual machine for which the RAM is being allocated
     * @param res  the amount of RAM to allocate
     * @return true if the RAM could be allocated; false otherwise
     */
    boolean allocateForVm(THost host, Vm vm, TResource res);

    /**
     * Gets the allocated RAM for a VM on the specified host.
     *
     * @param host the host
     * @param vm   the VM
     * @return the allocated RAM for the VM
     */
    TResource getAllocatedForVm(THost host, Vm vm);

    /**
     * Releases RAM used by a VM on the specified host.
     *
     * @param host the host
     * @param vm   the VM
     */
    void deallocateForVm(THost host, Vm vm);

    void deallocateForAllVms(THost host);

    /**
     * Checks if the host has sufficient RAM for a given VM allocation.
     *
     * @param host the host
     * @param vm   the VM
     * @param res  the amount of resource to check
     * @return true if the host has sufficient RAM; false otherwise
     */
    boolean isSuitableForVm(THost host, Vm vm, TResource res);
}

```

可以看到我为它的每一个方法添加了参数`THost host`，于是原本对`Host`或`Pe`中方法的调用，可以移动到此服务中，`Host`或`Pe`由方法的主人变成服务方法的参数，是为控制反转。

如有必要，可以适当引入新服务，如`src/io/github/hit_ices/serviceSim/service/HostManager.java`，大部分操作拆分自Host

### Step 2: 实体化与服务化

将实体的getter全部去掉， **重写equals，** 使其变为真正的实体类（记录）。然后，在服务中重构写方法，让它们返回修改后的实体。