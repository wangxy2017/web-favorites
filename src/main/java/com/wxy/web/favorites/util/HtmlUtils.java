package com.wxy.web.favorites.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HtmlUtils {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    /**
     * 获取重定向后的地址
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static String getFinalUrl(String url) throws IOException {
        Response response = client.newCall(new Request.Builder().url(url).build()).execute();
        if (response.code() == 302) {
            url = response.header("location");
        }
        return url;
    }

    /**
     * 获取网站根目录下的图标
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static String getRootIcon(String url) throws IOException {
        URL u = new URL(url);
        String iconUrl = u.getProtocol() + "://" + u.getHost() + (u.getPort() > 0 ? ":" + u.getPort() : "") + "/favicon.ico";
        Response response = client.newCall(new Request.Builder().url(iconUrl).build()).execute();
        if (response.code() == 200) {
            return iconUrl;
        } else {
            return null;
        }
    }

    /**
     * 获取html页面中的图标
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static String getIconByHtml(String url) throws IOException {
        String iconUrl = null;
        Response response = client.newCall(new Request.Builder().url(url).build()).execute();
        String body = response.body().string();
        Document document = Jsoup.parse(body);
        Elements elements = document.getElementsByAttributeValue("rel", "shortcut icon");
        if (elements.size() > 0) {
            iconUrl = getAbsoluteIconUrl(elements.get(0).attr("href"), url);
        } else {
            Elements elements1 = document.getElementsByAttributeValue("rel", "icon");
            if (elements1.size() > 0) {
                iconUrl = getAbsoluteIconUrl(elements1.get(0).attr("href"), url);
            }
        }
        return iconUrl;
    }

    /**
     * 获取html页面中的title内容
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static String getTitleByHtml(String url) throws IOException {
        String title = null;
        Response response = client.newCall(new Request.Builder().url(url).build()).execute();
        String body = response.body().string();
        Document document = Jsoup.parse(body);
        Elements elements = document.getElementsByTag("title");
        if (elements.size() > 0) {
            title = elements.get(0).text();
        }
        return title;
    }

    /**
     * 获取图标绝对路径
     *
     * @param iconUrl
     * @param urlString
     * @return
     * @throws MalformedURLException
     */
    private static String getAbsoluteIconUrl(String iconUrl, String urlString) throws MalformedURLException {
        if (iconUrl.contains("http")) {
            return iconUrl;
        }
        if (iconUrl.charAt(0) == '/') {//判断是否为相对路径或根路径
            URL url = new URL(urlString);
            iconUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "") + iconUrl;
        } else {
            URL url = new URL(urlString);
            iconUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() > 0 ? ":" + url.getPort() : "")
                    + url.getPath() + (url.getPath().endsWith("/") ? "" : "/") + iconUrl;
        }
        return iconUrl;
    }

    public static String getIcon(String url) throws IOException {
        String finalUrl = getFinalUrl(url);
        String rootIcon = getRootIcon(finalUrl);
        if (rootIcon != null) {
            return rootIcon;
        } else {
            return getIconByHtml(finalUrl);
        }
    }

    public static String getTitle(String url) throws IOException {
        return getTitleByHtml(getFinalUrl(url));
    }

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
//        String url = "https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp/3.14.2";
        String url = "https://disp-wfw.xiaopankeji.com/dd-admin/#/login";
        System.out.println(getIcon(url));
        System.out.println(getTitle(url));
        System.out.printf("耗时：[%s ms]", System.currentTimeMillis() - start);
    }
}
