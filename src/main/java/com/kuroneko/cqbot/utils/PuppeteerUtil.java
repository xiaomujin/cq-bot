package com.kuroneko.cqbot.utils;

import cn.hutool.core.io.FileUtil;
import com.kuroneko.cqbot.exception.BotException;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PuppeteerUtil {
    private static volatile Browser browser;

    private static final AtomicInteger renderNum = new AtomicInteger(0);

    private static final int restartNum = 500;
    private static final String savePath = "/opt";
    private static final String VERSION = "1270719";

    public static Browser getBrowser() {
        if (browser == null || !browser.isConnected()) {
            synchronized (PuppeteerUtil.class) {
                if (browser == null || !browser.isConnected()) {
                    init();
                }
            }
        }
        return browser;
    }


    private PuppeteerUtil() {
    }

    private static void init() {
        try {
            BrowserFetcher.downloadIfNotExist(VERSION);
            long start = System.currentTimeMillis();
            List<String> list = List.of(
                    "--disable-gpu",
                    "--disable-dev-shm-usage",
                    "--disable-setuid-sandbox",
                    "--no-first-run",
                    "--no-sandbox",
                    "--single-process",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-extensions",
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
                    "--no-zygote"
            );
            LaunchOptions options = new LaunchOptionsBuilder().withArgs(list).withHeadless(true).build();
//            FetcherOptions fetcherOptions = new FetcherOptions();
            //chrome 保存路径
//            fetcherOptions.setPath(savePath);
//            BrowserFetcher browserFetcher = new BrowserFetcher(savePath, fetcherOptions);
//            browserFetcher.download(VERSION);
            //获得指定版本的执行路径
//            String executablePath = browserFetcher.revisionInfo(VERSION).getExecutablePath();
//            options.setExecutablePath(executablePath);
            browser = Puppeteer.launch(options);
            log.info("Chrome 启动成功 用时:{}", System.currentTimeMillis() - start);
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
     * 截取整个页面
     *
     * @param page 页面
     * @param path 保存路径
     * @return base64
     */
    public static String screenshot(Page page, String path) {
        return screenshot(page, path, "");
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

    /**
     * 截取选择的元素
     *
     * @param page     页面
     * @param path     保存路径
     * @param selector 选择器
     * @return base64
     */
    public static String screenshot(Page page, String path, String selector) {
        return screenshot(page, path, selector, "");
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
        return screenshot(url, path, selector, css, "");
    }

    /**
     * 截取选择的元素
     *
     * @param page     页面
     * @param path     保存路径
     * @param selector 选择器
     * @param css      css属性 用于隐藏元素
     * @return base64
     */
    public static String screenshot(Page page, String path, String selector, String css) {
        return screenshot(page, path, selector, css, "");
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
    public static String screenshot(String url, String path, String selector, String css, String selectorOrFunctionOrTimeout) {
        Page page = getNewPage(url);
        return screenshot(page, path, selector, css, selectorOrFunctionOrTimeout);
    }

    /**
     * 截取选择的元素
     *
     * @param page                        页面
     * @param path                        保存路径
     * @param selector                    选择器
     * @param css                         css属性 用于隐藏元素
     * @param selectorOrFunctionOrTimeout 选择器, 方法 或者 超时时间
     *                                    <p>如果 selectorOrFunctionOrTimeout 是 string, 那么认为是 css 选择器或者一个xpath, 根据是不是'//'开头, 这时候此方法是 page.waitForSelector 或 page.waitForXPath的简写</p>
     *                                    <p>如果 selectorOrFunctionOrTimeout 是 function, 那么认为是一个predicate，这时候此方法是page.waitForFunction()的简写</p>
     *                                    <p>如果 selectorOrFunctionOrTimeout 是 number, 那么认为是超时时间，单位是毫秒，返回的是Promise对象,在指定时间后resolve</p>
     *                                    <p>否则会报错
     * @return base64
     */
    public static String screenshot(Page page, String path, String selector, String css, String selectorOrFunctionOrTimeout) {
        long start = System.currentTimeMillis();
        String screenshot = "";
        try {
            FileUtil.mkdir(new File(path).getParentFile());
            //添加css
            if (StringUtil.isNotBlank(css)) {
                StyleTagOptions styleTagOptions = new StyleTagOptions(null, null, css);
                page.addStyleTag(styleTagOptions);
            }
            //添加 选择器, 方法 或者 超时时间
            if (StringUtil.isNotBlank(selectorOrFunctionOrTimeout)) {
                page.waitFor(selectorOrFunctionOrTimeout);
            }
            ScreenshotOptions screenshotOptions = new ScreenshotOptions();
            if (StringUtil.isBlank(selector)) {
                //设置截图范围
                screenshotOptions.setFullPage(true);
                //设置存放的路径
                screenshotOptions.setPath(path);
                screenshot = page.screenshot(screenshotOptions);
            } else {
                // 超时处理
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        return;
                    }
                    safeClosePage(page);
                });
                thread.start();
                //设置截图范围
                ElementHandle elementHandle = page.waitForSelector(selector);
                thread.interrupt();
                screenshotOptions.setPath(path);
                screenshot = elementHandle.screenshot(screenshotOptions);
            }
            long cost = System.currentTimeMillis() - start;
            log.info("图片生成成功,耗时：{} ,url:{} ,path:{} ,selector:[{}] ,css:[{}] ", cost, page.mainFrame().getUrl(), path, selector, css);
        } catch (Exception e) {
            log.error("图片生成失败", e);
            throw new BotException("图片生成失败");
        } finally {
            safeClosePage(page);
        }
        timesRestart();
        return screenshot;
    }


    public static Page getNewPage(String url) {
        return getNewPage(url, 1920, 1080);
    }

    public static Page getNewPage(String url, Integer width, Integer height) {
        return getNewPage(url, "load", 15000, width, height);
    }

    /**
     * @param url       链接
     * @param waitUntil load - domcontentloaded - networkidle0 - networkidle2
     * @param width     宽度
     * @param height    高度
     * @param timeout   超时时间
     * @return 页面
     */
    public static Page getNewPage(String url, String waitUntil, Integer timeout, Integer width, Integer height) {
        long start = System.currentTimeMillis();
        Page page = getBrowser().newPage();
        Viewport viewport = new Viewport();
        viewport.setWidth(width);
        viewport.setHeight(height);
        page.setViewport(viewport);
        PageNavigateOptions pageNavigateOptions = new PageNavigateOptions();
        pageNavigateOptions.setTimeout(timeout);
        pageNavigateOptions.setWaitUntil(List.of(waitUntil));
        try {
            page.goTo(url, pageNavigateOptions);
        } catch (Exception e) {
            log.error("页面打开失败", e);
            safeClosePage(page);
        }
        long cost = System.currentTimeMillis() - start;
        log.info("打开页面成功,耗时：{} ,url:{}", cost, page.mainFrame().getUrl());
        return page;
    }

    public static void safeClosePage(Page page) {
        try {
            if (!page.isClosed()) {
                page.close();
            }
        } catch (Exception e) {
            log.error("标签页关闭失败", e);
            log.info("正在重新启动 Chrome");
            restart();
        }
    }

    private static void timesRestart() {
        if (restartNum == 0) {
            return;
        }
        if (renderNum.incrementAndGet() % restartNum == 0) {
            log.info("到达重启次数:{}", renderNum.get());
            if (browser.pages().size() == 1) {
                restart();
            }
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
