package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class UserVo {
    private int retcode;
    private String message;
    private MhyRolesData data;
}
