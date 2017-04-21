package webreduce.indexing;

import com.google.common.base.Joiner;
import com.google.common.net.InternetDomainName;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.martiansoftware.jsap.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.document.DoublePoint;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import webreduce.cleaning.CustomAnalyzer;
import webreduce.data.Dataset;
import webreduce.iterator.WebreduceIterator;
import webreduce.typing.DataType;
import webreduce.typing.Types;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fusesource.leveldbjni.JniDBFactory.bytes;
import static org.fusesource.leveldbjni.JniDBFactory.factory;

public class Indexer extends WebreduceIterator {

	/* command line flags: */
	protected static final String CORPUS_PATH = "corpusPath";
	protected static final String OUTPUT_PATH = "outputPath";

	// index only tables that contained <th> tags in the original HTML
	protected static final String HEADERED_TABLES_ONLY = "headeredTablesOnly";

	protected static final String NUMERICAL_RANGE_INDEXING = "numericalRangeIndexing";
	// store the original data as a stored field in the index
	protected static final String STORE_FULL_RESULT = "storeFullResult";
	// store the original data as in a leveldb
	protected static final String STORE_FULL_RESULT_IN_LEVELDB = "storeFullResultInLevelDB";

	// activate preprocessing (analysis of title, terms and, column typing, domain from url extraction)
	protected static final String PREPROCESSING = "preprocessing";

	protected final JSAPResult config;
	protected final Analyzer analyzer = new CustomAnalyzer();
	protected final Joiner joiner;
	protected final IndexWriter writer;

	private Pattern urlSplitPattern = Pattern.compile("[/_-]|%20");
	private DB leveldb;

	private AtomicLong nextKey = new AtomicLong(0);



