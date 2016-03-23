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
import static java.lang.Double.max;
import java.text.*;

/** This class implements a Hidden Markov Model, as well as
    the Baum-Welch Algorithm for training HMMs. 
*/
public class HMM {
    
    static double c[][] = new double[20][1];
    static String[] component = new String[20];
    static double f[][] = new double[20][1];
    static String[] feature = new String[20];
      
  /** number of states */
  static public int numStates;

  /** size of output vocabulary */
  static public int sigmaSize;

  /** initial state probabilities */
  static public double pi[];

  /** transition probabilities */
  static public double a[][];

  /** emission probabilities */
  static public double b[][];

  /** initializes an HMM.
      @param numStates number of states
      @param sigmaSize size of output vocabulary 
  */
  public HMM(int numStates, int sigmaSize) {
    this.numStates = numStates;
    this.sigmaSize = sigmaSize;
    trainDataset();
    pi = new double[numStates];
    a = new double[numStates][numStates];
    b = new double[numStates][sigmaSize];
  }

  /** implementation of the Baum-Welch Algorithm for HMMs.
      @param o the training set
      @param steps the number of steps
  */
  public void train(int[] o, int steps) {
    int T = o.length;
    double[][] fwd;
    double[][] bwd;

    double pi1[] = new double[numStates];
    double a1[][] = new double[numStates][numStates];
    double b1[][] = new double[numStates][sigmaSize];

    for (int s = 0; s < steps; s++) {
      /* calculation of Forward- und Backward Variables from the
	 current model */
      fwd = forwardProc(o);
      bwd = backwardProc(o);

      /* re-estimation of initial state probabilities */
      for (int i = 0; i < numStates; i++)
	pi1[i] = gamma(i, 0, o, fwd, bwd);

      /* re-estimation of transition probabilities */ 
      for (int i = 0; i < numStates; i++) {
	for (int j = 0; j < numStates; j++) {
	  double num = 0;
	  double denom = 0;
	  for (int t = 0; t <= T - 1; t++) {
	    num += p(t, i, j, o, fwd, bwd);
	    denom += gamma(i, t, o, fwd, bwd);
	  }
	  a1[i][j] = divide(num, denom);
	}
      }
      
      /* re-estimation of emission probabilities */
      for (int i = 0; i < numStates; i++) {
	for (int k = 0; k < sigmaSize; k++) {
	  double num = 0;
	  double denom = 0;
	  
	  for (int t = 0; t <= T - 1; t++) {
	    double g = gamma(i, t, o, fwd, bwd);
	    num += g * (k == o[t] ? 1 : 0);
	    denom += g;
	  }
	  b1[i][k] = divide(num, denom);
	}
      }
      pi = pi1;
      a = a1;
      b = b1;
    }
    
  }
  

  /** calculation of Forward-Variables f(i,t) for state i at time
      t for output sequence O with the current HMM parameters
      @param o the output sequence O
      @return an array f(i,t) over states and times, containing
              the Forward-variables. 
  */
  public double[][] forwardProc(int[] o) {
    int T = o.length;
    double[][] fwd = new double[numStates][T];
        
    /* initialization (time 0) */
    for (int i = 0; i < numStates; i++)
      fwd[i][0] = pi[i] * b[i][o[0]];

    /* induction */
    for (int t = 0; t <= T-2; t++) {
      for (int j = 0; j < numStates; j++) {
	fwd[j][t+1] = 0;
	for (int i = 0; i < numStates; i++)
	  fwd[j][t+1] += (fwd[i][t] * a[i][j]);
	fwd[j][t+1] *= b[j][o[t+1]];
      }
    }

    return fwd;
  }

  /** calculation of  Backward-Variables b(i,t) for state i at time
      t for output sequence O with the current HMM parameters
      @param o the output sequence O
      @return an array b(i,t) over states and times, containing
              the Backward-Variables. 
  */
  public static double[][] backwardProc(int[] o) {
    int T = o.length;
    double[][] bwd = new double[numStates][T];
        
    /* initialization (time 0) */
    for (int i = 0; i < numStates; i++)
      bwd[i][T-1] = 1;

    /* induction */
    for (int t = T - 2; t >= 0; t--) {
      for (int i = 0; i < numStates; i++) {
	bwd[i][t] = 0;
	for (int j = 0; j < numStates; j++)
	  bwd[i][t] += (bwd[j][t+1] * a[i][j] * b[j][o[t+1]]);
      }
    }

    return bwd;
  }

