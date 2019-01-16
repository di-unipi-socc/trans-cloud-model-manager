package analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {
    
    // List of nodes, each with its own management protocol
    private List<Node> nodes;
    // Binding of each requirement ("nodeName/reqName") with
    // the list of capabilities ("nodeName/capName") satisfying it 
    private Map<String,List<String>> binding;
    
    // TODO: Add global states/plans
    
    public Application(
            List<String> nodeNames,
            Map<String,List<String>> reqs,
            Map<String,List<String>> caps,
            Map<String,List<String>> binding
    ) {    
        nodes = new ArrayList<Node>();
        for(String nName : nodeNames) {
            Node n = new Node(nName,reqs.get(nName),caps.get(nName));
            nodes.add(n);
        }
        
        this.binding = binding;
    }
   
    public List<Node> getNodes() {
        return nodes;
    }
    
    public Map<String,List<String>> getBinding() {
        return binding;
    }
    
    public List<String> boundTo(String node, String requirement) {
        return binding.get(node + "." + requirement);
    }
    
    @Override
    public String toString() {
        return "** NODES **\n" + nodes.toString() + 
                "\n** RELATIONSHIPS **\n" + binding.toString();
    }
}
