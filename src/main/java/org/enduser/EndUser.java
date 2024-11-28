package org.enduser;

import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.enduser.networkPacket.NetworkConstants;
import org.enduser.networkPacket.NetworkPacket;
import org.serviceProvider.services.ServiceChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utils.Location;
import org.utils.ServiceSimEvents;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


@SuppressWarnings({"CommentedOutCode", "unused"})
public class EndUser extends SimEntity {
    private static final Logger logger = LoggerFactory.getLogger(EndUser.class);

    int low;
    int high;
    double timeUnit;
    double timeInterval;
    double simLimited;
    int serviceChainRange;
    int blockRange;
    Map<Integer, Map<Integer, Integer>> blockIndexServiceChain; // for type 3
    Map<Integer, ArrayList<Double>> blockProSubsection; // for type 3
    Map<Integer, ArrayList<Integer>> otherServiceChains; // for type 3
    int endUserId;
    int flag; // type
    // for type 4 and 5
    double addIntervalRange;
    // for type 6
    Map<Double, ArrayList<Pair<Integer, Double>>> timeToRequests; // time -> service chain -> delay
    String path;
    private int userId; // serviceProvider
    private double[][] dataset;

    /**
     * Creates a new entity.
     *
     * @param name the name to be associated with this entity
     */
    public EndUser(String name, String filename, int userId,
                   double simLimited) {
        super(name);

        //read workload: contains timestamp, service chainId, userId and response time for HTTP requests
        double[][] dataset = readWorkloadFile("src//others//", filename, Boolean.FALSE, simLimited);
        setDataset(dataset);
        this.userId = userId;
        endUserId = 0;
        flag = 0;
    }

    /* File is not used   */
    public EndUser(String name, int userId,
                   double simLimited, int low, int high, int timeUnit, double timeInterval, int serviceChainRage, int blockRange) {
        super(name);
        this.low = low;
        this.high = high;
        this.timeUnit = timeUnit;
        this.timeInterval = timeInterval;
        this.simLimited = simLimited;
        this.userId = userId;
        this.serviceChainRange = serviceChainRage;
        this.blockRange = blockRange;
        this.endUserId = 0;
        flag = 1;
    }

    public EndUser(String name, int userId,
                   double simLimited, int low, int high, int timeUnit, int serviceChainRage, int blockRange) {
        super(name);
        this.low = low;
        this.high = high;
        this.timeUnit = timeUnit;
        this.simLimited = simLimited;
        this.userId = userId;
        this.serviceChainRange = serviceChainRage;
        this.blockRange = blockRange;
        this.endUserId = 0;
        flag = 2;
    }

    /* Each block has the probability of service chain */
    public EndUser(String name, int userId,
                   double simLimited, int low, int high, double timeUnit, double timeInterval, int serviceChainRange, int blockRange, Map<Integer, Map<Integer, Double>> blockServiceChainPro) {
        super(name);
        this.low = low;
        this.high = high;
        this.timeUnit = timeUnit;
        this.timeInterval = timeInterval;
        this.simLimited = simLimited;
        this.userId = userId;
        this.blockRange = blockRange;
        this.serviceChainRange = serviceChainRange;

        this.blockIndexServiceChain = new HashMap<>();
        this.blockProSubsection = new HashMap<>();
        this.otherServiceChains = new HashMap<>();
        for (int block : blockServiceChainPro.keySet()) {
            blockIndexServiceChain.put(block, new HashMap<>());
            double start = 0;
            ArrayList<Double> subsection = new ArrayList<>();
            subsection.add(start);
            int index = 0;
            for (int serviceChain : blockServiceChainPro.get(block).keySet()) {
                start += blockServiceChainPro.get(block).get(serviceChain);
                subsection.add(start);
                blockIndexServiceChain.get(block).put(index, serviceChain);
                index++;
            }
            blockProSubsection.put(block, subsection);

            otherServiceChains.put(block, new ArrayList<>());
            for (int i = 0; i < serviceChainRange; i++) {
                if (!blockServiceChainPro.get(block).containsKey(i)) {
                    otherServiceChains.get(block).add(i);
                }
            }

        }

        this.endUserId = 0;
        flag = 3;
    }

    /* CompK8S - type4/5 */
    public EndUser(String name, int userId, double simLimited,
                   double timeUnit, double addIntervalRange, int blockRange, int flag) {
        super(name);
        this.userId = userId;
        this.simLimited = simLimited;
        this.timeUnit = timeUnit;
        this.addIntervalRange = addIntervalRange;
        this.blockRange = blockRange;
        this.endUserId = 0;
        this.flag = flag;
    }

