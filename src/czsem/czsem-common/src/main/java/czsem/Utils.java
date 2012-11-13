package czsem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.uncommons.maths.combinatorics.PermutationGenerator;

public class Utils {

	public static <TypeName> String listToStr(Collection<TypeName> c, String delim)
	{
		StringBuilder sb = new StringBuilder();
		int a=0;
		for (Iterator<TypeName> i = c.iterator(); i.hasNext(); )
		{
			TypeName s = i.next(); 
		    sb.append(s);
		    if (++a >= c.size()) break;
		    sb.append(delim);
		}
		return sb.toString();
	}

	public static String[] arrayConcatenate(String[] first, String[] second)
	{
		String [] ret = new String[first.length + second.length];
		
		System.arraycopy(first, 0, ret, 0, first.length);
		System.arraycopy(second, 0, ret, first.length, second.length);
		return ret;		
	}

	public static <ElementType> Iterable<List<ElementType>> allPermutations(final List<ElementType> collection)
	{
		return new Iterable<List<ElementType>>() {

			@Override
			public Iterator<List<ElementType>> iterator() {
				return new PermutationGenerator<ElementType>(collection).iterator();
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <RetType> List<RetType> objectArrayToGenericList(Object array)
	{
		if (array == null) return null;
		return Arrays.asList((RetType[]) array);
	}
	
	public static <ElementType> Evidence<ElementType>[] createRandomPermutation(Collection<ElementType> collection)
	{
		Random rand = new Random();
		
		@SuppressWarnings("unchecked")
		Evidence<ElementType>[] ret = new Evidence[collection.size()];
		
		int i = 0;
		for (Iterator<ElementType> iterator = collection.iterator(); iterator.hasNext();i++)
		{
			ElementType element = iterator.next();
			ret[i] = new Evidence<ElementType>(element, rand.nextInt());						
		}
				
		Arrays.sort(ret);
		
		return ret;
	}

	public static int [] createRandomPermutation(int length)
	{
		Integer [] input = new Integer[length];
		int [] ret = new int[length];
		for (int i = 0; i < input.length; i++) {
			input[i]=i;
		}
		
		Evidence<Integer>[] perm = createRandomPermutation(Arrays.asList(input));
		for (int i = 0; i < input.length; i++) {
			ret[i]=perm[i].element;
		}
		
		return ret;		
	}


	public static class Evidence<EvidenceElement> implements Comparable<Evidence<EvidenceElement>>
	{
		public Evidence(EvidenceElement doc, int random) {
			this.element = doc;
			this.random = random;
		}
		public EvidenceElement element;
		int random;
		
		@Override
		public int compareTo(Evidence<EvidenceElement> o)
		{
			return new Integer(random).compareTo(o.random);
		}
	}
	
	public static String findAvailableFileName(String destFileURI)
	{
	    String destFileName = destFileURI.substring(0,destFileURI.lastIndexOf("."));
	    String destFileExt = destFileURI.substring(destFileURI.lastIndexOf(".")+1);
	    int count = 1;      
	    File f;
	    while ((f=new File(destFileURI)).exists())
	    {
	        destFileURI=destFileName+"("+(count++)+")"+"."+destFileExt;
	    }            
	    String fName = f.getName();
	    String fPath = f.getParent();
	    destFileURI = destFileURI.replaceAll(" ", "_");
	    // Now we need to check if given file name is valid for file system, and if it isn't we need to convert it to valid form
	    if (!(testIfFileNameIsValid(destFileURI))) {
	        List<String> forbiddenCharsPatterns = new ArrayList<String>();
	        forbiddenCharsPatterns.add("[:]+"); // Mac OS, but it looks that also Windows XP
	        forbiddenCharsPatterns.add("[\\*\"/\\\\\\[\\]\\:\\;\\|\\=\\,]+");  // Windows
	        forbiddenCharsPatterns.add("[^\\w\\d\\.]+");  // last chance... only latin letters and digits
	        for (String pattern:forbiddenCharsPatterns) {
	            String nameToTest = fName;
	            nameToTest = nameToTest.replaceAll(pattern, "_");
	            destFileURI=fPath+"/"+nameToTest;
	            count=1;
	            destFileName = destFileURI.substring(0,destFileURI.lastIndexOf("."));
	            destFileExt = destFileURI.substring(destFileURI.lastIndexOf(".")+1);
	            while ((f=new File(destFileURI)).exists()) {
	                destFileURI=destFileName+"("+(count++)+")"+"."+destFileExt;
	                }
	                if (testIfFileNameIsValid(destFileURI)) break;
	        }
	    }         
	    return destFileURI;
	}

	private static boolean testIfFileNameIsValid(String destFileURI) {
		    boolean valid = false;
		    try {
		        File candidate = new File(destFileURI);                
	//	        String canonicalPath = candidate.getCanonicalPath();                
		        boolean b = candidate.createNewFile();
		        if (b) {
		            candidate.delete();
		        }
		        valid = true;
		    } catch (IOException ioEx) { }
		    return valid;
		}

	public static File URLToFile(URL url) throws IOException, URISyntaxException
	{
		return new File(url.toURI());		
	}

	public static URL filePathToUrl(String filepath) throws MalformedURLException
	{
		return new File(filepath).toURI().toURL();
	}

	public static String URLToFilePath(URL url) throws IOException, URISyntaxException
	{
		return URLToFile(url).getCanonicalPath();		
	}

	public static Set<Integer> copyArrayToSet(int[] src, Set<Integer> dest)
	{
		if (dest == null) dest = new HashSet<Integer>(src.length);
		for (int i = 0; i < src.length; i++) {
			dest.add(src[i]);
		}
		
		return dest;
	}
	
	public static void copyArrayToDepthMapExceptListed(int[] src, int depth, Map<Integer, Integer> dest, Set<Integer> except)
	{
		for (int i = 0; i < src.length; i++)
		{
			if (!except.contains(src[i]))
			{			
				Integer prew = dest.put(src[i], depth);
				if (prew != null && prew < depth) dest.put(src[i], prew);
			}
		}
	}


	public static Set<String> setFromArray(String[] array)
	{		
		return setFromList(Arrays.asList(array));
	}

	public static Set<String> setFromList(List<String> list)
	{		
		return new HashSet<String>(list);
	}
	
	public static int[] intArrayFromCollection(Collection<Integer> l)
	{
		int[] ret = new int[l.size()];
		Iterator<Integer> i = l.iterator();
		for (int a=0; a<ret.length; a++)
			ret[a] = i.next(); 
		return ret;
	}
	
	/**
	 * Checks to see if a specific port is available.
	 *
	 * @param port the port to check for availability
	 * @author http://mina.apache.org/
	 */
	public static boolean portAvailable(int port)
	{
	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}
	
	
	public static class StopRequestDetector
	{
		public Boolean stop_requested = false;
		
		public void startDetector()
		{
			Thread terminate_request_detector = new Thread() {
				@Override
				public void run()
				{
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					String input = "";
					do
					{
						try {
							input = in.readLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					} while (! input.equals("stop") && ! stop_requested);
					
					System.err.println("stop requsted!");
					stop_requested = true;
				}			
			};
			
			terminate_request_detector.start();
		}
	}
	
	public static String fileNameWithoutExtensions(File file)
	{
		int index = file.getName().indexOf('.');
		if (index>0&& index < file.getName().length())
		{
			return file.getName().substring(0, index);
		}
		return file.getName();
	}

	public static void main(String [] args) throws URISyntaxException, IOException
	{
		System.err.println(Arrays.toString(createRandomPermutation(10)));
		System.err.println(fileNameWithoutExtensions(new File("pok.txt.tar.gz")));
		System.err.println(fileNameWithoutExtensions(new File("pok.txt")));
		System.err.println(fileNameWithoutExtensions(new File("pok.")));
		System.err.println(fileNameWithoutExtensions(new File("pok")));
		
		Integer [] perm = {1,2,3}; 
		for (List<Integer> l : allPermutations(Arrays.asList(perm)))
		{
			System.err.println(l);
		}
	}

	public static void increaseCountingMap(Map<String, Integer> map, String key, int count)
	{
		Integer last_val = map.get(key);
		map.put(key, last_val == null ? count : count + last_val);

	}

	public static void mkdirsIfNotExists(String path) {
		File f = new File(path);
		if (! f.exists())
		{
			f.mkdirs();
		}		
	}

	public static String strTrimTo(String str, int maxLength) {
		return str.substring(0, Math.min(maxLength, str.length()));
	}
	
	public static void serializeToFile(Object obj, String fileName) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		out.writeObject(obj);
		out.close();
	}

	public static Object deserializeFromFile(String fileName) throws IOException, ClassNotFoundException {
		return deserializeFromStram(new FileInputStream(fileName));
	}

	public static Object deserializeFromStram(InputStream inputStram) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(inputStram));
		Object ret = in.readObject();
		in.close();
		return ret;
	}

	public static void loggerSetup(Level logLevel)
	{
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(logLevel);
	    logger.removeAllAppenders();
		BasicConfigurator.configure();		
	}

}
