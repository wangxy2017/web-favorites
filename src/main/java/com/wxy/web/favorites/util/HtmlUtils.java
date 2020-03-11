package com.wxy.web.favorites.util;

import lombok.AllArgsConstructor;
import lombok.Data;
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
                    iconUrl = elements1.get(0).attr("href");
                    // 判断是否相对路径
                    if (!iconUrl.startsWith("http")) {
                        URL url1 = new URL(urlString);
                        if (iconUrl.startsWith("/")) {
                            iconUrl = url1.getProtocol() + "://" + url1.getHost() + (url1.getPort() > 0 ? ":" + url1.getPort() : "") + iconUrl;
                        } else {
                            String path = url1.getProtocol() + "://" + url1.getHost() + (url1.getPort() > 0 ? ":" + url1.getPort() : "") + url1.getPath();
                            iconUrl = path.substring(path.lastIndexOf("/")) + "/" + iconUrl;
                        }
                    }
                }
                if (StringUtils.isBlank(iconUrl)) {
                    // 获取根icon
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
        }
        return new Html(title, iconUrl);
    }

    @Data
    @AllArgsConstructor
    public static class Html {
        String icon;
        String title;
    }
}
