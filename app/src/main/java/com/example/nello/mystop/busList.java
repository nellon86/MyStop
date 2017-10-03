package com.example.nello.mystop;

/**
 * Created by death on 02/10/2017.
 */

import java.util.ArrayList;
import java.util.List;

public class busList {
    private List<incomingBus> arrives;

    public busList(List<incomingBus> arrives) {
        super();
        this.arrives = arrives;
    }

    public busList() {
        arrives = new ArrayList<incomingBus>();
    }

    public List<incomingBus> getArrives() {
        return arrives;
    }

    public void setArrives(List<incomingBus> arrives) {
        this.arrives = arrives;
    }

    public void addIncomingBus(incomingBus incomingBus){
        arrives.add(incomingBus);
    }
}
