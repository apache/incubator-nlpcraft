package org.apache.nlpcraft.examples.solarsystem;

import org.apache.nlpcraft.model.tools.embedded.NCEmbeddedProbe;
import org.apache.nlpcraft.probe.NCProbe;
import org.apache.nlpcraft.probe.NCProbeBoot;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;

public class SolarSystemProbe {
    @PostConstruct
    public void start() {
        CompletableFuture<Integer> fut = new CompletableFuture<>();

        NCProbeBoot.start(new String[]{}, fut);
//
//        while (!fut.isDone())
//            fut.get();
//        }
//
//        if (fut.get() != 0)
//            System.exit(fut.get)
    }

    @PreDestroy
    public void stop() {
        NCEmbeddedProbe.stop();
    }
}
