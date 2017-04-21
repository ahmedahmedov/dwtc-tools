package webreduce.iterator;

import com.google.common.io.LineReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/* Most interesting subclass of ParallelItereator, subclass this to create custom iterators that work 
 * with the DWTC dataset.
 */
public abstract class WebreduceIterator extends ParallelIterator {

	@Override
	public void iterate(String inPath) throws IOException, InterruptedException {
		File inDir = new File(inPath);
		Collection<File> files = FileUtils.listFiles(inDir, new IOFileFilter() {
			@Override
			public boolean accept(File pathfile, String pathname) {
				return pathname.endsWith(".gz");
			}

			@Override
			public boolean accept(File pathfile) {
				return pathfile.getName().endsWith(".gz");
			}
		}, TrueFileFilter.INSTANCE);
		start();
		for (final File f : files) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					Reader reader = null;
					try {
						reader = new InputStreamReader(
								new GZIPInputStream(new BufferedInputStream(
										new FileInputStream(f))));
						LineReader lReader = new LineReader(reader);

						// do the actual processing
						while (true) {
							String l = lReader.readLine();
							//System.out.println(l);
							if (l == null)
								break;
							// process webtable
							String[] splitLine = l.split("\t");
							String key, value;
							if (splitLine.length > 1) {
								key = splitLine[0];
								value = splitLine[1];
							} else {
								key = "";
								value = splitLine[0];
							}

							//System.out.println("key: "+ key + " value: " + value);
							process(key, value);
							finishedItem();
						}
						finishProcessFile(f);
						reader.close();
					} catch (ZipException e) {
						System.err.println("Corrupt file: " + f.toString());
						reportCorrputFile();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
					}

				}
			});
		}
		close();
	}

	protected abstract void process(String key, String value)
			throws IOException;

	protected abstract void finishProcessFile(File f) throws IOException;

}
