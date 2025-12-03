package com.kuroneko.cqbot.controller;

import com.kuroneko.cqbot.utils.JsonUtil;
import tools.jackson.databind.JsonNode;
import com.kuroneko.cqbot.entity.Bullet;
import com.kuroneko.cqbot.service.BulletService;
import com.kuroneko.cqbot.utils.CacheUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Controller
@AllArgsConstructor
public class BulletCtrl {
    private final BulletService bulletService;

    @RequestMapping(value = {"/Bullet/{name}", "/Bullet/"})
    public String Bullet(Model model, @PathVariable(required = false) String name) {
        List<Bullet> bullets = bulletService.searchBulletList(name);
        model.addAttribute("bullets", bullets);
        return "BulletSearch";
    }

    @RequestMapping(value = {"/Markdown/{id}"})
    public String Markdown(Model model, @PathVariable(required = false) String id) {
        Collection<String> collection = CacheUtil.get(id);
        if (collection != null) {
            Object o = collection.toArray()[0];
            model.addAttribute("message", o);
        } else {
            model.addAttribute("message", """
                    没有找到  \s""");
        }
        return "md/Markdown";
    }

    @RequestMapping(value = {"/Life/{name}"})
    public String Life(Model model, @PathVariable(required = false) String name) {
        ProcessBuilder pb = new ProcessBuilder("node", "test");
//        File file = new File("E:\\project\\WebstormProjects\\lifeRestart\\");
        File file = new File("/mnt/qqbot/life_restart");
        pb.directory(file);
        String dataStr = "[]";
        try {
            Process start = pb.start();
            byte[] bytes = start.getInputStream().readAllBytes();
            dataStr = new String(bytes);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        model.addAttribute("name", name);
        JsonNode dataList = JsonUtil.toNode(dataStr);
        model.addAttribute("dataList", dataList);
        return "Life";
    }
}