/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package excelreadwritetest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    private static double[][] KHMean = new double[11][51];
    private static double[][] KIMean = new double[10][51];
    private static int N = 398;
    private static double[] KHGenuine;
    private static double[] KHImposter;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        initial();
        KHGenuine = new double[(400 - N) * 51];
        KHImposter = new double[(400 - N) * 50 * 51];

        int countGenuine = 0;
        for (int i = 0; i < 51; i++) {
            for (int j = 1; j < N + 1; j++) {
//                System.out.println(sh.getRow(i*400+j).getCell(0));
                for (int k = 0; k < KHMean.length; k++) {
                    KHMean[k][i] += sh.getRow(i * 400 + j).getCell(3 + k * 3).getNumericCellValue();
                }
            }
            for (int j = N + 1; j < 401; j++) {
                for (int k = 0; k < KHMean.length; k++) {
                    KHGenuine[countGenuine] += Math.abs(sh.getRow(i * 400 + j).getCell(3 + k * 3).getNumericCellValue() - (KHMean[k][i] / N));

                }
                KHGenuine[countGenuine] = KHGenuine[countGenuine] / 11;
                countGenuine++;
            }
        }
        
        int countImposter = 0;
        for (int l = 0; l < 51; l++) {
            for (int i = 0; i < 51; i++) {
                if (l != i) {
                    for (int j = N + 1; j < 401; j++) {
                        for (int k = 0; k < KHMean.length; k++) {
                            KHImposter[countImposter] += Math.abs(sh.getRow(i * 400 + j).getCell(3 + k * 3).getNumericCellValue() - (KHMean[k][l] / N));
                        }
                        KHImposter[countImposter] = KHImposter[countImposter] / 11;
                        countImposter++;
                    }
                }
            }
        }

//        displayKHMean();
        displayKHGenuine();
        displayKHImposter();
    }

    public static void initial() throws Exception {
        fis = new FileInputStream("./DSL-StrongPasswordData.xls");
        wb = WorkbookFactory.create(fis);
        sh = wb.getSheet("Sheet1");
    }

    public static void displayKHGenuine() {
        for (int j = 0; j < KHGenuine.length; j++) {
            String s = new String();

            s += (KHGenuine[j]) + ", ";

            System.out.println("The key hold genuine for User " + j + ":");
            System.out.println(s);
        }
    }

    public static void displayKHImposter() {
        for (int j = 0; j < KHImposter.length; j++) {
            String s = new String();

            s += (KHImposter[j]) + ", ";

            System.out.println("The key hold imposter for User " + j + ":");
            System.out.println(s);
        }
    }

    public static void displayKHMean() {
        for (int j = 0; j < KHMean[0].length; j++) {
            String s = new String();
            for (int i = 0; i < KHMean.length; i++) {
                s += (KHMean[i][j] / N) + ", ";
            }
            System.out.println("The key hold mean for User " + j + ":");
            System.out.println(s);
        }
    }
}
