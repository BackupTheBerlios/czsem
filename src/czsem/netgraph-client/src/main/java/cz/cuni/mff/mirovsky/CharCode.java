package cz.cuni.mff.mirovsky;

/**
 * Class CharCode converts strings from one character coding to another. Since UTF-8 is used in Netgraph now, the class is now only used to
 * transcode from pseudo coding in queries to UTF-8. Pseudo coding in queries allows entering accented characters as a sequence of
 * unaccented characters, e.g. e2=é, e3=ě.
 */
public class CharCode {

  //private final static char[] character = {'Ľ','Š','Ť','Ž','ľ','š','ť','ž','Á','Ä','Ĺ','Č','É','Ë','Ě','Í','Ď','Ň','Ó','Ô','Ö','Ř','Ů','Ú','Ü','Ý','á','ä','ĺ','č','é','ë','ě','í','ď','ň','ó','ô','ö','ř','ů','ú','ü','ý'}; // to jen pro orientaci
    private final static int[] isolatin2  = {165,169,171,174,181,185,187,190,193,196,197,200,201,203,204,205,207,210,211,212,214,216,217,218,220,221,225,228,229,232,233,235,236,237,239,242,243,244,246,248,249,250,252,253}; // iso-8859-2
    private final static int[] win1250    = {188,138,141,142,190,154,157,158,193,196,197,200,201,203,204,205,207,210,211,212,214,216,217,218,220,221,225,228,229,232,233,235,236,237,239,242,243,244,246,248,249,250,252,253}; // cp-1250
    private final static int[] unicode    = {317,352,356,381,318,353,357,382,193,196,313,268,201,203,282,205,270,327,211,212,214,344,366,218,220,221,225,228,314,269,233,235,283,237,271,328,243,244,246,345,367,250,252,253}; // unicode
    private final static int[] pseudo2    = {  0,  0,  0,  0,  0,  0,  0,  0,193,  0,313,  0,201,  0,  0,205,  0,  0,211,  0,  0,  0,  0,218,  0,221,225,  0,314,  0,233,  0,  0,237,  0,  0,243,  0,  0,  0,  0,250,  0,253}; // dlouhé písmeno
    private final static int[] pseudo3    = {317,352,356,381,318,353,357,382,  0,  0,  0,268,  0,  0,282,  0,270,327,  0,  0,  0,344,  0,  0,  0,  0,  0,  0,  0,269,  0,  0,283,  0,271,328,  0,  0,  0,345,  0,  0,  0,  0}; // písmeno s háčkem
    private final static int[] pseudo4    = {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,366,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,367,  0,  0,  0}; // písmeno s kroužkem
    private final static int[] pseudo5    = {  0,  0,  0,  0,  0,  0,  0,  0,  0,196,  0,  0,  0,203,  0,  0,  0,  0,  0,  0,214,  0,  0,  0,220,  0,  0,228,  0,  0,  0,235,  0,  0,  0,  0,  0,  0,246,  0,  0,  0,252,  0}; // přehlasované písmeno
    private final static int[] pseudo6    = {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,212,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,244,  0,  0,  0,  0,  0,  0}; // písmeno se stříškou
    private final static int[] ascii      = {'L','S','T','Z','l','s','t','z','A','A','L','C','E','E','E','I','D','N','O','O','O','R','U','U','U','Y','a','a','l','c','e','e','e','i','d','n','o','o','o','r','u','u','u','y'}; // odpovidající písmeno bez diakritiky
    private final static int[] cascii     = {'L','S','T','Z','l','s','t','z',193,'A','L','C',201,'E','E',205,'D','N',211,'O','O','R','U',218,'U',221,225,'a','l','c',233,'e','e',237,'d','n',243,'o','o','r','u',250,'u',253}; // odpovídající písmeno bez diakritiky kromě písmen s čárkou

	public final static int coding_unicode = 0;
	public final static int coding_ascii = 1;
	public final static int coding_semi_graphics = 2;
	public final static int coding_graphics = 3;
	public final static int coding_pseudo = 4;

	private final static String[] coding_names = {
		"unicode", "ascii", "semi graphics", "graphics", "pseudo"
	};

	public static String getCodingName(int coding) {
	    if (coding < 0 || coding > 4) return "undefined";
		else return coding_names[coding];
	}

	public static int getCodingNumber(String name) {
		int value;
	    if (name.compareToIgnoreCase("unicode") == 0) value = 0;
		else if (name.compareToIgnoreCase("ascii") == 0) value = 1;
		else if (name.compareToIgnoreCase("semi graphics") == 0) value = 2;
		else if (name.compareToIgnoreCase("graphics") == 0) value = 3;
		else if (name.compareToIgnoreCase("pseudo") == 0) value = 4;
		else value = -1;

		return value;
	}

