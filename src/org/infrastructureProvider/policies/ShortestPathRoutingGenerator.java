package org.infrastructureProvider.policies;

import javafx.util.Pair;
import org.infrastructureProvider.entities.NetworkDevice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reference iFogSim2: Created by Samodha Pallewatta on 6/18/2021.
 * Creates a routing table considering shortest path between devices.
 */
public class ShortestPathRoutingGenerator {

    public static Map<Integer, Map<Integer, Integer>> generateRoutingTable(List<NetworkDevice> devices) {
        // <source device id>  ->  <dest device id,next device to route to>
        Map<Integer, Map<Integer, Integer>> routing = new HashMap<>();
        Map<String, Map<String, String>> routingString = new HashMap<>();
        int size = devices.size();

        int[][] routingMatrix = new int[size][size];
        double[][] distanceMatrix = new double[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                routingMatrix[row][column] = -1;
                distanceMatrix[row][column] = -1;
            }
        }

        boolean change = true;
        boolean firstIteration = true;
        while (change || firstIteration) {
            change = false;
            for (int row = 0; row < size; row++) {
                for (int column = 0; column < size; column++) {
                    double dist = distanceMatrix[row][column];
                    NetworkDevice rDevice = devices.get(row);
                    NetworkDevice cDevice = devices.get(column);
                    if (firstIteration && dist < 0) {
                        if (row == column) {
                            dist = 0;
                        } else {
                            dist = directlyConnectedDist(rDevice, cDevice);
                        }
                        if (dist >= 0) {
                            change = true;
                            distanceMatrix[row][column] = dist;
                            distanceMatrix[column][row] = dist;

                            // directly connected
                            routingMatrix[row][column] = cDevice.getId();
                            routingMatrix[column][row] = rDevice.getId();
                        }
                    }
                    if (dist < 0) {
                        Pair<Double, Integer> result = indirectDist(row, column, size, distanceMatrix);
                        dist = result.getKey();
                        int mid = result.getValue();
                        if (dist >= 0) {
                            change = true;
                            distanceMatrix[row][column] = dist;
                            routingMatrix[row][column] = routingMatrix[row][mid];
                        }
                    }
                    if (dist > 0) {
                        Pair<Double, Integer> result = indirectDist(row, column, size, distanceMatrix);
                        double distNew = result.getKey();
                        int mid = result.getValue();
                        if (distNew < dist) {
                            change = true;
                            distanceMatrix[row][column] = distNew;
                            routingMatrix[row][column] = routingMatrix[row][mid];
                        }
                    }
                }
            }
            firstIteration = false;
        }

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                int sourceId = devices.get(row).getId();
                int destId = devices.get(column).getId();
                if (routing.containsKey(sourceId)) {
                    routing.get(sourceId).put(destId, routingMatrix[row][column]);
                    routingString.get(devices.get(row).getName()).put(devices.get(column).getName(), getFogDeviceById(routingMatrix[row][column], devices).getName());
                } else {
                    Map<Integer, Integer> route = new HashMap<>();
                    route.put(destId, routingMatrix[row][column]);
                    routing.put(sourceId, route);

                    Map<String, String> routeS = new HashMap<>();
                    routeS.put(devices.get(column).getName(), getFogDeviceById(routingMatrix[row][column], devices).getName());
                    routingString.put(devices.get(row).getName(), routeS);
                }
            }
        }

        System.out.println("Routing Table : ");
        for (String deviceName : routingString.keySet()) {
            System.out.println(deviceName + " : " + routingString.get(deviceName).toString());
        }
        System.out.println("\n");

        return routing;
    }


    private static Pair<Double, Integer> indirectDist(int row, int dest, int size, double[][] distanceMatrix) {
        double minDistFromDirectConn = distanceMatrix[row][dest];
        int midPoint = -1;
        for (int column = 0; column < size; column++) {
            if (distanceMatrix[row][column] >= 0 && distanceMatrix[column][dest] >= 0) {
                double totalDist = distanceMatrix[row][column] + distanceMatrix[column][dest];
                if (minDistFromDirectConn >= 0 && totalDist < minDistFromDirectConn) {
                    minDistFromDirectConn = totalDist;
                    midPoint = column;
                } else if (minDistFromDirectConn < 0) {
                    minDistFromDirectConn = totalDist;
                    midPoint = column;
                }
            }
        }
        return new Pair<>(minDistFromDirectConn, midPoint);
    }

    private static double directlyConnectedDist(NetworkDevice rDevice, NetworkDevice cDevice) {
        List<Integer> parent = rDevice.getParentDeviceIds();
        List<Integer> children = rDevice.getChildDeviceIds();
        List<Integer> sameLevel = rDevice.getSameLevelDeviceIds();

        if (parent != null && parent.contains(cDevice.getId())) {
            return rDevice.getParentDevicesToChannel().get(cDevice.getId()).getLatency();
        } else if (children != null && children.contains(cDevice.getId())) {
            return rDevice.getChildDevicesToChannel().get(cDevice.getId()).getLatency();
        } else if (sameLevel != null && sameLevel.contains(cDevice.getId())) {
            return rDevice.getSameLevelDevicesToChannel().get(cDevice.getId()).getLatency();
        }
        return -1;
    }

    private static NetworkDevice getFogDeviceById(int id, List<NetworkDevice> devices) {
        for (NetworkDevice f : devices) {
            if (f.getId() == id)
                return f;
        }
        return null;
    }
}
