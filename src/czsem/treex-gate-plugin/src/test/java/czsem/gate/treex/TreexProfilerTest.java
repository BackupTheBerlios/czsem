package czsem.gate.treex;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.utils.Config;

public class TreexProfilerTest {

	public void printPaths() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.show_treex_output = true;
		
		tse.startWithoutHandshake();
		
		tse.waitFor();
		
	}

	@Test(timeOut = 30000)
	public void profilingTest() throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.show_treex_output = true;
		tse.setHandshakeCode("profilingTest");
		
		
		String[] cmdarray = {
				"perl", 
//				"-d:NYTProf",
				Config.getConfig().getCzsemResourcesDir()+"/Treex/treex_online.pl",
				Integer.toString(tse.getPortNumber()),
				tse.getHandshakeCode()};

		tse.start(cmdarray);
		
		TreexServerConnection conn = tse.getConnection(); 
		
		conn.initScenario("cs", "W2A::CS::Segment", "W2A::CS::Tokenize");
		
		StringBuilder sb = new StringBuilder();
		
		int numSents = 500;
		for (int a=1; a<= numSents; a++)
		{
			sb.append(String.format("Toto je věta číslo %d. ", a));
		}
		
		Object ret = conn.analyzeText(sb.toString());
		
		conn.terminateServer();
		
		List<Map<String, Object>> treexRet = Utils.objectArrayToGenericList(ret);		
		Assert.assertEquals(treexRet.size(), numSents);


	}


}
