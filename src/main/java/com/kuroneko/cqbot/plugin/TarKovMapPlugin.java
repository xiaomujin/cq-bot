package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.service.BulletService;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.ThreeDog;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TarKovMapPlugin extends BotPlugin {
    private static final String CMD = CmdConst.MAP;
    private final BulletService bulletService;

    private static final Map<String, String> keywordToImageMap = new HashMap<>();

    static {
        keywordToImageMap.put("储备站", "Reserve.jpg");
        keywordToImageMap.put("灯塔", "Lighthouse.jpg");
        keywordToImageMap.put("工厂", "Factory.jpg");
        keywordToImageMap.put("海岸线", "Shoreline.jpg");
        keywordToImageMap.put("海关", "Customs.jpg");
        keywordToImageMap.put("街区", "StreetsOfTarKov.jpg");
        keywordToImageMap.put("立交桥", "Interchange.jpg");
        keywordToImageMap.put("森林", "Woods.jpg");
        keywordToImageMap.put("实验室", "TheLab.jpg");
        keywordToImageMap.put("疗养院", "ShorelineHose.jpg");
        keywordToImageMap.put("中心区", "Center.jpg");
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().contains(CMD)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD);
            String imgPath = STR."\{Constant.BASE_IMG_PATH}tarkov_map/";

            String rawMessage = event.getRawMessage();
            String imgName = null;
            for (Map.Entry<String, String> entry : keywordToImageMap.entrySet()) {
                if (rawMessage.contains(entry.getKey())) {
                    imgName = entry.getValue();
                    break;
                }
            }
            if (imgName == null) {
                return MESSAGE_BLOCK;
            }
            imgPath += imgName;
            OneBotMedia media = OneBotMedia.builder().file(STR."http://localhost:8081/getJpgImage?path=\{imgPath}").cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().equals(CmdConst.ZI_DAN)) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/" + "BulletData.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().equals(CmdConst.UPDATE_ZI_DAN)) {
            try {
                MsgUtils msg = MsgUtils.builder().text("开始更新子弹数据，大约需要60秒。");
                bot.sendMsg(event, msg.build(), false);

                Page page = PuppeteerUtil.getNewPage("https://escapefromtarkov.fandom.com/wiki/Ballistics", "domcontentloaded", 120000, 1650, 12000);
                ElementHandle b = page.$("div._2O--J403t2VqCuF8XJAZLK");
                if (b != null) {
                    try {
                        b.click();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }

                String fun = """
                        ()=>{
                            let list = document.querySelectorAll(".sortable.stickyheader tr");
                            let data_list = [];
                            let caliber = "";
                            for (let i = 0; i < list.length; i++) {
                                let line_data = list[i];
                                if(!line_data) continue;
                                let td_list = line_data.querySelectorAll("td");
                                if(!td_list || td_list.length == 0) continue;
                                let td_data_list = [];
                                for (let j = 0; j < td_list.length; j++) {
                                    let td_line_data = td_list[j];
                                    if(j == 0 && td_list.length == 17){
                                        caliber = td_line_data.innerText;
                                    } else if(j == 0 && td_list.length == 16){
                                        td_data_list.push(caliber);
                                    }
                                    if (j === (td_list.length - 16)) {
                                        td_data_list.push(td_line_data.querySelector("a").innerText);
                                        let b = [];
                                        let b_list = td_line_data.querySelectorAll("b");
                                        if (b_list) {
                                            for (let k = 0; k < b_list.length; k++) {
                                                b.push(b_list[k].innerText);
                                            }
                                            td_data_list.push(b);
                                        }
                                    } else {
                                        td_data_list.push(td_line_data.innerText);
                                    }
                                }
                                data_list.push(td_data_list);
                            }
                            return data_list;
                        }
                        """;
                JSHandle jsHandle = page.waitFor(fun);
                ArrayList<Object> jsonValue = CastUtils.cast(jsHandle.jsonValue());
                new Thread(() -> bulletService.updateAllBullet(jsonValue)).start();
                String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/" + "BulletData.png";
                PuppeteerUtil.screenshot(page, imgPath, "table.wikitable.sortable.stickyheader", "._1MLS_xjiUjam_u2qmURY4i { display: none }");
            } catch (Exception e) {
                log.error("子弹数据更新失败", e);
                MsgUtils msg = MsgUtils.builder().text("子弹数据更新失败" + e.getMessage());
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            }
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/" + "BulletData.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media).text("子弹数据更新成功");
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().contains("在哪")) {
            if (event.getRawMessage().contains("三兄弟") || event.getRawMessage().contains("三狗")) {
                ThreeDog threeDog = (ThreeDog) Constant.CONFIG_CACHE.get(RedisKey.THREE_DOG);
                if (threeDog != null) {
                    MsgUtils msg = MsgUtils.builder().text(threeDog.getLocationCN() + Constant.XN).text(threeDog.getLastReported());
                    bot.sendMsg(event, msg.build(), false);
                }
            }
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("任务流程图")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/TaskProcess.jpg";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getJpgImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("任务物品图")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/TaskItem.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("信誉栏位图")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/reputation.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("boss刷新率")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/bossRefreshRate.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("boss丢包时间")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/bossLossWrap.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("3x4道具")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/3x4.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("耳机强度")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/headset.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }


}
