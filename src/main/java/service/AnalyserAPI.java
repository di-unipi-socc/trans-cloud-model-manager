package service;

import analysis.Application;
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
                    
        // ============================
        // Parsing application topology
        // ============================
        Map<String,Object> topology = (Map) spec.get("topology_template");
        List<String> nodeNames;
        Map<String,List<String>> bindings = new HashMap<String,List<String>>();
        
        // Parsing node names
        Map<String,Object> nodes = (Map) topology.get("node_templates");
        Set<String> nodeSet = nodes.keySet();
        nodeNames = new ArrayList<String>(nodeSet);
        
        // Parsing relationships
        for (String nodeName : nodeNames) {
            Map<String,Object> node = (Map) nodes.get(nodeName);
            List<Map<String,Object>> nodeReqs = (List) node.get("requirements");
            
            if(nodeReqs != null) {
                for(Map reqMap : nodeReqs) {
                    // Parsing requirement name
                    String reqName = (String) reqMap.keySet().iterator().next();
                    String reqId = nodeName + "/" + reqName;
                    
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
        
        Application app = new Application(nodeNames,bindings);
        apps.put(appName,app);
        System.out.println(app.toString());
              
        // A URI for the posted app is created and returned with "201 Created"
        URI appUri = UriBuilder.fromResource(AnalyserAPI.class).path(appName).build();
        return Response.created(appUri).build();
    }
       
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
