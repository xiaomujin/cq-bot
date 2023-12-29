package com.kuroneko.cqbot.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
public class BaCtrl {
    @RequestMapping(value = {"/ba"})
    public String Bullet(Model model) {
        model.addAttribute("serverName", "官服");
        return "ba/BaRank";
    }

}
