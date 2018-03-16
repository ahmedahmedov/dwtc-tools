package tagger;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tag.Tagging;

import java.util.Arrays;
import java.util.List;

public class ExampleNumberCorpus
    extends Corpus<ObjectHandler<Tagging<String>>> {
	
	private static List<String> strings;
	private static List<String> tags;

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
    
    static {
    	strings = Arrays.asList("1,235,554.43","54387","9800 (2001)","2.34","3,437","ca. 5000","0,45");
    	tags 	= Arrays.asList("NTNNNTNNNDNN","NNNNN","NNNNOOOOOOO","NDNN","NTNNN","OOOONNNN","NDNN"); 
    }


    public static void main(String [] args){
    	ExampleNumberCorpus ex = new ExampleNumberCorpus();
	}
}

