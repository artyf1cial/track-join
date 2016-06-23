import javafx.util.Pair;
import model.Node;
import model.Table;
import model.Transmission;
import model.Util;

import java.util.*;

public class TrackJoin {

    private Integer joinKey = 1;
    private int totalTime = 0;
    private int totalTransmissionsVolume = 0;

    private Node[] nodes;

    public Node getNodeById(int id) {
        return nodes[id];
    }
    private void initializeNodes() {

        int joinTuplesQuantity = 7000;
        final int nodesQuantity = 4;

        nodes = new Node[nodesQuantity];
        for (int i = 0; i < nodesQuantity; i++) {
            nodes[i] = new Node(i);
        }

        Random random = new Random();
        for (int i = 0; i < joinTuplesQuantity/2; i++) {
            nodes[random.nextInt(4)].getTable(Table.R).add(new Pair<>(joinKey, "Value"));
            nodes[random.nextInt(4)].getTable(Table.S).add(new Pair<>(joinKey, "Value"));
        }
    }

    public static void main(String[] args) {
        TrackJoin trackJoin = new TrackJoin();
        trackJoin.initializeNodes();
        trackJoin.join();
        System.out.println(trackJoin.totalTime);
        System.out.println(trackJoin.totalTransmissionsVolume);
    }

    private void join() {
        Map<Integer, Integer> RVolumesByNodeId = new HashMap<>();
        Map<Integer, Integer> SVolumesByNodeId = new HashMap<>();
        for (Node node : nodes) {
            Integer nodeRVolume = getTuplesVolumes(Table.R, node);
            Integer nodeSVolume = getTuplesVolumes(Table.R, node);
            if (nodeRVolume != null) {
                RVolumesByNodeId.put(node.getId(), nodeRVolume);
            }
            if (nodeSVolume != null) {
                SVolumesByNodeId.put(node.getId(), nodeSVolume);
            }
        }

        Integer maxVolumeNodeId = getMaxVolumeNodeId(RVolumesByNodeId, SVolumesByNodeId);
        Pair<Integer, ArrayList<Integer>> RScalculationResults = calculateMigrCost(RVolumesByNodeId,
                                                                                    SVolumesByNodeId, maxVolumeNodeId);
        Pair<Integer, ArrayList<Integer>> SRcalculationResults = calculateMigrCost(SVolumesByNodeId,
                                                                                    RVolumesByNodeId, maxVolumeNodeId);

        if (RScalculationResults.getKey() < SRcalculationResults.getKey()) {
            migrate(RScalculationResults, maxVolumeNodeId, SVolumesByNodeId, Table.S);
            broadcast(RVolumesByNodeId, SVolumesByNodeId, Table.R);
        } else {
            migrate(SRcalculationResults, maxVolumeNodeId, RVolumesByNodeId, Table.S);
            broadcast(SVolumesByNodeId, RVolumesByNodeId, Table.S);
        }
    }

    private Integer getTuplesVolumes(Table table, Node node) {
        Integer totalTuplesVolume = 0;
        Transmission volumesTrans = formTuplesVolumesTransmission(table, node);
        if (volumesTrans == null) return null;
        for (Object pair : volumesTrans.getData()) {
            totalTuplesVolume += ((Pair<Integer, Integer>) pair).getValue();
        }
        totalTransmissionsVolume += volumesTrans.getVolume();
        totalTime += volumesTrans.getTime();
        return totalTuplesVolume;
    }

    private Transmission formTuplesVolumesTransmission(Table table, Node node) {
        ArrayList<Pair<Integer, String>> tuplesByKey = node.getTuplesByKey(table, joinKey);
        if (tuplesByKey == null) return null;
        ArrayList<Pair<Integer, Integer>> tuplesVolumes = new ArrayList<>();
        for (Pair<Integer, String> pair: tuplesByKey) {
            tuplesVolumes.add(new Pair<>(pair.getKey(), Util.getMemoryLength(pair.getValue())));
        }
        return new Transmission(tuplesVolumes);
    }

