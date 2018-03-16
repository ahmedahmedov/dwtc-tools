package benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class T2K {

	private static String dir = "C:\\Users\\Heiko\\Documents\\Forschung\\NumberParsing\\yago_datasets\\";
//	private static String dir = "C:\\Users\\Heiko\\Documents\\Forschung\\NumberParsing\\dbpedia_tables_dataset\\";
	private static String fileName = "dbpedia_gold.tsv";

	private static double parseNumeric(String text) {

		try {
			text= text.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
			return Double.parseDouble(text);
		} catch (NumberFormatException e) {
			return 0.0;
		}

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader BR = new BufferedReader(new FileReader(dir+fileName));
		double rmse_sum = 0.0;
		int count = 0;
		int limit = 10000;
		int correct = 0;
		Date d0 = new Date();
		while(BR.ready()) {
			if(++count>limit)
				break;
			String line = BR.readLine();
			String[] split = line.split("\t");
			double gold = Double.parseDouble(split[0]);
			double parsed = parseNumeric(split[1]);
			if(Double.isInfinite(parsed))
				parsed = Math.sqrt(Double.MAX_VALUE)/1000;
			rmse_sum += (gold-parsed)*(gold-parsed);
			System.out.println(split[1] + "\t"+ gold + "\t" + parsed + "\t" + rmse_sum);
			count++;
			if(Math.abs(gold-parsed)<0.000001)
				correct++;
			else if(Integer.toString((int)Math.floor(parsed)).equals(split[1]))
				correct++;
		}
		Date d1 = new Date();
		System.out.println("RMSE = " + Math.sqrt(rmse_sum/count));
		System.out.println("acc = " + 1.0*correct/count);
		System.out.println(1.0*(d1.getTime()-d0.getTime())/count + " milliseconds per string");
	}

	
}