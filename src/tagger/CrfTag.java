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
import com.aliasi.tag.ScoredTagging;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Pair;

public class CrfTag {
	
	private static String modelFileName = null;
	
	private ChainCrf<String> crfModel = null;
	
	@SuppressWarnings("unchecked")
	public CrfTag(String filename) throws ClassNotFoundException, IOException {
		modelFileName = filename;
        File modelFile = new File(modelFileName);
        crfModel = (ChainCrf<String>) 
            AbstractExternalizable.readObject(modelFile);
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

		List<Pair<Double,Double>> bestResults = new LinkedList<Pair<Double,Double>>();
		Map<Double,Double> scoredResults = new HashMap<Double,Double>();
		double sumUnparseableProbabilities = 0.0;


		Iterator<ScoredTagging<String>> it = crfModel.tagNBestConditional(tokens, (int) Math.ceil(1/lowerThreshold));
		//Iterator<ScoredTagging<String>> it = crfModel.tagNBest(tokens, 5);

		/*
			if the current character is the current digit of the number
			  if the current digit is a pre-decimal digit
				tag with N
			  else
				tag with P
			  current digit <- next digit
			else if the current digit is a -
			  tag with M
			  current digit <- next digit
			else if the remaining number of pre-decimal digits is 0
			  tag with D
			else if the remaining number of pre-decimal digits is a multitude of 3
			  tag with T
			else
			  tag with O
		 */
    	while(it.hasNext()) {
    		ScoredTagging<String> scoredTagging = it.next();

    		StringBuffer stringToParse = new StringBuffer();

    		if(Math.exp(scoredTagging.score())>=lowerThreshold) {
    			List<Double> results = new LinkedList<Double>();
    			for(int i=0;i<s.length();i++) {
    				if(scoredTagging.tag(i).equals("N")) //pre-decimal digit
    					stringToParse.append(s.charAt(i));
    				else if(scoredTagging.tag(i).equals("P")) { //normal digit
    					if(stringToParse.length()==0)
    						stringToParse.append("0." + s.charAt(i));
    					else
    						stringToParse.append(s.charAt(i));
    				}
    				else if(scoredTagging.tag(i).equals("D")) //else if the remaining number of pre-decimal digits is 0
    					stringToParse.append(".");
    				else if(scoredTagging.tag(i).equals("O")) {
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
		CrfTag t = new CrfTag("/Users/ahmedov/crf_mix500.model");
		System.out.println(t.crfModel.tags());
		Double number = t.parseBestGuess(args[0]);
		System.out.println("Number: " + number);

	}
}