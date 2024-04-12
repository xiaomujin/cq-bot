package com.kuroneko.cqbot.lagrange.markdown;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义按钮内容，最多可以发送5行按钮，每一行最多5个按钮。
 */
@Getter
@Setter
public class Keyboard {
    private final String type = "keyboard";
    private Data data = new Data();

    public static Keyboard Builder() {
        return new Keyboard();
    }

    /**
     * 添加按钮
     *
     * @param text 按钮文本
     * @param data 按钮数据
     * @return Keyboard
     */
    public Keyboard addButton(String text, String data) {
        return addButton(text, data, false);
    }

    /**
     * 添加按钮
     *
     * @param text  按钮文本
     * @param data  按钮数据
     * @param enter 自动发送
     * @return Keyboard
     */
    public Keyboard addButton(String text, String data, boolean enter) {
        return addButton(text, data, enter, List.of());
    }

    /**
     * 添加按钮
     *
     * @param text           按钮文本
     * @param data           按钮数据
     * @param enter          自动发送
     * @param specifyUserIds 指定可操作的QQ
     * @return Keyboard
     */
    public Keyboard addButton(String text, String data, boolean enter, List<Long> specifyUserIds) {
        Button button = new Button();
        button.getRenderData().setLabel(text);
        button.getRenderData().setVisitedLabel(text);
        button.getAction().setData(data);
        button.getAction().setEnter(enter);
        if (!specifyUserIds.isEmpty()) {
            Permission permission = button.getAction().getPermission();
            permission.setType(0);
            List<String> list = specifyUserIds.stream().map(String::valueOf).toList();
            permission.setSpecifyUserIds(list);
        }
        List<Row> rows = getData().getContent().getRows();
        rows.getLast().addButton(button);
        return this;
    }

    public Keyboard addRow() {
        if (getData().getContent().getRows().size() >= 5) {
            return this;
        }
        getData().getContent().getRows().add(new Row());
        return this;
    }

    @Getter
    @Setter
    public static class Data {
        private Content content = new Content();
    }

    @Getter
    @Setter
    public static class Content {
        private List<Row> rows = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Row {
        private List<Button> buttons = new ArrayList<>();

        public Row addButton(Button button) {
            if (getButtons().size() >= 5) {
                return this;
            }
            getButtons().add(button);
            return this;
        }
    }

    @Getter
    @Setter
    public static class Button {
        /**
         * 按钮ID：在一个keyboard消息内设置唯一
         */
        @JSONField(name = "id")
        private String id;
        @JSONField(name = "render_data")
        private RenderData renderData = new RenderData();
        @JSONField(name = "action")
        private Action action = new Action();
    }

    @Getter
    @Setter
    public static class RenderData {
        /**
         * 按钮上的文字
         */
        @JSONField(name = "label")
        private String label = "";
        /**
         * 点击后按钮的上文字
         */
        @JSONField(name = "visited_label")
        private String visitedLabel = "";
        /**
         * 按钮样式：0 灰色线框，1 蓝色线框
         */
        @JSONField(name = "style")
        private int style = 1;
    }

    @Getter
    @Setter
    public static class Action {
        /**
         * 设置 0 跳转按钮：http 或 小程序 客户端识别 scheme，<br/>
         * 设置 1 回调按钮：回调后台接口, data 传给后台，<br/>
         * 设置 2 指令按钮：自动在输入框插入 @bot data <br/>
         */
        @JSONField(name = "type")
        private int type = 2;
        /**
         * 点击后按钮的上文字
         */
        @JSONField(name = "permission")
        private Permission permission = new Permission();
        /**
         * 操作相关的数据
         */
        @JSONField(name = "data")
        private String data = "";
        /**
         * 指令按钮可用，指令是否带引用回复本消息，默认 false。支持版本 8983
         */
        @JSONField(name = "reply")
        private Boolean reply;
        /**
         * 指令按钮可用，点击按钮后直接自动发送 data，默认 false。支持版本 8983
         */
        @JSONField(name = "enter")
        private Boolean enter;
        /**
         * 本字段仅在指令按钮下有效，设置后后会忽略 action.enter 配置。
         * 设置为 1 时 ，点击按钮自动唤起启手Q选图器，其他值暂无效果。
         * 仅支持手机端版本 8983+ 的单聊场景，桌面端不支持）
         */
        @JSONField(name = "anchor")
        private Integer anchor;
        /**
         * 客户端不支持本action的时候，弹出的toast文案
         */
        @JSONField(name = "anchor")
        private String unsupport_tips = "暂不支持当前版本";
    }

    @Getter
    @Setter
    public static class Permission {
        /**
         * 0 指定用户可操作，<br/>
         * 1 仅管理者可操作，<br/>
         * 2 所有人可操作，<br/>
         * 3 指定身份组可操作（仅频道可用）<br/>
         */
        @JSONField(name = "type")
        private int type = 2;
        /**
         * 有权限的用户 id 的列表
         */
        @JSONField(name = "specify_user_ids")
        private List<String> specifyUserIds;
        /**
         * 有权限的身份组 id 的列表（仅频道可用）
         */
        @JSONField(name = "specify_role_ids")
        private List<String> specifyRoleIds;
    }


}
