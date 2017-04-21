package webreduce.indexing;

/**
 * Created by ahmedov on 03/06/16.
 */

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import webreduce.cleaning.CustomAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;


public class SearchEngine {
    private IndexSearcher searcher = null;
    private static String ATTRIBUTES_FIELD = "attributes";
    private static String ENTITIES_FIELD = "entities";
    private static String TABLE_TYPE_FIELD = "tableType";
    private static String VALUES_FIELD = "value";
    QueryParser qpa, qpe ;


    public SearchEngine(String path) throws IOException {
        //searcher = new IndexSearcher(DirectoryReader.open(RemoteDi))

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));

        searcher = new IndexSearcher(reader);

        qpa = new QueryParser(ATTRIBUTES_FIELD, new CustomAnalyzer()); //attributes parser

        qpe = new QueryParser(ENTITIES_FIELD, new CustomAnalyzer()); //entities parses
    }

    public TopDocs performSearch(String queryString, int n)
            throws IOException, ParseException {
        Query query = qpe.parse(queryString);
        return searcher.search(query, n);
    }

    public TopDocs performNumericRangeQuery(String[] entities, String[] attributes, double[] values, double min, double max, int n) throws IOException, ParseException {

        BooleanQuery.Builder finalQuery = new BooleanQuery.Builder();

        BooleanQuery.Builder bqa = new BooleanQuery.Builder(); //attribute final query

        for (String a : attributes) {

            Query qa = qpa.parse(a); //add each attribute to a query
           // qa.setBoost(0.9f); //boost the query

            Query qaph = qpa.parse("\"" + a + "\"");//add each attribute to a query as a phrase
            //qaph.setBoost(1.7f); //boost the query
            bqa.add(qa, BooleanClause.Occur.SHOULD);
            bqa.add(qaph, BooleanClause.Occur.SHOULD);

        }

        finalQuery.add(bqa.build(), BooleanClause.Occur.SHOULD);

        for (String e : entities) {
            Query qe = qpe.parse(QueryParserBase.escape(e));
            //qe.setBoost(0.9f);
            Query qeph = qpe.parse("\"" + QueryParserBase.escape(e) + "\"");
            //qeph.setBoost(1.7f);
            finalQuery.add(qe, BooleanClause.Occur.SHOULD);
            finalQuery.add(qeph, BooleanClause.Occur.SHOULD);
        }

        //search per value
        for (double v: values){
            Query numericQuery = LegacyNumericRangeQuery.newDoubleRange("value", v*0.8, v*1.2, true, true);
            finalQuery.add(numericQuery, BooleanClause.Occur.SHOULD);
        }


        //search for the whole range
        Query doubleRangeQuery = DoublePoint.newRangeQuery("value", min, max);

        finalQuery.add(doubleRangeQuery, BooleanClause.Occur.MUST);


        //search columnWise
        for(int i =0;i<10;i++){
            Query columnWiseQuery = DoublePoint.newRangeQuery(i+"", min, max);
            finalQuery.add(columnWiseQuery, BooleanClause.Occur.SHOULD);

        }

        TopDocs td = searcher.search(finalQuery.build(),n);

        ScoreDoc[] scoreDocs = td.scoreDocs;

        for(int i =0;i<scoreDocs.length;i++){
            int docID = scoreDocs[i].doc;
           System.out.println( searcher.explain(finalQuery.build(),docID).toString());
        }



        return td;
    }

    public Document getDocument(int docId)
            throws IOException {
        return searcher.doc(docId);
    }
}