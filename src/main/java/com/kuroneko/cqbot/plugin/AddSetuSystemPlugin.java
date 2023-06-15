package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.service.SeTuService;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.utils.RegexUtil;
import com.kuroneko.cqbot.vo.SeTuData;
import com.kuroneko.cqbot.vo.SeTuUrls;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.common.MsgId;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddSetuSystemPlugin extends BotPlugin {
    private final RedisUtil redisUtil;
    private static final String CMD1 = CmdConst.ADD_SE_TU_SYSTEM;


    private String getTag(String msg) {
        String tag = "";
        if (msg.length() > CMD1.length()) {
            tag = msg.substring(CMD1.length()).trim();
        }
        log.info("tag:{}", tag);
        return tag;
    }
    private MsgUtils getMsg(String addQq,Long qq) {
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
            msg = MsgUtils.builder().text("暂无添加涩图用户权限");
            return msg;
        }
        String setuSystem = redisUtil.get("setuSystem");

        if (StringUtils.isBlank(setuSystem)){
            setuSystem = addQq;
        }else {
            boolean addFlag = false;
            List<String> setuSystemList = Arrays.asList(setuSystem.split(","));
            for (String s : setuSystemList) {
                if (s.equals(addQq)){
                    addFlag = true;
                }
            }

            if (addFlag){
                msg = MsgUtils.builder().text("用户:"+addQq+"已存在,无法继续添加");
                return msg;
            }

            setuSystem = setuSystem + "," + addQq;
        }
        redisUtil.set("setuSystem",setuSystem);
        msg = MsgUtils.builder().text("用户:" + addQq + "添加成功");

        return msg;
    }


    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CMD1)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CMD1);
            String addQq = getTag(event.getRawMessage());
            MsgUtils msg = getMsg(addQq,event.getUserId());
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
