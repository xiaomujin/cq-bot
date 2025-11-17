package com.kuroneko.cqbot.timer;

import com.kuroneko.cqbot.entity.WordCloud;
import com.kuroneko.cqbot.service.BotTaskService;
import com.kuroneko.cqbot.service.TarKovMarketService;
import com.kuroneko.cqbot.service.WordCloudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableAsync
@EnableScheduling
public class BotTask {

    private final BotTaskService botTaskService;
    private final TarKovMarketService tarKovMarketService;
    private final WordCloudService wordCloudService;

//    @Scheduled(cron = "0 0/10 * * * ? ")
//    public void refreshThreeDog() {
//        log.info("刷新三兄弟位置 开始");
//        botTaskService.refreshThreeDog();
//        log.info("刷新三兄弟位置 结束");
//    }

//    @Scheduled(cron = "0 5 0/1 * * ? ")
//    public void refreshDaily() {
//        log.info("刷新日报图片 开始");
//        botTaskService.refreshDaily();
//        log.info("刷新日报图片 结束");
//    }

//    @Scheduled(cron = "0 6 0/1 * * ? ")
//    public void refreshRiLi() {
//        log.info("刷新日历图片 开始");
//        botTaskService.refreshRiLi();
//        log.info("刷新日历图片 结束");
//    }

//    @Scheduled(cron = "0 3 0/1 * * ? ")
//    public void refreshAgeListVo() {
//        log.info("刷新age番剧 开始");
//        botTaskService.refreshAgeListVo();
//        log.info("刷新age番剧 结束");
//    }

    @Scheduled(cron = "0 0/13 * * * ? ")
    public void refreshTkfServerInfo() {
        log.info("刷新塔科夫服务器状态 开始");
//        tarKovMarketService.cacheTkfServerInfo();
        tarKovMarketService.cacheTkfServerStatusMsg();
        log.info("刷新塔科夫服务器状态 结束");
    }

    @Scheduled(cron = "0 1/8 * * * ? ")
    public void refreshBiliSubscribe() {
        log.info("刷新哔哩订阅 开始");
        botTaskService.refreshBiliSubscribe(false);
        log.info("刷新哔哩订阅 结束");
    }

    @Scheduled(cron = "0 0 4 * * ? ")
    public void cleanWordCloud() {
        log.info("清理聊天日志 开始");
        LocalDateTime localDateTime = LocalDateTime.now().minusMonths(2);
        wordCloudService.lambdaUpdate().lt(WordCloud::getTime, localDateTime).remove();
        log.info("清理聊天日志 结束");
    }


}
