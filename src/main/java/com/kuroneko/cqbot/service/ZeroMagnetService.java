package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.vo.ZeroMagnetVo;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ZeroMagnetService {

    public ZeroMagnetVo getItem(String tag, int index) {
        Document document;
        try {
            document = Jsoup.connect(Constant.ZERO_MAGNET_URL + tag).get();
        } catch (IOException e) {
            log.error("获取失败", e);
            return null;
        }
        Element element = document.select("table").select("tr").get(index);
        String url = element.selectFirst("a").attr("abs:href");
        String size = element.getElementsByClass("td-size").text();
        return ZeroMagnetVo.builder().url(url).size(size).build();
    }

    public ZeroMagnetVo getZeroMagnetVo(String tag, int index) {
        ZeroMagnetVo zeroMagnetVo = getItem(tag, index);
        Document document;
        try {
            document = Jsoup.connect(zeroMagnetVo.getUrl()).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String title = document.getElementsByClass("magnet-title").get(0).text();
        String magNet = document.getElementById("input-magnet").val();
        String mag = magNet.split("&")[0];
        zeroMagnetVo.setTitle(title);
        zeroMagnetVo.setMagnet(mag);
        return zeroMagnetVo;
    }
}
