# ServiceSim: A Modelling and Simulation Toolkit of Microservice Systems #

ServiceSim is a toolkit built on the CloudSim simulation framework. It is designed to simulate the running of microservice systems in large-scale, distributed cloud-edge environments.

# Main features #

  * support for modeling and simulation of large scale cloud-edge computing environments
  * support for modeling and simulation of microservice systems, including service registration and discovery, load balancing, load admission, request dispatching and invocation relationship between microservices.
  * support for verification of various microservices system configuration policies, including service deployment, request dispatching, load balancing, and load admission, among others.

# Download #

The downloaded package contains all the source code, examples and jars.

# Publications #

  * Shi H, He X, Wang T, et al. ServiceSim: A Modelling and Simulation Toolkit of Microservice Systems in Cloud-Edge Environment[C]//International Conference on Service-Oriented Computing. Cham: Springer Nature Switzerland, 2023: 258-272.

# Test Examples #

* **TestExample**
    Test the processing of end-user requests generated from files on two edge nodes
* **TestExample1**
    Test the processing of randomly generated end-user requests on 32 edge nodes
* **TestExample2**
    Test the processing of randomly generated end-user requests on 32 edge nodes, and the deployment of microservices is different from TestExample1
* **CompK8STestExample2**
    The first test example compared with K8S. Test the processing of end-user requests that arrive at the system at specific times.
* **CompK8STestExample3**
    The second test example compared with K8S. Test the processing of end-user requests that arrive at the server where the first service is located at a specific time.
* **CompAllUpdateTestExample1**
    The third test example compared with K8S. Compared to the configuration of CompK8STestExample3, there are differences in the infrastructure setup.

**Explanation:** Partial files of test results have been retained in the project. Before running the above test examples, please delete these test result files or name them with a different name and generate a new test result file. Please refer to the test examples code for specific details.

# Configuration for Starting Simulation #

To create a new test example and start simulation, the following configuration is required:

* **DevicesProvider**
    You need to implement the interface class **DevicesProvider**. Implement this class to configure the cloud-edge environment. The project provides two simple implementations of **DevicesProvider**, namely **DevicesProviderSimple** and **DevicesProviderSimple1**. In the examples, initialization of the cloud-edge environment is required, including the nodes **(NetworkDevice)** and the network connections between the nodes **(Channel)**.

* **ServiceProvider**
    Once the cloud-edge environment is configured, the setup of the microservices system needs to be completed. You need to initialize a **ServiceProvider** class. The configuration includes the following:
    * **Servicechain**
    The invocation relationships between microservices are represented by **ServiceStage**. Refer to our publication or NetworkCloudSim for details.
    * **LoadAdmission**
    You can implement this interface class to design the admission conditions for end-user requests. The **NonLoadAdmission** has been implemented in the project to allow all end-user requests to enter the system for processing by default.
    * **LoadBalance**
    You can implement this interface class to design the load balancing mechanism for each node. The **RoundRobin** method has already been implemented in the project.
    * **RequestDispatchingRule**
    It holds the service discovery information for request dispatching between nodes. By implementing this class, you can control the scope and prioritize of request dispatching. The simple request dispatching priority rules are implemented in **RequestDispatchingSimple**.
    * **initInstance**
    The initial service deployment situation also needs to be configured.
    * **cloudletResultFile**
    Writing the results of the execution to files, and setting the filenames is also necessary.

* **EndUser**
    This class implements various methods for generating end-user requests, including reading from files and random generation. You can simulate the arrival of end-user requests to the system using these methods.

To initialize a new simulation instance, you need to complete the above configurations. After that, you can start running the simulation. The previous section introduced six test examples that can be used as references.

# Runtime Environment and Tool Recommendations #
* **Java JDK 1.8**
* **Intellij IDEA**



