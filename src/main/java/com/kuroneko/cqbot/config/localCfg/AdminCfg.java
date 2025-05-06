package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminCfg extends ConfigSave implements Serializable {
    private String startedOut = "系统启动成功!";
    private List<Long> adminList = new ArrayList<>(List.of(1419229777L, 728109103L));
    private Integer narakaSeasonId = 9620016;
}

