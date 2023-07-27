package com.kuroneko.cqbot.utils;

import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.protocol.network.CookieParam;
import com.ruiyun.jvppeteer.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PuppeteerUtil {
    private static volatile Browser browser;

    private static final AtomicInteger renderNum = new AtomicInteger(0);

    private static final int restartNum = 100;
    private static final String savePath = "/opt";
    private static final String VERSION = Constant.VERSION;
    private static final List<CookieParam> COOKIES;

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

    static {
        List<CookieParam> cookies = new ArrayList<>();
        CookieParam cookieParam = new CookieParam();
        cookieParam.setUrl("https://t.bilibili.com");
        cookieParam.setName("buvid3");
        cookieParam.setValue("4FA1248C-BF0D-A763-E831-8A93A5F8BFD758493infoc");
        cookieParam.setDomain(".bilibili.com");
        cookieParam.setPath("/");
        cookieParam.setExpires(1990454302000L);
        cookies.add(cookieParam);
        COOKIES = cookies;
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
                //设置截图范围
                ElementHandle elementHandle = page.waitForSelector(selector);
                screenshotOptions.setPath(path);
                screenshot = elementHandle.screenshot(screenshotOptions);
            }
            long cost = System.currentTimeMillis() - start;
            log.info("图片生成成功,耗时：{} ,url:{} ,path:{} ,selector:[{}] ,selector:[{}] ", cost, page.mainFrame().getUrl(), path, selector, css);
        } catch (Exception e) {
            log.error("图片生成失败", e);
            throw new RuntimeException("图片生成失败");
        } finally {
            safeClosePage(page);
        }
        renderNum.incrementAndGet();
        timesRestart();
        return screenshot;
    }


    public static Page getNewPage(String url) {
        return getNewPage(url, 1920, 1080);
    }

    public static Page getNewPage(String url, Integer width, Integer height) {
        return getNewPage(url, "load", 30000, width, height);
    }

    public static Page getNewPage(String url, String waitUntil, Integer timeout) {
        return getNewPage(url, waitUntil, timeout, 1920, 1080);
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
            page.setCookie(COOKIES);
            page.goTo(url, pageNavigateOptions);
        } catch (Exception e) {
            log.error("页面打开失败", e);
            safeClosePage(page);
            throw new RuntimeException("页面打开失败");
        }
        long cost = System.currentTimeMillis() - start;
        log.info("打开页面成功,耗时：{} ,url:{}", cost, page.mainFrame().getUrl());
        return page;
    }

    public static void safeClosePage(Page page) {
        try {
            page.close();
        } catch (Exception e) {
            log.error("标签页关闭失败", e);
            log.info("正在重新启动 Chrome");
            restart();
            throw new RuntimeException("标签页关闭失败");
        }
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
