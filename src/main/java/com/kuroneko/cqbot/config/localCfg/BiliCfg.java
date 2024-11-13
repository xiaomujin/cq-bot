package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import lombok.Data;

import java.util.*;

@Data
public class BiliCfg implements ConfigSave {
    private HashMap<String, List<Long>> subMap = new HashMap<>();

    public void addSub(String uid, Long groupId) {
        lock.writeLock().lock();
        try {
            List<Long> list = subMap.computeIfAbsent(uid, k -> new ArrayList<>());
            list.add(groupId);
        } finally {
            lock.writeLock().unlock();
        }
        save();
    }

    public Boolean removeSub(String uid, Long groupId) {
        boolean result;
        lock.writeLock().lock();
        try {
            List<Long> list = subMap.getOrDefault(uid, Collections.emptyList());
            result = list.remove(groupId);
            if (list.isEmpty()) {
                subMap.remove(uid);
            }
        } finally {
            lock.writeLock().unlock();
        }
        save();
        return result;
    }

    public List<Long> getSubListByUid(String uid) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(subMap.get(uid)).orElse(Collections.emptyList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<String> getAllSubUid() {
        lock.readLock().lock();
        try {
            return subMap.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }
}
