package com.kuroneko.cqbot.controller;

import com.kuroneko.cqbot.entity.Bullet;
import com.kuroneko.cqbot.service.BulletService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
