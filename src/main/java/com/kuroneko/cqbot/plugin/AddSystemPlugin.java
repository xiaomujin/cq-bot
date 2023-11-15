package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddSystemPlugin extends BotPlugin {
    private final RedisUtil redisUtil;
    private static final String CMD1 = CmdConst.ADD_SE_TU_ADMIN;
    private static final String CMD2 = CmdConst.DEL_SE_TU_ADMIN;


    private String getTag(String msg) {
        String tag = "";
        if (msg.length() > CMD1.length()) {
            tag = msg.substring(CMD1.length()).trim();
        }
        log.info("tag:{}", tag);
        return tag;
    }
    private String getTag2(String msg) {
        String tag = "";
        if (msg.length() > CMD2.length()) {
            tag = msg.substring(CMD2.length()).trim();
        }
        log.info("tag:{}", tag);
        return tag;
    }
    private MsgUtils getMsg(String addQq,Long qq) {
        MsgUtils msg;

        boolean flag = false;
        List<Long> list = Arrays.asList(1419229777L, 728109103L);
        if (list.contains(qq)) {
            flag = true;
        }
        if (!flag){
            msg = MsgUtils.builder().text("只有主人才可以添加哦！");
            return msg;
        }

//        String setuSystem = redisUtil.get("setuSystem");
        String system = redisUtil.get("system");

        if (StringUtils.isBlank(system)){
            system = addQq;
        }else {
            boolean addFlag = false;
            List<String> setuSystemList = Arrays.asList(system.split(","));
            for (String s : setuSystemList) {
                if (s.equals(addQq)){
                    addFlag = true;
                }
            }
            if (addFlag){
                msg = MsgUtils.builder().text("用户:"+addQq+"已存在,无法继续添加");
                return msg;
            }
            system = system + "," + addQq;
        }
        redisUtil.set("system",system);
        msg = MsgUtils.builder().text("用户:" + addQq + "添加管理成功");

        return msg;
    }
    private MsgUtils getMsg2(String addQq,Long qq) {
        MsgUtils msg;
        String system = redisUtil.get("system");
        boolean flag = false;
        if (!StringUtils.isBlank(system)){
            List<String> qqList = Arrays.asList(system.split(","));
            for (String s1 : qqList) {
                if (s1.equals(qq.toString())){
                    flag = true;
                }
            }
        }

        if (!flag){
            msg = MsgUtils.builder().text("暂无取消涩图用户权限");
            return msg;
        }
        String setuSystem = redisUtil.get("setuSystem");

        if (StringUtils.isBlank(setuSystem)){
            msg = MsgUtils.builder().text("用户:"+addQq+"不存在,无法取消");
            return msg;
        }else {
            boolean addFlag = true;
            List<String> setuSystemList = Arrays.asList(setuSystem.split(","));
            for (String s : setuSystemList) {
                if (s.equals(addQq)){
                    addFlag = false;
                    break;
                }
            }

            if (addFlag){
                msg = MsgUtils.builder().text("用户:"+addQq+"不存在,无法取消");
                return msg;
            }

            List<String> newSetuSystemList = setuSystemList.stream().filter(p -> {
                if (p.equals(addQq)) {
                    return false;
                } else {
                    return true;
                }
            }).toList();



            StringBuilder newSetuSystem = new StringBuilder();
            for (String s : newSetuSystemList) {
                if (StringUtils.isBlank(newSetuSystem.toString())){
                    newSetuSystem.append(s);
                }else {
                    newSetuSystem.append(",").append(s);
                }
            }
            redisUtil.set("setuSystem",newSetuSystem);
            msg = MsgUtils.builder().text("用户:" + addQq + "取消成功");
            return msg;
        }


    }


    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CMD1)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CMD1);
            String addQq = getTag(event.getRawMessage());
            MsgUtils msg = getMsg(addQq,event.getUserId());
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }else if (event.getRawMessage().startsWith(CMD2)){
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CMD2);
            String addQq = getTag2(event.getRawMessage());
            MsgUtils msg = getMsg2(addQq,event.getUserId());
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
