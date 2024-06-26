package com.kuroneko.cqbot.timer;

import com.kuroneko.cqbot.service.BotTaskService;
import com.kuroneko.cqbot.service.TarKovMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@EnableAsync
@EnableScheduling
public class BotTask {

    private final BotTaskService botTaskService;
    private final TarKovMarketService tarKovMarketService;

    @Scheduled(cron = "0 0/10 * * * ? ")
    public void refreshThreeDog() {
        log.info("刷新三兄弟位置 开始");
        botTaskService.refreshThreeDog();
        log.info("刷新三兄弟位置 结束");
    }

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


}