    /* CompK8S - type4/5 */
    public EndUser(String name, int userId, double simLimited,
                   double timeUnit, double addIntervalRange, int blockRange, Map<Double, ArrayList<Pair<Integer, Double>>> timeToRequests, int flag) {
        super(name);
        this.userId = userId;
        this.simLimited = simLimited;
        this.timeUnit = timeUnit;
        this.addIntervalRange = addIntervalRange;
        this.blockRange = blockRange;
        this.endUserId = 0;
        this.timeToRequests = timeToRequests;
        this.flag = flag;
    }

    public EndUser(String name, int userId, double simLimited,
                   double timeUnit, double addIntervalRange, int blockRange, Map<Double, ArrayList<Pair<Integer, Double>>> timeToRequests, int flag, String path) {
        super(name);
        this.userId = userId;
        this.simLimited = simLimited;
        this.timeUnit = timeUnit;
        this.addIntervalRange = addIntervalRange;
        this.blockRange = blockRange;
        this.endUserId = 0;
        this.timeToRequests = timeToRequests;
        this.flag = flag;
        this.path = path;
    }

    private static int getPossionVariable(double lambda) {
        int x = 0;
        double y = Math.random(), cdf = getPossionProbability(x, lambda);
        while (cdf < y) {
            x++;
            cdf += getPossionProbability(x, lambda);
        }
        return x;
    }

    private static double getPossionProbability(int k, double lambda) {
        double c = Math.exp(-lambda), sum = 1;
        for (int i = 1; i <= k; i++) {
            sum *= lambda / i;
        }
        return sum * c;
    }

