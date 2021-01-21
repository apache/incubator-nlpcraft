package org.apache.nlpcraft.common.pool;

import java.util.concurrent.ExecutorService;

public interface NCPoolFactory {
    ExecutorService mkExecutorService();
}
