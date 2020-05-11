package com.wxy.web.favorites.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @Author HL
 * @Date 2020/3/18 13:45
 * @Description 网页工具类
 **/
public class HtmlUtils {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    public static String getTitle(String urlString) throws IOException {
        // 获取title
        Response response = client.newCall(new Request.Builder().url(urlString).build()).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();
            Document document = Jsoup.parse(body);
            Elements elements = document.getElementsByTag("title");
            if (elements.size() > 0) {
                return elements.get(0).text();
            }
        }
        return "";
    }

    public static String getIcon(String urlString) throws IOException {
        Response response = client.newCall(new Request.Builder().url(urlString).build()).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();
            Document document = Jsoup.parse(body);
            Elements elements = document.getElementsByAttributeValueMatching("rel", "Shortcut Icon|shortcut icon|icon");
            if (elements.size() > 0) {
                String htmlIcon = elements.get(0).attr("href");
                // 相对路径自动补全
                if (!htmlIcon.startsWith("http")) {
                    URL url = new URL(urlString);
                    if (htmlIcon.startsWith("//")) {
                        htmlIcon = url.getProtocol() + ":" + htmlIcon;
                    } else if (htmlIcon.startsWith("/")) {
                        htmlIcon = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + htmlIcon;
                    } else {
                        String path = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + url.getPath();
                        htmlIcon = path.substring(0, path.lastIndexOf("/")) + "/" + htmlIcon;
                    }
                }
                // 验证是否有效
                Response response1 = client.newCall(new Request.Builder().url(htmlIcon).build()).execute();
                if (response1.isSuccessful()) {
                    return htmlIcon;
                }
            }
            // 如果html中没有icon，则从网站根目录获取
            URL url = new URL(urlString);
            String rootIcon = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + "/favicon.ico";
            Response response2 = client.newCall(new Request.Builder().url(rootIcon).build()).execute();
            if (response2.isSuccessful()) {
                return rootIcon;
            }
        }
        return "";
    }
}