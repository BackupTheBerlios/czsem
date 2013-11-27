package czsem.gate.treex.xmlwriter;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.ws.commons.serialize.CharSetXMLWriter;

public class FilteringCharSetXMLWriter extends CharSetXMLWriter {

	@Override
	public void setWriter(Writer pWriter) {
		Writer filter = new FilterWriter(pWriter) {

			@Override
			public void write(int c) throws IOException {
				
				if (c < 32) {				
					switch (c) {
						case 9: break;
						case 10: break;
						case 13: break;				
						default:
							//replace by space
							super.write(' ');
							return;
					}
				}

				super.write(c);
			}
		
		};
		
		super.setWriter(filter);
	}

}
