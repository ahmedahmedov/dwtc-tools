package tagger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.print.attribute.standard.PDLOverrideSupported;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tag.Tagging;
import com.sun.jndi.cosnaming.CNCtx;

public class TrainingInstances 
	extends Corpus<ObjectHandler<Tagging<String>>> {
	
	// private String fileName = "C:\\Users\\Heiko\\Documents\\Forschung\\NumberParsing\\yago_datasets\\populationDensity.tsv";
	
	private static List<String> strings = new ArrayList<String>();
	private static List<String> tags = new ArrayList<String>();
	
	public TrainingInstances() {
		
	}
	
	public void addExamples(String file, int trainingInstances, int offset) throws IOException {
		BufferedReader BR = new BufferedReader(new FileReader(file));
		
		int count=0;
		while(BR.ready()) {
			count++;

			if(count<offset)
				continue;
			if(count>trainingInstances+offset)
				break;
			String line = BR.readLine();
			String[] split = line.split("\t");
			// 0: actual value, 1: string
			String string = split[1];
			String tag = getTagged(split[1],split[0]);
			if(tag!=null) {
				strings.add(string);
				tags.add(tag);
			}
		}
		System.out.println(strings.size() + " training instances generated");
	}
	
	  public void visitTrain(ObjectHandler<Tagging<String>> handler) {
	    	for(int i=0;i<strings.size();i++) {
	    		String[] items = new String[strings.get(i).length()];
	    		String[] tagss = new String[strings.get(i).length()];
	    		for(int k=0;k<strings.get(i).length();k++) {
	    			items[k] = "" + strings.get(i).charAt(k);
	    			tagss[k] = "" + tags.get(i).charAt(k);
	    		}
	            Tagging<String> tagging = new Tagging<String>(Arrays.asList(items),Arrays.asList(tagss));
	            handler.handle(tagging);
	    	}
	    }

	    public void visitTest(ObjectHandler<Tagging<Character>> handler) {
	        /* no op */
	    }

	
	private String getTagged(String numberString, String realNumber) {
		int offset = 0;
		StringBuffer tagged = new StringBuffer();
		boolean pastDecimal = false;
		realNumber = normalizeDoubleString(realNumber);
		if(realNumber==null)
			return null;
		for(char c : numberString.toCharArray()) {

			if(offset>=realNumber.length())
				tagged.append("O");

			else if(c==(realNumber.charAt(offset))) {
					if(realNumber.charAt(offset)=='.') {
						tagged.append("D");
						pastDecimal = true;
					}
					if(realNumber.charAt(offset)=='-')
						tagged.append("M");
					else
						if(pastDecimal)
							tagged.append("P");
						else
							tagged.append("N");
					offset++;
				}
			else {
					if(offset==0 && realNumber.charAt(0)=='-') {
						tagged.append("M");
						offset++;
					}
					if(realNumber.charAt(offset)=='.') {
						tagged.append("D");
						pastDecimal = true;
						offset++;
					}
					else if(offset>0 && offset<realNumber.length()) {
						Double currentNumber = Double.parseDouble(realNumber.substring(offset));
						if((currentNumber==0.0 || (int)Math.floor(Math.log10(Math.floor(currentNumber))+1)%3==0) && !pastDecimal)
							tagged.append("T");
						else
							tagged.append("O");
					} else
						tagged.append("O");
				}
		}
		// not all elements have been found
		if(offset<realNumber.length() && realNumber.charAt(offset)!='.') 
			return null;
		return tagged.toString();
	}

	public static void main(String[] args) {
		NumberFormat NF = NumberFormat.getNumberInstance(Locale.ENGLISH);
		NF.setGroupingUsed(false);
		System.out.println(NF.format(Double.parseDouble("1.234584E-3")));
		
//		TrainingInstances TI = new TrainingInstances();
//		System.out.println(TI.getTagged("123", "123"));
//		System.out.println(TI.getTagged("0,5","0.5"));
//		System.out.println(TI.getTagged("123,456.2347", "123456.23"));
//		System.out.println(TI.getTagged("~3,000 (2001)", "3000"));
//		System.out.println(TI.getTagged("unknown", "0"));
//		System.out.println(TI.getTagged("123", "12"));
//		System.out.println(TI.getTagged("123", "124"));
//		System.out.println(TI.getTagged("-123", "-123.0"));
//		System.out.println(TI.getTagged("~1,000","-1000"));
//		System.out.println(TI.getTagged("~1,000 (200)","1000.200"));
		
	}

	private String normalizeDoubleString(String s) {
		NumberFormat NF = NumberFormat.getNumberInstance(Locale.ENGLISH);
		NF.setGroupingUsed(false);
		try {
			return NF.format(Double.parseDouble(s));
		} catch (NumberFormatException e) {
			return null;
		}
		
	}
}
