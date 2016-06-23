package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Transmission implements Serializable{

    private ArrayList data;
    private int volume;
    private double time;

    public Transmission(ArrayList data) {
        this.data = data;
        calculateVolume();
        calculateTime();
    }

    public ArrayList getData() {
        return data;
    }

    public int getVolume() {
        return volume;
    }

    public double getTime() {
        return time;
    }

    private void calculateTime() {
        time = volume/ Util.BANDWIDTH + Util.LATENCY;
    }

    private void calculateVolume() {
        volume += Util.getMemoryLength(data);
    }
}
