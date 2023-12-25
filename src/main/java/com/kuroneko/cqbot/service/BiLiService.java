package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class BiLiService {
    private final RestTemplate restTemplate;
    String imgPath = Constant.BASE_IMG_PATH + "bili/";

    public BiLiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

        File file = new File(imgPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 152065343 纱雾最可爱辣
     *{
     *   "code": 0,
     *   "msg": "",
     *   "message": "",
     *   "data": {
     *     "has_more": 0,
     *     "next_offset": 0,
     *     "_gt_": 0
     *   }
     * }
     * @param uid B站Uid
     * @return 最新动态id
     */
    public String getNew(String uid) {
        ResponseEntity<BiliDynamicVo> entity = restTemplate.getForEntity(Constant.BL_DYNAMIC_URL, BiliDynamicVo.class, uid);
        return Objects.requireNonNull(entity.getBody()).getData().getCards().get(0).getDesc().getDynamic_id_str();
    }

    public Optional<BiliDynamicVo.BiliDynamicCard> getFirstCard(String uid) {
        ResponseEntity<BiliDynamicVo> entity = restTemplate.getForEntity(Constant.BL_DYNAMIC_URL, BiliDynamicVo.class, uid);
        List<BiliDynamicVo.BiliDynamicCard> cards = Objects.requireNonNull(entity.getBody()).getData().getCards();
        return Optional.ofNullable(cards).map(c -> c.get(0));
    }

    /**
     * 152065343 纱雾最可爱辣
     *
     * @param dynamicId 动态id
     * @return 文件路径
     */
    public String getNewScreenshot(String dynamicId, String uid) {
        Page page = PuppeteerUtil.getBrowser().newPage();
        try {
            String path = imgPath + dynamicId + ".png";
            PuppeteerUtil.screenshot("https://www.bilibili.com/opus/" + dynamicId, path, "#app > div.opus-detail > div.bili-opus-view", ".z-top-container { display: none }");
            return path;
        } catch (Exception e) {
            log.error("动态截图异常", e);
        } finally {
            try {
                page.close();
            } catch (InterruptedException e) {
                log.error("page关闭失败", e);
                PuppeteerUtil.close();
            }
        }
        return "";
    }

    public MsgUtils buildDynamicMsg(String path, String dynamicId) {
        OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + path).cache(false);
        return MsgUtils.builder().img(media).text("https://www.bilibili.com/opus/" + dynamicId);
    }

}
