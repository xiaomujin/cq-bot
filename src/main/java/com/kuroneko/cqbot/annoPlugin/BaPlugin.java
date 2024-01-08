package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.BaVo;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class BaPlugin {
    private final RestTemplate restTemplate;
    private static final String ARONA_API_IMAGE = "https://arona.diyigemt.com/api/v2/image?name=";
    private static final String ARONA_IMAGE = "https://arona.cdn.diyigemt.com/image/s";
    private final ExpiringMap<String, Collection<String>> expiringMap = ExpiringMap.builder()
            //允许更新过期时间值,如果不设置variableExpiration，不允许后面更改过期时间,一旦执行更改过期时间操作会抛异常UnsupportedOperationException
            .variableExpiration()
//            1）ExpirationPolicy.ACCESSED ：每进行一次访问，过期时间就会重新计算；
//            2）ExpirationPolicy.CREATED：在过期时间内重新 put 值的话，过期时间重新计算；
            .expirationPolicy(ExpirationPolicy.CREATED)
            //设置每个key有效时间30m,如果key不设置过期时间，key永久有效
            .expiration(5L, TimeUnit.MINUTES)
            .build();

    @AnyMessageHandler(cmd = Regex.BA_TOTAL_BATTLE)
    public void ba(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            Collection<String> arrayList = expiringMap.get(Regex.BA_TOTAL_BATTLE);
            if (arrayList != null) {
                arrayList.forEach(s -> bot.sendMsg(event, s, false));
                return "";
            }
            Page page = PuppeteerUtil.getNewPage("http://localhost:8081/baRank", 880, 500);
            String imgPath = Constant.BASE_IMG_PATH + "ba/baRank.png";
            PuppeteerUtil.screenshot(page, imgPath, "#app");
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            String msg = MsgUtils.builder().img(media).build();
            expiringMap.put(Regex.BA_TOTAL_BATTLE, List.of(msg));
            bot.sendMsg(event, msg, false);
            return "";
        });
    }

    @AnyMessageHandler(cmd = Regex.BA_CALENDAR)
    public void baCalendar(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            Collection<String> arrayList = expiringMap.get(Regex.BA_CALENDAR);
            if (arrayList != null) {
                arrayList.forEach(s -> bot.sendMsg(event, s, false));
                return "";
            }
            Page page = PuppeteerUtil.getNewPage("http://localhost:8081/baCalendar", 1000, 400);
            String imgPath = Constant.BASE_IMG_PATH + "ba/baCalendar.png";
            PuppeteerUtil.screenshot(page, imgPath, "html");
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            String msg = MsgUtils.builder().img(media).build();
            expiringMap.put(Regex.BA_CALENDAR, List.of(msg));
            bot.sendMsg(event, msg, false);
            return "";
        });
    }


    @AnyMessageHandler(cmd = Regex.BA_IMAGE_BATTLE)
    public void image(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            String text = matcher.group("text").trim();
            BaVo baVo = restTemplate.getForObject(ARONA_API_IMAGE + text, BaVo.class);
            if (baVo == null) {
                throw new BotException("爱丽丝出错了");
            }
            List<BaVo.BaImage> baImages = baVo.getData().toJavaList(BaVo.BaImage.class);
            if (baImages == null || baImages.isEmpty()) {
                throw new BotException("爱丽丝什么都没有找到~");
            }
            if (baVo.getCode().equals(200)) {
                for (BaVo.BaImage baImage : baImages) {
                    MsgUtils msg = MsgUtils.builder();
                    if (baImage.getType().equalsIgnoreCase("file")) {
                        msg.img(ARONA_IMAGE + baImage.getContent());
                    } else if (baImage.getType().equalsIgnoreCase("plain")) {
                        msg.text(baImage.getContent());
                    }
                    bot.sendMsg(event, msg.build(), false);
                }
            } else if (baVo.getCode().equals(101)) {
                MsgUtils msg = MsgUtils.builder();
                msg.text("是想问什么呢：\n");
                for (BaVo.BaImage baImage : baImages) {
                    msg.text(baImage.getName() + "\n");
                }
                bot.sendMsg(event, msg.build(), false);
            } else {
                throw new BotException(baVo.getMessage());
            }
            return "";
        });
    }

}
