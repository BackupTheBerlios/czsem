package czsem.gate.treex;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import czsem.Utils;

public class TreexAnalysisTest {
	
	static Logger logger = Logger.getLogger(TreexAnalysisTest.class);
	
	@BeforeClass
	public static void initLogger()
	{
		Utils.loggerSetup(Level.ALL);
		
	}
	
	@Test
	public void segmentAndTokenizeTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.start();
				
		TreexServerConnection conn = tse.getConnection();
		
		conn.initScenario("cs", "W2A::Segment", "W2A::Tokenize");
		
		Object ret = conn.analyzeText("Ahoj světe! Hallo world!");
		
		conn.terminateServer();
		
		List<Map<String, Object>> treexRet = Utils.objectArrayToGenericList(ret);		
		Assert.assertEquals(treexRet.size(), 2);
		
		List<Object> nodes1 = Utils.objectArrayToGenericList(treexRet.get(0).get("nodes"));		
		Assert.assertEquals(nodes1.size(), 3);

		List<Object> nodes2 = Utils.objectArrayToGenericList(treexRet.get(1).get("nodes"));		
		Assert.assertEquals(nodes2.size(), 3);
		
	}

	@Test( groups="slow" )
	public void morceTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.show_treex_output = true;
		tse.start();
				
		TreexServerConnection conn = tse.getConnection();
		
		conn.initScenario("en", "W2A::EN::Segment", "W2A::EN::Tokenize", "W2A::EN::TagMorce");
		
		logger.debug("Before first sentence.");
		Object ret = conn.analyzeText("Hallo world!");

		logger.debug("Before second sentence.");
		ret = conn.analyzeText("Life is great, isn't it?");

		logger.debug("Second sentence finished!");
		
		conn.terminateServer();
		
		List<Map<String, Object>> treexRet = Utils.objectArrayToGenericList(ret);		
		Assert.assertEquals(treexRet.size(), 1);
		
		List<Object> nodes1 = Utils.objectArrayToGenericList(treexRet.get(0).get("nodes"));		
		Assert.assertEquals(nodes1.size(), 8);
	}

}
