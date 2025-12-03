package com.kuroneko.cqbot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kuroneko.cqbot.entity.Bullet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * @description 针对表【bullet】的数据库操作Service
 * @createDate 2023-08-21 17:28:18
 */
public interface BulletService extends IService<Bullet> {
    void updateAllBullet(ArrayList<ArrayList<Object>> list);

    List<Bullet> searchBulletList(String name);

    int searchBulletSize(String name);

    String screenshotBullet(String name, String path);


}
