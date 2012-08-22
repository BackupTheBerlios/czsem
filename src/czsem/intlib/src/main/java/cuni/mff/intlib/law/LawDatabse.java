package cuni.mff.intlib.law;

import gate.Annotation;
import gate.Document;
import gate.util.GateException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cuni.mff.intlib.law.BasicAnalysis.Analyzer;
import cuni.mff.intlib.law.LawDatabse.LawHeadAnalyssis;
import czsem.Utils;
import czsem.gate.GateUtils;

public class LawDatabse {

	public static String dbPath = "C:\\data\\law\\portal.gov.cz\\db";
	
	static Logger logger = Logger.getLogger(LawDatabse.class);
	
	static class LawHeadAnalyssis
	{
		private String prefix;
		private int year;
		private String suffix;
		String head;
		
		static Pattern p = Pattern.compile("[^0-9]([12][90][0-9][0-9])[^0-9]?"); 

		public LawHeadAnalyssis(String head) {
			this.head = head;
			
			Matcher m = p.matcher(head);
			if (!m.find()) throw new IllegalArgumentException(String.format("year couldn't be found in '%s'.", head));
			
			do {			
				year = Integer.parseInt(m.group(1));			
				prefix = head.substring(0, m.start());
				suffix = head.substring(m.end(), head.length()).replace(" ", "");
				suffix = suffix.replaceFirst("^[/-]", "");
			} while (m.find(m.start()+1)); //use the last match
		}

		public String getSuffix() {
			return suffix;
		}

		public int getYear() {
			return year;
		}

		public String getPrefix() {
			return prefix;
		}
	}

	public static void main(String[] args) throws GateException, IOException, URISyntaxException, Exception {
		
	}
	
	public static void createAndFillTable() throws GateException, IOException, URISyntaxException, Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:" + dbPath, "sa", "");
		
		final Statement s = conn.createStatement();
		String sqlCreateTable = "DROP TABLE gov;\n" +
				"CREATE TABLE gov (prefix VARCHAR(18), year INT, suffix VARCHAR(18), line0 VARCHAR(50), type VARCHAR(50), line1 VARCHAR(255), line2 VARCHAR(255));";

		s.execute(sqlCreateTable);
		
		BasicAnalysis.createDefaultAnalysisHandler().analyzeAll(new Analyzer() {
			
			@Override
			public void analyzeDoc(Document doc) throws Exception {
				List<Annotation> paras = gate.Utils.inDocumentOrder(doc.getAnnotations("Original markups").get("paragraph"));
				String orig = GateUtils.getAnnotationContent(paras.get(0), doc).replaceAll("[\\n\\r]", "");
				logger.debug(orig);
				try {
					LawHeadAnalyssis lha = new LawHeadAnalyssis(orig);

					String insertSqlStr = String.format(
							"INSERT INTO gov VALUES('%s', %s, '%s', '%s', '%s', '%s', '%s');",
							lha.getPrefix(),
							lha.getYear(),
							lha.getSuffix(),
							orig,		
							Utils.strTrimTo(GateUtils.getAnnotationContent(paras.get(1), doc).replaceAll("[\\n\\r]", ""), 50),
							Utils.strTrimTo(GateUtils.getAnnotationContent(paras.get(2), doc).replaceAll("[\\n\\r]", ""), 255),
							paras.size() >= 4 ? 
									Utils.strTrimTo(GateUtils.getAnnotationContent(paras.get(3), doc).replaceAll("[\\n\\r]", ""), 255)
									: ""
							);
					
					s.execute(insertSqlStr);
				} catch (IllegalArgumentException e)
				{
					logger.error(e.getMessage(), e);
				}
				
				
				
				
			}
		});
		
		
		conn.close();
	}

}
