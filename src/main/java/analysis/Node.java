package analysis;

import java.util.List;

public class Node {
    
    private String name;
    private List<String> requirements;
    private List<String> capabilities;
    private ManagementProtocol protocol;
    
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
