package czsem.ILP;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import czsem.utils.Config;
import czsem.utils.ProcessExec;

public class WekaRun
{

	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException
	{
		System.out.println(  );

		
		//Config.loadConfig();
		
		String [] cmdarray =
		{
				"java",
				"-classpath",
				Config.getConfig().getWekaJarPath() + 
					System.getProperty( "path.separator" ) +
					Config.getConfig().getWekaRunFuzzyILPClassPath(),
				"weka.gui.GUIChooser"
		};		
		//java -classpath "./weka.jar;." weka.gui.GUIChooser
		
		System.out.println(Arrays.toString(cmdarray));
		

		ProcessExec proc = new ProcessExec();
		proc.exec(cmdarray);
		proc.startStdoutReaderThreads();
					
		
//		BufferedReader is = new BufferedReader(new InputStreamReader(proc.getInputStream())); 
		
		proc.waitFor();
		
//		System.out.println(is.readLine());
	}

}
