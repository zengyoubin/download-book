package cn.zeng;

import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static cn.zeng.Consts.bookName;
import static cn.zeng.Consts.downloadUrl;
import static cn.zeng.Consts.searchUrl;

/**
 * @author ZYB
 * @since 2017-04-26 下午5:16
 */
@Slf4j
public class BookProcessor implements PageProcessor, Runnable {
    private static final Site SITE;
    private static final String XPATH_URL = "//a[@class='result-game-item-title-link' and @title='%s']/@href";
    private static final String XPATH_CHECK_BOOK_NAME = "//meta[@property='og:novel:book_name']/@content";
    private static final String XPATH_CHAPTER_URL = "//div[@id='list']/dl/dd/a/@href";
    private static final String XPATH_CHAPTER_NAME = "//h1/text()";
    private static final String XPATH_CHAPTER_CONTENT = "//div[@id='content']/text()";

    static {
        SITE = new Site();
        SITE.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")
            .setRetryTimes(5)
            .setTimeOut(1000 * 30)
            .setSleepTime(1000);

    }

    @Override

    public void process(Page page) {
        String url = page.getHtml().xpath(String.format(XPATH_URL, bookName)).toString();
        if (url != null) {
            page.setSkip(true);
            downloadUrl = url;
            page.addTargetRequest(url);
            return;
        }
        if (bookName.equals(page.getHtml().xpath(XPATH_CHECK_BOOK_NAME).toString())) {
            page.setSkip(true);
            List<String> strings = page.getHtml().xpath(XPATH_CHAPTER_URL).all();
            strings = strings.stream().sorted(Comparator.comparing(BookProcessor::parseUrl)).collect(Collectors.toList());
            page.addTargetRequests(strings);
            return;
        }
        Chapter chapter = new Chapter();
        chapter.setName(page.getHtml().xpath(XPATH_CHAPTER_NAME).toString());
        chapter.setContent(page.getHtml().xpath(XPATH_CHAPTER_CONTENT).toString());
        page.putField("chapter", chapter);

    }

    private static Integer parseUrl(String str) {
        try {
            return Integer.parseInt(str.substring(str.lastIndexOf("/") + 1, str.length() - 5));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            log.error("parse {} error", str);
        }
        return 0;
    }

    @Override
    public Site getSite() {
        return SITE;
    }

    @Override
    public void run() {
        Spider.create(new BookProcessor())
            .addUrl(String.format(searchUrl, bookName))
            .addPipeline(new WriteBookPipeline())
            .thread(1)
            //启动爬虫
            .run();
    }



}
