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
import org.jsoup.internal.StringUtil;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeTuPlugin extends BotPlugin {
    private final SeTuService seTuService;

    private final RedisUtil redisUtil;
    private static final String CMD1 = CmdConst.SE_TU;
    private static final String CMD2 = CmdConst.HIGH_SE_TU;

    private String getTag(String msg, String cmd) {
        String tag = "";
        if (msg.length() > cmd.length()) {
            tag = msg.substring(cmd.length()).trim();
        }
        log.info("tag:{}", tag);
        return tag;
    }

    private Two getMsg(String tag, Long qq, boolean high) {
        boolean isPid = RegexUtil.isNumber(tag);
        Two two = new Two();
//        redisUtil.set("setuSystem","728109103");
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

        if (!flag){
            MsgUtils msg = MsgUtils.builder().text("暂无查看涩图权限");
            two.setMsg(msg);
            return two;
        }

        if (isPid) {
            SeTuData seTuData = new SeTuData();
            seTuData.setPid(Long.parseLong(tag));
            String imgUrl = Constant.SE_TU_PID_URL.replace("IMGID", tag);
//            imgUrl = high ? imgUrl : imgUrl + "&web=true";
            log.info("图片url：{}", imgUrl);
            MsgUtils msg = MsgUtils.builder().img(imgUrl);
            SeTuUrls seTuUrls = new SeTuUrls();
            seTuUrls.setRegular(imgUrl);
            seTuData.setUrls(seTuUrls);
            two.setSeTuData(seTuData);
            two.setMsg(msg);
        } else {
            SeTuData seTuData = seTuService.getSeTuData(tag);
            if (seTuData == null) {
                MsgUtils msg = MsgUtils.builder().text("没有查询到tag： " + tag);
                two.setMsg(msg);
                return two;
            }
            String imgUrl = seTuData.getImgUrl();
            log.info("图片url：{}", imgUrl);
            MsgUtils msg = MsgUtils.builder().img(imgUrl).text("标题：" + seTuData.getTitle() + Constant.XN).text("作者：" + seTuData.getAuthor() + Constant.XN).text("pid：" + seTuData.getPid());
            two.setSeTuData(seTuData);
            two.setMsg(msg);
        }
        return two;
    }

    private MsgUtils getFailMsg(Long qq, SeTuData seTuData) {
        return MsgUtils.builder().text(" 发送失败\n 链接：" + seTuData.getImgUrl() + Constant.XN).text("标题：" + seTuData.getTitle() + Constant.XN).text("作者：" + seTuData.getAuthor() + Constant.XN).text("pid：" + seTuData.getPid());
    }

    @Data
    static class Two {
        private MsgUtils msg;
        private SeTuData seTuData;
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        boolean high = event.getRawMessage().startsWith(CMD2);
        if (event.getRawMessage().startsWith(CMD1) || high) {
            String cmd = high ? CMD2 : CMD1;
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), cmd);
            String tag = getTag(event.getRawMessage(), cmd);
            Two two = getMsg(tag, event.getUserId(), high);
            ActionData<MsgId> sendMsg = bot.sendMsg(event, two.getMsg().build(), false);
            if (sendMsg == null || sendMsg.getData().getMessageId() == null) {
                bot.sendMsg(event, getFailMsg(event.getUserId(), two.getSeTuData()).build(), false);
            }
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
