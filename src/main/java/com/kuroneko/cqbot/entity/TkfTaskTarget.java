package com.kuroneko.cqbot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import lombok.*;

/**
 * 
 * @TableName tkf_task_target
 */
@TableName(value ="tkf_task_target")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TkfTaskTarget extends BaseEntity {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long parentId;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private Boolean isOptional;

    /**
     * 
     */
    private Integer count;

    /**
     * 
     */
    private Boolean isRaid;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}