package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planner {
    
    // Application topology
    private final List<Node> nodes;
    private final Map<String,List<String>> binding;
    // List of all possible global states
    private final List<GlobalState> globalStates;
    
    // Current and target global states
    private GlobalState current;
    private GlobalState target;
    
    public Planner(List<Node> nodes, Map<String,List<String>> binding) {
        // Storing topology
        this.nodes = nodes;
        this.binding = binding;
        
        // Generating all global states and all possible steps between them
        this.globalStates = generateGlobalStates(nodes);
        generateSteps();
        
        // Initialising current and target global states to the initial global state
        Map<String,String> initialMapping = new HashMap();
        for(Node n : this.nodes) {
            String nodeName = n.getName();
            String initialState = n.getProtocol().getInitialState();
            initialMapping.put(nodeName,initialState);
        }
        GlobalState initialG = search(initialMapping);
        this.current = initialG;
        this.target = initialG;
    }
    
    // Private method for generating all possible global states for a given 
    // list of nodes
    private List<GlobalState> generateGlobalStates(List<Node> nodes) {
        // Creating a list of global states with an empty state
        List<GlobalState> gStates = new ArrayList();
        gStates.add(new GlobalState(nodes,binding));
        
        // Generating all possible global states
        for(Node n : nodes) {
            // Generating a new list of global states by adding a new pair
            // for each state of current node to those already generated
            List<GlobalState> newGStates = new ArrayList();
            for(String s : n.getProtocol().getStates()) {
                for(GlobalState g : gStates) {
                    GlobalState newG = new GlobalState(nodes,binding);
                    newG.addMapping(g);
                    newG.addMapping(n.getName(),s);
                    newGStates.add(newG);
                }
            }
            // Updating the list of global states with the new one
            gStates = newGStates;
        }
        return gStates;
    }

    // Private method for generating all possible steps that can be performed
    // in each global state
    private void generateSteps() {
        for (GlobalState g : this.globalStates) {
            List<String> faults = g.getPendingFaults(); 
            if(faults.isEmpty()) {
                // Identifying all available operation transitions (when there 
                // are no faults), and adding them as steps
                List<String> gSatisfiableReqs = g.getSatisfiableReqs();
                
                for(Node n : nodes) {
                    String nName = n.getName();
                    String nState = g.getStateOf(nName);
                    List<Transition> nTau = n.getProtocol().getTau().get(nState);
                    for(Transition t : nTau) {
                        boolean firable = true;
                        for(String req : t.getRequirements()) {
                            if(!(gSatisfiableReqs.contains(n.getName() + "/" + req)))
                                firable = false;
                        }
                        if(firable) {
                            // Creating a new mapping for the actual state of n
                            Map<String,String> nextMapping = new HashMap();
                            nextMapping.putAll(g.getMapping());
                            nextMapping.put(nName, t.getTargetState());
                            // Searching the ref to the corresponding global 
                            // state in the list globalStates
                            GlobalState next = search(nextMapping);
                            // Adding the step to list of steps in g
                            g.addStep(new Step(nName,t.getOperation(),next));
                        }
                    }
                }
            } else {
                // Identifying all settling handlers for handling the faults
                // pending in this global state
                for(Node n: nodes) {
                    // Computing the "targetState" of the settling handler for n
                    String targetState = null;
                    
                    String nName = n.getName();
                    String nState = g.getStateOf(nName);
                    Map<String,List<String>> nRho = n.getProtocol().getRho();
                    
                    List<String> nFaults = new ArrayList();
                    for(String req : nRho.get(nState)) {
                        if(faults.contains(nName + "/" + req)) 
                            nFaults.add(req);
                    }

                    // TODO : Assuming handlers to be complete 

                    if(nFaults.size() > 0) {
                        Map<String,List<String>> nPhi = n.getProtocol().getPhi();
                        for(String handlingState : nPhi.get(nState)) {
                            // Check if handling state is handling all faults
                            boolean handles = true;
                            for(String req : nRho.get(handlingState)) {
                                if(nFaults.contains(req))
                                    handles = false;
                            }

                            // TODO : Assuming handlers to be race-free

                            // Updating targetState (if the handlingState is 
                            // assuming a bigger set of requirements)
                            if(handles) {
                                if(targetState == null || nRho.get(handlingState).size() > nRho.get(targetState).size())
                                    targetState = handlingState;
                            }
                        }
                        // Creating a new mapping for the actual state of n
                        Map<String,String> nextMapping = new HashMap();
                        nextMapping.putAll(g.getMapping());
                        nextMapping.put(nName, targetState);
                        // Searching the ref to the corresponding global 
                        // state in the list globalStates
                        GlobalState next = search(nextMapping);
                        // Adding the step to list of steps in g
                        g.addStep(new Step(nName,next));
                    }
                }
            }
        }
    }
 
    // Method for retrieving the (cheapest) sequence of steps for changing the
    // the configuration of the application from "start" to "target"
    public List<String> getSequentialPlan() {
        // ==========================================
        // Computing the (cheapest) sequence of steps
        // ==========================================
        
        // Maps of steps and cost from the global state s to the others
        Map<GlobalState,List<Step>> steps = new HashMap();
        steps.put(current,new ArrayList());
        Map<GlobalState,Integer> costs = new HashMap();
        costs.put(current,0);
        // List of visited states
        List<GlobalState> visited = new ArrayList();
        visited.add(current);

        // List of global states still to be visited
        List<GlobalState> toBeVisited = new ArrayList();
        
        // Adding the states reachable from start to "toBeVisited"
        for(Step step : current.getSteps()) {
            GlobalState next = step.getNextGlobalState();
            // Adding the sequence of operations towards "next" 
            List<Step> stepSeq = new ArrayList();
            stepSeq.add(step);
            steps.put(next,stepSeq);
            // Adding the cost of the sequence of operation towards "next"
            costs.put(next,step.getCost());
            toBeVisited.add(next);
        }
        
        // Exploring the graph of global states by exploiting "toBeVisited"
        while(toBeVisited.size() > 0) {
            // Removing the first global state to be visited and marking it
            // as visited
            GlobalState current = toBeVisited.remove(0);
            visited.add(current);
            
            for(Step step : current.getSteps()) {
                GlobalState next = step.getNextGlobalState();
                // Adding the sequence of operations from "start" to "next"
                // (if more convenient)
                int nextCost = costs.get(current) + step.getCost();
                if(visited.contains(next)) {
                    // If current path is cheaper, updates "steps" and "costs"
                    if(costs.get(next) > nextCost) {
                        List<Step> stepSeq = new ArrayList();
                        stepSeq.addAll(steps.get(current));
                        stepSeq.add(step);
                        steps.put(next,stepSeq);
                        costs.put(next,nextCost);
                    }
                } else {
                    List<Step> stepSeq = new ArrayList();
                    stepSeq.addAll(steps.get(current));
                    stepSeq.add(step);
                    steps.put(next,stepSeq);
                    costs.put(next, nextCost);
                    if(!(toBeVisited.contains(next))) toBeVisited.add(next);
                }
            }
        }
        
        // ====================================================
        // Computing the sequence of operations from "s" to "t"
        // ====================================================
        // If no plan is available, return null
        if(steps.get(target) == null) 
            return null;
        // Otherwise, return the corresponding sequence of operations
        List<String> opSequence = new ArrayList();
        for(Step step : steps.get(target)) {
            if(!(step.getReason().contains(Step.handling))) {
                opSequence.add(step.getReason());
            }
        }
        return opSequence;
    }
    
    // Private method for searching a global state corresponding to a given
    // "stateMapping" (null, if not found)
    private GlobalState search(Map<String,String> stateMapping) {
        GlobalState desired = new GlobalState(nodes,binding);
        desired.addMapping(stateMapping);
        for(GlobalState g : globalStates) {
            if(g.equals(desired))
                return g;
        }
        return null;
    }
    
    public void setCurrent(Map<String,String> stateMapping) {
        GlobalState currentG = search(stateMapping);
        this.current = currentG;
    }

    public void setTarget(Map<String,String> stateMapping) {
        GlobalState targetG = search(stateMapping);
        this.target = targetG;
    }
}
