package org.test.workloadGenerator;

import org.cloudbus.cloudsim.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utils.PolicyConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class WorkloadGenerator1 {
    private static final Logger logger = LoggerFactory.getLogger(WorkloadGenerator1.class);
    public static void main(String[] args) {
        // One request every 100 ms, randomly selected by service chain and user level.
        // Request for a total of 60 minutes.
        String fileName = "src//others//workloadTest1.csv";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            long userId = 0;
            double timeslot = 0;
            while (timeslot < PolicyConstants.aMinute) {
                Random random = new Random();
                int serviceChainId = random.nextInt(2);
                int userLevel = random.nextInt(3);
                Log.printLine(serviceChainId + " " + userId + " " + userLevel);
                double latitude = -1; // not used
                double longitude = -1; // not used
                int block = random.nextInt(3);
                fileWriter.append(String.valueOf(timeslot));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(serviceChainId));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(userId));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(userLevel));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(latitude));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(longitude));
                fileWriter.append(',');
                fileWriter.append(String.valueOf(block));
                fileWriter.append('\n');
                userId++;
                timeslot += 0.02; // 20ms
            }

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            logger.error("Error in CsvFileWriter !!!");
        }
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
        } catch (IOException e) {
            logger.error("Error while flushing/closing fileWriter !!!");
        }
    }
}
