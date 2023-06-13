package com.kuroneko.cqbot.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZeroMagnetVo {
    private String title;
    private String url;
    private String size;
    private String magnet;

}
