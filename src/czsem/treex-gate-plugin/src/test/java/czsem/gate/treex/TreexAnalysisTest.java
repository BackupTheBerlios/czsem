package czsem.gate.treex;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;

public class TreexAnalysisTest {
	
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

}
