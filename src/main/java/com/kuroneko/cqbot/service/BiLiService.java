package com.kuroneko.cqbot.service;

import cn.hutool.core.io.FileUtil;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.ruiyun.jvppeteer.api.core.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BiLiService {
    private final RestTemplate restTemplate;
    String imgPath = Constant.BASE_IMG_PATH + "bili/";

    public BiLiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        FileUtil.mkdir(imgPath);
    }

    public Optional<BiliDynamicVo.BiliDynamicCard> getFirstCard(String uid) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "buvid3=11CE4181-D4A7-D91E-92AD-E0213612BC2113883infoc");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<BiliDynamicVo> entity = restTemplate.exchange(Constant.BL_DYNAMIC_URL, HttpMethod.GET, httpEntity, BiliDynamicVo.class, uid);
        if (entity.getBody() == null || entity.getBody().getData() == null) {
            log.info("bili动态获取失败, uid: {}", uid);
            return Optional.empty();
        }
        List<BiliDynamicVo.BiliDynamicCard> items = entity.getBody().getData().getItems();
        return items.stream().min((o1, o2) -> o2.getModules().getModule_author().getPub_ts().compareTo(o1.getModules().getModule_author().getPub_ts()));
    }

    /**
     * 152065343 纱雾最可爱辣
     *
     * @param dynamicId 动态id
     * @return 文件路径
     */
    public String getNewScreenshot(String dynamicId, String uid) {
        try {
            String path = imgPath + dynamicId + ".png";
            Page page = PuppeteerUtil.getNewPage("https://www.bilibili.com/opus/" + dynamicId);
            String redirect = page.url();
            String selector = null;
            if (redirect.contains("bilibili.com/opus")) {
                selector = "#app > div.opus-detail > div.bili-opus-view";
            } else if (redirect.contains("t.bilibili.com")) {
                selector = "#app > div.content > div.card > div.bili-dyn-item";
            } else if (redirect.contains("bilibili.com/read")) {
                selector = "#app > div > div.article-container";
            }
            PuppeteerUtil.screenshot(page, path, selector, ".z-top-container { display: none } .international-header { display: none } .van-popper { display: none } .bili-mini-mask { display: none } .login-tip { display: none }");
            return path;
        } catch (Exception e) {
            log.error("动态截图异常", e);
        }
        return "";
    }

    public MsgUtils buildDynamicMsg(String path, BiliDynamicVo.BiliDynamicCard card) {
        OneBotMedia localMedia = BotUtil.getLocalMedia(path, false);
        return MsgUtils.builder()
                .img(localMedia)
                .text("up: " + card.getModules().getModule_author().getName() + "\n")
                .text("uid: " + card.getModules().getModule_author().getMid() + "\n")
                .text("https://www.bilibili.com/opus/" + card.getId_str());
    }

    public MsgUtils buildDynamicMsgLess(String path, String dynamicId) {
        OneBotMedia localMedia = BotUtil.getLocalMedia(path, false);
        return MsgUtils.builder()
                .img(localMedia)
                .text("https://www.bilibili.com/opus/" + dynamicId);
    }

}