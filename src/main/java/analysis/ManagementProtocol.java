package analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagementProtocol {
    
    private final List<String> states;
    private final String initialState;
    // rho - function associating states with the requirements they need
    private final Map<String,List<String>> rho;
    // gamma - function associating states with the capabilities they provide
    private final Map<String,List<String>> gamma;
    // tau - transition relation
    private final Map<String,List<Transition>> tau;
    // phi - fault handling transition relation
    private final Map<String,List<String>> phi;
    
    // Constructor for obtaining default (Brooklyn) management protocol
    public ManagementProtocol(List<String> reqs, List<String> caps) {
        states = new ArrayList<String>();
        rho = new HashMap<String,List<String>>();
        gamma = new HashMap<String,List<String>>();
        tau = new HashMap<String,List<Transition>>();
        phi = new HashMap<String,List<String>>();
                
        // States
        createState("unavailable");
        initialState = "unavailable";
        createState("started");
        createState("stopped");
        createState("failed");
        
        // rho
        for(String r : reqs) {
            rho.get("started").add(r);
        }
        
        // gamma
        for(String c : caps) {
            gamma.get("started").add(c);
        }
        
        // tau
        List<String> none = new ArrayList<String>();
        tau.get("unavailable").add(new Transition("Lifecycle/start",reqs,none,"started"));
        tau.get("started").add(new Transition("Lifecycle/release",none,none,"unavailable"));
        tau.get("started").add(new Transition("Lifecycle/stop",none,none,"stopped"));
        tau.get("stopped").add(new Transition("Lifecycle/release",none,none,"unavailable"));
        tau.get("stopped").add(new Transition("Lifecycle/start",reqs,none,"started"));
        tau.get("failed").add(new Transition("Lifecycle/release",none,none,"unavailable"));
                
        // phi
        phi.get("started").add("failed");
    }
    
    public ManagementProtocol(List<String> states,
            String initialState,
            Map<String,List<String>> rho,
            Map<String,List<String>> gamma,
            Map<String,List<Transition>> tau,
            Map<String,List<String>> phi) {
        this.states = states;
        this.initialState = initialState;
        this.rho = rho;
        this.gamma = gamma;
        this.tau = tau;
        this.phi = phi;
    }
    
    private void createState(String stateName) {
        this.states.add(stateName);
        rho.put(stateName, new ArrayList());
        gamma.put(stateName, new ArrayList());
        tau.put(stateName, new ArrayList());
        phi.put(stateName, new ArrayList());
    }

    public List<String> getStates() {
        return this.states;
    } 
    
    public Map<String,List<String>> getRho() {
        return this.rho;
    }

    public Map<String,List<String>> getGamma() {
        return this.gamma;
    }

    public Map<String,List<Transition>> getTau() {
        return this.tau;
    }

    public Map<String,List<String>> getPhi() {
        return this.phi;
    }
            
    @Override
    public String toString() {
        return "{ states: " + this.states +
                ", rho: " + this.rho +
                ", gamma: " + this.gamma +
                ", tau: " + this.tau +
                ", phi: " + this.phi + "}";
    }
}
