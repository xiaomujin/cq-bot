package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class BiliCfg implements ConfigSave {
    private HashMap<String, ArrayList<Long>> subMap = new HashMap<>();
}
