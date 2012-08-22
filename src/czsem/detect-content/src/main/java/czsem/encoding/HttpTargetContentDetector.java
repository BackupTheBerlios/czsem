package czsem.encoding;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;

import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

public class HttpTargetContentDetector {
	static {
		MimeUtil.registerMimeDetector(MagicMimeMimeDetector.class.getCanonicalName());
    }
	
	protected Connection con = null;
	
	public HttpTargetContentDetector(String url) throws IOException
	{
		con = Jsoup.connect(url);
		con.execute();		
		try {
			con.response().parse();
		} catch (Exception e) {
			//do nothing - as failure of parsing does not necessarily mean failure of detection 
		}
	}
	
	public String getMimeFromContnetType()
	{
		return con.response().contentType().split(";")[0];		
	}

	public String detectMimeType() {
		String mfc = getMimeFromContnetType();
		
		if (mfc == null || mfc.equalsIgnoreCase("application/octet-stream"))
			return MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(con.response().bodyAsBytes())).toString();
		else
			return mfc;
	}
	


	public String detectEncoding() {		
		return con.response().charset();
	}

	public static gate.Document gateDocFromUrl(String url) throws IOException,	ResourceInstantiationException {
		
		HttpTargetContentDetector detect = new HttpTargetContentDetector(url);
		
		String enc = detect.detectEncoding();
		String mime = detect.detectMimeType();
		
		
		//debug
		//System.err.format("%s %s %s\n", mime, enc, url);

		FeatureMap parameterValues = Factory.newFeatureMap();

		URL sourceUrl = new URL(url);
		parameterValues.put(gate.Document.DOCUMENT_URL_PARAMETER_NAME,	sourceUrl);
		
		if (mime != null)
			parameterValues.put(gate.Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, mime);
		
		if (enc != null)
			parameterValues.put(gate.Document.DOCUMENT_ENCODING_PARAMETER_NAME,	enc);
		
		detect = null;
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();

		return (gate.Document) Factory.createResource("gate.corpora.DocumentImpl", parameterValues);

	}

	public static void main(String[] args) throws IOException, GateException {
	        
		
		
		// testUrl("http://google.com");
		// testUrl("http://www.horosvaz.cz/drytooling/");
		// testUrl("http://lezec.cz");


		Gate.runInSandbox(true);
		Gate.init();

		
		System.err.println(gateDocFromUrl("http://www.w3.org/TR/xhtml1/").getContent().getContent(0L, 100L).toString());
		System.err.println(gateDocFromUrl("http://dedekj.appspot.com/genderTagger/assignGenderSimple?_function=assignGenderSimple&inputName=Novak&account=a&_convention=_xins-std").getContent().toString());
		System.err.println(gateDocFromUrl("http://google.com").getContent().getContent(0L, 100L).toString());
		System.err.println(gateDocFromUrl("http://lezec.cz").getContent().getContent(0L, 100L).toString());
		System.err.println(gateDocFromUrl("http://www.horosvaz.cz/drytooling/").getContent().getContent(0L, 100L).toString());
		System.err.println(gateDocFromUrl("http://aplikace.mvcr.cz/sbirka-zakonu/ViewFile.aspx?type=z&id=24495").getContent().getContent(0L, 100L).toString());
		System.err.println(gateDocFromUrl("http://www.ceska-kamenice.cz/files/formulare/hs/Ozn%C3%A1men%C3%AD%20o%20kon%C3%A1n%C3%AD%20shrom%C3%A1%C5%BEd%C4%9Bn%C3%AD%20-%20FO.rtf").getContent().getContent(0L, 100L).toString());
		System.err.println(gateDocFromUrl("http://www.ceska-kamenice.cz/files/formulare/smm/Zadost_o_prodej_pozemku.doc").getContent().getContent(0L, 100L).toString());

		System.err.println("end");
				
		/*
		 * gate.Document doc = Factory.newDocument(new
		 * URL("http://google.com"));
		 * System.err.println(doc.getContent().toString());
		 * 
		 * gate.Document doc2 = gateDocFromUrl("http://google.com");
		 * System.err.println(doc2.getContent().toString()); /*
		 */

	}

	public static void testUrl(String url) throws IOException {
		Connection con = Jsoup.connect(url);
		con.method(Method.GET);
		con.execute();
		System.err.println(con.response().charset());
		System.err.println(con.response().contentType());
		con.response().parse();
		System.err.println(con.response().charset());
		System.err.println(con.response().contentType());
		/*
		 * 
		 * Document doc = Jsoup.connect(url).get();
		 * System.err.println(doc.html());
		 */
		System.err.format(
				"------ %s -----------------------------------------------\n",
				url);
	}

}
