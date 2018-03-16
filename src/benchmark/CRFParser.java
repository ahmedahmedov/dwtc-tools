package benchmark;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import tagger.CrfTag;

public class CRFParser {
	private String crfModelFile = "models/crf_mix500.model";
//	private String testDataFile = "C:\\Users\\Heiko\\Documents\\Forschung\\NumberParsing\\yago_datasets\\all_gold.tsv";
	private String testDataFile = "C:\\Users\\Heiko\\Documents\\Forschung\\NumberParsing\\dbpedia_tables_dataset\\tables_gold.tsv";
	private CrfTag generator = null;
	
	public CRFParser() throws ClassNotFoundException, IOException {
		generator = new CrfTag(crfModelFile);
	}
	
	public void testAgainstGoldStandard() throws IOException {
		BufferedReader BR = new BufferedReader(new FileReader(testDataFile));
		
		int countLines = 0;
		double rmse_crf = 0.0;
		int correct_crf = 0;

		Date d0 = new Date();
		while(BR.ready()) {
			countLines++;
			String line = BR.readLine();
			String[] parts = line.split("\t");
			double actual = Double.parseDouble(parts[0]);

			Double bestGuess = generator.parseBestGuess(parts[1]);
			double candidate = bestGuess == null ? 0.0 : bestGuess;
			rmse_crf += (actual-candidate)*(actual-candidate);
			if(Math.abs(actual-candidate)<0.000001)
				correct_crf++;
			else
				System.out.println("CRF error: " + parts[1] + "\t" + candidate);
		}
		Date d1 = new Date();
		
		System.out.println("RMSE = " + Math.sqrt(rmse_crf/countLines));
		System.out.println("acc = " + 1.0*correct_crf/countLines);

		System.out.println(1.0*(d1.getTime()-d0.getTime())/countLines + " milliseconds per string");
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		CRFParser parser = new CRFParser();
		parser.testAgainstGoldStandard();
	}
}
