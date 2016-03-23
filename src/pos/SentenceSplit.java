/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Shiv
 */
public class SentenceSplit {

    /*public static void main(String[] args) {
        String str = "I bought the 256GB version and also reviewed it here http://goo.gl/e3tLhT. This is my second device from Apple.In the past i have used a bunch of laptops and usually switch them after a year and so.Mostly i bought products from HP and lenovo.The last one was Lenovo Ideapad Y510,which was kind of a good device but bad battery life for a traveler like me.\n"
                + "So, the main reason i bought the macbook pro was the good battery life and excellent performance it offered. Apple MacBook Pro MF840HN/A is the latest version of this one http://amzn.to/1EvEWcb";
        for(String s : splitSentences(str)){
            System.out.println(s);
        }
    }
    */
    
    
    public SentenceSplit(String str){
        int i;
        String newStr = str.charAt(0) + "";
        for (i = 1; i < str.length() - 1; i++) {
            if(str.charAt(i)=='\n'){
                newStr+=". ";
                continue;
            }
            if (str.charAt(i) == '.' && (int) str.charAt(i - 1) > 96 && (int) str.charAt(i+1) <= 90) {
                newStr += str.charAt(i) + " ";
            } else {
                newStr += str.charAt(i) + "";
            }
        }
        
        newStr += str.charAt(i) + "";
        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
         Matcher reMatcher = re.matcher(newStr);
         List<String> sentences = new ArrayList<>();
         while (reMatcher.find()) {
             sentences.add(reMatcher.group());
         }
        writeToFile(sentences);
    }
    
    private void writeToFile(List<String> sentences){
        try {
            FileWriter fileWriter = new FileWriter("sentences.txt");
            for(String string : sentences){
                fileWriter.write(string+"\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SentenceSplit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SentenceSplit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}