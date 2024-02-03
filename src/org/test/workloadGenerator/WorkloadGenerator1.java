package org.test.workloadGenerator;

import org.cloudbus.cloudsim.Log;
import org.utils.PolicyConstants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class WorkloadGenerator1 {
    public static void main(String[] args) {
        // One request every 100ms, randomly selected by service chain and userlevel.
        // Request for a total of 60 minutes.
        String fileName = "src//others//workloadTest1.csv";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(fileName);
            long userId = 0;
            double timeslot = 0;
            while (timeslot < 1 * PolicyConstants.aMinute){
                Random random = new Random();
                int serviceChainId = random.nextInt(2);
                int userLevel = random.nextInt(3);
                Log.printLine(serviceChainId+ " " + userId + " " + userLevel);
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
                userId ++;
                timeslot += 0.02; // 20ms
            }

        }catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        }
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
