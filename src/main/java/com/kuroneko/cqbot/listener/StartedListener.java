package com.kuroneko.cqbot.listener;

import com.kuroneko.cqbot.service.BotTaskService;
import com.kuroneko.cqbot.service.TarKovMarketService;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StartedListener implements ApplicationListener<ApplicationStartedEvent> {
    private final BotTaskService botTaskService;
    private final TarKovMarketService tarKovMarketService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("系统启动成功");
        PuppeteerUtil.getBrowser();
//        botTaskService.refreshDaily();
//        botTaskService.refreshRiLi();
//        botTaskService.refreshAgeListVo();
        botTaskService.refreshTKFDT();
        tarKovMarketService.cacheTkfServerInfo();
        botTaskService.refreshThreeDog();
        botTaskService.refreshBiliSubscribe(true);
//        banGuMiMoeService.cacheMoeList();
    }
}
