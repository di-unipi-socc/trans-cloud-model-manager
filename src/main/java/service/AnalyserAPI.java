package service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/mm")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyserAPI {
    
    private final String aliveMsg;
    
    public AnalyserAPI(String aliveMsg) {
        this.aliveMsg = aliveMsg;
    }
    
    @GET
    public Response isAlive() {
        return Response.ok()
                .entity(aliveMsg)
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
    
}
