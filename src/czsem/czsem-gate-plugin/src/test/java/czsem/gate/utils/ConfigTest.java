package czsem.gate.utils;

import gate.Gate;
import gate.util.GateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.rules.TemporaryFolder;
import org.testng.annotations.Test;

import czsem.gate.GateUtils;
import czsem.utils.AbstractConfig.ConfigLoadEception;

public class ConfigTest {

	@Test(groups = { "excludeByMaven" }, expectedExceptions=ConfigLoadEception.class)
	public void getConfigExcept() throws IOException, URISyntaxException, GateException {
		System.err.println(System.getProperty("user.dir"));
		
		GateUtils.initGateInSandBox();
		
		Gate.setPluginsHome(new File("."));
		
		TemporaryFolder temp = new TemporaryFolder();
		temp.create();
		temp.getRoot().deleteOnExit();
		
		File f = temp.newFolder(Config.czsem_plugin_dir_name);
		temp.create();
		f.mkdir();
		f.deleteOnExit();
		
		File cf = new File(f, "creole.xml");
		FileWriter wr = new FileWriter(cf);
		wr.write("<CREOLE-DIRECTORY/>"); 
		wr.close();
		cf.deleteOnExit();
		
		Gate.getCreoleRegister().addDirectory(f.toURI().toURL());

		Config.getConfig();
	}
}
