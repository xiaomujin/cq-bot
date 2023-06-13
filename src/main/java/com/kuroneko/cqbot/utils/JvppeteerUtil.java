package com.kuroneko.cqbot.utils;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class JvppeteerUtil {
    private static volatile Browser browser;

    private static final AtomicInteger renderNum = new AtomicInteger(0);

    private static final int restartNum = 100;
    private static final String savePath = "/opt";
    private static final String VERSION = Constant.VERSION;

    public static Browser getBrowser() {
        if (browser == null || !browser.isConnected()) {
            synchronized (JvppeteerUtil.class) {
                if (browser == null || !browser.isConnected()) {
                    init();
                }
            }
        }
        return browser;
    }

//    static {
//        init();
//    }

    private JvppeteerUtil() {
    }

    private static void init() {
        try {
//            BrowserFetcher.downloadIfNotExist(null);
            ArrayList<String> arrayList = new ArrayList<>();
            LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).build();
            arrayList.add("--disable-gpu");
            arrayList.add("--disable-dev-shm-usage");
            arrayList.add("--disable-setuid-sandbox");
            arrayList.add("--no-first-run");
            arrayList.add("--no-sandbox");
            arrayList.add("--no-zygote");
            arrayList.add("--single-process");
            FetcherOptions fetcherOptions = new FetcherOptions();
            //chrome 保存路径
            fetcherOptions.setPath(savePath);
            BrowserFetcher browserFetcher = new BrowserFetcher(savePath, fetcherOptions);
            //下载指定版本
            browserFetcher.download(VERSION);
            //获得指定版本的执行路径
            String executablePath = browserFetcher.revisionInfo(VERSION).getExecutablePath();
            options.setExecutablePath(executablePath);
            browser = Puppeteer.launch(options);
            log.info("Chrome 启动成功");
        } catch (Exception e) {
            log.error("Chrome 启动失败", e);
        }
    }


    /**
     * 截取整个页面
     *
     * @param url  链接
     * @param path 保存路径
     * @return base64
     */
    public static String screenshot(String url, String path) {
        return screenshot(url, path, "");
    }

    /**
     * 截取选择的元素
     *
     * @param url      链接
     * @param path     保存路径
     * @param selector 选择器
     * @param css      css属性 用于隐藏元素
     * @return base64
     */
    public static String screenshot(String url, String path, String selector, String css) {
        long start = System.currentTimeMillis();
        Page page = getBrowser().newPage();
        page.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
        String screenshot = "";
        try {
            page.goTo(url);
            //添加css
            if (StringUtil.isNotBlank(css)) {
                StyleTagOptions styleTagOptions = new StyleTagOptions(null, null, css);
                page.addStyleTag(styleTagOptions);
            }
            ScreenshotOptions screenshotOptions = new ScreenshotOptions();
            if (StringUtil.isBlank(selector)) {
                //设置截图范围
                screenshotOptions.setFullPage(true);
                //设置存放的路径
                screenshotOptions.setPath(path);
                screenshot = page.screenshot(screenshotOptions);
            } else {
                //设置截图范围
                ElementHandle elementHandle = page.waitForSelector(selector);
                screenshotOptions.setPath(path);
                screenshot = elementHandle.screenshot(screenshotOptions);
            }

            long cost = System.currentTimeMillis() - start;
            log.info("图片生成成功,耗时：{} ,url:{} ,path:{} ,selector:[{}] ,selector:[{}] ", cost, url, path, selector, css);
        } catch (Exception e) {
            log.error("图片生成失败", e);
        } finally {
            try {
                page.close();
            } catch (Exception e) {
                log.error("标签页关闭失败", e);
                log.info("正在重新启动 Chrome");
                restart();
            }
        }
        renderNum.incrementAndGet();
        timesRestart();
        return screenshot;
    }

    /**
     * 截取选择的元素
     *
     * @param url      链接
     * @param path     保存路径
     * @param selector 选择器
     * @return base64
     */
    public static String screenshot(String url, String path, String selector) {
        return screenshot(url, path, selector, "");
    }

    private static void timesRestart() {
        if (renderNum.get() >= restartNum) {
            log.info("到达重启次数:{}", restartNum);
            restart();
        }
    }

    public static void restart() {
        close();
        getBrowser();
    }

    public static void close() {
        log.info("关闭 Chrome");
        if (browser != null && browser.isConnected()) {
            browser.close();
        }
    }
}