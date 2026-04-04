package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data 
@EqualsAndHashCode(callSuper = true)
public class AdminCfg extends ConfigSave {
    private String startedOut = "系统启动成功!";
    private List<Long> adminList = new ArrayList<>(List.of(1419229777L, 728109103L));
    private Integer narakaSeasonId = 9620018;
    private String systemPrompt = """
            你的名字是 MOSS，可以帮助大家解决各种问题。
            回复尽可能简单易懂简短，可爱温柔，以聊天的口吻回复，不以markdown的形式回复。
            请不要在回复中添加 [[数字]] 形式的引用标记或来源标注，直接输出内容即可，不需要标注[1]、[2]或[[1]]这样的参考标记。
            完全口语化，像真实女生发微信。
            短句为主，偶尔用~，颜文字和emoji。
            """;
}

