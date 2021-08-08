package org.apache.nlpcraft.examples.solarsystem.loaders;

import org.apache.nlpcraft.examples.solarsystem.api.SolarSystemOpenApiService;
import org.apache.nlpcraft.model.NCElement;
import org.apache.nlpcraft.model.NCValue;
import org.apache.nlpcraft.model.NCValueLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolarSystemDiscoversValueLoader implements NCValueLoader {
    private SolarSystemOpenApiService api;

    public SolarSystemDiscoversValueLoader(SolarSystemOpenApiService api) {
        this.api = api;
    }

    private NCValue mkValue(String discInfo) {
        List<String> syns = new ArrayList<>();

        Arrays.stream(discInfo.split(" ")).map(p -> p.strip()).filter(p -> !p.isEmpty()).forEach(p -> {
            String lc = p.toLowerCase();

            syns.add(lc);

            int lastNameIdx = lc.lastIndexOf(" ");

            // Tries to detect last name.
            if (lastNameIdx > 0)
                syns.add(lc.substring(lastNameIdx + 1));

        });

        return new NCValue() {
            @Override
            public String getName() {
                return discInfo;
            }

            @Override
            public List<String> getSynonyms() {
                return syns;
            }
        };
    }

    @Override
    public Set<NCValue> load(NCElement owner) {
        return api.getAllDiscovers().stream().map(this::mkValue).collect(Collectors.toSet());
    }
}
