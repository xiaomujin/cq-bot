package com.kuroneko.cqbot.controller;

import com.alibaba.fastjson2.JSON;
import com.kuroneko.cqbot.entity.Bullet;
import com.kuroneko.cqbot.service.BulletService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
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

    @RequestMapping(value = {"/Life/{name}"})
    public String Life(Model model, @PathVariable(required = false) String name) {
        ProcessBuilder pb = new ProcessBuilder("node", "test");
        File file = new File("E:\\project\\WebstormProjects\\lifeRestart\\");
//        File file = new File("/mnt/qqbot/life_restart");
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
        model.addAttribute("dataList", JSON.parseArray(dataStr));
        return "Life";
    }
}
