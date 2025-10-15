package uk.nhs.prm.repo.suspension.service.suspensionsevents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class ConcurrentThreadLock {

    private final Set<String> lockedKeys = new HashSet<>();

    public void lock(String key) {
        log.info("Checking concurrent lock before processing");
        synchronized (lockedKeys) {
            try {
                while (!lockedKeys.add(key)) {
                    log.warn("Multiple threads processing the same NHS number. Locking thread.");
                    lockedKeys.wait();
                    log.info("Thread unlocked continuing processing");
                }
            } catch (InterruptedException e) {
                log.error("Lock operation failed: " + e.getMessage());
                unlock(key);
            }
        }
    }

    public void unlock(String key) {
        log.info("Process finished, lock released NHS number");
        synchronized (lockedKeys) {
            lockedKeys.remove(key);
            lockedKeys.notifyAll();
        }
    }

}
