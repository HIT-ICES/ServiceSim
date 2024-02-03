package others.results.compK8Snode4a1.statisticsData;


import javafx.util.Pair;
import org.cloudbus.cloudsim.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AvgRT {

    public static void main(String[] args) {

        Map<Integer, Map<Integer,Double>> avgtime = readFile("src//others//results//compK8Snode4a1//workloadResult26.csv", Boolean.FALSE);

        for (int serchain: avgtime.keySet()){

            FileWriter fileWriter = null;
            String fileName = "src//others//results//compK8Snode4a1//statisticsData//exp26//BS"+String.valueOf(serchain)+".txt";
            try {
                fileWriter = new FileWriter(fileName,true);
                for (int requestTime : avgtime.get(serchain).keySet()){
                    fileWriter.append(requestTime+" "+String.valueOf(avgtime.get(serchain).get(requestTime)));
                    fileWriter.append('\n');
                }

            } catch (Exception e) {
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

    public static Map<Integer, Map<Integer,Double>> readFile(String filename, boolean labeled){

        //Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay1 = getWorkload("src//org//test//workloadGenerator//k8snode4-1//request.txt", 1688453347);
        Map<Double, ArrayList<Pair<Integer,Double>>> requestToDelay =  getWorkloadForAccDelay("src//org//test//workloadGenerator//k8snode4-1//request.txt", 1688453347, "src//org//test//workloadGenerator//k8snode4-1//serRequest15//");


        Map<Integer, Map<Integer,Double>> avgtime = new HashMap<>(); // servicechain -> time ->
        Map<Integer, Map<Integer,Integer>> num = new HashMap<>();//servicechain -> time ->
        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(filename));
            try {
                String line;
                if (labeled)
                    fileReader.readLine();
                while ( (line = fileReader.readLine()) != null ) {
                    String[] rowStr = line.split(",");
                    int servicechainId = Integer.valueOf(rowStr[1]);
                    double requestTime = Double.valueOf(rowStr[5]);
                    double time = Double.valueOf(rowStr[7]);
                    int requestTime2 = (int) requestTime;
                    int requestTime1 = requestTime2;
//                    for (int i = requestTime2;i>=0;i--){
//                        double i1 = (double) i;
//                        int flag = 0;
//                        if (requestToDelay.containsKey(i1)){
//                            double delay1 = 0;
//                            for (int j = 0; j< requestToDelay.get(i1).size();j++){
//                                if (requestToDelay.get(i1).get(j).getKey() == servicechainId){
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
                    for (int i = requestTime2+2;i>=0;i--){
                        double i1 = (double) i;
                        int flag = 0;
                        if (requestToDelay.containsKey(i1)){
                            double delay1 = 0;
                            for (int j = 0; j< requestToDelay.get(i1).size();j++){
                                if (requestToDelay.get(i1).get(j).getKey() == servicechainId){
                                    double jj = requestToDelay.get(i1).get(j).getValue() + i1;
                                    int jj1 = (int) jj;
                                    if (jj1 == requestTime2){
                                        delay1 = requestToDelay.get(i1).get(j).getValue();
                                        if (servicechainId == 2){
                                            Log.printLine(delay1);
                                        }

                                        time += delay1;
                                        flag = 1;
                                    }
                                    break;
                                }
                            }
                            if (flag == 1){
                                requestTime1 = (int) (requestTime - delay1);
                                break;
                            }

                        }
                    }
//                    if (servicechainId == 0){
//                        requestTime = requestTime - 0.0391;
//                    }else if (servicechainId == 1){
//                        requestTime = requestTime - 0.1238;
//                    }else if (servicechainId == 2){
//                        requestTime = requestTime - 0.0810;
//                    }else if (servicechainId == 3){
//                        requestTime = requestTime - 0.2152;
//                    }else if (servicechainId == 4){
//                        requestTime = requestTime -0.0517;
//                    }else if (servicechainId == 5){
//                        requestTime = requestTime -0.1660;
//                    }
//                    int requestTime1 = (int) requestTime;
//
//                    if (servicechainId == 0){
//                        time = time + 0.0391;
//                    }else if (servicechainId == 1){
//                        time = time + 0.1238;
//                    }else if (servicechainId == 2){
//                        time = time + 0.0810;
//                    }else if (servicechainId == 3){
//                        time = time + 0.2152;
//                    }else if (servicechainId == 4){
//                        time = time + 0.0517;
//                    }else if (servicechainId == 5){
//                        time = time + 0.1660;
//                    }
                    //int requestTime1 = (int)requestTime;

                    if (avgtime.containsKey(servicechainId)){
                        if (avgtime.get(servicechainId).containsKey(requestTime1)){
                            avgtime.get(servicechainId).put(requestTime1,avgtime.get(servicechainId).get(requestTime1) + time);
                            num.get(servicechainId).put(requestTime1,num.get(servicechainId).get(requestTime1) + 1);

                        }else{
                            avgtime.get(servicechainId).put(requestTime1,time);
                            num.get(servicechainId).put(requestTime1,1);
                        }
                    }else{
                        avgtime.put(servicechainId,new TreeMap<>());
                        avgtime.get(servicechainId).put(requestTime1,time);
                        num.put(servicechainId,new TreeMap<>());
                        num.get(servicechainId).put(requestTime1,1);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        for (int servicechain : avgtime.keySet()){
            for (int requesttime : avgtime.get(servicechain).keySet()){
                avgtime.get(servicechain).put(requesttime,avgtime.get(servicechain).get(requesttime) / num.get(servicechain).get(requesttime));
            }

        }
        return avgtime;
    }


    private static Map<Double,ArrayList<Pair<Integer,Double>>> getWorkloadForAccDelay(String filename, double startTime, String path){

        //Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelay = getWorkload(filename,startTime);
        String file1 = path + "ser1.txt";
        String file2 = path + "ser2.txt";
        String file3 = path + "ser3.txt";
        String file4 = path + "ser4.txt";
        String file5 = path + "ser5.txt";
        String file6 = path + "ser6.txt";

        Map<Double,ArrayList<Pair<Integer,Double>>> requestToDelayNew = new HashMap<>();

        BufferedReader fileReader;
        try {
            fileReader = new BufferedReader(new FileReader(file1));
            try {
                String line;
                while ( (line = fileReader.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        BufferedReader fileReader1;
        try {
            fileReader1 = new BufferedReader(new FileReader(file2));
            try {
                String line;
                while ( (line = fileReader1.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        BufferedReader fileReader2;
        try {
            fileReader2 = new BufferedReader(new FileReader(file3));
            try {
                String line;
                while ( (line = fileReader2.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        BufferedReader fileReader3;
        try {
            fileReader3 = new BufferedReader(new FileReader(file4));
            try {
                String line;
                while ( (line = fileReader3.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        BufferedReader fileReader4;
        try {
            fileReader4 = new BufferedReader(new FileReader(file5));
            try {
                String line;
                while ( (line = fileReader4.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (serchain!=4){
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }

                    Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }


        BufferedReader fileReader5;
        try {
            fileReader5 = new BufferedReader(new FileReader(file6));
            try {
                String line;
                while ( (line = fileReader5.readLine()) != null ) {
                    String[] rowStr = line.split(" ");
                    double time = Double.valueOf(rowStr[0]);
                    int serchain = Integer.valueOf(rowStr[1]);
                    double delay = Double.valueOf(rowStr[3]);
                    if (serchain!=5){
                        continue;
                    }
                    if (!requestToDelayNew.containsKey(time)){
                        requestToDelayNew.put(time,new ArrayList<>());
                    }
                    Pair<Integer,Double> pair = new Pair<>(serchain,delay);
                    requestToDelayNew.get(time).add(pair);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
            e.printStackTrace();
        }

        return requestToDelayNew;
    }



}
