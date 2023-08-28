package com.kuroneko.cqbot.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kuroneko.cqbot.entity.Bullet;
import com.kuroneko.cqbot.mapper.BulletMapper;
import com.kuroneko.cqbot.service.BulletService;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.ruiyun.jvppeteer.core.page.Page;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author admin
 * @description 针对表【bullet】的数据库操作Service实现
 * @createDate 2023-08-21 17:28:18
 */
@Service
public class BulletServiceImpl extends ServiceImpl<BulletMapper, Bullet>
        implements BulletService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAllBullet(Collection<Object> jsonValue) {
        List<Bullet> list = jsonValue.stream()
                .map(v -> (ArrayList<Object>) v)
                .filter(v -> !v.isEmpty())
                .map(v -> {
                    int isTracer = 0;
                    int isSubsonic = 0;
                    int isNoMarket = 0;
                    ArrayList<String> shu = CastUtils.cast(v.get(2));
                    for (String s : shu) {
                        switch (s.toLowerCase()) {
                            case "t" -> isTracer = 1;
                            case "s" -> isSubsonic = 1;
                            case "fm" -> isNoMarket = 1;
                        }
                    }
                    return Bullet.builder()
                            .caliber(v.get(0).toString().replace("\n", " "))
                            .name(CastUtils.cast(v.get(1)))
                            .tracer(isTracer)
                            .subsonic(isSubsonic)
                            .noMarket(isNoMarket)
                            .damage(CastUtils.cast(v.get(3)))
                            .penetrationPower(NumberUtil.parseInt((String) v.get(4), 0))
                            .armorDamage(NumberUtil.parseInt((String) v.get(5), 0))
                            .accuracy(NumberUtil.parseInt((String) v.get(6), 0))
                            .recoil(NumberUtil.parseInt((String) v.get(7), 0))
                            .fragChance(NumberUtil.parseInt((String) v.get(8), 0))
                            .bleedLt(NumberUtil.parseInt((String) v.get(9), 0))
                            .bleedHvy(NumberUtil.parseInt((String) v.get(10), 0))
                            .effectivenessLv1(NumberUtil.parseInt((String) v.get(11), 0))
                            .effectivenessLv2(NumberUtil.parseInt((String) v.get(12), 0))
                            .effectivenessLv3(NumberUtil.parseInt((String) v.get(13), 0))
                            .effectivenessLv4(NumberUtil.parseInt((String) v.get(14), 0))
                            .effectivenessLv5(NumberUtil.parseInt((String) v.get(15), 0))
                            .effectivenessLv6(NumberUtil.parseInt((String) v.get(16), 0))
                            .build();
                })
                .toList();
        this.remove(null);
        this.saveBatch(list);
    }

    @Override
    public int searchBulletSize(String name) {
        Long count = getBulletWrapper(name).count();
        int size;
        if (count == 0) {
            size = 0;
        } else if (count <= 10) {
            size = 550;
        } else if (count <= 30) {
            size = 1080;
        } else if (count <= 60) {
            size = 1600;
        } else {
            size = 2100;
        }
        return size;
    }

    @Override
    public String screenshotBullet(String name, String path) {
        int size = searchBulletSize(name);
        if (size == 0) {
            return "";
        }
        Page page = PuppeteerUtil.getNewPage("http://localhost:8081/Bullet/" + name, size, 900);
        return PuppeteerUtil.screenshot(page, path, ".flex_div");
    }

    @Override
    public List<Bullet> searchBulletList(String name) {
        return getBulletWrapper(name).list();
    }

    private LambdaQueryChainWrapper<Bullet> getBulletWrapper(String name) {
        return lambdaQuery().like(StrUtil.isNotBlank(name), Bullet::getName, name);
    }
}




