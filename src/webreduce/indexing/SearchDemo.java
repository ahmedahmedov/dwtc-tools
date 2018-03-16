package webreduce.indexing;

import com.google.common.primitives.Longs;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import webreduce.data.Dataset;
import webreduce.visualize.tableVisualizer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * Created by ahmedov on 03/06/16.
 */
public class SearchDemo {
    //public static String[] entities = {"Apple","IBM", "Bank of China", "BMW", "Daimler", "Royal Bank of Canada","Volkswagen Group","General Electric", "Deutsche Bank"};//{"Germany", "USA", "Italy", "Azerbaijan", "France"};
    public static String[] entities = {"Azerbaijan", "USA", "Germany","UK"};
    //public static double [] values = {199.4,93.4, 105.1, 106.6, 18.82, 91.33, 134.19, 33.5};
    public static double [] values = {37.56};

    //public static String[] attributes = {"GDP", "Gross Domestic Product"};
    public static String[] attributes = {"GDP", "Gross Domestic Product"};

    public static List<Dataset> resultList = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        // instantiate the search engine

        double min = Double.parseDouble(args[0]);
        double max = Double.parseDouble(args[1]);
        int numberOfResults = Integer.parseInt(args[2]);
        String path = args[3];

        SearchEngine se = new SearchEngine(path);
        DB leveldb;
        Options options = new Options();
        options.createIfMissing(true);
        leveldb = factory.open(new File(path, "leveldb"), options);

        try {
            TopDocs topDocs = se.performNumericRangeQuery(entities, attributes, values, min, max, numberOfResults);
            ScoreDoc[] hits = topDocs.scoreDocs;
            System.out.println(hits.length);
            for (int i = 0; i < hits.length; i++) {
                Document doc = se.getDocument(hits[i].doc);
                //String jsonString = doc.get("full_result");
                byte[] result = leveldb.get(Longs.toByteArray(Longs.tryParse(doc.get("document_id"))));
                String jsonString = new String(result);
                System.out.println(doc.get("value"));
                Dataset er = Dataset.fromJson(jsonString);
                resultList.add(er);
            }
            int i =0;
            DBIterator iterator2 = leveldb.iterator();
            /*try {
                for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    String key = new String(iterator.peekNext().getKey());
                    String value = new String(iterator.peekNext().getValue());
                    System.out.println(key+" = "+value);
                }
            } finally {
                // Make sure you close the iterator to avoid resource leaks.
                iterator.close();
            }
            */
            iterator2.close();
            String stats = leveldb.getProperty("leveldb.stats");
            System.out.println(stats);



        } catch (ParseException e) {
            e.printStackTrace();
        }

        Iterator<Dataset> iterator = resultList.iterator();
        final tableVisualizer tv = new tableVisualizer();//accessed from within an inner class, needs to be declared final

        while(iterator.hasNext()){
            final Dataset ds = iterator.next();
            String[][] orgrelation = ds.getRelation();
            final String[][] relation = datasetTools.transpose(orgrelation);
            final String[] cols = ds.getAttributes();
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tv.drawTable(relation, cols, ds);
                }
            });

        }

    }
}