	private static int prelozPrvek (int[] zdrojKod, int[] cilKod, int znak) { // přeloží znak z jednoho kódování do druhého
		// vrátí původní znak, nelze-li přeložit
		int i,j,k,l;
		j=zdrojKod.length; // délka pole vstupního kódování
		k=cilKod.length; // délka pole cílového kódování
		for (i=0; i<j; i++) {
			if (znak == zdrojKod[i]) {
				if (i<k) { // jsem-li v hranicích pole cílového kódování
					l = cilKod[i];
					if (l != 0) return l; // pokud znak je definován v cílovém kódování
				}
			}
		}
		return znak;
	}

	public static String unicodeToIsolatin2 (String vstupni_retezec) { // převede vstupní řetězec v unicodu do výstupního řetězce v isolatin2
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(unicode, isolatin2, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static String unicodeToAscii (String vstupni_retezec) { // převede vstupní řetězec v unicodu do výstupního řetězce v ascii
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(unicode, ascii, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static String unicodeToCAscii (String vstupni_retezec) { // převede vstupní řetězec v unicodu do výstupního řetězce v ascii s čárkami
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(unicode, cascii, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static String unicodeToWin1250 (String vstupni_retezec) { // převede vstupní řetězec v unicodu do výstupního řetězce ve win1250
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(unicode, win1250, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static String win1250ToUnicode (String vstupni_retezec) { // převede vstupní řetězec ve win1250 do výstupního řetězce v unicodu
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(win1250, unicode, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static String win1250ToAscii (String vstupni_retezec) { // převede vstupní řetězec ve win1250 do výstupního řetězce v ascii
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(win1250, ascii, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static String isolatin2ToUnicode (String vstupni_retezec) { // převede vstupní řetězec v isolatin2 do výstupního řetězce v unicodu
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(isolatin2, unicode, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

   	public static String isolatin2ToAscii (String vstupni_retezec) { // převede vstupní řetězec v isolatin2 do výstupního řetězce v ascii
		String vystupni_retezec = new String ();
		int i,j;
		j=vstupni_retezec.length();
		for (i=0; i<j; i++) { // přes všechny znaky řetězce
			vystupni_retezec += (char)prelozPrvek(isolatin2, ascii, (int)vstupni_retezec.charAt(i));
		}
		return vystupni_retezec;
	}

	public static int pseudoToUnicode (int znak1, int znak2) { // vrátí unicodovy znak reprezentovany touto dvojici v pseudocodu;
		// nejedná-li se o pseudokód, vrátí znak1
		int preklad;
		int vystup = znak1; // implicitní vystup
		switch (znak2) { // podle typu pseudointerpunkce
			case 50: // '2', tj. čárka
				vystup = prelozPrvek (ascii, pseudo2, znak1);
				break;
			case 51: // '3', tj. háček
				vystup = prelozPrvek (ascii, pseudo3, znak1);
				break;
			case 52: // '4', tj. kroužek
				vystup = prelozPrvek (ascii, pseudo4, znak1);
				break;
			case 53: // '5', tj. přehláska
				vystup = prelozPrvek (ascii, pseudo5, znak1);
				break;
			case 54: // '6', tj. stříška
				vystup = prelozPrvek (ascii, pseudo6, znak1);
				break;
			default: break;
		}
		return vystup;
	}

	public static String pseudoToUnicode (String vstupni_retezec) { // převede vstupní řetězec v pseudo kódu do výstupního řetězce v unicodu
		// pseudo kódování je: e2 = é, e3 = ě, u4 = ů
		String vystupni_retezec = new String ();
		int i,j;
		int prvni_znak, druhy_znak, vystupni_znak;
		j=vstupni_retezec.length();
		i=0;
		while (i<j) { // přes všechny znaky řetězce
			prvni_znak = (int)vstupni_retezec.charAt(i); // znak na aktuální pozici
			if (i+1 == j) { // jsem u posledního znaku řetězce; po něm nemůže následovat pseudointerpunkce
				druhy_znak = 32; // neutrální znak, např. mezera
			}
			else druhy_znak = (int)vstupni_retezec.charAt(i+1); // případná pseudointerpunkce
			vystupni_znak = pseudoToUnicode (prvni_znak, druhy_znak);
			vystupni_retezec += (char)vystupni_znak;
			if (prvni_znak != vystupni_znak) i++; // šlo o znak s následující pseudointerpunkcí; musím pokračovat o dvě pozice dál
			i++;
		}
		return vystupni_retezec;
	}

} // class CharCode
