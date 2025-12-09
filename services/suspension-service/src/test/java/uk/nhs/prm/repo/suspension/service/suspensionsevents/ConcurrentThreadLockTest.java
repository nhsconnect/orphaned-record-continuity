package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.WAITING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class ConcurrentThreadLockTest {

    @Test
    @Timeout(value = 2)
    void shouldLockThreadWhenKeyHasAlreadyBeenSet() {
        ConcurrentThreadLock threadLock = new ConcurrentThreadLock();

        String firstKey = "keyA";
        String secondKey = "keyB";

        threadLock.lock(firstKey);
        Thread t1 = new Thread(() -> threadLock.lock(firstKey));
        Thread t2 = new Thread(() -> threadLock.lock(secondKey));
        t1.start();
        t2.start();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(t2.getState()).isEqualTo(TERMINATED);
            assertThat(t1.getState()).isEqualTo(WAITING);
        });
    }

    @Test
    void shouldUnlockTheadWhenUnlockIsCalledWithKey() {
        ConcurrentThreadLock threadLock = new ConcurrentThreadLock();
        String firstKey = "keyA";

        threadLock.lock(firstKey);
        Thread t1 = new Thread(() -> threadLock.lock(firstKey));

        t1.start();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() ->  assertThat(t1.getState()).isEqualTo(WAITING));

        threadLock.unlock(firstKey);

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() ->  assertThat(t1.getState()).isEqualTo(TERMINATED));
    }

}