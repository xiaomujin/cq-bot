package com.kuroneko.cqbot.utils;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class QqUtil {
    public final  RedisUtil redisUtil;

    public QqUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }


    public boolean verifyQq(Long qq){
        String s = redisUtil.get("setuSystem");
        boolean flag = false;
        if (!StringUtils.isBlank(s)){
            List<String> qqList = Arrays.asList(s.split(","));
            for (String s1 : qqList) {
                if (s1.equals(qq.toString())){
                    flag = true;
                }
            }
        }

        return flag;
    }
}
