package com.kuroneko.cqbot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import lombok.*;

/**
 * @TableName bullet
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "bullet")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bullet extends BaseEntity {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 口径
     */
    private String caliber;

    /**
     * 名称
     */
    private String name;

    /**
     * 曳光弹
     */
    private Integer tracer;

    /**
     * 亚音速弹
     */
    private Integer subsonic;

    /**
     * 跳蚤禁售
     */
    private Integer noMarket;

    /**
     * 伤害
     */
    private String damage;

    /**
     * 穿透力
     */
    private Integer penetrationPower;

    /**
     * 护甲伤害
     */
    private Integer armorDamage;

    /**
     * 精度
     */
    private Integer accuracy;

    /**
     * 后坐力
     */
    private Integer recoil;

    /**
     * 碎弹率
     */
    private Integer fragChance;

    /**
     * 小出血概率
     */
    private Integer bleedLt;

    /**
     * 大出血概率
     */
    private Integer bleedHvy;

    /**
     * 子弹速度
     */
    private Integer speed;

    /**
     * 1级甲有效穿透等级
     */
    private Integer effectivenessLv1;

    /**
     * 2级甲有效穿透等级
     */
    private Integer effectivenessLv2;

    /**
     * 3级甲有效穿透等级
     */
    private Integer effectivenessLv3;

    /**
     * 4级甲有效穿透等级
     */
    private Integer effectivenessLv4;

    /**
     * 5级甲有效穿透等级
     */
    private Integer effectivenessLv5;

    /**
     * 6级甲有效穿透等级
     */
    private Integer effectivenessLv6;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}