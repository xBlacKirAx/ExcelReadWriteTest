/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreadwritetest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Kira
 */
public class ExcelReadWriteTest {

    private static Workbook wb;
    private static Sheet sh;
    private static FileInputStream fis;
    private static double[][] TSum = new double[21][51];
    private static int N;
    private static double[] Genuine;
    private static double[] Impostor;
    private static double T;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        initial();
        
        Scanner sc=new Scanner(System.in);
        
        System.out.println("Please input the N(the number of samples to be used for building the template):" );
        N=sc.nextInt();
        System.out.println("Please input the T(the verification threshold): ");
        T=sc.nextDouble();
        
        Genuine = new double[(400 - N) * 51]; 
        Impostor = new double[(400 - N) * 50 * 51];
        int countGenuine = 0;
        //i indicates the user, i=0 means the first user.
        for (int i = 0; i < 51; i++) {
            //j starts from 1 since the first row is the text to indicates the meaning of each column, and j will stop at Nth row of real data for each user.
            for (int j = 1; j < N + 1; j++) {
                //this for loop is to get the sum of first N rows key hold value. k indexed the letter, i indexed the user.
                for (int k = 0; k < 11; k++) {
                    TSum[k][i] += sh.getRow(i * 400 + j).getCell(3 + k * 3).getNumericCellValue();
                }
                //this for loop is to get the sum of first N rows key interval value. k indexed the letter, i indexed the user
                for (int k = 0; k < 10; k++) {
                    TSum[k+11][i] +=sh.getRow(i * 400 + j).getCell(5 + k * 3).getNumericCellValue();
                }
            }
            
            //since genuine score is calculated for the same user, the first N rows data is used for building the template the rest datat starts with N+1th row is the test data
            for (int j = N + 1; j < 401; j++) {
                //this for loop is similar to the previous one except this is for the test data (for key hold)
                //first get each letter key hold value of the test data, then subtract with the mean of the first N rows which is the template
                //since the genuine scores using Manhattan Distance is sum the each subtraction then divide the number of the data we don't need to record the subtraction seperately 
                for (int k = 0; k < 11; k++) {
                    Genuine[countGenuine] += Math.abs(sh.getRow(i * 400 + j).getCell(3 + k * 3).getNumericCellValue() - (TSum[k][i] / N));
                }
                //this for loop is similar to the the previous one(for key interval)
                for (int k = 0; k < 10; k++) {
                    Genuine[countGenuine] += Math.abs(sh.getRow(i * 400 + j).getCell(5 + k * 3).getNumericCellValue() - (TSum[k+11][i] / N));
                }
                Genuine[countGenuine] = Genuine[countGenuine] / 21;
                countGenuine++;
            }
        }
        
        int countImposter = 0;
        //in order to calculate the imposter score, we need the test data from diferent user. 
        //l and i are both indicates the index of users, if they are equal that means they are the same user, we are not going to use that test data for calculating the imposter score
        for (int l = 0; l < 51; l++) {
            for (int i = 0; i < 51; i++) {
                if (l != i) {
                    for (int j = N + 1; j < 401; j++) {
                        //same thing for key hold first
                        for (int k = 0; k < 11; k++) {
                            Impostor[countImposter] += Math.abs(sh.getRow(i * 400 + j).getCell(3 + k * 3).getNumericCellValue() - (TSum[k][l] / N));
                        }
                        //then for key interval
                        for (int k = 0; k < 10; k++) {
                            Impostor[countImposter] += Math.abs(sh.getRow(i * 400 + j).getCell(5 + k * 3).getNumericCellValue() - (TSum[k+11][l] / N));
                            
                        }
                        Impostor[countImposter] = Impostor[countImposter] / 21;
                        countImposter++;
                    }
                }
            }
        }
        
        //Now we are going to calculate the False Reject Rate(FRR) and Impostor Pass Rate(IPR)
        //First, FRR is equal to the number of the genuine samples rejected divided by total number of genuine samples presented
        //Therefore, we need to loop through all the genuine scores and to find the ones are rejected (the genuine score is over the threshold T)
        double countGenuineSamplesRejected=0;
        for (int i = 0; i < Genuine.length; i++) {
            if(Genuine[i]>T){
                countGenuineSamplesRejected++;
            }
        }
        double FRR=countGenuineSamplesRejected*100/Genuine.length;
        
        //Then, IPR is equal to the number of the impostor samples accepted divided by total number of impostor samples presented
        //Therefore, we need to loop through all the impostor scores and to find the ones are accepted (the impostor score is less than or equal to the threshold T) 
        double countImpostorSamplesAccepted=0;
        for (int i = 0; i < Impostor.length; i++) {
            if(Impostor[i]<=T){
                countImpostorSamplesAccepted++;
            }
        }
        double IPR=countImpostorSamplesAccepted*100/Impostor.length;
        System.out.println("N="+ N+ ", T="+T+": ");
        System.out.println("IPR(Impostor Pass Rate)= "+IPR+"%");
        System.out.println("FRR(False Reject Rate )= "+FRR+"%");
        
        System.out.println("MaxGenuineScore "+findMaxGenuineScore());
        System.out.println("MinImpostorScore "+findMinImpostorScore());
//        displayMean();
//        displayGenuine();
//        displayImpostor();
    }
    
    //Read the dataset
    public static void initial() throws Exception {
        fis = new FileInputStream("./DSL-StrongPasswordData.xls");
        wb = WorkbookFactory.create(fis);
        sh = wb.getSheet("Sheet1");
    }
    
    //Max Genuine score of all the genuine score will be the threshold for 0 false reject rate
    public static double findMaxGenuineScore(){
        double MAX=Double.MIN_VALUE;
        for (int i = 0; i < Genuine.length; i++) {
            MAX=Math.max(MAX, Genuine[i]);
        }
        return MAX;
    }
    public static double findMinImpostorScore(){
        double MIN=Double.MAX_VALUE;
        for (int i = 0; i < Impostor.length; i++) {
            MIN=Math.min(MIN, Impostor[i]);
        }
        return MIN;
    }
    //Display all the genuine score
    public static void displayGenuine() {
        for (int j = 0; j < Genuine.length; j++) {
            String s = new String();

            s += (Genuine[j]) + ", ";

            System.out.println("Genuine score " + j + ":");
            System.out.println(s);
        }
    }
    //Display all the impostor score
    public static void displayImpostor() {
        for (int j = 0; j < Impostor.length; j++) {
            String s = new String();

            s += (Impostor[j]) + ", ";

            System.out.println("Impostor score " + j + ":");
            System.out.println(s);
        }
    }
    //Display all user's template
    public static void displayMean() {
        for (int j = 0; j < TSum[0].length; j++) {
            String s = new String();
            for (int i = 0; i < TSum.length; i++) {
                s += (TSum[i][j] / N) + ", ";
            }
            System.out.println("The key hold mean for User " + j + ":");
            System.out.println(s);
        }
    }
}
