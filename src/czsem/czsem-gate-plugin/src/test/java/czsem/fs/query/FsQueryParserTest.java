package czsem.fs.query;

import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.fs.FSTreeWriter.NodeAttributes;
import czsem.fs.query.FsQueryParser.SyntaxError;

public class FsQueryParserTest {
	
	@Test(expectedExceptions = SyntaxError.class)
	public static void testParseExcept() throws SyntaxError {
		FsQueryParser p = new FsQueryParser(new FsQueryBuilder(new FsQuery()));
		
		p.parse("foo");
	}

	
	@Test
	public static void testParse() throws SyntaxError {
		Utils.loggerSetup(Level.ALL);
		
		FsQueryParser p = new FsQueryParser(new FsQueryBuilder(new FsQuery()));
		
		p.parse("[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],length=10]([string=annotations]([],[]),[])");
	}

	@Test
	public static void testParseAndEvaluate() throws SyntaxError {
		FsQuery q = FsQueryTest.buidQueryObject();
		FsQueryBuilder b = new FsQueryBuilder(q);
		FsQueryParser p = new FsQueryParser(b);
		
		p.parse("[]([]([]))");
		
		FsQueryTest.evaluateQuery(b.getRootNode());
		
		b = new FsQueryBuilder(q);
		p = new FsQueryParser(b);
		
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
		FsQueryTest.evaluateQuery(b.getRootNode(), res);

		
		b = new FsQueryBuilder(q);
		p = new FsQueryParser(b);
		
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
		
		FsQueryTest.evaluateQuery(b.getRootNode(), res2);

	}
}
