package webreduce.cleaning;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;

import java.io.Reader;

/* Just an example custom Lucene analyzer */
public class CustomAnalyzer extends Analyzer {


	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		final ClassicTokenizer src = new ClassicTokenizer();
		src.setMaxTokenLength(255);
		TokenStream filter = new ClassicFilter(src);
		filter = new LowerCaseFilter(filter);
		filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		filter = new ASCIIFoldingFilter(filter);
		return new TokenStreamComponents(src, filter) {
			@Override
			protected void setReader(final Reader reader) {
				src.setMaxTokenLength(255);
				super.setReader(reader);
			}
		};
	}
}