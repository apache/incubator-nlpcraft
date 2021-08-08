package org.apache.nlpcraft.examples.solarsystem;

import org.apache.nlpcraft.examples.solarsystem.SolarSystemModel;
import org.apache.nlpcraft.examples.solarsystem.SolarSystemProbe;
import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService;
import org.apache.nlpcraft.examples.solarsystem.loaders.SolarSystemDiscoversValueLoader;
import org.apache.nlpcraft.examples.solarsystem.loaders.SolarSystemPlanetsValueLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class SolarSystemConfig {


    @Bean
    public SolarSystemModel getSolarSystemModel(SolarSystemOpenApiService s) {
        return new SolarSystemModel(s);
    }

    @Bean
    public SolarSystemOpenApiService getSolarSystemOpenApiService() {
        return new SolarSystemOpenApiService();
    }

    @Bean
    public SolarSystemPlanetsValueLoader getSolarSystemPlanetsValueLoader(SolarSystemOpenApiService s) {
        return new SolarSystemPlanetsValueLoader(s);
    }

    @Bean
    public SolarSystemDiscoversValueLoader getSolarSystemDiscoversValueLoader(SolarSystemOpenApiService s) {
        return new SolarSystemDiscoversValueLoader(s);
    }
}
