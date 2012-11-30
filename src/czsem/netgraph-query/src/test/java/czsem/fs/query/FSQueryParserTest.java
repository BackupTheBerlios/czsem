package czsem.fs.query;

import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.fs.NodeAttributes;
import czsem.fs.query.FSQueryParser.SyntaxError;

public class FSQueryParserTest {
	
	@Test(expectedExceptions = SyntaxError.class)
	public static void testParseExcept() throws SyntaxError {
		FSQueryParser p = new FSQueryParser(new FSQueryBuilder(new FSQuery()));
		
		p.parse("foo");
	}

	
	@Test
	public static void testParse() throws SyntaxError {
		Utils.loggerSetup(Level.ALL);
		
		FSQueryParser p = new FSQueryParser(new FSQueryBuilder(new FSQuery()));
		
		p.parse("[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],length=10]([string=annotations]([],[]),[])");
	}

	@Test
	public static void testParseAndEvaluate() throws SyntaxError {
		FSQuery q = FSQueryTest.buidQueryObject();
		FSQueryBuilder b = new FSQueryBuilder(q);
		FSQueryParser p = new FSQueryParser(b);
		
		p.parse("[]([]([]))");
		
		FSQueryTest.evaluateQuery(b.getRootNode());
		
		b = new FSQueryBuilder(q);
		p = new FSQueryParser(b);
		
		p.parse("[]([],[])");
		
		int[] res = {
				0, 1, 1,
				0, 1, 2,
				0, 1, 7,
				0, 2, 1,
				0, 2, 2,
				0, 2, 7,
				0, 7, 1,
				0, 7, 2,
				0, 7, 7,};
		FSQueryTest.evaluateQuery(b.getRootNode(), res);

		
		b = new FSQueryBuilder(q);
		p = new FSQueryParser(b);
		
		p.parse("[]([],[id=7])");
		
		int[] res2 = {
				0, 1, 7,
				0, 2, 7,
				0, 7, 7,};
		
		q.setNodeAttributes(new NodeAttributes() {
			
			@Override
			public Object getValue(int node_id, String attrName) {
				return node_id;
			}
			
			@Override
			public Iterable<Entry<String, Object>> get(int node_id) {
				return null;
			}
		});
		
		FSQueryTest.evaluateQuery(b.getRootNode(), res2);

	}
}