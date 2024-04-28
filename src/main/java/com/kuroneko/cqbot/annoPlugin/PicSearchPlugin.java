package com.kuroneko.cqbot.annoPlugin;

import com.alibaba.fastjson2.JSON;
import com.kuroneko.cqbot.dto.SauceDTO;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class PicSearchPlugin {
    private final ModePlugin modePlugin;
    private static final String sauceNaoKey = "a4aa702c9ee5f0f66a7f0f4846a7a77c5a1f5ec2";

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.PIC_SEARCH, at = AtEnum.BOTH)
    public void handler(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.PIC_SEARCH);
        ExceptionHandler.with(bot, event, () -> {
            modePlugin.setSearchMode("搜图", event.getUserId(), event.getGroupId(), bot);
            return "";
        });
    }

    @AnyMessageHandler
    public void searchPic(Bot bot, AnyMessageEvent event) {
        if (!modePlugin.check("搜图", event.getUserId(), event.getGroupId())) {
            return;
        }
        ExceptionHandler.with(bot, event, () -> {
            List<ArrayMsg> list = event.getArrayMsg().stream()
                    .filter(msg -> msg.getType() == MsgTypeEnum.image)
                    .toList();
            if (list.isEmpty()) {
                return "";
            }
            modePlugin.resetExpiration(event.getUserId(), event.getGroupId());
            String url = list.getFirst().getData().get("url");
            SauceDTO request = request(url);
            List<SauceDTO.Result> results = request.getResults();
            if (results.isEmpty()) {
                return "未能找到相似的内容";
            }
            SauceDTO.Result first = results.getFirst();
            SauceDTO.ResultHeader header = first.getHeader();
            SauceDTO.ResultData data = first.getData();
            MsgUtils msgUtils = MsgUtils.builder()
                    .img(header.getThumbnail())
                    .text(STR."\n相似度：\{header.getSimilarity()}%");
            switch (header.getIndexId()) {
                case 5 -> {
                    msgUtils.text(STR."\n标题：\{data.getTitle()}");
                    msgUtils.text(STR."\n画师：\{data.getAuthorName()}");
                    msgUtils.text(STR."\n作品主页：https://pixiv.net/i/\{data.getPixivId()}");
                    msgUtils.text(STR."\n画师主页：https://pixiv.net/u/\{data.getAuthorId()}");
                    msgUtils.text(STR."\n反代地址：https://i.loli.best/\{data.getPixivId()}");
                    msgUtils.text("\n数据来源：SauceNao (Pixiv)");
                }
                case 41 -> {
                    msgUtils.text(STR."\n链接：\{data.getExtUrls().getFirst()}");
                    msgUtils.text("\n用户：" + STR."https://twitter.com/\{data.getTwitterUserHandle()}");
                    msgUtils.text("\n数据来源：SauceNao (Twitter)");
                }
                case 18, 38 -> {
                    msgUtils.text(STR."\n来源：\{data.getSource()}");
                    msgUtils.text(STR."\n日文名：\{data.getJpName()}");
                    msgUtils.text(STR."\n英文名：\{data.getEngName()}");
                    msgUtils.text("\n数据来源：SauceNao (H-Misc)");
                }
                default -> {
                    msgUtils.text(STR."\n链接：\{data.getExtUrls().getFirst()}");
                    msgUtils.text("\n数据来源：SauceNao");
                }
            }
            return msgUtils.build();
        });
    }

    @Synchronized
    private SauceDTO request(String img) {
        try {
            String api = STR."https://saucenao.com/search.php?api_key=\{sauceNaoKey}&output_type=2&numres=3&db=999&url=\{URLEncoder.encode(img, Charset.defaultCharset())}";
            String resStr = HttpUtil.get(api);
            SauceDTO sauceDTO = JSON.parseObject(resStr, SauceDTO.class);
            if (sauceDTO.getHeader().getLongRemaining() <= 0) throw new BotException("今日的搜索配额已耗尽啦");
            if (sauceDTO.getHeader().getShortRemaining() <= 0) throw new BotException("短时间内搜索配额已耗尽啦");
            if (sauceDTO.getResults() == null || sauceDTO.getResults().isEmpty())
                throw new BotException("服务出现问题或未能找到相似的内容");
            return sauceDTO;
        } catch (Exception e) {
            throw new BotException(STR."SauceNao数据获取异常：\{e.getMessage()}");
        }
    }
}

