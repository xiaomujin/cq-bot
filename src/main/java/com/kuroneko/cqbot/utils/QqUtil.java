package com.kuroneko.cqbot.utils;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
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


    public boolean verifyQq(GroupMessageEvent event){
        if (event.getSender().getRole().equals("member")){
            String s = redisUtil.get("setuSystem");
            boolean flag = true;
            if (!StringUtils.isBlank(s)){
                List<String> qqList = Arrays.asList(s.split(","));
                for (String s1 : qqList) {
                    if (s1.equals(event.getUserId().toString())){
                        flag = false;
                    }
                }
            }
            return flag;
        }else {
            return false;
        }

    }
}
