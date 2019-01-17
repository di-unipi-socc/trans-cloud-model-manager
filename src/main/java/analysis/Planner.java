package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planner {
    
    // List of all possible global states
    private final List<GlobalState> globalStates;
    
    public Planner(List<Node> nodes, Map<String,List<String>> binding) {
        
        // Generating all possible global states
        this.globalStates = generateGlobalStates(nodes);
        
        // Generating all possible steps between global states
        
        // TODO : add all possible steps
    }
    
    // Private method for generating all possible global states for a given 
    // list of nodes
    private List<GlobalState> generateGlobalStates(List<Node>nodes) {
        // Creating a list of global states with an empty state
        List<GlobalState> gStates = new ArrayList();
        gStates.add(new GlobalState());
        
        // Generating all possible global states
        for(Node n : nodes) {
            // Generating a new list of global states by adding a new pair
            // for each state of current node to those already generated
            List<GlobalState> newGStates = new ArrayList();
            for(String s : n.getProtocol().getStates()) {
                for(GlobalState g : gStates) {
                    GlobalState newG = new GlobalState(g,n.getName(),s);
                    newGStates.add(newG);
                }
            }
            // Updating the list of global states with the new one
            gStates = newGStates;
        }
        return gStates;
    }
    
    // Private method for generating shortest plans (shortest sequences of ops)
    // for going from a given global state to another
    private Map<String,Map<String,List<String>>> generatePlans(
            List<Node> nodes, 
            Map<String, List<String>> binding) {
        Map<String,Map<String,List<String>>> plans = new HashMap();

        return plans;
    }
}
