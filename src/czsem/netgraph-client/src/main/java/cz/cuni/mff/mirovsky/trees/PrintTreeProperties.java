package cz.cuni.mff.mirovsky.trees;

import java.awt.print.PageFormat;

import cz.cuni.mff.mirovsky.CharCode;

/**
 * A class that keeps properties of trees related to printing the trees on a printer
 */

public class PrintTreeProperties {

	private String font_family; // rodina fontu z množiny
	// {"Dialog", "DialogInput", "Monospaced", "Serif", "SansSerif",}
	private int font_size; // velikost fontu z množiny {"8","10","12","14"}
	//private int character_coding; // kódování znaků při tisku
	private boolean center; // centrování stromu na stránce
	private boolean keep_ratio; // zachování poměru stran při zmenšování stromu
	private boolean background; // tisk na pozadí (ve vlastním vlákně)
    private boolean black_white; // tisk v černé barvě
    private PageFormat page_format; // zde se bude uchovávat uživatelovo nastavení papíru

	public PrintTreeProperties () {
		this ("SansSerif",10,/*CharCode.coding_unicode,*/false,false,false,false);
	}

	public PrintTreeProperties (String p_font_family,
	                     int p_font_size,
						 /*int p_character_coding,*/
						 boolean p_center,
						 boolean p_keep_ratio,
						 boolean p_background,
                         boolean p_black_white) {
		font_family = p_font_family;
		font_size = p_font_size;
		//character_coding = p_character_coding;
		center = p_center;
		keep_ratio = p_keep_ratio;
		background = p_background;
        black_white = p_black_white;
        page_format = new PageFormat(); // zde se bude uchovávat uživatelovo nastavení stránky
	}

	public String getFontFamily () { return font_family; }
	public int getFontSize () { return font_size; }
	//public int getCharacterCoding () { return character_coding; }
	public boolean getCenter () { return center; }
	public boolean getKeepRatio () { return keep_ratio; }
	public boolean getBackground () { return background; }
    public boolean getBlackWhite () { return black_white; }
	public PageFormat getPageFormat () { return page_format; }

	public void setFontFamily (String p_font_family) { font_family = p_font_family; }
	public void setFontSize (int p_font_size) { font_size = p_font_size; }
	//public void setCharacterCoding (int p_character_coding) { character_coding = p_character_coding; }
	public void setCenter (boolean p_center) { center = p_center; }
	public void setKeepRatio (boolean p_keep_ratio) { keep_ratio = p_keep_ratio; }
	public void setBackground (boolean p_background) { background = p_background; }
    public void setBlackWhite (boolean p_black_white) { black_white = p_black_white; }
	public void setPageFormat (PageFormat p_page_format) { page_format = p_page_format; }

} // class PrintTreeProperties