	public Indexer(JSAPResult config) throws IOException {
		String outputPath = config.getString(OUTPUT_PATH, "<NONE>");
        if (!outputPath.equals("<NONE>")) {
            SimpleFSDirectory idx_dir = new SimpleFSDirectory(Paths.get(outputPath));

			IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
			this.writer = new IndexWriter(idx_dir, indexConfig);
		}  else {
			this.writer = null;
		}

		this.joiner = Joiner.on(" ").skipNulls();
		this.config = config;

		Options options = new Options();
		options.createIfMissing(true);
		leveldb = factory.open(new File(outputPath, "leveldb"), options);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, JSAPException {
		JSAP jsap = new JSAP();
		jsap.registerParameter(new Switch(HEADERED_TABLES_ONLY).setLongFlag(
				HEADERED_TABLES_ONLY).setShortFlag('h'));
		jsap.registerParameter(new Switch(PREPROCESSING).setLongFlag(
				PREPROCESSING).setShortFlag('r'));
		jsap.registerParameter(new Switch(STORE_FULL_RESULT).setLongFlag(
				STORE_FULL_RESULT).setShortFlag('s'));
		jsap.registerParameter(new Switch(STORE_FULL_RESULT_IN_LEVELDB).setLongFlag(
				STORE_FULL_RESULT_IN_LEVELDB).setShortFlag('l'));
		jsap.registerParameter(new UnflaggedOption(CORPUS_PATH)
				.setRequired(true));
		jsap.registerParameter(new UnflaggedOption(OUTPUT_PATH)
				.setRequired(true));
		JSAPResult config = jsap.parse(args);

		if (!config.success()) {
			System.err.println();
			System.err.println("Usage: java " + Indexer.class.getName());
			System.err.println("                " + jsap.getUsage());
			System.err.println();
			System.exit(1);
		}

		Indexer fi = new Indexer(config);
		fi.iterate(config.getString(CORPUS_PATH));
	}

	@Override
	protected void process(String key, String value) throws IOException {
		// deserialize the json formatted data
		Dataset er = Dataset.fromJson(value);
		processDataset(er);
		return;
	}

    protected void processDataset(Dataset er) throws IOException {
		String url = er.getUrl();
        System.out.println("Er: " + er.getUrl());
		if (!(url.contains(".com") || url.contains(".net")
				|| url.contains(".org") || url.contains(".uk"))) {
			return;
		}

		if (config.getBoolean(HEADERED_TABLES_ONLY) && !er.getHasHeader()) {
			return;
		}

		String[][] cols = er.getRelation();
		int numCols = cols.length;
		String[] attributes = new String[numCols];

		for (int j = 0; j < numCols; j++) {
			attributes[j] = cols[j][0];
		}

		int numRows = cols[0].length;

		StringBuilder sb = new StringBuilder();
		for (String[] col : cols) {
			for (int p = 1; p < numRows; p++) {
				sb.append(col[p]);
				sb.append(" ");
			}
		}

		StringBuilder sk = new StringBuilder();
		for (int p = 0; p < numRows; p++) {
			sk.append(cols[0][p]);
			sk.append(" ");
		}

		StringBuilder sa = new StringBuilder();
		for (String s : attributes) {
			sa.append(s);
			sa.append(" ");
		}

		String[] terms = er.getTermSet();
		if (terms == null)
			terms = new String[] {};
		StringBuilder st = new StringBuilder();
		for (String s : terms) {
			st.append(s);
			st.append(" ");
		}

		String title = er.getTitle();
		if (title == null)
			title = er.getPageTitle();
		if (title == null)
			title = "";

		String attributesStr = sa.toString();
		String entitiesStr = sb.toString();
		String keysStr = sk.toString();
		String termsStr = st.toString();

		Document doc = new Document();

		doc.add(new TextField("url", er.getUrl(), Field.Store.NO));
		doc.add(new TextField("title", title, Field.Store.NO));
		doc.add(new TextField("attributes", attributesStr, Field.Store.NO));
		doc.add(new TextField("entities", entitiesStr, Field.Store.NO));
		doc.add(new TextField("terms", termsStr, Field.Store.NO));
		doc.add(new TextField("keys", keysStr, Field.Store.NO));
		doc.add(new StringField("tableType", er.getTableType().name(), Field.Store.YES));
		if (config.getBoolean(STORE_FULL_RESULT)) {
			doc.add(new StoredField("full_result", preprocess(er)));
		}
		if (config.getBoolean(STORE_FULL_RESULT_IN_LEVELDB)) {
			long key = nextKey.incrementAndGet();
			doc.add(new StoredField("document_id", key));
			String preprocessedResult = preprocess(er);
			synchronized (leveldb) {
				leveldb.put(Longs.toByteArray(key), bytes(preprocessedResult));
			}
		}


        String[] columnTypes = er.getColumnTypes();

        // System.out.println("URL: " + er.getUrl());
        for (int i = 0; i < columnTypes.length; i++) {
            String columnType = columnTypes[i];
            switch (columnType) {
                case "Double": {//todo
                    for (String value : er.relation[i]) {

                        double dvalue = coerceToNumber(value);
                        //System.out.println(value + " - " + dvalue);

                        if (dvalue != Double.MAX_VALUE) {
                            doc.add(new DoublePoint("value", dvalue));
                        }
                    }
                }
                break;
                case "Integer": {

                    for (String value : er.relation[i]) {

                        double dvalue = coerceToNumber(value);
                        // System.out.println(value + " - " + dvalue);
                        if (dvalue != Double.MAX_VALUE) {
                            doc.add(new DoublePoint("value", dvalue));
                        }
                    }
                }
                break;
                case "Long": {
                    for (String value : er.relation[i]) {
                        double dvalue = coerceToNumber(value);
                        // System.out.println(value + " - " + dvalue);
                        if (dvalue != Double.MAX_VALUE) {
                            doc.add(new DoublePoint("value", dvalue));
                        }
                    }
                }
                break;
                case "Currency": { //todo
                    for (String value : er.relation[i]) {
                        double dvalue = coerceToNumber(value);
                        //System.out.println(dvalue);
                        if (dvalue != Double.MAX_VALUE) {
                            doc.add(new DoublePoint("value", dvalue));
                        }
                    }
                }
                break;


            }
        }

        indexNumericColumn(er, doc);




        writer.addDocument(doc);
	}

	@Override
	protected void finishProcessFile(File f) throws IOException {
	}

	@Override
	protected void close() throws IOException, InterruptedException {
		super.close();
		writer.close();
		leveldb.close();
	}

	protected String preprocess(webreduce.data.Dataset ds) {
		/* example of some useful preprocessing done while indexing */
		try {
			ds.domain = InternetDomainName.from(new URI(ds.url).getHost())
					.topPrivateDomain().toString();
		} catch (Exception e) {
			ds.domain = null;
		}
		ds.titleTermSet = analyze(ds.title);
		ds.urlTermSet = analyze(joiner.join(splitURL(ds.url)));
		ds.columnTypes = new String[ds.relation.length];
		for (int i = 0; i < ds.relation.length; i++)
			ds.columnTypes[i] = Types.columnType(Arrays.copyOfRange(
					ds.relation[i], 1, ds.relation[i].length)).Name;
		return ds.toJson();
	}

	protected String[] analyze(String s) {
		List<String> result = new ArrayList<String>();
		TokenStream stream;
		try {
			stream = analyzer.tokenStream(null, new StringReader(s));
			stream.reset();
			while (stream.incrementToken())
				result.add(stream.getAttribute(CharTermAttribute.class)
						.toString());
			stream.end();
			stream.close();
		} catch (IOException e) {
			// what to do?
		}
		return result.toArray(new String[] {});
	}

	protected String[] splitURL(String url) {
		URL u;
		String s;
		try {
			u = new URL(url);
			s = u.getPath();
			if (s.endsWith(".htm") || s.endsWith(".html")) {
				s = s.substring(0, s.lastIndexOf("."));
			}
		} catch (MalformedURLException e) {
			s = url;
		}
		return urlSplitPattern.split(s);
	}

    //added by ahmedov
    protected  void indexNumericColumn(Dataset er, Document doc){
        String [][] relation = er.relation;
        //AtomicInteger i = new AtomicInteger(0);
        //SynchronizedCounter s = new SynchronizedCounter();
        ThreadLocal<Integer> f = new ThreadLocal<Integer>();
        f.set(0);
        for(String [] column: relation){
            DataType type = Types.columnType(column);
            if (type==DataType.EMAIL || type ==DataType.DATETIME || type ==DataType.NONE || type ==DataType.STRING || type == DataType.URL ){
                continue;
            }
            else{
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                for(String value: column){
                    double d = coerceToNumber(value);
                    if(d!=Double.MAX_VALUE){
                        if(min>d) min = d;
                        if(max<d) max = d;
                        doc.add(new DoublePoint(""+f.get()));
                    }
                }
                //i.incrementAndGet();
                f.set(f.get()+1);
                doc.add(new DoublePoint("minmax", min));
                doc.add(new DoublePoint("minmax", max));
                //s.increment();
            }

        }


    }

    protected double coerceToNumber(String v) {

        double THOUSAND = 1000;
        double MILLION = 1000000;
        double BILLION = 1000000000;

        Matcher matcher = constants.toDouble.matcher(v);

        Set numbers = new HashSet<String>();
        while (matcher.find()) {
            numbers.add(matcher.group());
            //System.out.println("Matched to: " + matcher.group());
        }
        //System.out.println(numbers.size());

        if (numbers.isEmpty()) {
            return Double.MAX_VALUE;
        }
        String s = findMaxSize(numbers);

        if (s.length() < ((double) v.length()) * 0.5 && !v.startsWith(s))
            return Double.MAX_VALUE;

        double d = Double.MAX_VALUE;
        // System.out.println("matched string: " + s);
        if (constants.toDoubleK.matcher(s).matches()) {
            return Doubles.tryParse(s.trim().replaceAll("[^\\d\\.-]+", "")).doubleValue() * THOUSAND;
        } else if (constants.toDoubleM.matcher(s).matches()) {
            return Doubles.tryParse(s.trim().replaceAll("[^\\d\\.-]+", "")).doubleValue() * MILLION;

        } else if (constants.toDoubleB.matcher(s).matches()) {
            return Doubles.tryParse(s.trim().replaceAll("[^\\d\\.-]+", "")).doubleValue() * BILLION;

        } else {
             System.out.println("Didn't match anything");
            return Doubles.tryParse(s.trim().replaceAll("[^\\d\\.-]+", "")).doubleValue();

        }

    }

    protected String findMaxSize(Set numbers) {
        String s = new String();
        int max = 0;
        Iterator it = numbers.iterator();
        while (it.hasNext()) {

            String nextString;
            nextString = (String) it.next();
            int size = nextString.length();
            if (size > max) {
                s = nextString;
            }
        }
        return s;
    }


}
