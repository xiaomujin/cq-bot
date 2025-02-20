package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import com.kuroneko.cqbot.core.process.CommonProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class BiliCfg extends ConfigSave implements Serializable {
    private HashMap<String, List<Long>> subMap = new HashMap<>();

    public void addSub(String uid, Long groupId) {
        CommonProcessor.getInstance().execute(() -> {
            List<Long> list = subMap.computeIfAbsent(uid, k -> new ArrayList<>());
            list.add(groupId);
            save();
        });
    }

    public Boolean removeSub(String uid, Long groupId) {
        return CommonProcessor.getInstance().submitSync(() -> {
            boolean result;
            List<Long> list = subMap.getOrDefault(uid, new ArrayList<>());
            result = list.remove(groupId);
            if (list.isEmpty()) {
                subMap.remove(uid);
            }
            save();
            return result;
        });
    }

    public List<Long> getSubListByUid(String uid) {
        return Optional.ofNullable(subMap.get(uid)).orElse(Collections.emptyList());
    }

    public Set<String> getAllSubUid() {
        return subMap.keySet();
    }
}
