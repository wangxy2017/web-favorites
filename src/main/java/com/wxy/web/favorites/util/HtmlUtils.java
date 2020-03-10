package com.wxy.web.favorites.util;

import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HtmlUtils {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    /**
     * 获取网站根目录下的图标
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String getIcon(String url) throws Exception {
        URL u = new URL(url);
        String iconUrl = u.getProtocol() + "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "") + "/favicon.ico";
        Response response = client.newCall(new Request.Builder().url(iconUrl).build()).execute();
        if (response.isSuccessful()) {
            return iconUrl;
        }
        return null;
    }

    /**
     * 获取html页面中的title内容
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String getTitle(String url) throws Exception {
        URL u = new URL(url);
        String rootPath = u.getProtocol() + "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "") + "/";
        Response response = client.newCall(new Request.Builder().url(rootPath).build()).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();
            Document document = Jsoup.parse(body);
            Elements elements = document.getElementsByTag("title");
            if (elements.size() > 0) {
                return elements.get(0).text();
            }
        }
        return null;
    }

    public static Html parseUrl(String urlString) {
        return new Html();
    }

    @Data
    public static class Html {
        String icon;
        String title;
    }
}
