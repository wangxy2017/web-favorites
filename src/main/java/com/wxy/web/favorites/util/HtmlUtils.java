package com.wxy.web.favorites.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HtmlUtils {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    public static Html parseUrl(String urlString) {
        String iconUrl = "";
        String title = "";
        try {
            // 获取title
            Response response1 = client.newCall(new Request.Builder().url(urlString).build()).execute();
            if (response1.isSuccessful()) {
                String body = response1.body().string();
                Document document = Jsoup.parse(body);
                Elements elements = document.getElementsByTag("title");
                if (elements.size() > 0) {
                    title = elements.get(0).text();
                }
                Elements elements1 = document.getElementsByAttributeValueMatching("rel", "Shortcut Icon|shortcut icon|icon");
                if (elements1.size() > 0) {
                    String htmlIcon = elements1.get(0).attr("href");
                    // 相对路径自动补全
                    if (!htmlIcon.startsWith("http") && !htmlIcon.startsWith("//")) {
                        URL url1 = new URL(urlString);
                        if (htmlIcon.startsWith("/")) {
                            htmlIcon = url1.getProtocol() + "://" + url1.getHost() + (url1.getPort() > 0 ? ":" + url1.getPort() : "") + htmlIcon;
                        } else {
                            String path = url1.getProtocol() + "://" + url1.getHost() + (url1.getPort() > 0 ? ":" + url1.getPort() : "") + url1.getPath();
                            htmlIcon = path.substring(0, path.lastIndexOf("/")) + "/" + htmlIcon;
                        }
                    }
                    // 验证是否有效
                    Response response = client.newCall(new Request.Builder().url(htmlIcon).build()).execute();
                    if (response.isSuccessful()) {
                        iconUrl = htmlIcon;
                    }
                }
                // 如果html中没有icon，则从网站根目录获取
                if (StringUtils.isBlank(iconUrl)) {
                    URL url = new URL(urlString);
                    String rootIcon = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + "/favicon.ico";
                    Response response = client.newCall(new Request.Builder().url(rootIcon).build()).execute();
                    if (response.isSuccessful()) {
                        iconUrl = rootIcon;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            log.info("获取iconUrl：[{}],获取title：[{}]", iconUrl, title);
        }
        return new Html(iconUrl, title);
    }

    @Data
    @AllArgsConstructor
    public static class Html {
        String icon;
        String title;
    }
}