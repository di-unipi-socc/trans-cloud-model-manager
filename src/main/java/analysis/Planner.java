package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planner {
    
    // List of all possible global states
    private final List<Node> nodes;
    private final Map<String,List<String>> binding;
    private final List<GlobalState> globalStates;
    
    public Planner(List<Node> nodes, Map<String,List<String>> binding) {
        this.nodes = nodes;
        this.binding = binding;
        
        // Generating all possible global states
        this.globalStates = generateGlobalStates(nodes);
        
        // Generating all possible steps between global states
        generateSteps();
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
                            // Creating a new global state with the updated 
                            // mapping for the actual state of n
                            GlobalState next = new GlobalState(nodes,binding);
                            next.addMapping(g);
                            next.addMapping(nName,t.getTargetState());
                            // Searching the ref to the corresponding global 
                            // state in the list globalStates
                            for(GlobalState g1 : this.globalStates) {
                                if(g1.equals(next)) next = g1;
                            }
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


                        // Creating a new global state with the updated 
                        // mapping for the actual state of n
                        GlobalState next = new GlobalState(nodes,binding);
                        next.addMapping(g);
                        next.addMapping(nName,targetState);
                        // Searching the ref to the corresponding global 
                        // state in the list globalStates
                        for(GlobalState g1 : this.globalStates) {
                            if(g1.equals(next)) next = g1;
                        }
                        // Adding the step to list of steps in g
                        g.addStep(new Step(nName,next));
                    }
                }
            }
            // System.out.println("\n** GLOBAL STATE ** \n " + g);
        }
    }
 
    // Method for retrieving the (cheapest) sequence of steps for changing the
    // the configuration of the application from "start" to "target"
    public List<String> getSequentialPlan(Map<String,String> start, Map<String,String> target) {
        // Identifying global states corresponding to start and target
        GlobalState s = new GlobalState(nodes,binding);
        s.addMapping(start);
        GlobalState t = new GlobalState(nodes,binding);
        t.addMapping(target);
        for(GlobalState g : globalStates) {
            if(s.equals(g)) s = g;
            else if(t.equals(g)) t = g;
        }
        
        //System.out.println("** Start ** \n " + s.getMapping());
        //System.out.println("** Target ** \n " + t.getMapping());
        
        // ==========================================
        // Computing the (cheapest) sequence of steps
        // ==========================================
        
        // Maps of steps and cost from the global state s to the others
        Map<GlobalState,List<Step>> steps = new HashMap();
        steps.put(s,new ArrayList());
        Map<GlobalState,Integer> costs = new HashMap();
        costs.put(s,0);
        // List of visited states
        List<GlobalState> visited = new ArrayList();
        visited.add(s);

        // List of global states still to be visited
        List<GlobalState> toBeVisited = new ArrayList();
        
        // Adding the states reachable from start to "toBeVisited"
        for(Step step : s.getSteps()) {
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
            //System.out.println("States to be visited: " + toBeVisited.size());
            // Removing the first global state to be visited and marking it
            // as visited
            GlobalState current = toBeVisited.remove(0);
            //System.out.println("  Selected: " + current.getMapping() + 
            //        "\n  (available steps: " + current.getSteps().size() + ")");
            visited.add(current);
            
            for(Step step : current.getSteps()) {
                // System.out.println("      Step: " + step.getReason());
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
                    //System.out.println("      Added: " + next.getMapping());
                }
            }
        }
        
        //System.out.println("total global states: " + this.globalStates.size());
        //System.out.println("visited global states: " + visited.size());
        
        // ====================================================
        // Computing the sequence of operations from "s" to "t"
        // ====================================================
        List<String> opSequence = new ArrayList();
        for(Step step : steps.get(t)) {
            if(!(step.getReason().contains(Step.handling))) {
                opSequence.add(step.getReason());
            }
        }
        return opSequence;
    }
}
