package analysis;

public class Step {

    public static String handling = "$";
    
    // Reason for the step, either operation or fault handling
    // (by default, fault handling)
    private final String reason;
    // Cost for executing the step 
    // (by default, 1)
    private final int cost;
    // Global state reached after executing the step
    private final GlobalState next;
    
    public Step(String nodeName, GlobalState next) {
        this.reason = nodeName + "/" + Step.handling;
        this.cost = 1;
        this.next = next;
    }
    
    public Step(String nodeName, String reason, GlobalState next) {
        this.reason = nodeName + "/" + reason;
        this.cost = 1;
        this.next = next;
    }
    
    public Step(String nodeName, String reason, int weight, GlobalState next) {
        this.reason = nodeName + "/" + reason;
        this.cost = weight;
        this.next = next;
    }
    
    public String getReason() {
        return this.reason;
    }
    
    public int getCost() {
        return this.cost;
    }
    
    public GlobalState getNextGlobalState() {
        return this.next;
    }
    
    @Override
    public String toString() {
        return "<" + reason + "," + cost + "," + next.getMapping() + ">";
    }
}
