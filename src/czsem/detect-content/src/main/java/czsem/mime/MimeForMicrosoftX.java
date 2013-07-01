package czsem.mime;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

public class MimeForMicrosoftX {

	public static String detectMimeWithFileSuffixConfusion(String fileName) throws FileNotFoundException, IOException {
		Tika tika = new Tika();
		Detector detector = tika.getDetector();
		
		String shortName = FilenameUtils.getName(fileName);
		String nameWithoutX = shortName;
		
		if (shortName.endsWith("x")) {
			nameWithoutX = shortName.substring(0, shortName.length()-1);
		}
		
		
		Metadata m = new Metadata();
		m.set(Metadata.RESOURCE_NAME_KEY, nameWithoutX);
		MediaType r = detector.detect(new BufferedInputStream(new FileInputStream(fileName)), m);
		
		if (r.getSubtype().startsWith("x")) //e.g. x-tika-msoffice or x-tika-ooxml
		{
			m.set(Metadata.RESOURCE_NAME_KEY, nameWithoutX+"x");
			r = detector.detect(new BufferedInputStream(new FileInputStream(fileName)), m);
		}
		
		return r.toString();
	}

}