    @Override
    public void startEntity() {
        Log.printLine(getName() + " is starting...");
        if (flag == 0) {
            workloadGenerator();
        } else if (flag == 1) {
            workloadGeneratorRandom(); // random
        } else if (flag == 2) {
            workloadGeneratorPossion();
        } else if (flag == 3) {
            workloadGeneratorRandomBSCP();
        } else if (flag == 4) {
            workloadGeneratorCompK8SForTest2();
        } else if (flag == 5) {
            workloadGeneratorCompK8SForTest3();
        } else if (flag == 6) {
            workloadGeneratorCompK8S();
        } else if (flag == 7) {
            workloadGeneratorCompK8SNode4();
        }

    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == ServiceSimEvents.Workload_GENERATE) {
            if (flag == 1) {
                workloadGeneratorRandom();
            } else if (flag == 2) {
                workloadGeneratorPossion();
            } else if (flag == 4) {
                workloadGeneratorCompK8SForTest2();
            } else if (flag == 5) {
                workloadGeneratorCompK8SForTest3();
            } else if (flag == 6) {
                workloadGeneratorCompK8S();
            } else if (flag == 7) {
                workloadGeneratorCompK8SNode4();
            }
        } else {
            processOtherEvent(ev);
        }
    }

    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
        }
    }

    @Override
    public void shutdownEntity() {

    }

    /**
     * Read a CSV file
     *
     * @param filePath  path to the file
     * @param file      file name
     * @param labeled   if the file has a header
     * @return          dataset
     */
    public double[][] readWorkloadFile(String filePath, String file, boolean labeled, double simLimited) {
        double[][] dataset;
        ArrayList<ArrayList<Double>> dataList = new ArrayList<>();
        ArrayList<Double> row;
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(filePath + file));
            try {
                String line;
                if (labeled)
                    fileReader.readLine();
                while ((line = fileReader.readLine()) != null) {
                    String[] rowStr = line.split(",");

                    row = new ArrayList<>();
                    for (String s : rowStr) {
                        row.add(Double.valueOf(s));
                    }
                    if (row.get(0) > simLimited) {
                        break;
                    }
                    dataList.add(row);
                }

            } catch (IOException e) {
                logger.error("Error reading the file: {}", file);
            }
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", file);
        }
        System.out.println("The file: " + file + " was read");

        //Move to a new array
        dataset = new double[dataList.size()][7];

        for (int i = 0; i < dataList.size(); i++) {
            dataset[i][0] = dataList.get(i).get(0);
            dataset[i][1] = dataList.get(i).get(1); //service chainId
            dataset[i][2] = dataList.get(i).get(2); //end userId
            dataset[i][3] = dataList.get(i).get(3); // endUserLevel
            dataset[i][4] = dataList.get(i).get(4); //latitude
            dataset[i][5] = dataList.get(i).get(5); //longitude
            dataset[i][6] = dataList.get(i).get(6); //block
        }

        Log.printLine("workload dataset rows = " + dataset.length);
        return dataset;

    }

    /**
     * Generates workload and sends it to application provider continuously
     */
    protected void workloadGeneratorRandom() {

        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            //
            Random random = new Random();
            int totalNum;
            if (high == low) {
                totalNum = low;
            } else {
                totalNum = random.nextInt(high - low) + low;
            }
            int intervalNum = (int) (timeUnit / timeInterval);
            int numPerInterval = totalNum / intervalNum;
            for (int i = 0; i < timeUnit / timeInterval; i++) {
                for (int j = 0; j < numPerInterval; j++) {
                    EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                    ServiceChain servicechain = new ServiceChain(random.nextInt(serviceChainRange));
                    Location location = new Location(-1, -1, random.nextInt(blockRange));
                    NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                    NetworkConstants.currentAppId++;
                    Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                    send(userId, i * timeInterval, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
                }
            }
            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }
    }

    protected void workloadGenerator() {
        for (double[] request : dataset) {
            double delay = request[0];
            int serviceChainId = (int) request[1];
            int endUserId = (int) request[2];
            int userLevel = (int) request[3];
            double latitude = request[4];
            double longitude = request[5];
            int block = (int) request[6];
            EndUserInformation endUserInformation = new EndUserInformation(endUserId, userLevel);
            ServiceChain servicechain = new ServiceChain(serviceChainId);
            Location location = new Location(latitude, longitude, block);
            NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
            NetworkConstants.currentAppId++;
            Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
            send(userId, delay, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
        }
    }

    protected void workloadGeneratorPossion() {
        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            //
            Random random = new Random();
            int totalNum = random.nextInt(high - low) + low;

            double interval;
            double totalTime = 0;
            while ((currentTime + totalTime) < (currentTime + timeUnit)) {
                EndUserInformation endUserInformation = new EndUserInformation(endUserId, random.nextInt(1));
                this.endUserId++;
                ServiceChain servicechain = new ServiceChain(random.nextInt(serviceChainRange));
                Location location = new Location(-1, -1, random.nextInt(blockRange));
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, totalTime, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
                interval = nextTime(totalNum);
                totalTime = totalTime + interval;
            }
            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }

    }

    protected void workloadGeneratorRandomBSCP() {
        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            //
            Random random = new Random();
            int totalNum;
            if (high == low) {
                totalNum = low;
            } else {
                totalNum = random.nextInt(high - low) + low;
            }
            int intervalNum = (int) (timeUnit / timeInterval);
            int numPerInterval = totalNum / intervalNum;
            for (int i = 0; i < timeUnit / timeInterval; i++) {
                for (int j = 0; j < numPerInterval; j++) {
                    EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));

                    int block = random.nextInt(blockRange);
                    int highLevelBlock = block / (blockRange / blockProSubsection.size());
                    Location location = new Location(-1, -1, block);
                    double pro = random.nextDouble();
                    int serviceChainId = -1;
                    for (int h = 0; h < (blockProSubsection.get(highLevelBlock).size() - 1); h++) {
                        if (pro >= blockProSubsection.get(highLevelBlock).get(h) && pro < blockProSubsection.get(highLevelBlock).get(h + 1)) {
                            serviceChainId = blockIndexServiceChain.get(highLevelBlock).get(h);
                            if (serviceChainId == -1) {
                                int serviceChainIdIndex = random.nextInt(otherServiceChains.get(highLevelBlock).size());
                                serviceChainId = otherServiceChains.get(highLevelBlock).get(serviceChainIdIndex);
                            }
                            break;
                        }
                    }
                    if (serviceChainId == -1) {
                        Log.printLine("service chain id generate error");
                        return;
                    }
                    ServiceChain servicechain = new ServiceChain(serviceChainId);
                    NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                    NetworkConstants.currentAppId++;
                    Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                    send(userId, i * timeInterval, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
                }
            }
            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }
    }

    public double nextTime(double rateParameter) {
        Random random = new Random();

        return -Math.log(1.0 - random.nextDouble()) / rateParameter;
    }

    // CompK8S - 1
    // for testRequest2.py
    protected void workloadGeneratorCompK8SForTest2() {
        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            // BS0

            int num = 80;
            if (currentTime >= 100) {
                num = 20;
            }
            if (currentTime >= 160) {
                num = 80;
            }
            int num1 = getPossionVariable(num);
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(0);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS1
            num = 20;
            num1 = getPossionVariable(num);
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(1);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS2
//            num = 20;
            if (currentTime >= 300) {
                num = 80;
            }
            if (currentTime >= 360) {
                num = 20;
            }
            num1 = getPossionVariable(num);

            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(2);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS3
            num = 20;
            if (currentTime >= 500) {
                num = 80;
            }
            if (currentTime >= 560) {
                num = 20;
            }
            num1 = getPossionVariable(num);

            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(3);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            ArrayList<Integer> user_num1 = new ArrayList<>(Arrays.asList(20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 25, 20));
            ArrayList<Integer> user_num2 = new ArrayList<>(Arrays.asList(80, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 25, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80));

            // BS4
            int currentTime1 = (int) currentTime;
            int index = currentTime1 / 10 % 25;
            num = user_num1.get(index);
            num1 = getPossionVariable(num);

            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(4);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);

            }

            // BS5
            currentTime1 = (int) currentTime;
            index = currentTime1 / 10 % 25;
            num = user_num2.get(index);
            num1 = getPossionVariable(num);

            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(5);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }
            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }
    }

    // CompK8S - 1
    // for testRequest3.py
    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    protected void workloadGeneratorCompK8SForTest3() {
        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            // BS0

            int num = 20;
            //int num1 = getPossionVariable(num);
            int num1 = num;
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(0);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS1
//            num = 20;
            //num1 = getPossionVariable(num);
//            num1 = num;
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(1);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS2
//            num = 20;
            if (currentTime >= 300) {
                num = 80;
            }
            if (currentTime >= 360) {
                num = 20;
            }
            //num1 = getPossionVariable(num);
            num1 = num;
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(2);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS3
            num = 20;
            if (currentTime >= 500) {
                num = 80;
            }
            if (currentTime >= 560) {
                num = 20;
            }
            //num1 = getPossionVariable(num);
            num1 = num;
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(3);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            ArrayList<Integer> user_num1 = new ArrayList<>(Arrays.asList(20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 25, 20));
            ArrayList<Integer> user_num2 = new ArrayList<>(Arrays.asList(80, 75, 70, 65, 60, 55, 50, 45, 40, 35, 30, 25, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80));

            // BS4
            num = 20;
            //num1 = getPossionVariable(num);
            num1 = num;
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(4);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }

            // BS5
