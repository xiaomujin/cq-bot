package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.exception.BotException;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.browser.RevisionInfo;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.*;
import com.ruiyun.jvppeteer.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
//            BrowserFetcher.downloadIfNotExist(null);
            long start = System.currentTimeMillis();
            List<String> list = List.of(
                    "--disable-gpu",
//                    "--disable-dev-shm-usage",
//                    "--disable-setuid-sandbox",
                    "--no-first-run",
                    "--no-sandbox",
//                    "--single-process"
                    "--disable-blink-features=AutomationControlled",
                    "--disable-extensions"
//                    "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
//                    "--no-zygote"
            );
            LaunchOptions options = new LaunchOptionsBuilder().withArgs(list).withHeadless(false).build();
            FetcherOptions fetcherOptions = new FetcherOptions();
            //chrome 保存路径
            fetcherOptions.setPath(savePath);
            fetcherOptions.setHost("https://npmmirror.com/mirrors");
            BrowserFetcher browserFetcher = new BrowserFetcher(savePath, fetcherOptions);
            //下载指定版本 原方法zip解压有问题
//            browserFetcher.download(VERSION);
            RevisionInfo revisionInfo = download(VERSION, browserFetcher);
            //获得指定版本的执行路径
//            String executablePath = browserFetcher.revisionInfo(VERSION).getExecutablePath();
            options.setExecutablePath(revisionInfo.getExecutablePath());
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
            File file = new File(path).getParentFile();
            if (file != null && !file.exists()) {
                boolean mkdir = file.mkdirs();
                if (mkdir) {
                    log.info("创建目录成功:{}", file.getAbsolutePath());
                }
            }
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


    //-------------------------------------- 补丁 -------------------------------------------------

    /**
     * 根据给定得浏览器版本下载浏览器，可以利用下载回调显示下载进度
     *
     * @param revision 浏览器版本
     * @return RevisionInfo
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public static RevisionInfo download(String revision, BrowserFetcher browserFetcher) throws IOException, InterruptedException, ExecutionException {
        String url = browserFetcher.downloadURL(browserFetcher.product(), browserFetcher.platform(), browserFetcher.host(), revision);
        int lastIndexOf = url.lastIndexOf("/");
        String archivePath = Helper.join(browserFetcher.getDownloadsFolder(), url.substring(lastIndexOf));
        String folderPath = browserFetcher.getFolderPath(revision);
        if (browserFetcher.existsAsync(folderPath))
            return browserFetcher.revisionInfo(revision);
        if (!(browserFetcher.existsAsync(browserFetcher.getDownloadsFolder())))
            mkdirAsync(browserFetcher.getDownloadsFolder());
        try {

            downloadFile(url, archivePath, defaultDownloadCallback());
            install(archivePath, folderPath);
        } finally {
            unlinkAsync(archivePath);
        }
        RevisionInfo revisionInfo = browserFetcher.revisionInfo(revision);
        if (revisionInfo != null) {
            try {
                File executableFile = new File(revisionInfo.getExecutablePath());
                executableFile.setExecutable(true, false);
            } catch (Exception e) {
                log.error("Set executablePath:{} file executation permission fail.", revisionInfo.getExecutablePath());
            }
        }
        return revisionInfo;
    }

    private static BiConsumer<Integer, Integer> defaultDownloadCallback() {
        return (integer1, integer2) -> {
            BigDecimal decimal1 = new BigDecimal(integer1);
            BigDecimal decimal2 = new BigDecimal(integer2);
            int percent = decimal1.divide(decimal2, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
            log.info("Download progress: total[{}M],downloaded[{}M],{}", decimal2, decimal1, percent + "%");
        };
    }

    /**
     * 创建文件夹
     *
     * @param folder 要创建的文件夹
     * @throws IOException 创建文件失败
     */
    private static void mkdirAsync(String folder) throws IOException {
        File file = new File(folder);
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }

    /**
     * 下载浏览器到具体的路径
     * ContentTypeapplication/x-zip-compressed
     *
     * @param url              url
     * @param archivePath      zip路径
     * @param progressCallback 回调函数
     */
    private static void downloadFile(String url, String archivePath, BiConsumer<Integer, Integer> progressCallback) throws IOException, ExecutionException, InterruptedException {
        log.info("Downloading binary from " + url);
        DownloadUtil.download(url, archivePath, progressCallback);
        log.info("Download successfully from " + url);
    }

    /**
     * 删除压缩文件
     *
     * @param archivePath zip路径
     * @throws IOException 异常
     */
    private static void unlinkAsync(String archivePath) throws IOException {
        Files.deleteIfExists(Paths.get(archivePath));
    }

    private static void install(String archivePath, String folderPath) throws IOException {
        log.info("Installing " + archivePath + " to " + folderPath);
        if (archivePath.endsWith(".zip")) {
//            extractZip(archivePath, folderPath);
            installDMG(archivePath, folderPath);
        } else if (archivePath.endsWith(".tar.bz2")) {
            extractTar(archivePath, folderPath);
        } else if (archivePath.endsWith(".dmg")) {
            mkdirAsync(folderPath);
            installDMG(archivePath, folderPath);
        } else {
            throw new IllegalArgumentException("Unsupported archive format: " + archivePath);
        }
    }

    /**
     * 解压zip文件
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException 异常
     */
    private static void extractZip(String archivePath, String folderPath) throws IOException {
        BufferedOutputStream wirter = null;
        BufferedInputStream reader = null;
        ZipFile zipFile = new ZipFile(archivePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        try {
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                Path path = Paths.get(folderPath, name);
                if (zipEntry.isDirectory()) {
                    path.toFile().mkdirs();
                } else {
                    if (path.getParent() != null && !path.getParent().toFile().exists()) {
                        path.getParent().toFile().mkdirs();
                    }
                    try {
                        reader = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                        int perReadcount;
                        byte[] buffer = new byte[Constant.DEFAULT_BUFFER_SIZE];
                        wirter = new BufferedOutputStream(new FileOutputStream(path.toString()));
                        while ((perReadcount = reader.read(buffer, 0, Constant.DEFAULT_BUFFER_SIZE)) != -1) {
                            wirter.write(buffer, 0, perReadcount);
                        }
                        wirter.flush();
                    } finally {
                        StreamUtil.closeQuietly(wirter);
                        StreamUtil.closeQuietly(reader);
                    }
                }
            }
        } finally {
            StreamUtil.closeQuietly(wirter);
            StreamUtil.closeQuietly(reader);
            StreamUtil.closeQuietly(zipFile);
        }
    }

    /**
     * 解压tar文件
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException 异常
     */
    private static void extractTar(String archivePath, String folderPath) throws IOException {
        BufferedOutputStream wirter = null;
        BufferedInputStream reader = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(archivePath));
            ArchiveEntry nextEntry;
            while ((nextEntry = tarArchiveInputStream.getNextEntry()) != null) {
                String name = nextEntry.getName();
                Path path = Paths.get(folderPath, name);
                File file = path.toFile();
                if (nextEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    reader = new BufferedInputStream(tarArchiveInputStream);
                    int bufferSize = 8192;
                    int perReadcount;
                    FileUtil.createNewFile(file);
                    byte[] buffer = new byte[bufferSize];
                    wirter = new BufferedOutputStream(new FileOutputStream(file));
                    while ((perReadcount = reader.read(buffer, 0, bufferSize)) != -1) {
                        wirter.write(buffer, 0, perReadcount);
                    }
                    wirter.flush();
                }
            }
        } finally {
            StreamUtil.closeQuietly(wirter);
            StreamUtil.closeQuietly(reader);
            StreamUtil.closeQuietly(tarArchiveInputStream);
        }
    }

    /**
     * Install *.app directory from dmg file
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException 异常
     */
    private static void installDMG(String archivePath, String folderPath) throws IOException {
        try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(archivePath)) {
            zipFile.extractAll(folderPath);
        }
    }
}
