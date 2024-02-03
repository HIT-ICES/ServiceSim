package others.results.compK8Snode4.statisticsData;


import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AvgRT {

    public static void main(String[] args) {

        Map<Integer, Map<Integer,Double>> avgtime = readFile("src//others//results//compK8Snode4//workloadResult35.csv", Boolean.FALSE);

        for (int serchain: avgtime.keySet()){

            FileWriter fileWriter = null;
            String fileName = "src//others//results//compK8Snode4//statisticsData//exp35//BS"+String.valueOf(serchain)+".txt";
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
                    int requestTime1 = (int) requestTime;
                    double time = Double.valueOf(rowStr[7]);
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
}
