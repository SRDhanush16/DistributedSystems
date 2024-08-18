package org.dhanush;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Aggregator {
    private WebClient webClient;
    public  Aggregator(){
        this.webClient = new WebClient();
    }
    public List<String> sendTasksToWorkers(List<String> workersAddress,List<String> tasks){
        CompletableFuture<String>[] future = new CompletableFuture[workersAddress.size()];
        for(int i=0;i<workersAddress.size();i++){
            String workeraddr = workersAddress.get(i);
            String task = tasks.get(i);
            byte[] requestPayload = task.getBytes();
            future[i] = webClient.sendTask(workeraddr,requestPayload);
        }
        List<String> results = Stream.of(future).map(CompletableFuture::join).collect(Collectors.toList());
        return results;
    }
}
