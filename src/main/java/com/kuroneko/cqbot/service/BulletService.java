package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.entity.Bullet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

/**
* @author admin
* @description 针对表【bullet】的数据库操作Service
* @createDate 2023-08-21 17:28:18
*/
public interface BulletService extends IService<Bullet> {
    void updateAllBullet(Collection<Object> list);
}
