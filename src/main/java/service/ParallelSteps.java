package service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ParallelSteps {
    
        private List<PlanStep> psteps;
        
        public ParallelSteps() { }
        
        public ParallelSteps(List<PlanStep> psteps) {
            this.psteps = psteps;
        }
        
        public void addStep(String node, String intf, String operation) {
            this.psteps.add(new PlanStep(node,intf,operation));
        }
        
        @JsonProperty
        public List<PlanStep> getParallelSteps() {
            return this.psteps;
        }
    
}
