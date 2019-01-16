package analysis;

import java.util.List;

public class Node {
    
    private final String name;
    private final List<String> requirements;
    private final List<String> capabilities;
    private final ManagementProtocol protocol;
    
    public Node(String name,
            List<String> requirements,
            List<String> capabilities) {
        this.name = name;
        this.requirements = requirements;
        this.capabilities = capabilities;
        this.protocol = new ManagementProtocol(requirements,capabilities);
    }         
    
    // TODO: Add constructor with parsed management protocol
    
    public String getName() {
        return name; 
    }

    public List<String> getRequirements() {
        return requirements;
    }
    
    public List<String> getCapabilities() {
        return capabilities;
    }
    
    public boolean is(String name) {
        return (this.name.equals(name));
    }
    
    @Override
    public String toString() {
        return name + 
                "{reqs: " + this.requirements + 
                ", caps: " + this.capabilities + 
                ", protocol : " + this.protocol + "}";
    }
    
}
