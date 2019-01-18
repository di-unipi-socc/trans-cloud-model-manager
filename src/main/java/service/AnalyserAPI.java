package service;

import analysis.Application;
import analysis.Node;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.yaml.snakeyaml.Yaml;

@Path("/mm")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyserAPI {
    
    private final String aliveMsg;
    private final Map<String,Application> apps;
    
    // TODO : Add persistent storage of apps
    
    public AnalyserAPI(String aliveMsg) {
        this.aliveMsg = aliveMsg;
        this.apps = new HashMap<String,Application>();
    }
    
    // Method for GET-checking aliveness of the API
    @GET
    public Response isAlive() {
        return Response.ok()
                .entity(aliveMsg)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
    
    // Method for POSTing a new application
    @POST
    public Response addApp(String tosca) {
        // TODO: Add parsing/generation of management protocols
        
        Yaml yaml = new Yaml(); 
        Map<String,Object> spec = (Map) yaml.load(tosca);
        
        // ========================
        // Parsing application name
        // ========================
        String appName = (String) spec.get("template_name");
        if(appName == null)
            return Response.status(Status.BAD_REQUEST)
                    .entity("Template name not found")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        if(apps.containsKey(appName))
            return Response.status(Status.CONFLICT)
                    .entity("Application already posted")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
                    
        // ==================
        // Parsing node types
        // ==================
        
        // TODO : add parsing of normative node types
        
        Map<String,Object> nodeTypes = (Map) spec.get("node_types");
        Map<String,List<String>> nodeTypeCaps = new HashMap<String,List<String>>();
        for (String nodeTypeName : nodeTypes.keySet()) {
            Map<String,Object> nodeType = (Map) nodeTypes.get(nodeTypeName);
            nodeTypeCaps.put(nodeTypeName, new ArrayList<String>());
            Map<String,Object> capDefs = (Map) nodeType.get("capabilities");
            for(String capName : capDefs.keySet())
                nodeTypeCaps.get(nodeTypeName).add(capName);
        }
        
        // ============================
        // Parsing application topology
        // ============================
        Map<String,Object> topology = (Map) spec.get("topology_template");
        List<String> nodeNames;
        Map<String,List<String>> bindings = new HashMap<String,List<String>>();
        
        // Parsing node template names
        Map<String,Object> nodes = (Map) topology.get("node_templates");
        Set<String> nodeSet = nodes.keySet();
        nodeNames = new ArrayList<String>(nodeSet);
        
        // Creating maps for node capabilities and requirements
        Map<String,List<String>> caps = new HashMap<String,List<String>>();
        Map<String,List<String>> reqs = new HashMap<String,List<String>>();
        
        // Parsing relationships
        for (String nodeName : nodeNames) {
            Map<String,Object> node = (Map) nodes.get(nodeName);
            
            // Adding the node to the maps of requirements and capabilities
            caps.put(nodeName, new ArrayList<String>());
            reqs.put(nodeName, new ArrayList<String>());
            
            // Parsing node capabilities
            String nodeTypeName = (String) node.get("type");
            for(String cap : nodeTypeCaps.get(nodeTypeName))
                caps.get(nodeName).add(cap);
            Map<String,Object> nodeCaps = (Map) node.get("capabilities");
            if(nodeCaps != null) {
                for(String capName : nodeCaps.keySet()) {
                    if(!caps.get(nodeName).contains(capName))
                        caps.get(nodeName).add(capName);
                }
            }

            // Parsing node requirements and corresponding bindings
            List<Map<String,Object>> nodeReqs = (List) node.get("requirements");
            if(nodeReqs != null) {
                for(Map reqMap : nodeReqs) {
                    // Parsing requirement name
                    String reqName = (String) reqMap.keySet().iterator().next();
                    String reqId = nodeName + "/" + reqName;
                    
                    // Adding requirement name to the map of requirements
                    if(!reqs.get(nodeName).contains(reqName))
                        reqs.get(nodeName).add(reqName);
                    
                    // Creating a new binding for the requirement (if not already there)
                    if(!bindings.containsKey(reqId))
                        bindings.put(reqId, new ArrayList<String>());
                    
                    // Filling the requirement binding with the target capability
                    Map<String,Object> reqInfo = (Map) reqMap.get(reqName);
                    String targetNode = (String) reqInfo.get("node");
                    String targetCap = (String) reqInfo.get("capability");
                    
                    // TODO : consider single line requirement assignment 
                    // TODO : include possibility of not specifying capability name
                    if(targetNode == null || targetCap == null)
                        return Response.status(Status.BAD_REQUEST)
                                .entity("Missing 'node' or 'capability' in req " + reqId)
                                .type(MediaType.TEXT_PLAIN)
                                .build();
                    
                    bindings.get(reqId).add(targetNode + "/" + targetCap);
                }
            }
        }
        
        Application app = new Application(nodeNames,reqs,caps,bindings);
        apps.put(appName,app);
        //System.out.println(app.toString());
              
        // A URI for the posted app is created and returned with "201 Created"
        URI appUri = UriBuilder.fromResource(AnalyserAPI.class).path(appName).build();
        return Response.created(appUri).build();
    }
    
    // Method for retrieving a plan allowing to change the configuration of 
    // "appName" from the "current" global state to a "target" global state
    // ("current" and "target" are represented in JSON in "body")
    @GET
    @Path("/{appName}/plan")
    public Response deleteApp(@PathParam("appName") String name, String body) {
        
        Application app = this.apps.get(name);
        
        // TODO : Add parsing of "body" to extract global states
        
        // TODO : Invoke planner of "appName" to retrieve plan
        
                
        // Trying to get a plan example
        Map<String,String> unavailable = new HashMap();
        Map<String,String> started = new HashMap();
        for(Node n : app.getNodes()) {
            unavailable.put(n.getName(),"unavailable");
            started.put(n.getName(),"started");
        }
        
        try {
            List<String> deploymentPlan = app.getPlanner().getSequentialPlan(unavailable,started);
            System.out.println(deploymentPlan);
        } catch(Exception e) {
            System.out.println("error");
        }
        
        
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity("Method not yet implemented")
                .type(MediaType.TEXT_PLAIN)
                .build();
        
    }
    
    // Method for deleting a previously registered application 
    // identified by "appName"
    @DELETE
    @Path("/{appName}")
    public Response deleteApp(@PathParam("appName") String name) {
        if(apps.containsKey(name)) {
            apps.remove(name);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
}
