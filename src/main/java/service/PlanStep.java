package service;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlanStep {

    private String node;
    private String intf;
    private String operation;
    
    public PlanStep() { }
    
    public PlanStep(String node, String intf, String operation) {
        this.node = node;
        this.intf = intf;
        this.operation = operation;
    }
    
    @JsonProperty
    public String getNode() {
        return this.node;
    }
    
    @JsonProperty
    public String getInterface() {
        return this.intf;
    }
    
    @JsonProperty
    public String getOperation() {
        return this.operation;
    }
}
