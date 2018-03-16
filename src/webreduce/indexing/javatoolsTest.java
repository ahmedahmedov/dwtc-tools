package webreduce.indexing;

import javatools.parsers.NumberParser;

public class javatoolsTest {

    public static void main(String args[]){

        System.out.println(NumberParser.normalize("It was 1.2 inches long"));

        String d1 = NumberParser.normalize("I spent 5 bil dollar yesterday");
        String d2 = NumberParser.normalize("$3 billion dollar");
        String d3 = NumberParser.normalize("$3 bil dollar");
        int pos[] = new int[2];
        String normalizedNumber = NumberParser.normalize(args[0]);
        System.out.println("Normalized Number: " + normalizedNumber);
        String numUnit [] = NumberParser.getNumberAndUnit(normalizedNumber,pos);
        System.out.println("Number: " + numUnit[0]+ ", Unit: " + numUnit[1]);
        //System.out.println(d1 + " " + d2 + " " + " " + d3);


    }


}
