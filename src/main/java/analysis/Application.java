package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Application {
    
    // List of nodes, each with its own management protocol
    private final List<Node> nodes;
    // Binding of each requirement ("nodeName/reqName") with
    // the list of capabilities ("nodeName/capName") satisfying it 
    private final Map<String,List<String>> binding;
    // Map modelling inter-node dependencies, with each "node" being mapped
    // to the set of nodes it depends on (directly, and through other nodes)
    private final Map<String,Set<String>> dependencies;
    
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
        
        // Computing all depedencies
        dependencies = new HashMap<String,Set<String>>();
        for(String nodeName : nodeNames) {
            if(!dependencies.containsKey(nodeName))
                dependencies.put(nodeName,computeDependencies(nodeName));
        }
//        // DEBUGGING - start
//        for(String nodeName : nodeNames) {
//            System.out.println("Node: " + nodeName);
//            System.out.println("Dependency:");
//            for(String dependency : dependencies.get(nodeName))
//                System.out.println("  - " + dependency);
//            System.out.println("");
//        }
//        // DEBUGGING - end
        
        // Computing all possible plans from each global state to each other
        this.planner = new Planner(this.nodes,this.binding);
    }
   
    private Set<String> computeDependencies(String node) {
        Set<String> nodeDependencies = new HashSet<String>();
        // Getting dependencies on other nodes from "binding"
        for(String source : binding.keySet()) {
            String sourceNode = source.split("/")[0];
            if(sourceNode.equals(node)) {
                for (String target : binding.get(source)) {
                    String targetNode = target.split("/")[0];
                    // Adding direct dependency on "targetNode"
                    nodeDependencies.add(targetNode);
                    // Getting recursive dependencies
                    if(!dependencies.containsKey(targetNode))
                        dependencies.put(targetNode,computeDependencies(targetNode));
                    Set<String> recursiveDependencies = dependencies.get(targetNode);
                    System.out.println(node + "," + targetNode + " - " + recursiveDependencies);
                    nodeDependencies.addAll(recursiveDependencies);                
                }
            }
        }
        return nodeDependencies;
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
    
    public void setCurrent(Map<String,String> globalState) {
        this.planner.setCurrent(globalState);
    }
    
    public void setTarget(Map<String,String> globalState) {
        this.planner.setTarget(globalState);
    }

    public boolean dependsOn(String sourceNode, String targetNode) {
        return dependencies.get(sourceNode).contains(targetNode);
    }
    
    @Override
    public String toString() {
        return "** NODES **\n" + nodes.toString() + 
                "\n** RELATIONSHIPS **\n" + binding.toString();
    }
}
