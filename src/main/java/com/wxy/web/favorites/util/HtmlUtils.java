package com.wxy.web.favorites.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;

/**
 * @Author HL
 * @Date 2020/3/18 13:45
 * @Description 网页工具类
 **/
@Slf4j
public class HtmlUtils {

    public static String getTitle(String urlString) {
        String title = "";
        try {
            HttpResponse response = HttpRequest.get(urlString).timeout(500).execute();
            if (response.isOk()) {
                String body = response.body();
                Document document = Jsoup.parse(body);
                Elements elements = document.getElementsByTag("title");
                if (elements.size() > 0) {
                    title = elements.get(0).text();
                }
            }
        } catch (Exception e) {
            log.info("解析失败: url:{}, error:{}", urlString, e.getMessage());
        }
        return title;
    }

    public static String getIcon(String urlString) {
        String iconUrl = "";
        try {
            HttpResponse response = HttpRequest.get(urlString).timeout(500).execute();
            if (response.isOk()) {// 页面响应成功，解析页面
                String body = response.body();
                Document document = Jsoup.parse(body);
                Elements elements = document.getElementsByAttributeValueMatching("rel", "Shortcut Icon|shortcut icon|icon");
                if (elements.size() > 0) {
                    String htmlIcon = elements.get(0).attr("href");
                    if (StrUtil.isNotBlank(htmlIcon)) {
                        URL url = new URL(urlString);
                        if (htmlIcon.startsWith("//")) {
                            iconUrl = url.getProtocol() + ":" + htmlIcon;
                        } else if (htmlIcon.startsWith("/")) {
                            iconUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + htmlIcon;
                        } else if (htmlIcon.startsWith("http")) {
                            iconUrl = htmlIcon;
                        } else {
                            if (!htmlIcon.startsWith("data:image")) {// 相对路径开头
                                String path = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + url.getPath();
                                iconUrl = path.substring(0, path.lastIndexOf("/")) + "/" + htmlIcon;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("解析失败: url:{}, error:{}", urlString, e.getMessage());
        }
        if (StrUtil.isBlank(iconUrl)) {
            // 如果html中没有icon，则从网站根目录获取
            try {
                URL url = new URL(urlString);
                iconUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + "/favicon.ico";
            } catch (Exception e) {
                log.info("解析失败: url:{}, error:{}", urlString, e.getMessage());
            }
        }
        return iconUrl;
    }
}