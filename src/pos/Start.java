/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Shiv
 */
public class Start {

    static Map<String, List<Double>> scoreMap = new HashMap<>();
    static HMM hmm = new HMM(20,1);
    
    public Start(){
        hmm.train(a, 20);
    }
    
    public static void main(String args[]) throws FileNotFoundException, IOException {
        
        //processSentence(str);

        String productName = "samsung galaxy y";
        new Parser().startParsing(productName);
        getSentenceFromJson();
        
        //printScore();
        scoreMap = sortByValue(scoreMap);
        //printScore();
        Graph chart = new Graph("Summarized Review", productName, scoreMap);
        chart.drawGraph();
    }

    public static void getSentenceFromJson(){
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("JSON.txt"));
            JSONObject jObject = (JSONObject) obj;
            for(Object o : jObject.keySet()){
                System.out.println(o);
                JSONObject ob = (JSONObject)jObject.get(o);
                System.out.println("Body : "+ob.get("Body"));
                processSentence(ob.get("Body")+"");
            }
            //System.out.println(jObject.toString());
            
        } catch (ParseException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void processSentence(String str) throws IOException{
        SentenceSplit sentenceSplit = new SentenceSplit(str); //Output will be written to the file sentences.txt
        List<ComponentFeature> componentFeaturePairs = new POS().getComponentFeaturePair();
        Polarity polarity = new Polarity();
        //System.out.println("Polarity Score : " + polarity.getPolarityScore("Mostly", "r"));
        int index = 1;

        double enhancerScore, featureScore;
        for (ComponentFeature obj : componentFeaturePairs) {
            double score = 0.0;
            if(obj.component.size()>=1){
                for(String com : obj.component){
                    for(String fea: obj.feature){
                        score = hmm.getScore(com, fea);
                        if(score <= 0.5){
                            continue;
                        }
                    }
                }
            }
            enhancerScore = featureScore = 0.0;
            System.out.println("Sentence " + index++);

            for (String enhancer : obj.enhancer) {
                //System.out.print(enhancer + " ");
                if (enhancer != null) {
                    //System.out.println(enhancer);
                    enhancerScore = polarity.getPolarityScore(enhancer, "r");
//                    System.out.print("score : " + enhancerScore + "   ");

                }
            }

  //          System.out.println("");
            for (String feature : obj.feature) {
    //            System.out.print(feature + " ");
                if (feature != null) {
                    featureScore = polarity.getPolarityScore(feature, "a");
      //              System.out.print("score : " + featureScore + "   ");
                }
            }
        //    System.out.println("");
            addScore(obj.component, enhancerScore, featureScore);
        }
    }
    
    
    public static void addScore(List<String> components, double enhancerScore, double featureScore) {
        double score = getScore(enhancerScore, featureScore);

        for (String component : components) {
            component = component.toLowerCase();
            if (scoreMap.containsKey(component)) {
                List<Double> list = scoreMap.get(component);
                list.add(score);
                scoreMap.put(component, list);
            } else {
                List<Double> list = new ArrayList<>();
                list.add(score);
                scoreMap.put(component, list);
            }
        }
    }

    public static double getScore(double enhancerScore, double featureScore) {
        if (enhancerScore + featureScore >= 1.0) {
            return 1.0;
        }
        return enhancerScore + featureScore;
    }

    public static void printScore() {
        System.out.println("PRINTING SCORE STARTED");
        for (String string : scoreMap.keySet()) {
            System.out.println(string + " " + scoreMap.get(string));
        }
    }

    static Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((List<Double>)((Map.Entry) (o2)).getValue()).size()).compareTo(((List<Double>)((Map.Entry) (o1)).getValue()).size());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    
    int[] a = {1,2,3,4,5,6,1,2,3,4,5,3,1,2,3,2,1,2,3,1};
}