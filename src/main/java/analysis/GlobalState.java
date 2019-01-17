package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalState {
 
    private final List<Node> nodes;
    private final Map<String,List<String>> binding;
    private final Map<String,String> stateMapping;
    private final List<Step> steps;
    
    public GlobalState(List<Node> nodes,
            Map<String,List<String>> binding) {
        this.nodes = nodes;
        this.binding = binding;
        stateMapping = new HashMap();
        steps = new ArrayList<Step>();
    }
    
    public void addMapping(String node, String state) {
        stateMapping.put(node,state);
    }
    
    public void addMapping(GlobalState g) {
        stateMapping.putAll(g.getMapping());
    }
    
    public void addStep(Step s) {
        // System.out.println("--> Adding step: " + s);
        this.steps.add(s);
    }
    
    // Method for retrieving the list of requirements assumed by the nodes
    // in this global state
    public List<String> getAssumedReqs() {
        List<String> assumedReqs = new ArrayList();
        
        for(Node n : this.nodes) {
            String nName = n.getName();
            ManagementProtocol nProtocol = n.getProtocol();
            List<String> nRho = nProtocol.getRho().get(stateMapping.get(nName));
            for(String req : nRho) {
                assumedReqs.add(nName + "/" + req);
            }
        }
        
        return assumedReqs;
    }
     
    // Method for retrieving the list of capabilities provided by the nodes
    // in this global state
    public List<String> getProvidedCaps() {
        List<String> providedCaps = new ArrayList();
        
        for(Node n : this.nodes) {
            String nName = n.getName();
            ManagementProtocol nProtocol = n.getProtocol();
            List<String> nGamma = nProtocol.getGamma().get(stateMapping.get(nName));
            for(String cap : nGamma) {
                providedCaps.add(nName + "/" + cap);
            }
        }
        
        return providedCaps;
    }

    // Method for retrieving the list of satisfiable requirements (i.e.,
    // requirements that - even if not assumed - are bound to capabilities
    // provided by the nodes in this global state)
    public List<String> getSatisfiableReqs() {
        List<String> satisfiableReqs = new ArrayList();
        
        List<String> providedCaps = getProvidedCaps();
        for(Node n : this.nodes) {
            String nName = n.getName();
            for(String req : n.getRequirements()) {
                if(providedCaps.containsAll(binding.get(nName + "/" + req)))
                    satisfiableReqs.add(nName + "/" + req);
            }
        }
        
        return satisfiableReqs;
    }

    // Method for retrieving the list of requirements that are assumed and 
    // bound to capabilities that are not provided
    public List<String> getPendingFaults() {
        List<String> assumedReqs = getAssumedReqs();
        List<String> providedCaps = getProvidedCaps();
        List<String> faults = new ArrayList();
        
        for(String nodeReq : assumedReqs) {
            if(!(providedCaps.containsAll(binding.get(nodeReq))))
                faults.add(nodeReq);
        }

        return faults; 
    }
    
    // Method to check whether there are pending faults in a global state
    public boolean isConsistent() {
        List<String> faults = getPendingFaults();
        return faults.isEmpty();
    }
    
    public Map<String,String> getMapping() {
        return stateMapping;
    }
    
    public String getStateOf(String node) {
        return stateMapping.get(node);
    }
    
    public List<Step> getSteps() {
        return steps;
    }
    
    @Override
    public boolean equals(Object o) {
       
        // Check whether o is compared with itself
        if(o == this) return true;
        
        // Check whether o is not a global state
        if(!(o instanceof GlobalState)) return false;
        
        Map<String,String> oStateMapping = ((GlobalState) o).getMapping();
        
        // Check whether o and this have the same mappings
        for(String node : this.stateMapping.keySet()) {
            String oMapping = oStateMapping.get(node);
            if(oMapping == null) 
                return false;
            if(!(oMapping.equals(this.stateMapping.get(node)))) 
                return false;
        }
        for(String node : oStateMapping.keySet()) {
            if(!(this.stateMapping.containsKey(node)))
                return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return "Mapping: " + this.stateMapping + 
                "\n Steps: " + this.steps;
    }
    
}
