package analysis;

import java.util.List;

public class Transition {
    
    private String operation;
    private List<String> requirements;
    private List<String> capabilities;
    private String targetState;
    
    public Transition(String operation,
            List<String> requirements, 
            List<String> capabilities,
            String targetState) {
        this.operation = operation;
        this.requirements = requirements;
        this.capabilities = capabilities;
        this.targetState = targetState;
    }
    
    public String getOperation() { 
        return this.operation;
    }
    
    public List<String> getRequirements() { 
        return this.requirements;
    }
    
    public List<String> getCapabilities() { 
        return this.capabilities;
    }
    
    public String getTargetState() { 
        return this.targetState;
    }
    
    @Override
    public String toString() {
        return "<" + this.operation + 
                "," + this.requirements + 
                "," + this.capabilities + 
                "," + this.targetState + ">";
    }
}
