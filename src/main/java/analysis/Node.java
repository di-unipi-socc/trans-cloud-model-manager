package analysis;

public class Node {
    
    private String name;
    
    // TODO: Add management protocol
    
    public Node(String name) {
        this.name = name;
    }         
    
    public String getName() {
        return name; 
    }

    public boolean is(String name) {
        return (this.name.equals(name));
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
