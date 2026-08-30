package io.github.finalwave.network.sync;

import io.github.finalwave.network.MessageTypes;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentLinkedDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressSyncServiceQueueTest {

    @Test
    void queuePreservesFifoOrder() {
        ConcurrentLinkedDeque<String> queue = new ConcurrentLinkedDeque<>();
        queue.add(MessageTypes.UPDATE_WALLET);
        queue.add(MessageTypes.UPDATE_PLANT);
        queue.add(MessageTypes.UNLOCK_CONTENT);
        assertEquals(MessageTypes.UPDATE_WALLET, queue.poll());
        assertEquals(MessageTypes.UPDATE_PLANT, queue.poll());
        assertEquals(MessageTypes.UNLOCK_CONTENT, queue.poll());
    }
}
