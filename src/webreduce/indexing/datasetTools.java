/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webreduce.indexing;

import cern.colt.matrix.DoubleMatrix2D;
import com.opencsv.CSVReader;
import org.apache.commons.lang3.ArrayUtils;
import webreduce.data.Dataset;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author ahmedov
 */
public class datasetTools {

    public static List<String> result = Collections
            .synchronizedList(new ArrayList<String>());

    private static final ThreadLocal<List<String>> localResult = new ThreadLocal<List<String>>() {
        @Override
        protected List<String> initialValue() {
            return new ArrayList<String>();
        }
    };

    private static String indexDir = new String();
    private static String query = new String();
    private static String field = new String();
    private static String csvFileLocation = new String();

    protected ArrayList<String[]> getLocalDataset(String csvFileLocation) throws FileNotFoundException, IOException {
        List<String> csvDataset = new ArrayList<String>();
        CSVReader reader = new CSVReader(new FileReader(csvFileLocation));
        ArrayList csvLines = (ArrayList) reader.readAll();

        return csvLines;

    }

    public static int[] generateRandomNumbers(int size, int max) {
        Random rng = new Random(); // Ideally just create one instance globally
// Note: use LinkedHashSet to maintain insertion order
        Set<Integer> generated = new LinkedHashSet<Integer>();
        while (generated.size() < size) {
            Integer next = rng.nextInt(max-1) + 1;
            // As we're adding to a set, this will automatically do a containment check
            generated.add(next);
        }
        List<Integer> genList = new ArrayList<>();
        genList.addAll(generated);
        Collections.shuffle(genList);
        Integer rowIDs[] = new Integer[genList.size()];
        return ArrayUtils.toPrimitive(genList.toArray(rowIDs));
    }


    public static String[][] listToArray(List<String[]> input) {
        List<String[]> list = new ArrayList<String[]>(input);
        String[][] matrix = list.toArray(new String[0][]);
        return matrix;
    }

    public static List<String []> ArrayToList(String[][] input) {
        List<String[]> list = new ArrayList<String[]>();
        for(String[] a: input){
            list.add(a);
        }
            return list;
    }

    


    public static void printMatrix(DoubleMatrix2D matrix) {

        String format = "0.000";
        DecimalFormat df = new DecimalFormat(format);

        for (int i = 0; i < matrix.rows(); i++) {
            for (int j = 0; j < matrix.columns(); j++) {
                System.out.print(df.format(matrix.get(i, j)) + " ");
            }
            System.out.println();
        }
    }

    protected String[][] getMissingDataSubset(String[][] dataset) {
        boolean missingMatrix[][] = this.getTrueFalseMatrix(dataset);
        String[][] missingDataSubset = null;
        int row = missingMatrix.length;
        int col = missingMatrix[0].length;
        int j = 0;
        for (int i = 0; i < row; i++) {
            boolean rows[] = missingMatrix[i];
            for (boolean cell : rows) {
                if (cell == false) {
                    missingDataSubset[j++] = dataset[i];
                    break;
                }
            }
        }
        return missingDataSubset;
    }

    //return the first column of the dataset
    public static List<String> getEntities(Dataset er) {
        return Arrays.asList(er.getRelation()[0]);
    }

    //return the entities of the column 
    protected List<String> getEntities(String columnName) {
        return null;
    }

    public static boolean[][] getTrueFalseMatrix(Dataset er) {
        boolean[][] tfMatrix = new boolean[er.getRelation().length][er.getNumCols()];
        int i = 0;
        int j = 0;
        String[][] dataset = transpose(er.getRelation());
        for (String[] row : dataset) {
            for (String cell : row) {
                System.out.println("value" + dataset[i][j] + "" + dataset[i][j].isEmpty());
                tfMatrix[i][j++] = !dataset[i][j++].isEmpty();
            }
            i++;
        }
        return tfMatrix;
    }

    public static boolean[][] getTrueFalseMatrix(String[][] dataset) {
        boolean[][] tfMatrix = new boolean[dataset.length][dataset[0].length];
        int i = 0;
        int j = 0;
        for (String[] row : dataset) {
            for (String cell : row) {
                System.out.println("value" + dataset[i][j] + "" + dataset[i][j].isEmpty());
                tfMatrix[i][j++] = !dataset[i][j++].isEmpty();
            }
            i++;
        }
        return tfMatrix;
    }

    public static boolean[][] getTrueFalseMatrix(List<String[]> dataset) {
        System.out.println("row: " + dataset.size());
        System.out.println("column: " + dataset.get(0).length);
        boolean[][] tfMatrix = new boolean[dataset.size()][dataset.get(0).length];
        int i = 0;
        int j = 0;
        for (String[] row : dataset) {
            j = 0;
            for (String cell : row) {
                System.out.println("value " + dataset.get(i)[j] + " " + dataset.get(i)[j].isEmpty());
                tfMatrix[i][j] = !dataset.get(i)[j].isEmpty();
                j++;
            }
            i++;
        }

        return tfMatrix;
    }

    //print the True-False matrix of the local dataset
    public static void printTFMatrix(boolean[][] tfMatrix) {
        System.out.println(Arrays.deepToString(tfMatrix));
    }

    public static String[][] transpose(String[][] dataset) {
        int i, j;
        int col = dataset.length;
        int row = dataset[0].length;
        //   System.out.println("col: " + col + " row: " + row);
        String[][] datasetOut = new String[row - 1][col];
        for (i = 0; i < row - 1; i++) {
            for (j = 0; j < col; j++) {
                datasetOut[i][j] = dataset[j][i + 1];
            }
        }
        return datasetOut;
    }

    public static String[][] transpose(List<String[]> dataset) {
        int i, j;
        int col = dataset.size();
        int row = dataset.get(0).length;
        System.out.println("col: " + col + " row: " + row);
        String[][] datasetOut = new String[row][col];
        for (i = 0; i < row; i++) {
            for (j = 0; j < col; j++) {
                datasetOut[i][j] = dataset.get(j)[i];
            }
        }
        return datasetOut;
    }
    /*    static <T> List<List<T>> transpose(List<List<T>> table) {
     List<List<T>> ret = new ArrayList<List<T>>();
     final int N = table.get(0).size();
     for (int i = 0; i < N; i++) {
     List<T> col = new ArrayList<T>();
     for (List<T> row : table) {
     col.add(row.get(i));
     }
     ret.add(col);
     }
     return ret;
     }*/

   /* public static void printDataset(String[][] dataset) {
        String[] header = dataset[0];

        List<String[]> dataList = new ArrayList(Arrays.asList(dataset));

        dataList.remove(0);
        int i = 0;
        String data[][] = new String[dataList.size()][dataList.get(0).length];
        for (String[] obj : dataList) {
            data[i++] = obj;
        }
        ASCIITable.getInstance().printTable(header, data);
    }

    public static void printDataset(List<String[]> csvDataset) {
        String[] header = csvDataset.get(0);
        csvDataset.remove(0);
        String[][] data = listToArray(csvDataset);
        ASCIITable.getInstance().printTable(header, data);
    }

    public static void printDataset(Dataset er) {
        String[] header = er.getAttributes();
        String[][] data = er.getRelation();
        ASCIITable.getInstance().printTable(header, transpose(data));
    }*/

    public static Map<Integer, Integer> sortByComparator(Map<Integer, Integer> unsortMap) {

        // Convert Map to List
        List<Map.Entry<Integer, Integer>> list
                = new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1,
                    Map.Entry<Integer, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
        for (Iterator<Map.Entry<Integer, Integer>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Integer, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

}
