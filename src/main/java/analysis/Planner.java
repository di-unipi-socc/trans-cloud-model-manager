package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Planner {
    
    // Application topology
    private final List<Node> nodes;
    private final Map<String,List<String>> binding;
    private final Map<String,Set<String>> dependencies;
    // List of all possible global states
    private final List<GlobalState> globalStates;
    
    // Current and target global states
    private GlobalState current;
    private GlobalState target;
    
    public Planner(List<Node> nodes, Map<String,List<String>> binding, Map<String,Set<String>> dependencies) {
        // Storing topology
        this.nodes = nodes;
        this.binding = binding;
        this.dependencies = dependencies;
        
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
        List<String> opSequence = new ArrayList();
        
        // ==========================================
        // Computing the initial sequence of steps to
        // prioritize nodes with failed dependencies
        // ==========================================
        GlobalState start = this.current;
        
        List<String> nodesWithFailedDeps = start.getNodesInState(ManagementProtocol.failedDependencies);
        for(String n : nodesWithFailedDeps) {
            List<Step> steps = start.getSteps();
            Step nStop = null;
            for(Step s : steps) {
                if(s.getReason().contains(n) && s.getReason().contains("stop"))
                    nStop = s;
            }
            opSequence.add(nStop.getReason());
            start = nStop.getNextGlobalState();
        }

        // ==========================================
        // Computing the (cheapest) sequence of steps
        // ==========================================
        
        // Maps of steps and cost from the global state s to the others
        Map<GlobalState,List<Step>> steps = new HashMap();
        steps.put(start,new ArrayList());
        Map<GlobalState,Integer> costs = new HashMap();
        costs.put(start,0);
        // List of visited states
        List<GlobalState> visited = new ArrayList();
        visited.add(start);

        // List of global states still to be visited
        List<GlobalState> toBeVisited = new ArrayList();
        
        // Adding the states reachable from start to "toBeVisited"
        for(Step step : start.getSteps()) {
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
                
                // If no cost has been computed, or if current path is cheaper,
                // updates "steps" and "costs"
                if(costs.get(next) == null || nextCost < costs.get(next).intValue()) {
                        List<Step> stepSeq = new ArrayList();
                        stepSeq.addAll(steps.get(current));
                        stepSeq.add(step);
                        steps.put(next,stepSeq);
                        costs.put(next,nextCost);
                }
                
                // "next" is to be visited if not already visited
                if(!(visited.contains(next)) && !(toBeVisited.contains(next))) 
                    toBeVisited.add(next);
            }
        }
        
        // ====================================================
        // Adding the sequence of operations to reach target
        // ====================================================
        // If no plan is available, return null
        if(steps.get(target) == null) 
            return null;
        // Otherwise, return the corresponding sequence of operations
        for(Step step : steps.get(target)) {
            if(!(step.getReason().contains(Step.handling))) {
                opSequence.add(step.getReason());
            }
        }
        return opSequence;
    }
    
    // Method for retrieving a set of parallel steps, executable in "current"
    // and allowing to move towards "target"
    public List<String> getParallelSteps() {
        List<String> parSteps = new ArrayList<String>();
        
        // Computing the sequential "plan"
        List<String> plan = getSequentialPlan();
        
        // Returning no step if "plan" is empty
        if(plan.isEmpty()) return parSteps;
        
        // Getting the first step of "plan" (executable by definition)
        Iterator<String> planIter = plan.iterator();
        parSteps.add(planIter.next());
        
        // Getting steps of "plan" executable in parallel with the first one
        // (1) since they are executable in "current" and 
        // (2) since they apply to nodes not depending one another
        while(planIter.hasNext()) {
            // Getting potential "step"
            String step = planIter.next();
            String stepNode = step.split("/")[0];
            Set<String> stepNodeDeps = dependencies.get(stepNode);
            
            // Checking conditions for adding "step" to "parSteps"             
            boolean toBeAdded = false;
            
            // (1) Adding "step" only if executable in current
            for(Step s : current.getSteps()) {
                if(s.getReason().equals(step)) 
                    toBeAdded = true;
            }
            
            // If condition (1) holds
            if(toBeAdded) {
                // (2) Adding "step" only if (the node corresponding to) "step" does
                // not depend on (the nodes corresponding to) the steps in "parSteps"
                // (and if the viceversa also holds)
                for(String parStep : parSteps) {
                    String parStepNode = parStep.split("/")[0];
                    Set<String> parStepNodeDeps = dependencies.get(parStepNode);
                    if(stepNodeDeps.contains(parStepNode) || parStepNodeDeps.contains(stepNode))
                        toBeAdded = false;
                }
            }
            // "step" added if both (1) and (2) hold
            if(toBeAdded) parSteps.add(step);
        }
        return parSteps;
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
        
        // Handle pending failures
        Step handler;
        while(currentG.getPendingFaults().size() > 0) {
            handler = null;
            for(Step s : currentG.getSteps())
                if(s.getReason().contains(Step.handling))
                    handler = s;
            currentG = handler.getNextGlobalState();
        }
        
        this.current = currentG;
    }

    public void setTarget(Map<String,String> stateMapping) {
        GlobalState targetG = search(stateMapping);
        this.target = targetG;
    }
}
