/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos;

import org.json.simple.JSONObject;
import com.jaunt.ResponseException;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import com.jaunt.UserAgent;
import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;

/**
 *
 * @author Saishibi
 */
public class Parser {

    static String FinalLinks[] = new String[1000];
    static int finalcount = 0;
    static String FlipkartLinks[] = new String[100];
    static int no_of_flipkart_links;
    static String AmazonLinks[] = new String[100];
    static int no_of_amazon_links;
    static int count = 1;
    static UserAgent userAgent = new UserAgent();         //create new userAgent (headless browser)

    static String productName;
    static Scanner scanner;

    static JSONObject obj = new JSONObject();
    
    public void startParsing(String productname){
        try {
            System.out.println("Web Parsing Started");
            //scanner = new Scanner(System.in);
            
            productName = productname;//scanner.nextLine();
            parseAmazon(productName);
            parseFlipkart(productName);
            
            //System.out.println(obj.toString());
            writeLinksToFile();
            writeJSONToFile();
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Web Parsing Ended");
       
    }

    public void parseFlipkart(String productName) throws Exception {
        getFlipkartLinks(productName);

        //System.out.println(FlipkartLinks.length);
        File link_file = new File("flipkart_reviews.txt");
        link_file.createNewFile();
        FileWriter writer = new FileWriter(link_file);
        for (String link : FlipkartLinks) {
            if (link == null) {
                continue;
            }
            System.out.println("Parsing Started for link : " + link);
            parseFlipkartLink(link, writer);
            //break;//Just to pass a single url as of now
        }
        writer.flush();
        writer.close();
    }

    public void parseAmazon(String productName) throws IOException {
        getAmazonLinks(productName);
        //System.out.println(AmazonLinks.length);
        File link_file = new File("amazon_reviews.txt");
        link_file.createNewFile();
        FileWriter writer = new FileWriter(link_file);
        for (String link : AmazonLinks) {
            if (link == null) {
                continue;
            }
            System.out.println("Parsing Started for link : " + link);
            parseAmazonLink(link, writer);
            //break;//Just to pass a single url as of now
        }
        writer.flush();
        writer.close();
    }

    public void getFlipkartLinks(String product_name) throws ResponseException {
        //Code for Flipkart
        try {

            userAgent.visit("http://google.com");          //visit google
            userAgent.doc.apply(product_name + " reviews flipkart");            //apply form input (starting at first editable field)
            userAgent.doc.submit("Google Search");         //click submit button labelled "Google Search"
            String LinkArray[] = new String[1000];
            int linkcount = 0;

            com.jaunt.Elements links = userAgent.doc.findEvery("<h3 class=r>").findEvery("<a>");  //find search result links
            //System.out.println("Inside getFlipkartLinks function");
            for (com.jaunt.Element link : links) {
                String s = link.getAt("href");
                if (s.contains("www.flipkart.com")) {
                    if(linkcount>2)
                        break;
                    String temp1[] = s.split("&");
                    String temp2[] = temp1[0].split("=");
                    LinkArray[linkcount++] = temp2[1];
                }
            }

            for (int i = 0; i < linkcount; i++) {
                if (LinkArray[i].contains("review")) {
                    FlipkartLinks[finalcount] = LinkArray[i];
                    FinalLinks[finalcount++] = LinkArray[i];
                }
            }
            no_of_flipkart_links = finalcount;
            //System.out.println("Flipkart Link Count : " + no_of_flipkart_links);
        } catch (Exception e) {
            System.out.println("Exception inside getFlipkartlinks function");
        }
    }

    public void getAmazonLinks(String product_name) {
        //Code for Amazon

        try {
            //System.out.println("Inside getAmazonLinks function");
            userAgent = new UserAgent();         //create new userAgent (headless browser)
            userAgent.visit("http://google.com");          //visit google
            userAgent.doc.apply(product_name + " reviews amazon");            //apply form input (starting at first editable field)
            userAgent.doc.submit("Google Search");         //click submit button labelled "Google Search"
            String LinkArray[] = new String[1000];
            int linkcount = 0;
            com.jaunt.Elements links = userAgent.doc.findEvery("<h3 class=r>").findEvery("<a>");  //find search result links
            int c=0;
            for (com.jaunt.Element link : links) {
                String s = link.getAt("href");
                //System.out.println(s);
                if (s.contains("www.amazon")) {
                    if(linkcount>2)
                        break;
                    String temp1[] = s.split("&");
                    String temp2[] = temp1[0].split("=");
                    LinkArray[linkcount++] = temp2[1];
                    c++;
                }
            }
            int amazon_count = 0;
            for (int i = 0; i < linkcount; i++) {
                if (LinkArray[i].contains("review")) {
                    AmazonLinks[amazon_count++] = LinkArray[i];
                    FinalLinks[finalcount++] = LinkArray[i];
                }
            }
            no_of_amazon_links = amazon_count;
        } catch (Exception e) {
            System.out.println("Exception inside getAmazonlinks function");
        }

        System.out.println("Amazon Link Count : " + no_of_amazon_links);
    }

    public void parseFlipkartLink(String url, FileWriter writer) {
        try {
            Document doc = Jsoup.connect(url).get();
            Elements reviews = doc.getElementsByClass("review-list");

            for (Element e : reviews.select("div.fclear.fk-review.fk-position-relative.line")) {
                writer.write("\nReview no:" + (count++) + "\n");
                writer.write(e.text() + "\n");

                Elements titles = e.select("div.line.fk-font-normal.bmargin5.dark-gray");
                Elements body = e.select("span.review-text");
                Elements votes = e.select("div.line.fk-font-small.review-status-bar");
                String vote = votes.get(0).select("div.unit").get(0).text();
                float percentage;
                if (vote.contains("%")) {
                    percentage = getFlipkartPercentage(votes.get(0).select("div.unit").get(0).text());
                } else {
                    int value1;
                    int value2;
                    value1 = getLikedValue(vote);
                    value2 = getTotalValue(vote);
                    percentage = ((float) value1 / (float) value2) * 100;
                }
                //System.out.println("Vote Text :" + votes.get(0).select("div.unit").get(0).text());

                String reviewText = "";
                for (Element rev : body) {
                    reviewText += rev.text();
                }
                JSONObject object = new JSONObject();
                object.put("Title", titles.get(0).text());
                object.put("Body", reviewText);
                object.put("Score", percentage);
                obj.put("review" + count, object);

              
               // System.out.println("Title : " + titles.get(0).text());
               // System.out.println("Body : " + reviewText);
                //System.out.println("Score : " + percentage);
                //System.out.println("E Ended &&&&&&&&&&&&&&&&&&&");
            }
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void parseAmazonLink(String url, FileWriter writer) {
        try {
            
            Document doc = Jsoup.connect(url).get();
            Element review = doc.getElementById("cm_cr-review_list");
            //System.out.println(review);
            //System.out.println("Review Ended ************************");
            Elements reviews = review.select("div.a-section.review");
            //System.out.println(reviews);
            //System.out.println("Reviews Ended ++++++++++++++++++++++++");
            for (Element e : reviews) {
                //System.out.println(e);

                Elements titles = e.select("a.a-size-base.a-link-normal.review-title.a-color-base.a-text-bold");
                Elements body = e.select("span.a-size-base.review-text");
                Elements vote = e.select("span.a-size-small.a-color-secondary.review-votes");
                int value1 = 0, value2 = 0;
                String reviewText = "";
                for (Element rev : body) {
                    reviewText += rev.text();
                }
                if(vote.size()==0)
                    continue;
                
                writer.write("\nReview no:" + (count++) + "\n");
                writer.write(e.text() + "\n");
                
               // System.out.println("Vote : " + vote.get(0).text());
                value1 = getLikedValue(vote.get(0).text());
                value2 = getTotalValue(vote.get(0).text());
                if(value2==2){
                    String s = value1+"";
                    int n=s.length();
                    value2=10;
                    while(n-- > 0)
                    value2*=10;
                }
                JSONObject object = new JSONObject();
                object.put("Title", titles.get(0).text());
                object.put("Body", reviewText);
                object.put("Score", ((float) value1 / (float) value2) * 100);
                obj.put("review" + count, object);
                //System.out.println("Title : " + titles.get(0).text());
                //System.out.println("Body : " + reviewText);
                //System.out.println("Score : " + ((float) value1 / (float) value2)*100);
                //System.out.println("E Ended &&&&&&&&&&&&&&&&&&&");
            }
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getFlipkartPercentage(String s) {
        int i = 0;
        if (s == null || s.length() == 0) {
            return 50;
        }
        String t = "";
        while (i < s.length() && s.charAt(i) != '%') {
            t += s.charAt(i) + "";
            i++;
        }
        int score = 1;
        try{
                score = Integer.parseInt(t);
            }catch(NumberFormatException e){
        } 
        return score;
    }

    public int getLikedValue(String s) {

        s = s.replace("[A-Za-z]", "");
        if (s == null || s.length() == 0) {
            return 1;
        }
        int i = 0;
        String t = "";
        while (s.charAt(i) != ' ') {
            if (s.charAt(i) == ',') {
                i++;
                continue;
            }
            t += s.charAt(i) + "";
            i++;
        }
        int score = 1;
        try{
                score = Integer.parseInt(t);
            }catch(NumberFormatException e){
        } 
        return score;
    }

    public int getTotalValue(String s) {

        if (s == null || s.length() == 0) {
            return 2;
        }
        int i = s.indexOf("of");
        if(i==-1)
            return 2;
        //System.out.println("Index "+ i);
        i += 3;
        String t = "";
        while (s.charAt(i) != ' ') {
            // System.out.println(s.charAt(i));
            if (s.charAt(i) == ',') {
                i++;
                continue;
            }
            t += s.charAt(i) + "";
            i++;
        }
     //   System.out.println(t);
        return Integer.parseInt(t);
    }

    public void writeJSONToFile() throws IOException {

        File link_file = new File("JSON.txt");
        link_file.createNewFile();
        FileWriter writer = new FileWriter(link_file);
        writer.write(obj.toString());
        writer.flush();
        writer.close();
    }

    public void writeLinksToFile() throws IOException {
        File link_file = new File("links.txt");
        link_file.createNewFile();
        FileWriter writer = new FileWriter(link_file);

        for (int i = 0; i < finalcount; i++) {
            System.out.println(FinalLinks[i]);
            writer.write("\n" + FinalLinks[i]);
        }
        writer.flush();
        writer.close();
    }
}