    private Pair<Integer, ArrayList<Integer>> calculateMigrCost(Map<Integer, Integer> sourceTableVolumesByNode,
                                                                Map<Integer, Integer> destinationTableVolumesByNode,
                                                                Integer maxVolumeNodeId) {

        final int transmissionVolume = 16;

        Integer totalFromTableVolume = 0;
        Integer localFromTableVolume = 0;
        for (Integer nodeId : sourceTableVolumesByNode.keySet()) {
            totalFromTableVolume += sourceTableVolumesByNode.get(nodeId);
            if (destinationTableVolumesByNode.keySet().contains(nodeId)) {
                localFromTableVolume += destinationTableVolumesByNode.get(nodeId);
            }
        }
        int cost = totalFromTableVolume * destinationTableVolumesByNode.size() - localFromTableVolume +
                sourceTableVolumesByNode.size() * destinationTableVolumesByNode.size() * transmissionVolume;

        ArrayList<Integer> nodeIdsToMigr = new ArrayList<>();
        for (Integer nodeId : sourceTableVolumesByNode.keySet()) {
            if (destinationTableVolumesByNode.get(nodeId) > 0 && !nodeId.equals(maxVolumeNodeId)) {

                int delta = sourceTableVolumesByNode.get(nodeId) + destinationTableVolumesByNode.get(nodeId)
                        - totalFromTableVolume - sourceTableVolumesByNode.size();

                if (delta < 0) {
                    cost += delta;
                    nodeIdsToMigr.add(nodeId);
                }
            }
        }
        return new Pair<>(cost, nodeIdsToMigr);
    }


    private Integer getMaxVolumeNodeId(Map<Integer, Integer> sourceTableVolumesByNode,
                                       Map<Integer, Integer> destinationTableVolumesByNode) {
        Integer maxVolumeNodeId = 0;
        int maxNodeVolume = 0;
        Set<Integer> nodeIds = new HashSet<>();
        nodeIds.addAll(sourceTableVolumesByNode.keySet());
        nodeIds.addAll(destinationTableVolumesByNode.keySet());

        for (Integer nodeId : nodeIds) {
            int nodeVolume = 0;
            nodeVolume += sourceTableVolumesByNode.get(nodeId);
            nodeVolume += destinationTableVolumesByNode.get(nodeId);
            if (nodeVolume > maxNodeVolume) {
                maxNodeVolume = nodeVolume;
                maxVolumeNodeId = nodeId;
            }
        }
        return maxVolumeNodeId;
    }

    private void migrate(Pair<Integer, ArrayList<Integer>> calculationResults, Integer maxVolumeNodeId,
                         Map<Integer, Integer> volumesByNodeId, Table table) {
        for (Integer nodeId : calculationResults.getValue()) {
            Transmission maxVolumeNodeTrans = new Transmission(new ArrayList<>(maxVolumeNodeId));
            totalTransmissionsVolume += maxVolumeNodeTrans.getVolume();
            totalTime += maxVolumeNodeTrans.getTime();

            Integer currentMaxNodeVolume = volumesByNodeId.get(maxVolumeNodeId);
            volumesByNodeId.put(maxVolumeNodeId, currentMaxNodeVolume + volumesByNodeId.get(nodeId));
            volumesByNodeId.remove(nodeId);
            for (Node node : nodes) {
                Transmission migration = new Transmission(node.getTuplesByKey(table, joinKey));
                totalTransmissionsVolume += migration.getVolume();
                totalTime += migration.getTime();
            }
        }
    }

    private void broadcast(Map<Integer, Integer> sourceNodeIds,
                           Map<Integer, Integer> destinationNodeIds,
                           Table sourceTable) {

        ArrayList<Pair<Integer, Integer>> nodeIdsForCast = new ArrayList<>();
        for (Integer nodeId : destinationNodeIds.keySet()) {
            nodeIdsForCast.add(new Pair<>(joinKey, nodeId));
        }
        for (Integer nodeId : sourceNodeIds.keySet()) {
            Transmission destinationNodesTrans = new Transmission(nodeIdsForCast);
            totalTransmissionsVolume += destinationNodesTrans.getVolume();
            totalTime += destinationNodesTrans.getTime();
        }

        for (Pair<Integer, Integer> node: nodeIdsForCast) {
            for (Integer nodeId : sourceNodeIds.keySet()) {
                Transmission broadcast = new Transmission(getNodeById(nodeId).getTuplesByKey(sourceTable, joinKey));
                totalTransmissionsVolume += broadcast.getVolume();
                totalTime += broadcast.getTime();
            }
        }
    }
}