//            num = 20;
            //num1 = getPossionVariable(num);
//            num1 = num;
            for (int i = 0; i < num1; i++) {
                Random random = new Random();
                EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                int block = random.nextInt(blockRange);
                double timeInterval1 = random.nextDouble() * addIntervalRange;
                Location location = new Location(-1, -1, block);
                ServiceChain servicechain = new ServiceChain(5);
                NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                NetworkConstants.currentAppId++;
                Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                send(userId, timeInterval1, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
            }
            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }
    }

    //CompK8S - 1
    //for testRequest6.py
    protected void workloadGeneratorCompK8S() {
        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            ArrayList<Pair<Integer, Double>> requestToDelay = timeToRequests.get(currentTime);
            if (requestToDelay != null) {
                for (Pair<Integer, Double> serChainIdToDelay : requestToDelay) {
                    int serChainId = serChainIdToDelay.getKey();
                    double delay = serChainIdToDelay.getValue();

                    Random random = new Random();
                    EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                    Location location = new Location(-1, -1, 4);
                    ServiceChain servicechain = new ServiceChain(serChainId);
                    NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                    NetworkConstants.currentAppId++;
                    Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                    send(userId, delay, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
                }
            }

            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }
    }

    //CompK8S - 1
    //for 4 node
    protected void workloadGeneratorCompK8SNode4() {
        double currentTime = CloudSim.clock();
        if (currentTime < simLimited) {
            ArrayList<Pair<Integer, Double>> requestToDelay = timeToRequests.get(currentTime);
            if (requestToDelay != null) {
                for (Pair<Integer, Double> serChainIdToDelay : requestToDelay) {
                    int serChainId = serChainIdToDelay.getKey();
                    double delay = serChainIdToDelay.getValue();

                    Random random = new Random();
                    EndUserInformation endUserInformation = new EndUserInformation(endUserId++, random.nextInt(1));
                    int block = random.nextInt(blockRange);
                    if (serChainId == 1 || serChainId == 5) {
                        block = 1;
                    } else if (serChainId == 2) {
                        block = 2;
                    } else if (serChainId == 3) {
                        block = 3;
                    }
                    Location location = new Location(-1, -1, block);
                    ServiceChain servicechain = new ServiceChain(serChainId);
                    NetworkPacket networkPacket = new NetworkPacket(userId, NetworkConstants.REQUEST, NetworkConstants.currentAppId, servicechain, endUserInformation);
                    NetworkConstants.currentAppId++;
                    Pair<Location, NetworkPacket> data = new Pair<>(location, networkPacket);
                    send(userId, delay, ServiceSimEvents.EndUserRequest_ARRIVAL, data);
                }
            }

            send(getId(), timeUnit, ServiceSimEvents.Workload_GENERATE);
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double[][] getDataset() {
        return dataset;
    }

    public void setDataset(double[][] dataset) {
        this.dataset = dataset;
    }
}
