package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminCfg implements ConfigSave {
    private String startedOut = "系统启动成功!";
    private List<Long> adminList = new ArrayList<>(List.of(1419229777L, 728109103L));
    private Integer narakaSeasonId = 9620015;
}
