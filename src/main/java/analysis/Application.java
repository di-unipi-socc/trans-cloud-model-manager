package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Application {
    
    // List of nodes, each with its own management protocol
    private final List<Node> nodes;
    // Binding of each requirement ("nodeName/reqName") with
    // the list of capabilities ("nodeName/capName") satisfying it 
    private final Map<String,List<String>> binding;
    
    private final Planner planner;
    
    public Application(
            List<String> nodeNames,
            Map<String,List<String>> reqs,
            Map<String,List<String>> caps,
            Map<String,List<String>> binding
    ) {    
        // Creating nodes
        nodes = new ArrayList<Node>();
        for(String nName : nodeNames) {
            Node n = new Node(nName,reqs.get(nName),caps.get(nName));
            nodes.add(n);
        }
        
        // Updating binding
        this.binding = binding;
        
        // Computing all possible plans from each global state to each other
        this.planner = new Planner(this.nodes,this.binding);
    }
   
    public List<Node> getNodes() {
        return nodes;
    }
    
    public Map<String,List<String>> getBinding() {
        return binding;
    }
    
    public List<String> getSequentialPlan() {
        return planner.getSequentialPlan();
    }
    
    public List<String> boundTo(String node, String requirement) {
        return binding.get(node + "." + requirement);
    }
    
    public void setCurrent(Map<String,String> globalState) {
        this.planner.setCurrent(globalState);
    }
    
    public void setTarget(Map<String,String> globalState) {
        this.planner.setTarget(globalState);
    }

    @Override
    public String toString() {
        return "** NODES **\n" + nodes.toString() + 
                "\n** RELATIONSHIPS **\n" + binding.toString();
    }
}
