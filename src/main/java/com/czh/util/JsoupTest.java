package com.czh.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author chenzh
 */
public class JsoupTest {
    public static void main(String[] args) throws IOException {
        InputStream is = JsoupTest.class.getResourceAsStream("/test.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        Document doc = Jsoup.parse(sb.toString());
        // 获取summary内的内容
        Elements summary = doc.getElementsByClass("summary");
        if (summary != null) {
            System.out.println(summary.html());
            System.out.println("----------------");
            // <div> #content-p 转换为 <p> 标签
            Document summaryDoc = Jsoup.parse(summary.html());
            Elements contentPs = summaryDoc.getElementsByClass("content-p");
            System.out.println("contentPs size: " + contentPs.size());
            /*for (int i = 0; i < contentPs.size(); i++) {
                String contentPText = contentPs.get(i).text();
                summaryDoc.select(".content-p").get(i).replaceWith(new Element(Tag.valueOf("p"), contentPText));
            }*/
            while (summaryDoc.select(".content-p").size() > 0) {
                String contentPText = summaryDoc.select(".content-p").get(0).text();
                System.out.println("--");
                System.out.println(contentPText);
                System.out.println("--");
                Element pTag = new Element("p");
                pTag.html(contentPText);
                summaryDoc.select(".content-p").get(0).replaceWith(pTag);
            }

            System.out.println(summaryDoc.html());
        }
    }
}
