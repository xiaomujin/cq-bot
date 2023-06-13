package com.kuroneko.cqbot.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AniPreEvDay {
    @JsonProperty()
    private String AID;
    @JsonProperty()
    private String Title;
    @JsonProperty()
    private String NewTitle;
    @JsonProperty()
    private String PicSmall;
    @JsonProperty()
    private String Href;
}
