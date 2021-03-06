package czsem.fs.query;

import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.fs.NodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.fs.query.restrictions.OptionalEvaluator;

public class FSQueryParserTest {
	
	@Test(expectedExceptions = SyntaxError.class)
	public static void testParseExcept() throws SyntaxError {
		FSQueryParser p = new FSQueryParser(new FSQueryBuilderImpl());
		
		p.parse("foo");
	}

	@Test
	public static void testParseName() throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		p.parse("[_name=node1]");
		
		Assert.assertEquals(b.getRootNode().getName(), "node1");
	}
	
	@Test
	public static void testParse() throws SyntaxError {
		Utils.loggerSetup(Level.ALL);
		
		FSQueryParser p = new FSQueryParser(new FSQueryBuilderImpl());
		p.parse("[]");

		p = new FSQueryParser(new FSQueryBuilderImpl());				
		p.parse("[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],length=10]([string=annotations]([],[]),[])");
		
		evalQuery("[]( [id=1] , [id=2] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [id =2] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [ id =2] )", new int [] {0, 1, 2});		
		evalQuery("[]( [id=1] , [ id = 2] )", new int [] {});		
		evalQuery("[]( [id=1] , [ id =2 ] )", new int [] {});		
		evalQuery("[]( [id=1] , [ id = 2 ] )", new int [] {});		
	}
	
	@Test
	public static void testParseAndEvaluate() throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		
		p.parse("[]([]([]))");
		
		FSQueryTest.evaluateQuery(b.getRootNode());
		
		b = new FSQueryBuilderImpl();
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

		
		b = new FSQueryBuilderImpl();
		p = new FSQueryParser(b);
		
		p.parse("[]([],[id=7])");
		
		int[] res2 = {
				0, 1, 7,
				0, 2, 7,
				0, 7, 7,};
		
		FSQueryTest.evaluateQuery(b.getRootNode(), res2);

	}

	public static void evalQuery(QueryData data, String queryString, int[] res) throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		
		p.parse(queryString);
		FSQueryTest.evaluateQuery(data, b.getRootNode(), res);				
	}


	public static void evalQuery(String queryString, int[] res) throws SyntaxError {
		QueryData data = FSQueryTest.buidQueryObject();		
		evalQuery(data, queryString, res);
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
		QueryData data = FSQueryTest.buidQueryObject();		
		TreeIndex i = data.getIndex();
		i.addDependency(1, 12);
		evalQuery(data, "[]([]([id=12],[id=3],[]))", new int [] {	
				0, 1, 12, 3, 3,
				0, 1, 12, 3, 4,
				0, 1, 12, 3, 12,
				});		

		evalQuery(data, "[]([]([id=12],[id=3],[id=4]))", new int [] {	
				0, 1, 12, 3, 4,
				});		
	}

	@Test
	public static void testThreeBrothers2() throws SyntaxError {
		QueryData data = FSQueryTest.buidQueryObject();		
		TreeIndex i = data.getIndex();
		i.addDependency(0, 8);
		i.addDependency(8, 9);
		i.addDependency(8, 10);
		i.addDependency(8, 11);
		
		evalQuery(data, "[]([]([],[],[]))", new int [] {	0, 1, 3, 3, 3,
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
		evalQuery(data, "[]([id=8]([id=9]))", new int [] {	0, 8, 9,});
		evalQuery(data, "[]([]([id=9]))", new int [] {	0, 8, 9,});
		evalQuery(data, "[]([]([id=11],[id=10],[id=9]))", new int [] {	0, 8, 11, 10, 9,});
	}

	@Test
	public static void testOptional() throws SyntaxError {
		FSQueryBuilderImpl b = new FSQueryBuilderImpl();
		FSQueryParser p = new FSQueryParser(b);
		p.parse("[id=1, _optional=true]");
		
		Assert.assertTrue(b.getRootNode().evaluator instanceof OptionalEvaluator, "OptionalEvaluator should be used");


		evalQuery("[]([id=2,_optional=true])", new int [] {0, 2});
		evalQuery("[]([id=1,_optional=true])", new int [] {0, 1});

		evalQuery("[]([_optional=true,_name=opt]([id=7]))", new int [] {0, 7});

		evalQuery("[]([_optional=true])", new int [] {
				0, 1,
				0, 2,
				0, 7,
				});

		evalQuery("[]([_optional=true,_name=opt1]([id=xxx]))", new int [] {});

		evalQuery("[]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id=7])))", new int [] {0, 7});

		evalQuery("[]([_optional=true,_name=opt1]([id~=\\[124\\]]))", new int [] {
				0, 1, 4,
				});
		
		evalQuery("[]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[165\\]])))", new int [] {
				0, 1, 3, 6,
				0, 2, 5,
				});
		
		evalQuery("[]([id=1]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[346\\]]))))", new int [] {
				0, 1, 3, 6,
				});

		
		QueryData data = FSQueryTest.buidQueryObject();
		data.getIndex().addDependency(4, 8);
		evalQuery(data, "[]([id=1]([_optional=true,_name=opt1]([_optional=true,_name=opt2]([id~=\\[3468\\]]))))", new int [] {
				0, 1, 3, 6,
				0, 1, 4, 8,
				});

	}

	@Test
	public static void testRegexp() throws SyntaxError {
		evalQuery("[]([id~=\\[12\\]])", new int [] {0, 1, 0, 2});
		
		
		
		TreeIndex index = new TreeIndex();		
		index.addDependency(0,1);
		index.addDependency(0,2);
		index.addDependency(0,3);
		index.addDependency(0,4);

		QueryData data = new FSQuery.QueryData(index, new NodeAttributes.IdNodeAttributes() {

			@Override
			public Object getValue(int node_id, String attrName) {
				switch (node_id) {
				case 0:					
					return "string0";
				case 1:					
					return "string1";
				case 2:					
					return "string2";
				case 3:					
					return "#PersPron";
				case 4:					
					return "a#string4";
				default:
					return "";
				}
			}
		});

		evalQuery(data, "[]([str~=\\[^#\\].*])", new int [] {0, 1, 0, 2, 0, 4});
		evalQuery(data, "[]([str~=\\[#\\].*])", new int [] {0, 3});

	}

	@Test
	public static void testNotEqual() throws SyntaxError {
		evalQuery("[]([id!=1])", new int [] {0, 2, 0, 7});		
		evalQuery("[]([ id !=1])", new int [] {0, 2, 0, 7});		
	}

	@Test
	public static void testIterateSubtree() throws SyntaxError {
		evalQuery("[_name=root]([id=1,_name=one]([_name=subtree,_subtree_eval_depth=20]))", new int [] {
				0, 1,
				0, 1, 4, 
				0, 1, 3, 
				0, 1, 3, 4, 
				0, 1, 3, 6, 
				0, 1, 3, 6, 4});		
	}

	@Test
	public static void testInListOperator() throws SyntaxError {
		evalQuery("[_name=root]([id @=2;7])", new int [] {
				0, 2,
				0, 7, 
				});		
	}

}
