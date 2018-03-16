package chunker;

import com.aliasi.crf.ChainCrf;
import com.aliasi.util.AbstractExternalizable;
import tagger.CrfTagger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

public class CRFChunker {

    private static String modelFileName = null;

    private ChainCrf<String> crfModel = null;

    @SuppressWarnings("unchecked")
    public CRFChunker(String filename) throws  IOException {
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

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        CRFChunker t = new CRFChunker("/model1.model");
        System.out.println(t.crfModel.tags());
        //Double number = t.parseBestGuess(args[0]);
        //System.out.println("Number: " + number);

    }
}
