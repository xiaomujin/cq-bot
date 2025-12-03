package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.exception.BotException;
import com.ruiyun.jvppeteer.api.core.Browser;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.cdp.core.Puppeteer;
import com.ruiyun.jvppeteer.cdp.entities.*;
import com.ruiyun.jvppeteer.common.Product;
import com.ruiyun.jvppeteer.common.PuppeteerLifeCycle;
import com.ruiyun.jvppeteer.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PuppeteerUtil {
    private static volatile Browser browser;
    private static final CookieParam[] COOKIES;
    private static final AtomicInteger renderNum = new AtomicInteger(0);
    private static final int restartNum = 500;

    public static Browser getBrowser() {
        if (browser == null || !browser.connected()) {
            synchronized (PuppeteerUtil.class) {
                if (browser == null || !browser.connected()) {
                    init();
                }
            }
        }
        return browser;
    }

    static {
        List<CookieParam> cookies = new ArrayList<>();
        CookieParam cookieParam = new CookieParam();
        cookieParam.setUrl("https://t.bilibili.com");
        cookieParam.setName("buvid3");
        cookieParam.setValue("D9440E6F-A342-4CD6-461C-A66BCB5CA5A555172infoc");
        cookieParam.setDomain(".bilibili.com");
        cookieParam.setPath("/");
        cookieParam.setExpires(1990454302000L);
        cookies.add(cookieParam);
        CookieParam cookieParam2 = new CookieParam();
        cookieParam2.setUrl("https://www.bilibili.com");
        cookieParam2.setName("buvid3");
        cookieParam2.setValue("D9440E6F-A342-4CD6-461C-A66BCB5CA5A555172infoc");
        cookieParam2.setDomain(".bilibili.com");
        cookieParam2.setPath("/");
        cookieParam2.setExpires(1990454302000L);
        cookies.add(cookieParam2);
        COOKIES = cookies.toArray(CookieParam[]::new);
    }

    private PuppeteerUtil() {
    }

    private static void init() {
        try {
            Puppeteer.downloadBrowser();
            long start = System.currentTimeMillis();
            List<String> list = List.of(
                    "--disable-gpu",
                    "--disable-dev-shm-usage",
                    "--disable-setuid-sandbox",
                    "--no-first-run",
                    "--no-sandbox",
//                    "--single-process",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-extensions",
                    "--no-zygote",
                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
            );
            LaunchOptions options = LaunchOptions.builder().product(Product.Chrome).args(list).headless(true).build();
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
    public static String screenshot(String url, String path, String selector, String css, String function) {
        Page page = getNewPage(url);
        return screenshot(page, path, selector, css, function);
    }

    /**
     * 截取选择的元素
     *
     * @param page     页面
     * @param path     保存路径
     * @param selector 选择器
     * @param css      css属性 用于隐藏元素
     * @param function 等待提供的函数 pageFunction 在页面上下文中计算时返回真值。
     * @return base64
     */
    public static String screenshot(Page page, String path, String selector, String css, String function) {
        long start = System.currentTimeMillis();
        String screenshot = "";
        try {
            //添加 css
            if (StringUtil.isNotBlank(css)) {
                FrameAddStyleTagOptions frameAddStyleTagOptions = new FrameAddStyleTagOptions(null, null, css);
                page.addStyleTag(frameAddStyleTagOptions);
            }
            //添加 方法
            if (StringUtil.isNotBlank(function)) {
                page.waitForFunction(function);
            }
            //图片生成位置
            if (StringUtil.isNotBlank(path)) {
                File parentFile = new File(path).getParentFile();
                parentFile.mkdirs();
            }
            if (StringUtil.isBlank(selector)) {
                ScreenshotOptions screenshotOptions = new ScreenshotOptions();
                screenshotOptions.setPath(path);
                screenshotOptions.setFullPage(true);
                screenshot = page.screenshot(screenshotOptions);
            } else {
                WaitForSelectorOptions waitForSelectorOptions = new WaitForSelectorOptions();
                waitForSelectorOptions.setTimeout(15000);
                ElementHandle elementHandle = page.waitForSelector(selector, waitForSelectorOptions);
                screenshot = elementHandle.screenshot(path);
            }
            long cost = System.currentTimeMillis() - start;
            log.info("图片生成成功,耗时：{} ,url:{} ,path:{} ,selector:[{}] ,css:[{}] ", cost, page.url(), path, selector, css);
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
        return getNewPage(url, PuppeteerLifeCycle.load, 15000, width, height);
    }

    /**
     * @param url       链接
     * @param waitUntil load - domcontentloaded - networkidle0 - networkidle2
     * @param width     宽度
     * @param height    高度
     * @param timeout   超时时间
     * @return 页面
     */
    public static Page getNewPage(String url, PuppeteerLifeCycle waitUntil, Integer timeout, Integer width, Integer height) {
        long start = System.currentTimeMillis();
        Page page = getBrowser().newPage();
        try {
            Viewport viewport = new Viewport();
            viewport.setWidth(width);
            viewport.setHeight(height);
            page.setViewport(viewport);
            GoToOptions pageNavigateOptions = new GoToOptions();
            pageNavigateOptions.setTimeout(timeout);
            pageNavigateOptions.setWaitUntil(List.of(waitUntil));
            page.setCookie(COOKIES);
            page.goTo(url, pageNavigateOptions);
        } catch (Exception e) {
            log.error("页面打开失败", e);
            safeClosePage(page);
        }
        long cost = System.currentTimeMillis() - start;
        log.info("打开页面成功,耗时：{} ,url:{}", cost, page.url());
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
        if (browser != null && browser.connected()) {
            BotUtil.closeQuietly(browser);
        }
    }

}
