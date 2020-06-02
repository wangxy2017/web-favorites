package com.wxy.web.favorites.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
            log.error("解析异常: url:{}, error:{}", urlString, e.getMessage());
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
                    if (StringUtils.isNotBlank(htmlIcon)) {
                        URL url = new URL(urlString);
                        if (htmlIcon.startsWith("//")) {
                            htmlIcon = url.getProtocol() + ":" + htmlIcon;
                        } else if (htmlIcon.startsWith("/")) {
                            htmlIcon = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + htmlIcon;
                        } else if (htmlIcon.startsWith("http")) {
                            // 不做处理
                        } else if (htmlIcon.startsWith("data:image")) {
                            htmlIcon = "";
                        } else {// 相对路径开头
                            String path = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + url.getPath();
                            htmlIcon = path.substring(0, path.lastIndexOf("/")) + "/" + htmlIcon;
                        }
                    }
                    // 验证是否有效
                    if (StringUtils.isNotBlank(htmlIcon)) {
                        HttpResponse response1 = HttpRequest.get(htmlIcon).timeout(500).execute();
                        if (response1.isOk()) {
                            iconUrl = htmlIcon;
                        }
                    }
                }
            }
            if (StringUtils.isBlank(iconUrl)) {
                // 如果html中没有icon，则从网站根目录获取
                URL url = new URL(urlString);
                String rootIcon = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + "/favicon.ico";
                HttpResponse response2 = HttpRequest.get(rootIcon).timeout(500).execute();
                if (response2.isOk()) {
                    iconUrl = rootIcon;
                }
            }
        } catch (Exception e) {
            log.error("解析异常: url:{}, error:{}", urlString, e.getMessage());
        }
        return iconUrl;
    }
}