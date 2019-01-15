package service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class AnalyserConfiguration extends Configuration {
    
    private String aliveMsg;
    
    @JsonProperty
    public String getAliveMsg() { return aliveMsg; }
    
    @JsonProperty
    public void setAliveMsg(String aliveMsg) { this.aliveMsg = aliveMsg; }
}
