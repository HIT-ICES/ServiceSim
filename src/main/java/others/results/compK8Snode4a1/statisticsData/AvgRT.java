package others.results.compK8Snode4a1.statisticsData;


import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings({"CommentedOutCode", "unused", "SameParameterValue"})
public class AvgRT {
    private static final Logger logger = LoggerFactory.getLogger(AvgRT.class);

    public static void main(String[] args) {

        Map<Integer, Map<Integer, Double>> avgTime = readFile("src//others//results//compK8Snode4a1//workloadResult26.csv", Boolean.FALSE);

        for (int serChain : avgTime.keySet()) {

            FileWriter fileWriter = null;
            String fileName = "src//others//results//compK8Snode4a1//statisticsData//exp26//BS" + serChain + ".txt";
            try {
                fileWriter = new FileWriter(fileName, true);
                for (int requestTime : avgTime.get(serChain).keySet()) {
                    fileWriter.append(String.valueOf(requestTime)).append(" ").append(String.valueOf(avgTime.get(serChain).get(requestTime)));
                    fileWriter.append('\n');
                }

            } catch (Exception e) {
                System.out.println("Error in CsvFileWriter !!!");
                logger.error("Error in CsvFileWriter", e);
            }
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                logger.error("Error while flushing/closing fileWriter", e);
            }
        }


    }

    public static Map<Integer, Map<Integer, Double>> readFile(String filename, boolean labeled) {

        //Map<Double, 
        // ArrayList<Pair<Integer,Double>>> requestToDelay1 = getWorkload("src//org//test//workloadGenerator//k8snode4-1//request.txt",
        // 1688453347);
        Map<Double, ArrayList<Pair<Integer, Double>>> requestToDelay = getWorkloadForAccDelay("src//org//test//workloadGenerator//k8snode4-1//request.txt", 1688453347, "src//org//test//workloadGenerator//k8snode4-1//serRequest15//");


        Map<Integer, Map<Integer, Double>> avgTime = new HashMap<>(); // serviceChain -> time ->
        Map<Integer, Map<Integer, Integer>> num = new HashMap<>();//serviceChain -> time ->
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(filename));
            try {
                String line;
                if (labeled)
                    fileReader.readLine();
                while ((line = fileReader.readLine()) != null) {
                    String[] rowStr = line.split(",");
                    int serviceChainId = Integer.parseInt(rowStr[1]);
                    double requestTime = Double.parseDouble(rowStr[5]);
                    double time = Double.parseDouble(rowStr[7]);
                    int requestTime2 = (int) requestTime;
                    int requestTime1 = requestTime2;
//                    for (int i = requestTime2;i>=0;i--){
//                        double i1 = (double) i;
//                        int flag = 0;
//                        if (requestToDelay.containsKey(i1)){
//                            double delay1 = 0;
//                            for (int j = 0; j< requestToDelay.get(i1).size();j++){
//                                if (requestToDelay.get(i1).get(j).getKey() == serviceChainId){
//                                    double jj = requestToDelay.get(i1).get(j).getValue() + i1;
//                                    int jj1 = (int) jj;
//                                    if (jj1 == requestTime2){
//                                        delay1 = requestToDelay.get(i1).get(j).getValue();
//                                        Log.printLine(delay1);
//                                        time += delay1;
//                                        flag = 1;
//                                    }
//                                    break;
//                                }
//                            }
//                            if (flag == 1){
//                                requestTime1 = (int) (requestTime - delay1);
//                                break;
//                            }
//
//                        }
//                    }
                    for (int i = requestTime2 + 2; i >= 0; i--) {
                        int flag = 0;
                        if (requestToDelay.containsKey((double) i)) {
                            double delay1 = 0;
                            for (int j = 0; j < requestToDelay.get((double) i).size(); j++) {
                                if (requestToDelay.get((double) i).get(j).getKey() == serviceChainId) {
                                    double jj = requestToDelay.get((double) i).get(j).getValue() + (double) i;
                                    int jj1 = (int) jj;
                                    if (jj1 == requestTime2) {
                                        delay1 = requestToDelay.get((double) i).get(j).getValue();
                                        if (serviceChainId == 2) {
                                            Log.printLine(delay1);
                                        }

                                        time += delay1;
                                        flag = 1;
                                    }
                                    break;
                                }
                            }
                            if (flag == 1) {
                                requestTime1 = (int) (requestTime - delay1);
                                break;
                            }

                        }
                    }
//                    if (serviceChainId == 0){
//                        requestTime = requestTime - 0.0391;
//                    }else if (serviceChainId == 1){
//                        requestTime = requestTime - 0.1238;
//                    }else if (serviceChainId == 2){
//                        requestTime = requestTime - 0.0810;
//                    }else if (serviceChainId == 3){
//                        requestTime = requestTime - 0.2152;
//                    }else if (serviceChainId == 4){
//                        requestTime = requestTime -0.0517;
//                    }else if (serviceChainId == 5){
//                        requestTime = requestTime -0.1660;
//                    }
//                    int requestTime1 = (int) requestTime;
//
//                    if (serviceChainId == 0){
//                        time = time + 0.0391;
//                    }else if (serviceChainId == 1){
//                        time = time + 0.1238;
//                    }else if (serviceChainId == 2){
//                        time = time + 0.0810;
//                    }else if (serviceChainId == 3){
//                        time = time + 0.2152;
//                    }else if (serviceChainId == 4){
//                        time = time + 0.0517;
//                    }else if (serviceChainId == 5){
//                        time = time + 0.1660;
//                    }
                    //int requestTime1 = (int)requestTime;

                    if (avgTime.containsKey(serviceChainId)) {
                        if (avgTime.get(serviceChainId).containsKey(requestTime1)) {
                            avgTime.get(serviceChainId).put(requestTime1, avgTime.get(serviceChainId).get(requestTime1) + time);
                            num.get(serviceChainId).put(requestTime1, num.get(serviceChainId).get(requestTime1) + 1);

                        } else {
                            avgTime.get(serviceChainId).put(requestTime1, time);
                            num.get(serviceChainId).put(requestTime1, 1);
                        }
                    } else {
                        avgTime.put(serviceChainId, new TreeMap<>());
                        avgTime.get(serviceChainId).put(requestTime1, time);
                        num.put(serviceChainId, new TreeMap<>());
                        num.get(serviceChainId).put(requestTime1, 1);
                    }
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }
        for (int serviceChain : avgTime.keySet()) {
            for (int requestTime : avgTime.get(serviceChain).keySet()) {
                avgTime.get(serviceChain).put(requestTime, avgTime.get(serviceChain).get(requestTime) / num.get(serviceChain).get(requestTime));
            }

        }
        return avgTime;
    }


    private static Map<Double, ArrayList<Pair<Integer, Double>>> getWorkloadForAccDelay(String filename, double startTime, String path) {

        //Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay = getWorkload(filename,startTime);
        String file1 = path + "ser1.txt";
        String file2 = path + "ser2.txt";
        String file3 = path + "ser3.txt";
        String file4 = path + "ser4.txt";
        String file5 = path + "ser5.txt";
        String file6 = path + "ser6.txt";

        Map<Double, ArrayList<Pair<Integer, Double>>> requestToDelayNew = new HashMap<>();

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(file1));
            try {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }


        BufferedReader fileReader1;
        try {
            fileReader1 = new BufferedReader(new FileReader(file2));
            try {
                String line;
                while ((line = fileReader1.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }

        BufferedReader fileReader2;
        try {
            fileReader2 = new BufferedReader(new FileReader(file3));
            try {
                String line;
                while ((line = fileReader2.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }


        BufferedReader fileReader3;
        try {
            fileReader3 = new BufferedReader(new FileReader(file4));
            try {
                String line;
                while ((line = fileReader3.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }

        BufferedReader fileReader4;
        try {
            fileReader4 = new BufferedReader(new FileReader(file5));
            try {
                String line;
                while ((line = fileReader4.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (serChain != 4) {
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }

                    Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }


        BufferedReader fileReader5;
        try {
            fileReader5 = new BufferedReader(new FileReader(file6));
            try {
                String line;
                while ((line = fileReader5.readLine()) != null) {
                    String[] rowStr = line.split(" ");
                    double time = Double.parseDouble(rowStr[0]);
                    int serChain = Integer.parseInt(rowStr[1]);
                    double delay = Double.parseDouble(rowStr[3]);
                    if (serChain != 5) {
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)) {
                        requestToDelayNew.put(time, new ArrayList<>());
                    }
                    Pair<Integer, Double> pair = new Pair<>(serChain, delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                logger.error("Error in CsvFileReader", e);
            }
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        }

        return requestToDelayNew;
    }


}
