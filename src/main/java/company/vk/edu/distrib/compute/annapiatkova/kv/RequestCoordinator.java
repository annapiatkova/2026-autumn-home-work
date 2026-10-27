package company.vk.edu.distrib.compute.annapiatkova.kv;

import java.net.http.HttpRequest;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

class RequestCoordinator {
    static final String REPLICA_PREFIX = "/v0/replica?";
    static final String ID_PREFIX = "id=";
    static final String CLUSTER_PROPERTY = "CLUSTER";
    static final int HTTP_OK = 200;
    static final int HTTP_CREATED = 201;
    static final int HTTP_ACCEPTED = 202;

    private RendezvousHashRing hashRing;
    private List<Integer> clusterPorts;
    private int replicationFactor;

    RequestCoordinator() {
        this.hashRing = null;
    }

    public boolean validateAck(int ack) {
        if (this.hashRing == null) {
            initialiseHashRing();
        }
        return ack >= 1 && ack <= this.replicationFactor;
    }

    HttpRequest get(int port, String id) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + REPLICA_PREFIX + ID_PREFIX + id))
            .timeout(Duration.ofMillis(100))
            .GET()
            .build();
    }

    HttpRequest put(int port, String id, byte[] data) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + REPLICA_PREFIX + ID_PREFIX + id))
            .timeout(Duration.ofMillis(100))
            .PUT(HttpRequest.BodyPublishers.ofByteArray(data))
            .build();
    }

    HttpRequest delete(int port, String id) {
        return HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + REPLICA_PREFIX + ID_PREFIX + id))
            .timeout(Duration.ofMillis(100))
            .DELETE()
            .build();
    }

    List<Integer> getReplicaPorts(String key) {
        if (this.hashRing == null) {
            initialiseHashRing();
        }
        int index = this.hashRing.getClusterIndex(key);
        List<Integer> res = new ArrayList<>(this.replicationFactor);
        for (int i = 0; i < this.replicationFactor; i++) {
            res.add(this.clusterPorts.get((index + i) % this.clusterPorts.size()));
        }
        return res;
    }

    private void initialiseHashRing() {
        this.clusterPorts = getClusterPorts();
        this.replicationFactor = this.clusterPorts.size();
        List<String> clusters = new ArrayList<>(this.clusterPorts.size());
        for (int port : this.clusterPorts) {
            clusters.add("cluster-" + port);
        }
        this.hashRing = new RendezvousHashRing(clusters);
    }

    private List<Integer> getClusterPorts() {
        String cluster = System.getProperty(CLUSTER_PROPERTY);
        if (cluster == null) {
            throw new RuntimeException("Cluster property not set");
        }
        String[] ports = cluster.split(",");
        List<Integer> res = new ArrayList<>(ports.length);
        for (String port : ports) {
            res.add(Integer.parseInt(port));
        }
        return res;
    }
}
