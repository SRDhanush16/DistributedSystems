package org.dhanush;

import java.util.Arrays;
import java.util.List;

public class Application {
    public static final String WORKER_ADDRESS_1 = "http://localhost:8080/task";
    public static final String WORKER_ADDRESS_2 = "http://localhost:8082/task";
    public static void main(String[] args) {
        Aggregator aggregator = new Aggregator();
        String task1 = "10,200";
        String task2 = "123456789,987654321";

        List<String> results = aggregator.sendTasksToWorkers(Arrays.asList(WORKER_ADDRESS_1,WORKER_ADDRESS_2),
                Arrays.asList(task1,task2));
        for(String r : results){
            System.out.println(r);
        }
    }
}
