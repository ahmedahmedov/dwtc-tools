package tagger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.aliasi.crf.ChainCrf;
import org.apache.commons.lang3.StringUtils;


import com.aliasi.tag.ScoredTagging;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Pair;
import com.aliasi.tag.Tagging;

import java.util.Arrays;

class StringSplitter {

    public static List<String> splitString(String s) {
        String[] arr = new String[s.length()];
        for(int i = 0; i < s.length(); i++)
        {
            arr[i] = String.valueOf(s.charAt(i));
        }
        return Arrays.asList(arr);
    }

}

public class CrfTagger {

    private static String modelFileName = null;

    private ChainCrf<String> crfModel = null;

    @SuppressWarnings("unchecked")
    public CrfTagger(String filename) throws  IOException {
        modelFileName = filename;
        File modelFile = new File(modelFileName);
        try {
            crfModel = (ChainCrf<String>) AbstractExternalizable.readObject(modelFile);
            System.out.println(crfModel.coefficients());
            System.out.println(crfModel.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void getTags(String s, double lowerThreshold) {
        List<String> tokens = StringSplitter.splitString(s);

        Iterator<ScoredTagging<String>> it = crfModel.tagNBestConditional(tokens, (int) Math.ceil(1 / lowerThreshold));

        while (it.hasNext()) {
            ScoredTagging<String> scoredTagging = it.next();
            List<String> tags = scoredTagging.tags();
            double score = scoredTagging.score();
            System.out.println("Score: " + score);
            StringBuilder sb = new StringBuilder();
            for (String tag : tags) {
                sb.append(tag);
            }
            System.out.println("Tag: " + sb.toString());

        }
    }


    public Double parseBestGuess(String s) {
        Collection<Pair<Double,Double>> candidates = parseBestGuesses(s, 0.01);

        double bestScore = 0.0;
        double best = 0.0;
        for(Pair<Double,Double> p : candidates) {
            if(p.b()>bestScore) {
                bestScore = p.b();
                best=p.a();
            }
        }
        return best;
    }


    public List<Pair<Double,Double>> parseBestGuesses(String s, double lowerThreshold) {
        List<String> tokens = StringSplitter.splitString(s);
        Iterator<ScoredTagging<String>> it = crfModel.tagNBestConditional(tokens, (int) Math.ceil(1/lowerThreshold));

        List<Pair<Double,Double>> bestResults = new LinkedList<Pair<Double,Double>>();
        Map<Double,Double> scoredResults = new HashMap<Double,Double>();

        double sumUnparseableProbabilities = 0.0;

        while(it.hasNext()) { //for each possible tag for a given string
            ScoredTagging<String> scoredTagging = it.next();
            System.out.println("Scored Tagging: " + scoredTagging.toString());

            StringBuffer stringToParse = new StringBuffer();

            if(Math.exp(scoredTagging.score())>=lowerThreshold) {
                List<Double> results = new LinkedList<Double>();
                //for each character in the given string-number ($US123.45)
                for(int i=0;i<s.length();i++) {
                    //this is the line that generates error
                    //tagger doesn't return all tags, returns only one tag
                    final String tag = scoredTagging.tag(i);
                    System.out.println("tag(i): " + tag);

                    if(tag.equals("N"))
                        stringToParse.append(s.charAt(i));
                    else if(tag.equals("P")) {
                        if(stringToParse.length()==0)
                            stringToParse.append("0." + s.charAt(i));
                        else
                            stringToParse.append(s.charAt(i));
                    }
                    else if(tag.equals("D"))
                        stringToParse.append(".");
                    else if(tag.equals("O")) {
                        try {
                            Double d = Double.parseDouble(stringToParse.toString());
                            results.add(d);
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                try {
                    Double d = Double.parseDouble(stringToParse.toString());
                    results.add(d);
                } catch (NumberFormatException e) {
                }
                if(results.size()==0)
                    sumUnparseableProbabilities+=scoredTagging.score();
                for(Double d : results) {
                    if(!scoredResults.containsKey(d))
                        scoredResults.put(d, Math.exp(scoredTagging.score())/results.size());
                    else
                        scoredResults.put(d, scoredResults.get(d)+Math.exp(scoredTagging.score())/results.size());
                }
            }
            else
                // scores are descending, so it's not going to get any better
                break;
        }
        for(Map.Entry<Double, Double> entry : scoredResults.entrySet())
            bestResults.add(new Pair<Double,Double>(entry.getKey(),entry.getValue()/(1-sumUnparseableProbabilities)));

        return bestResults;
    }

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        CrfTagger t = new CrfTagger("/Users/ahmedov/crf_mix500.model");
        System.out.println(t.crfModel.tags());
        //Double number = t.parseBestGuess(args[0]);
        //System.out.println("Number: " + number);
        String input = null;
        if(args.length==0)
            input = "$US123.45";
        else
            input = args[0];
        System.out.println("Input: " + input);
        System.out.println("Split Input to Tokens: " + StringSplitter.splitString(input));
        System.out.println(t.parseBestGuess(input));

    }

}