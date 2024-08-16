package org.dhanush;

import java.io.IOException;
import java.util.*;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

public class LeaderElection implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;

    private static final String ELECTION_NAMESPACE = "/election";
    private static final String TARGET_ZNODE = "/target_znode";
    private String currentZnodeName;

    public static void main(String[] arg) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();

        leaderElection.connectToZookeeper();
        try{
            leaderElection.volunteerForLeadership();
            leaderElection.Election_Reelection_Leader();
        } catch(Exception e){
            System.out.println("Some Error");
            System.out.println(e);
        }
        //leaderElection.watchTargetZnode();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }

    public void volunteerForLeadership() throws Exception{
        String znodePrefix = ELECTION_NAMESPACE +"/c_";
        String znodeFullPath = zooKeeper.create(znodePrefix,new byte[]{},ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("znode name"+znodeFullPath);
        this.currentZnodeName = znodeFullPath.replace(ELECTION_NAMESPACE+"/","");
    }

    /*
    // only election
    public void electLeader() throws Exception{
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE,false);
        Collections.sort(children);
        String smallestChild = children.get(0);
        if(smallestChild.equals(currentZnodeName)){
            System.out.println("I'm the Leader");
            return;
        }
        System.out.println("I'm not Leader, " +smallestChild+" is the leader" );
    }
    * */

    public void Election_Reelection_Leader() throws KeeperException, InterruptedException{
        Stat predecessorStat = null;
        String predecessorZnodeName = "";
        while(predecessorStat == null){
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE,false);
            Collections.sort(children);
            String smallestChild = children.get(0);

            if(smallestChild.equals(currentZnodeName)){
                System.out.println("I'm the Leader");
                return;
            }else{
                System.out.println("I'm not the leader");
                int predecessorIndex = Collections.binarySearch(children,currentZnodeName) - 1;
                predecessorZnodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE+"/"+predecessorZnodeName,this);
            }
        }

        System.out.println("Watching znode "+ predecessorZnodeName);
        System.out.println();

    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    public void watchTargetZnode() throws KeeperException, InterruptedException{
        Stat stat = zooKeeper.exists(TARGET_ZNODE,this);
        if(stat==null){
            return;
        }
        byte[] data = zooKeeper.getData(TARGET_ZNODE,this,stat);
        List<String> children = zooKeeper.getChildren(TARGET_ZNODE,this);
        System.out.println("Data : "+new String(data)+" children : "+children);
    }


   /*
    // this is for event watcher
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                System.out.println(TARGET_ZNODE+" was deleted");
                break;
            case NodeCreated:
                System.out.println(TARGET_ZNODE+" was created");
                break;
            case NodeDataChanged:
                System.out.println(TARGET_ZNODE+" data changed");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE+" children changed");
                break;
        }
        try{
            watchTargetZnode();
        }catch(KeeperException e){}
        catch(InterruptedException e){}
    }

   *  */

     // this is for Fault Tolerance
    @Override
    public void process(WatchedEvent event){
        switch (event.getType()){
            case None:
                if(event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("Successfully connected to Zookeeper");
                }else{
                    synchronized (zooKeeper){
                        System.out.println("Disconnected from zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                try{
                    Election_Reelection_Leader();
                }catch (KeeperException e){}
                catch (InterruptedException e){}
        }
    }

}
