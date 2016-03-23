/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pos;

/**
 *
 * @author Shiv
 */
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class Graph extends ApplicationFrame {

    final String positive = "POSITIVE";
        final String negative = "NEGATIVE";
    public Graph(String applicationTitle, String chartTitle, Map scoreMap) {
        super(applicationTitle);
        JFreeChart barChart = ChartFactory.createBarChart(
                chartTitle,
                "Category",
                "Score",
                createDataset(scoreMap),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
        setContentPane(chartPanel);
    }
    /*  private CategoryDataset createDataset( )
     {
     final String positive = "POSITIVE";
     final String negative = "NEGATIVE";
      
     final String fiat = "FIAT";        
     final String audi = "AUDI";        
     final String ford = "FORD";        
      
     final String speed = "Camera";        
     final String millage = "Battery";        
     final String userrating = "Performance";        
     final String safety = "Display";        
     final DefaultCategoryDataset dataset = new DefaultCategoryDataset( );  

     dataset.addValue( 5.0 , negative , speed );        
     dataset.addValue( 6.0 , negative , userrating );       
     dataset.addValue( 10.0 ,negative , millage );        
     dataset.addValue( 4.0 , negative , safety );              
      
     dataset.addValue( 1.0 , positive , speed );        
     dataset.addValue( 3.0 , positive , userrating );        
     dataset.addValue( 5.0 , positive , millage ); 
     dataset.addValue( 5.0 , positive , safety );           
 
     return dataset; 
     }
     */

    private CategoryDataset createDataset(Map<String, List<Double>> scoreMap) {
        
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
       
        int i = 0;
        for(Map.Entry<String, List<Double>> key : scoreMap.entrySet()){
        
            if(i>5)
                break;
            double positiveScore = 0.0, negativeScore = 0.0;
            int p=0, n=0;
            for(double score : key.getValue()){
                if(score>=0){
                    positiveScore+=score;
                    p++;
                }
                else{
                    n++;
                    negativeScore+=score;
                }
            }
            int t=p+n;
            negativeScore*=-1;
            
            
            dataset.addValue((negativeScore)*10, negative, key.getKey());
            
            dataset.addValue((positiveScore)*10, positive, key.getKey());
            i++;
        }
        /*

        dataset.addValue(5.0, negative, speed);
        dataset.addValue(6.0, negative, userrating);
        dataset.addValue(10.0, negative, millage);
        dataset.addValue(4.0, negative, safety);

        dataset.addValue(1.0, positive, speed);
        dataset.addValue(3.0, positive, userrating);
        dataset.addValue(5.0, positive, millage);
        dataset.addValue(5.0, positive, safety);
        */
        return dataset;
    }

    public void drawGraph() {
        //Graph chart = new Graph("Summarized Review", "Review Result", map);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        Graph chart = new Graph("Summarized Review", "Review Result", null);
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }
}