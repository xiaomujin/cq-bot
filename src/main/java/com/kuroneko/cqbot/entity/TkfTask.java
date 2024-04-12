package com.kuroneko.cqbot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import lombok.*;

/**
 * 
 * @TableName tkf_task
 */
@TableName(value ="tkf_task")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TkfTask extends BaseEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String sName;

    /**
     * 
     */
    private Integer minLevel;

    /**
     * 
     */
    private Boolean isKappa;

    /**
     * 
     */
    private Boolean isLightkeeper;

    /**
     * 
     */
    private String traderName;

    /**
     * 
     */
    private String traderImg;

    /**
     * 
     */
    private String taskImg;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}