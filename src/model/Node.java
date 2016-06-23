package model;

import javafx.util.Pair;

import java.util.*;

public class Node {

    private int id;
    private ArrayList<Pair<Integer, String>> tableR;
    private ArrayList<Pair<Integer, String>> tableS;

    public Node(int id) {
        this.id = id;
        tableR = new ArrayList<>();
        tableS = new ArrayList<>();
    }

    public ArrayList<Pair<Integer, String>> getTuplesByKey(Table table, Integer key) {
        ArrayList<Pair<Integer, String>> result = null;
        for (Pair<Integer, String> tuple : getTable(table)) {
            if (Objects.equals(tuple.getKey(), key)) {
                if (result == null) result = new ArrayList<>();
                result.add(tuple);
            }
        }
        return result;
    }

    public ArrayList<Pair<Integer, String>> getTable(Table table) {
        switch (table) {
            case R:
                return tableR;
            case S:
                return tableS;
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
//    @Override
//    public String toString() {
//
//        StringBuilder string =  new StringBuilder("model.Node" + id + "{\n");
//        string.append(String.format("   %s%48s\n", "model.Table R:", "model.Table S:"));
//
//        for (int i = 0; i < Math.max(tableR.size(), tableS.size()); i++) {
//            boolean rEntryExist = false;
//            Integer[] tableRKeys = new Integer[tableR.size()];
//            tableRKeys = tableR.keySet().toArray(tableRKeys);
//            if (i < tableR.size()) {
//                Integer currentKey = tableRKeys[i];
//                string.append(String.format("   %-3d->%-40s", currentKey, tableR.get(currentKey)));
//                rEntryExist = true;
//            }
//
//            Integer[] tableSKeys = new Integer[tableS.size()];
//            tableRKeys = tableS.keySet().toArray(tableSKeys);
//            if (i < tableS.size()) {
//                Integer currentKey = tableSKeys[i];
//                if (!rEntryExist) {
//                    string.append(String.format("   %-45s", ""));
//                }
//                string.append(String.format("   %-3d->%s", currentKey, tableS.get(currentKey)));
//            }
//            string.append("\n");
//        }
//        string.append("}\n");
//        return string.toString();
//    }
