package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class BiliCfg implements ConfigSave {
    private HashMap<String, List<Long>> subMap = new HashMap<>();
}
