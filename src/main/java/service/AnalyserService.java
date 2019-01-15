package service;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class AnalyserService extends Application<AnalyserConfiguration> {
    
    public static void main(String[] args) throws Exception {
        new AnalyserService ().run(args);
    }
    
    @Override
    public void run(AnalyserConfiguration config, Environment env) {
        final AnalyserAPI api = new AnalyserAPI(
                config.getAliveMsg()
        );
        env.jersey().register(api);
    }
    
}
