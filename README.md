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

# Test examples #

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
