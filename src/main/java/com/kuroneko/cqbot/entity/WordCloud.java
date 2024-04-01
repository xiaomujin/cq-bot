package com.kuroneko.cqbot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;

/**
 * 群友聊天记录
 *
 * @TableName word_cloud
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "word_cloud")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordCloud extends BaseEntity implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者
     */
    private Long senderId;

    /**
     * 发送的群
     */
    private Long groupId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息发送时间
     */
    private LocalDateTime time;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}