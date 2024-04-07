package com.kuroneko.cqbot.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class ImageController {

    @RequestMapping(value = "/getImage", produces = {MediaType.IMAGE_PNG_VALUE})
    public BufferedImage getImage(@RequestParam(defaultValue = "") String path) throws IOException {
        File file = new File(path);
        return ImageIO.read(file);
    }

    @RequestMapping(value = "/getJpgImage", produces = {MediaType.IMAGE_JPEG_VALUE})
    public BufferedImage getJpgImage(@RequestParam(defaultValue = "") String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/static/img/404.png");
            assert resourceAsStream != null;
            return ImageIO.read(resourceAsStream);
        }
        return ImageIO.read(file);
    }

}
