package czsem.ILP;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import czsem.net.NetgraphServerComunication;
import czsem.net.NetgraphProtocolConnection.SepartorCahrs;
import czsem.utils.NetgraphQuery;
import czsem.utils.SimpleXMLQueryProcessor;
import czsem.utils.NetgraphQuery.ResultProcessor;

public abstract class DataMaker implements ResultProcessor {
	public static final int[] PRINT_ATTRIBUTES =	{
//		0, //atree.rf
//		1, //compl.rf
//		2, //coref_gram.rf
//		3, //coref_special
//		4, //coref_text.rf
//		5, //deepord
		6, //functor
//		7, //gram/aspect
//		8, //gram/degcmp
//		9, //gram/deontmod
//		10, //gram/dispmod
//		11, //gram/gender
//		12, //gram/indeftype
//		13, //gram/iterativeness
//		14, //gram/negation
//		15, //gram/number
//		16, //gram/numertype
//		17, //gram/person
//		18, //gram/politeness
//		19, //gram/resultative
		20, //gram/sempos
//		21, //gram/tense
//		22, //gram/verbmod
//		23, //id
//		24, //is_dsp_root
//		25, //is_generated
//		26, //is_member
//		27, //is_name_of_person
//		28, //is_parenthesis
//		29, //is_state
//		30, //nodetype
//		31, //quot/set_id
//		32, //quot/type
//		33, //sentmod
//		34, //subfunctor
		35, //t_lemma
//		36, //tfa
		37, //val_frame.rf
//		38, //sentence
//		39, //a/ref_type
//		40, //a/afun
//		41, //a/is_member
//		42, //m/form
//		43, //m/lemma
//		44, //m/tag
//		45, //w/token
//		46, //w/no_space_after
//		47, //a/ord
//		48, //a/parent_id
//		49, //hide				
		};

	
	
	protected PrintStream tree_out = System.out;
//	private String query_string = null;
	private String searchPath = null;
	protected NetgraphQuery current_query = new NetgraphQuery();

	
	public DataMaker()
	{}
	
	public DataMaker(String output_file) throws FileNotFoundException, UnsupportedEncodingException
	{
		tree_out = new PrintStream(output_file, "UTF-8");
	}
	
	public void performOutput() throws Exception
	{
		performOutput(this);
	}
	
	public void performOutput(ResultProcessor rp) throws Exception
	{
		NetgraphServerComunication nc = new NetgraphServerComunication();
		nc.openConnection("localhost", 2000);
		nc.login("dedek", "50eb3b47fbee4df59eaef6368261063b");
		
		
		if (getSearchPath() != null)
			nc.setSearchPathAndInitializeGlobalHead(getSearchPath());
		else
			nc.setSearchPathAndInitializeGlobalHead(nc.getCurrentDirectory());

		SimpleXMLQueryProcessor.AttributeIndexes.initAttributeIndexes(nc);
		
//		current_query = new NetgraphQuery(getQueryString(), nc);
		current_query.setNetgraphComunication(nc);
		
		if (getQueryString() != null)
			current_query.startTheQuery();
		else
			current_query.startQueryAll();
		
		current_query.processResult(rp);
		
		nc.close();		
	}
		
	/**
	 * From Long path name extract filename without extension.
	 * @param filename
	 * @return
	 */
	public static String stripFilename(String filename)
	{				
		return filename.subSequence(filename.lastIndexOf('/')+1, filename.indexOf('.')).toString();
	}
	
	public static String makeAtomString(String src_token)
	{
//		String norm = ASCIINormalise(src); 
//		String norm = src_token.replaceAll("[-+^#*()!?,.:`';/{} \\\\\"\\[\\]]", "_");
		String norm = src_token.replaceAll("[^\\p{L}\\p{Alnum}]", "_");
		if (! Character.isLetter(norm.charAt(0)) || Character.isUpperCase(norm.charAt(0)))
		{
			try
			{
				Integer.parseInt(norm); 
			}
			catch (NumberFormatException e)
			{
				norm = "q" + norm;
			}
		}
		return norm;
	}
	
	protected void printCaluse(String name, String arg1)
	{
		tree_out.println(
				makeAtomString(name) + "(" +		
				makeAtomString(arg1) + ").");				
	}
	protected void printCaluse(String name, String arg1, String arg2)
	{
		tree_out.println(
				makeAtomString(name) + "(" +		
				makeAtomString(arg1) + "," +				
				makeAtomString(arg2) + ").");				
	}

	protected static String stripQuotes(String src_token)
	{
		return src_token.replaceAll("'", "`");
	}

	protected void printCaluseQuoted(String name, String arg1, String quoted_arg)
	{
		tree_out.println(
				makeAtomString(name) + "(" +		
				makeAtomString(arg1) + ",'" +				
				stripQuotes(quoted_arg) + "').");				
	}

	protected void printCaluseQuoted(String name, String quoted_arg)
	{
		tree_out.println(
				makeAtomString(name) + "('" +		
				stripQuotes(quoted_arg) + "').");				
	}

	
	public static final String[] train_set = {
		"jihomoravsky55788",
/**/	"jihomoravsky48793",
		"jihomoravsky49921",
		"jihomoravsky51460",
		"jihomoravsky54536",
		"jihomoravsky56560",
		"jihomoravsky48752",
		"jihomoravsky53388",
		"jihomoravsky51719",
		"jihomoravsky56762",
		"jihomoravsky55629",
		"jihomoravsky50788",
		"jihomoravsky55918",
		"jihomoravsky56118",
		"jihomoravsky52147",
		"jihomoravsky47443",
		"jihomoravsky55789",
		"jihomoravsky56177",
		"jihomoravsky49737",
		"jihomoravsky56184",
		"jihomoravsky48880",
		"jihomoravsky54637",
		"jihomoravsky51054",
		"jihomoravsky50885",/**/
		"jihomoravsky48749"};
	
	public static final String corpus_path = "/cygdrive/c/WorkSpace/czSem/UNIX_copy/netgraph_server/corpus\\hasici\\jihomoravsky/"; 
	public static final String extension = ".t.gz.fs"; 
	public static final char separator = SepartorCahrs.FS; 
	
	public static String makeSearchPath(String[] filenames)
	{
		StringBuilder sb = new StringBuilder();
		
		for (String file : filenames) {
			sb.append(corpus_path);
			sb.append(file);
			sb.append(extension);
			sb.append(separator);
		}
		
		return sb.toString();
	}
	

	public void setSearchPath(String searchPath) {
		this.searchPath = searchPath;
	}

	public String getSearchPath() {
		return searchPath;
	}

	public String getQueryString() {
		return current_query.getQueryString();
	}

	public void setQueryString(String query) {
		current_query.setQueryString(query);
	}
}
