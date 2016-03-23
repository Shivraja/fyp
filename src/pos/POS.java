/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shiv
 */
public class POS {
    
    List<ComponentFeature> componentFeaturePairs = new ArrayList<>();
    
    public POS(){
        try {
            MaxentTagger tagger = new MaxentTagger("models/english-left3words-distsim.tagger");
            List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader("sentences.txt")));
            for (List<HasWord> sentence : sentences) {
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                ComponentFeature obj = new ComponentFeature();
                obj.component = new ArrayList<>();
                obj.enhancer = new ArrayList<>();
                obj.feature = new ArrayList<>();
                String previousEnhancer = null;
                String previousComponent = null;
                for (TaggedWord word : tSentence) {
                    //System.out.println("*****\n "+tSentence+" \n*****");
                    if (word.tag().startsWith("NN")) {
                        if(previousComponent==null)
                            previousComponent = word.word();
                        else previousComponent+=" "+word.word();
                        //obj.component.add(word.word());
                    } else if (word.tag().startsWith("JJ")) {
         //               obj.component.add(previousComponent);
                        if(previousComponent!=null){
                            obj.component.add(previousComponent);
                        }
                        obj.enhancer.add(previousEnhancer);
                        obj.feature.add(word.word());
                        previousComponent = null;
                        previousEnhancer = null;
                    } else if (word.tag().startsWith("RB")) {
                        if(previousComponent!=null){
                            obj.component.add(previousComponent);
                            previousComponent = null;
                        }
                        previousEnhancer = word.word();
                    }
                }
                componentFeaturePairs.add(obj);
                //System.out.println(Sentence.listToString(tSentence, false));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(POS.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public List<ComponentFeature> getComponentFeaturePair(){
       // printComponentFeaturePair(componentFeaturePairs);
        return componentFeaturePairs;
    }
    
    public void printComponentFeaturePair(List<ComponentFeature> pairs){
        int index = 1;
        for(ComponentFeature obj : pairs){
            System.out.println("Sentence "+index++);
            for(String component : obj.component){
                System.out.print(component+" ");
            }
            System.out.println("");
            for(String enhancer : obj.enhancer){
                System.out.print(enhancer+" ");
            }
            System.out.println("");
            for(String feature : obj.feature){
                System.out.print(feature+" ");
            }
            System.out.println("");
        }
    }
}

class ComponentFeature {
        List<String> component;
        List<String> enhancer;
        List<String> feature;
    }