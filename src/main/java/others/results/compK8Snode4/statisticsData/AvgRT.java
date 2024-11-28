package others.results.compK8Snode4.statisticsData;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AvgRT {
    private static final Logger logger = LoggerFactory.getLogger(AvgRT.class);

    public static void main(String[] args) {

        Map<Integer, Map<Integer, Double>> avgTime = readFile("src//others//results//compK8Snode4//workloadResult35.csv", Boolean.FALSE);

        for (int serChain : avgTime.keySet()) {

            FileWriter fileWriter = null;
            String fileName = "src//others//results//compK8Snode4//statisticsData//exp35//BS" + serChain + ".txt";
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
        Map<Integer, Map<Integer, Double>> avgTime = new HashMap<>(); // service chain -> time ->
        Map<Integer, Map<Integer, Integer>> num = new HashMap<>();//service chain -> time ->
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
                    int requestTime1 = (int) requestTime;
                    double time = Double.parseDouble(rowStr[7]);
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
                logger.error("Error in reading file", e);
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
}