  /** calculation of probability P(X_t = s_i, X_t+1 = s_j | O, m).
      @param t time t
      @param i the number of state s_i
      @param j the number of state s_j
      @param o an output sequence o
      @param fwd the Forward-Variables for o
      @param bwd the Backward-Variables for o
      @return P
  */
  public static double p(int t, int i, int j, int[] o, double[][] fwd, double[][] bwd) {
      if(o==null)
          return 0.6;
    double num;
    if (t == o.length - 1)
      num = fwd[i][t] * a[i][j];
    else
      num = fwd[i][t] * a[i][j] * b[j][o[t+1]] * bwd[j][t+1];

    double denom = 0;

    for (int k = 0; k < numStates; k++)
      denom += (fwd[k][t] * bwd[k][t]);

    return max(divide(num, denom),max(f[i][0],c[j][0]));
  }

  /** computes gamma(i, t) */
  public static double gamma(int i, int t, int[] o, double[][] fwd, double[][] bwd) {
    double num = fwd[i][t] * bwd[i][t];
    double denom = 0;

    for (int j = 0; j < numStates; j++)
      denom += fwd[j][t] * bwd[j][t];

    return divide(num, denom);
  }

  /** prints all the parameters of an HMM */
  public static void print() {
    DecimalFormat fmt = new DecimalFormat();
    fmt.setMinimumFractionDigits(5);
    fmt.setMaximumFractionDigits(5);
    
    for (int i = 0; i < numStates; i++)
      System.out.println("pi(" + i + ") = " + fmt.format(pi[i]));
    System.out.println();

    for (int i = 0; i < numStates; i++) {
      for (int j = 0; j < numStates; j++)
	System.out.print("a(" + i + "," + j + ") = " + 
			 fmt.format(a[i][j]) + "  ");
      System.out.println();
    }

    System.out.println();
    for (int i = 0; i < numStates; i++) {
      for (int k = 0; k < sigmaSize; k++)
	System.out.print("b(" + i + "," + k + ") = " + 
			 fmt.format(b[i][k]) + "  ");
      System.out.println();
    }
  }

  /** divides two doubles. 0 / 0 = 0! */
  public static double divide(double n, double d) {
    if (n == 0)
      return 0;
    else
      return n / d;
  }
  
  public static double findScore(int i, int j){
      double score = 0.6;
      try{
           score = p(20,i,j,null,a,b);
      }catch(NullPointerException e){
          
      }
      return score;
  }
  
  public static void trainDataset(){
      
      c[0][0] =0.5; component[0]="camera";
      c[1][0] =0.6; component[1]="screen";
      c[2][0] =0.1; component[2]="Wealth";
      c[3][0] =0.5; component[3]="Clarity";
      c[4][0] =0.7; component[4]="book";
      c[5][0] =0.8; component[5]="battery";
      c[6][0] =0.3; component[6]="keypad";
      c[7][0] =0.8; component[7]="Resolution";
      c[8][0] =0.1; component[8]="Amazon";
      c[9][0] =0.9; component[9]="speaker";
      c[10][0] =0.6; component[10]="Mobile";
      c[11][0] =0.4; component[11]="Charger";
      c[12][0] =0.3; component[12]="Woman";
      c[13][0] =0.2; component[13]="Man";
      c[14][0] =0.5; component[14]="touch";
      c[15][0] =0.6; component[15]="health";
      c[16][0] =0.0; component[16]="Person";
      c[17][0] =0.4; component[17]="life";
      c[18][0] =0.6; component[18]="battery";
      c[19][0] =0.7; component[19]="laptop";

      f[0][0] =0.5; feature[0]="high";
      f[1][0] =0.6; feature[1]="low";
      f[2][0] =0.1; feature[2]="poor";
      f[3][0] =0.5; feature[3]="beautiful";
      f[4][0] =0.7; feature[4]="good";
      f[5][0] =0.8; feature[5]="nice";
      f[6][0] =0.3; feature[6]="late";
      f[7][0] =0.8; feature[7]="better";
      f[8][0] =0.1; feature[8]="honest";
      f[9][0] =0.9; feature[9]="awesome";
      f[10][0] =0.1; feature[10]="careful";
      f[11][0] =0.3; feature[11]="orange";
      f[12][0] =0.3; feature[12]="red";
      f[13][0] =0.2; feature[13]="clever";
      f[14][0] =0.5; feature[14]="same";
      f[15][0] =0.6; feature[15]="early";
      f[16][0] =0.0; feature[16]="main";
      f[17][0] =0.4; feature[17]="few";
      f[18][0] =0.6; feature[18]="able";
      f[19][0] =0.7; feature[19]="public";
  }
  
  public String getFeature(int i){
      return feature[i];
  }
  
  public String getComponenet(int i){
      return component[i];
  }
  
  public static double getScore(String com, String fea){
      if(com==null || fea==null)
          return 0.6;
      int c=-1,f=-1;
      try{
      for(int i=0;i<20;i++){
          if(com.matches(component[i]))
          {
              c=i;
          }
      }
      for(int i=0; i<20;i++){
          if(fea.matches(feature[i])){
              f=i;
          }
      }}
      catch(NullPointerException e){
          return 0.6;
      }
      return findScore(c,f);
  }
}