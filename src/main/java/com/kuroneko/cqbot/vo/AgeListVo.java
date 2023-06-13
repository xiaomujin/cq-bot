package com.kuroneko.cqbot.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AgeListVo {
    @JsonProperty()//解决首字母大写json和javabean转换接收不到值的问题
    private List<AniPreUP> AniPreUP;
    @JsonProperty()
    private List<AniPreEvDay> AniPreEvDay;
    @JsonProperty()
    private List<XinfansInfo> XinfansInfo;
    @JsonProperty()
    private String Tip;
}
