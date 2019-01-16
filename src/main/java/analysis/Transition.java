package analysis;

import java.util.List;

public class Transition {
    
    private final String operation;
    private final List<String> requirements;
    private final List<String> capabilities;
    private final String targetState;
    
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
