package czsem.fs.query;

import org.apache.log4j.Level;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.fs.TreeIndex;
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
		
		FSQueryTest.evaluateQuery(b.getRootNode(), res2);

	}

	public static void evalQuery(String queryString, int[] res) throws SyntaxError {
		FSQuery q = FSQueryTest.buidQueryObject();
		evalQuery(q, queryString, res);
	}

	public static void evalQuery(FSQuery q, String queryString, int[] res) throws SyntaxError {
		FSQueryBuilder b = new FSQueryBuilder(q);
		FSQueryParser p = new FSQueryParser(b);
		
		p.parse(queryString);
		FSQueryTest.evaluateQuery(b.getRootNode(), res);		
	}
	
	@Test
	public static void testDeeperNesting() throws SyntaxError {

		evalQuery("[]([]([id=6]))", new int [] {});
		evalQuery("[]([]([id=4]))", new int [] {0, 1, 4});
		evalQuery("[]([id=1]([]))", new int [] {0, 1, 3,
												0, 1, 4});
		evalQuery("[]([]([]([id=6])))", new int [] {0, 1, 3, 6});
		evalQuery("[]([]([]([id=x])))", new int [] {});
		evalQuery("[]([]([]([]([]))))", new int [] {});

		evalQuery("[]([]([]([])),[]([]([])))", new int [] {0, 1, 3, 6, 1, 3, 6});
		evalQuery("[]([]([]([])),[]([id=4]([])))", new int [] {});
		evalQuery("[]([]([]([])),[]([id=4]))", new int [] {0, 1, 3, 6, 1, 4});
	}

	@Test
	public static void testTwoBrothers() throws SyntaxError {
		evalQuery("[]([]([id=4],[id=3]))", new int [] {	0, 1, 4, 3});
		evalQuery("[]([]([id=3],[id=4]))", new int [] {	0, 1, 3, 4});
		evalQuery("[]([id=2])", new int [] {0, 2});
		evalQuery("[]([id=2]([id=5]))", new int [] {0, 2, 5});
		evalQuery("[]([]([id=5]))", new int [] {0, 2, 5});
	}

	@Test
	public static void testThreeBrothers1() throws SyntaxError {
		FSQuery q = FSQueryTest.buidQueryObject();		
		TreeIndex i = q.getIndex();
		i.addDependency(1, 12);
		evalQuery(q, "[]([]([id=12],[id=3],[]))", new int [] {	
				0, 1, 12, 3, 3,
				0, 1, 12, 3, 4,
				0, 1, 12, 3, 12,
				});		

		evalQuery(q, "[]([]([id=12],[id=3],[id=4]))", new int [] {	
				0, 1, 12, 3, 4,
				});		
	}

	@Test
	public static void testThreeBrothers2() throws SyntaxError {
		FSQuery q = FSQueryTest.buidQueryObject();		
		TreeIndex i = q.getIndex();
		i.addDependency(0, 8);
		i.addDependency(8, 9);
		i.addDependency(8, 10);
		i.addDependency(8, 11);
		
		evalQuery(q, "[]([]([],[],[]))", new int [] {	0, 1, 3, 3, 3,
														0, 1, 3, 3, 4,
														0, 1, 3, 4, 3,
														0, 1, 3, 4, 4,
														0, 1, 4, 3, 3,
														0, 1, 4, 3, 4,
														0, 1, 4, 4, 3,
														0, 1, 4, 4, 4,
														0, 2, 5, 5, 5,
														0, 8, 9, 9, 9,
														0, 8, 9, 9, 10,
														0, 8, 9, 9, 11,
														0, 8, 9, 10, 9,
														0, 8, 9, 10, 10,
														0, 8, 9, 10, 11,
														0, 8, 9, 11, 9,
														0, 8, 9, 11, 10,
														0, 8, 9, 11, 11,
														0, 8, 10, 9, 9,
														0, 8, 10, 9, 10,
														0, 8, 10, 9, 11,
														0, 8, 10, 10, 9,
														0, 8, 10, 10, 10,
														0, 8, 10, 10, 11,
														0, 8, 10, 11, 9,
														0, 8, 10, 11, 10,
														0, 8, 10, 11, 11,
														0, 8, 11, 9, 9,
														0, 8, 11, 9, 10,
														0, 8, 11, 9, 11,
														0, 8, 11, 10, 9,
														0, 8, 11, 10, 10,
														0, 8, 11, 10, 11,
														0, 8, 11, 11, 9,
														0, 8, 11, 11, 10,
														0, 8, 11, 11, 11,
														});
		evalQuery(q, "[]([id=8]([id=9]))", new int [] {	0, 8, 9,});
		evalQuery(q, "[]([]([id=9]))", new int [] {	0, 8, 9,});
		evalQuery(q, "[]([]([id=11],[id=10],[id=9]))", new int [] {	0, 8, 11, 10, 9,});
	}

}
