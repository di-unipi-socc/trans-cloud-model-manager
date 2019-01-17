package analysis;

import java.util.HashMap;
import java.util.Map;

public class GlobalState {
 
    private final Map<String,String> stateMapping;
    
    // TODO : Add possible steps (op/fault,weight,next global state) --> List<Step> steps with Step being a NEW CLASS?
    
    public GlobalState() {
        stateMapping = new HashMap();
    }
    
    public GlobalState(GlobalState g, String node, String state) {
        stateMapping = new HashMap();
        stateMapping.put(node,state);
        stateMapping.putAll(g.getMapping());
    }
    
    public Map<String,String> getMapping() {
        return stateMapping;
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
}
