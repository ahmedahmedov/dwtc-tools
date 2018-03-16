/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webreduce.indexing;

import com.google.common.collect.*;
import com.google.common.collect.Multiset;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import webreduce.cleaning.CustomAnalyzer;
import webreduce.data.Dataset;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author ahmedov
 */
public class luceneSearcher {
    String indexDir = new String();
    int numberOfResults;
    String[] entities;
    String[] attributes;
    List<Dataset> resultLists = new ArrayList<>();

    private static String ATTRIBUTES_FIELD = "attributes";
    private static String ENTITIES_FIELD = "entities";
    private static String TABLE_TYPE_FIELD = "tableType";

    public luceneSearcher(String indexDir, int numberOfResults, String[] entities, String[] attributes) {
        this.indexDir = indexDir;
        this.numberOfResults = numberOfResults;
        this.entities = entities;
        this.attributes = attributes;
    }

    public List<Dataset> search() throws IOException {

        List<Dataset> resultList;
        resultList = new ArrayList<>();

        BooleanQuery.Builder finalQueryBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder entityQueryBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder attributeQueryBuilder = new BooleanQuery.Builder();


        //gives me queries
        QueryParser qpa = new QueryParser(ATTRIBUTES_FIELD, new CustomAnalyzer());

        QueryParser qpe = new QueryParser(ENTITIES_FIELD, new CustomAnalyzer());

        //QueryWrapperFilter queryFilter = new QueryWrapperFilter(query);
        //CachingWrapperFilter cachingFilter = new CachingWrapperFilter(queryFilter);

        //CachingWrapperQuery typeFilterR = new CachingWrapperFilter(new TermsFilter(new Term(TABLE_TYPE_FIELD, "RELATION")));

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));

        IndexSearcher searcher = new IndexSearcher(reader);

        QueryBuilder queryBuilder = new QueryBuilder(new CustomAnalyzer());


        System.out.println("Attributes: \n"+ Arrays.deepToString(attributes));
        System.out.println("Entities: \n"+ Arrays.deepToString(entities));

        //add attributes one by one
        for (String a : attributes) {

            Query qa;
            try {
                qa = qpa.parse("\"" + a + "\"");
                attributeQueryBuilder.add(qa, BooleanClause.Occur.SHOULD);

            } catch (ParseException ex) {
            }
        } //end of for loop
        //remove null

        HashSet<String> entitySet;
        entitySet = new HashSet<>(Arrays.asList(entities));
        entitySet.remove(null);

        entities = entitySet.toArray(new String[entitySet.size()]);

        System.out.println("Entities after null removal \n"+ Arrays.deepToString(entities));

        Multiset<Integer> docNoCount;
        docNoCount = HashMultiset.create();

        //Take only top 50 entities;
        String[] entities50 = new String[50];
        System.arraycopy(entities, 0, entities50, 0, 50);

        System.out.println(Arrays.deepToString(entities50));

        for (String e : entities50) {
            System.out.println(e);
            if (e == null) {
                continue;
            }
            Query qe;
            try {
                qe = qpe.parse(QueryParserBase.escape(e));
                //Query qeph = qpe.parse("\"" + QueryParserBase.escape(e) + "\"");
                finalQueryBuilder.add(qe, BooleanClause.Occur.MUST); //add entities boolean query
                finalQueryBuilder.add(attributeQueryBuilder.build(), BooleanClause.Occur.MUST); //add attributes query

                TopDocs td = searcher.search(finalQueryBuilder.build(), numberOfResults * 10);
                for (ScoreDoc sd : td.scoreDocs) {
                    int docNo = sd.doc;
                    docNoCount.add(docNo);
                }
            } catch (ParseException ex) {
            }

            System.out.println("Top Doc id: \n"+Multisets.copyHighestCountFirst(docNoCount).entrySet().iterator().next().getElement());

        }

        //Sort the returned docs by their frequency and store it in docNoSorted
        ImmutableMultiset<Integer> docNoSorted = Multisets.copyHighestCountFirst(docNoCount);
        //Get the entry set of the frequency ordered document set
        ImmutableSet<Multiset.Entry<Integer>> entrySet = Multisets.copyHighestCountFirst(docNoCount).entrySet();
        //Get the iterator for the sorted entry set
        UnmodifiableIterator<Multiset.Entry<Integer>> iterator = entrySet.iterator();

        int bestDocId = iterator.next().getElement();
        System.out.println("first count" + iterator.next());

        //
        Set<Integer> elementSet = docNoSorted.elementSet();
        Integer next = elementSet.iterator().next();
        System.out.println("Most frequent document id: " + next);
        int resultSetSize;
        resultSetSize = docNoSorted.elementSet().size();

        System.out.println("Entry Set Size: " + resultSetSize + " Cardinality: " + docNoSorted.size());

        Set<Integer> elementSet1 = Multisets.copyHighestCountFirst(docNoSorted).elementSet();

        List<Integer> t = new ArrayList<Integer>(elementSet1);

        List<Integer> subList = t.subList(0, numberOfResults);
        //ArrayList subArrayList = new ArrayList(subList);
        Iterator<Integer> subListIterator = subList.iterator();

        //we have all the web table doc IDs
        //We snould take
        while (subListIterator.hasNext()) {
            int docID = subListIterator.next();
            Document doc;
            doc = searcher.doc(docID);
            String jsonString = doc.get("full_result");
            Dataset er = Dataset.fromJson(jsonString);
            resultList.add(er);
        }
        return resultList;
    }


}
