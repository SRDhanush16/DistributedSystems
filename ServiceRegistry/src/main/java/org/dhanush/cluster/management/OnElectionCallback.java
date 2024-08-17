package org.dhanush.cluster.management;

public interface OnElectionCallback {
    void onElectedToBeLeader();
    void onWorker();
}
