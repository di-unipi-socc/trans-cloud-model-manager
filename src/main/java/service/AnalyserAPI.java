package service;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.yaml.snakeyaml.Yaml;

@Path("/mm")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyserAPI {
    
    private final String aliveMsg;
    
    public AnalyserAPI(String aliveMsg) {
        this.aliveMsg = aliveMsg;
    }
    
    @POST
    public Response addApp(String tosca) {
        // TODO: Add Response - to manage HTTP codes and the like
        // TODO: Add parsing of app and creation of representation
        // TODO: Add storing of created apps 
        // TODO: Add parsing/generation of management protocols
        
        Yaml yaml = new Yaml(); 
        Map<String,Object> spec = (Map) yaml.load(tosca);
        String appName = (String) spec.get("template_name");
        System.out.println("App name: " + appName);
        
        Map<String,Object> topology = (Map) spec.get("topology_template");
        System.out.println(topology.toString());
        
        // A URI for the posted app is created and returned with "201 Created"
        URI appUri = UriBuilder.fromResource(AnalyserAPI.class).path(appName).build();
        return Response.created(appUri).build();
    }
    
    @GET
    public Response isAlive() {
        return Response.ok()
                .entity(aliveMsg)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
    
}
