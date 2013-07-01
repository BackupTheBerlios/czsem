package czsem.gate.utils;

import gate.Document;
import gate.Factory;

import java.io.File;

import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.mime.MimeForMicrosoftX;

public class SpcAnalysisPersistentTest {
	@Test
	public static void detectMime() throws Exception{
		String dir = SpcAnalysisConfig.getConfig().getSpcAllDirectory();
		
		File file = new File(dir, "SPC133131.doc");
		String mimeDetected = MimeForMicrosoftX.detectMimeWithFileSuffixConfusion(file.getAbsolutePath());		
		Assert.assertEquals(mimeDetected, "application/msword");
	}

	@Test
	public static void detectMimeX() throws Exception{
		String dir = SpcAnalysisConfig.getConfig().getSpcAllDirectory();
		
		File file = new File(dir, "SPC100622.doc");
		String mimeDetected = MimeForMicrosoftX.detectMimeWithFileSuffixConfusion(file.getAbsolutePath());		
		Assert.assertEquals(mimeDetected, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
	}

	
	@Test
	public static void parsedoc() throws Exception{
		GateUtils.initGate(Level.ALL);
		
		String dir = SpcAnalysisConfig.getConfig().getSpcAllDirectory();
		
		File file = new File(dir, "SPC100622.doc");

	    Document doc = GateUtils.createDoc(file, "utf8", 
	    		MimeForMicrosoftX.detectMimeWithFileSuffixConfusion(file.getAbsolutePath()));
	    
	    Assert.assertNotNull(doc);
	    
	    Factory.deleteResource(doc);
	}
}
