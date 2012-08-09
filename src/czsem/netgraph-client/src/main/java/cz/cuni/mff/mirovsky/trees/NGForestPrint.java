package cz.cuni.mff.mirovsky.trees;

import java.awt.*;

import cz.cuni.mff.mirovsky.ShowMessagesAble;


/**
 * This class provides functions for printing trees on a printer.
 */
public class NGForestPrint extends NGForestView {

	private PrintTreeProperties print_properties; // vlastnosti tisku na tiskárnu

	/**
	 * Creates a new object for printing a forest on a printer
     * @param p_mess an object capable of printing messages
	 */
   	public NGForestPrint(ShowMessagesAble p_mess) { // konstruktor

		super(p_mess); // zavolam konstruktor rodicovskeho objektu
		// nastavím hodnoty lišící se od defaultních
		tree_properties.setDiameterChosen(9);
		tree_properties.setDiameter(7);
		tree_properties.setFontSize(11); // výška fontu
		tree_properties.setColorEdge(Color.cyan);
		tree_properties.setColorEdgeHidden(new Color(200,200,200));

		print_properties = new PrintTreeProperties();

	} // konstruktor


	private void printForest(Graphics2D g) {
        if (forest == null) return;

        // nastavím globální proměnné podle aktuálních properties
        setGlobalProperties();

        for (NGTree tree : forest.getTrees()) { // přes všechny stromy
            if (tree.getRoot() != null) {
                drawTree(tree, tree.getRoot(), null, tree.getRoot().getX(), tree.getRoot().getY(), g); // nakreslím strom
            }
            // stromy se kreslí vedle sebe, takže y_shift je vždy nula
        }
    } // printForest

    /**
     * Sets global variables for printing in accordance with the actual tree properties and print properties.
     */
	protected void setGlobalProperties() { // nastaví globální proměnné podle aktuálních tree_properties a print_properties
	    super.setGlobalProperties();
		font_size = print_properties.getFontSize(); // akorát výšku písma změním podle vlastností pro tisk
	}

    /**
     * Draws a description of one attribute to one node.
     * @param g Graphics object
     * @param description the description to be drawn
     * @param x x-coordinate
     * @param y y-coordinate
     */
	protected void drawDescription(Graphics g, String description, int x, int y) {
		// napíše obsah jednoho atributu k vrcholu - voláno funkcí nakresli_strom z rodičovského objektu
		Graphics2D g2D = (Graphics2D)g;

		String font_family = print_properties.getFontFamily();
		/*int coding = print_properties.getCharacterCoding();
		kodovany = CharCode.isolatin2ToUnicode (description); // nejprve převedu řetězec do unicodu
		String kodovany2;
		if (coding == CharCode.coding_semi_graphics) { // převede se do ascii s carkami
		    kodovany2 = new String (CharCode.unicodeToCAscii(kodovany));
	    }
		else if (coding == CharCode.coding_ascii) { // převede se do ascii i bez carek
		    kodovany2 = new String (CharCode.unicodeToAscii(kodovany));
		    }
			else kodovany2 = kodovany;

		if (coding == CharCode.coding_graphics)
   			kresliObrazkemText(g,kodovany,x,y,font_family,font_size,tree_properties.getColorWriting(),5);
    	else
			g2D.drawString (kodovany2,x,y);
        */
        g2D.drawString(description,x,y);
        /*if (coding == CharCode.coding_semi_graphics)
			kresliHackyKrouzky(g2D, kodovany2, kodovany, x, y, font_size);
        */
	} // drawDescription


	/**
	 * Sets the print properties of the tree.
     * @param new_properties new print properties
	 */
	public void setPrintProperties (PrintTreeProperties new_properties) {
		print_properties = new_properties;
	}

	/**
	 * Returns the print properties of the tree.
     * @return the print properties
	 */
	public PrintTreeProperties getPrintProperties () {
	    return print_properties;
	}

    /**
     * Prints the forest to the graphic object g.
     * @param g Graphics2D object to be drawn to
     * @param max_x x-size of the printable area
     * @param max_y y-size of the printable area
     */
	public void print (Graphics2D g, /*NGForest forest_printed,*/ double max_x, double max_y) { // vykreslení stromů pro tiskárnu
		// vejít se musejí do max_x * max_y
        //debug ("\nNGForestPrint.print: Entering the function.");

		if (forest == null) {
            debug ("\nNGForestPrint.print: There is no forest.");
            return; // žádný strom k nakreslení
        }
        if (forest.getTrees() == null) return; // žádný strom k nakreslení

		vypocti_nakresleni (g);

		// teď mám rozměry lesa
		int print_forest_x = forest_width;
		int print_forest_y = forest_height;

		// porovnám rozměry stromu s maximálními povolenými a kdyžtak provedu scale
		//debug ("\nRozměry lesa jsou: " + forest_width + " x " + forest_height);
		//debug ("\nMaximální možná velikost je: " + max_x + " x " + max_y);
		double scale_x = 1.0;
		double scale_y = 1.0;
		double orig_scale_x = 1.0;
		double orig_scale_y = 1.0;
		if (print_forest_x > max_x*0.97) scale_x = orig_scale_x = max_x*0.97 / (double)print_forest_x; // !!! ta 0.97, aby mi nepřelézal strom vpravo ve vyjímečných případech; je to berlička
		if (print_forest_y > max_y*0.97) scale_y = orig_scale_y = max_y*0.97 / (double)print_forest_y; // a stejná berlička pro přelézání dole
		if (!(scale_x == 1.0 && scale_y == 1.0)) {
			if (print_properties.getKeepRatio()) { // pokud chce uživatel zachovat poměr stran
				if (scale_x < scale_y) scale_y = scale_x;
				else scale_x = scale_y;
			}
			//debug("\nProvádím scale hodnotami " + scale_x + ", " + scale_y);
			g.scale(scale_x,scale_y);
		}

		// nyní, pokud chce uživatel centrovat, posunu souřadnice
		if (print_properties.getCenter()) {
			//debug ("\nCentruji");
			int dx = (int)(max_x - print_forest_x*scale_x/orig_scale_x);
			int dy = (int)(max_y - print_forest_y*scale_y/orig_scale_y);
			if (dx < 0) dx = 0;
			if (dy < 0) dy = 0;
		    g.translate((int)(dx/2/scale_x), (int)(dy/2/scale_y));
		}

        printReferences(g);
        printForest(g);
        printChosen(g, tree_properties);

	} // print


	/*public static void kresliHacekKrouzek (Graphics2D g, String text, int x, int y, int font_size, int pozice) { // namaluje háček nebo kroužek nad písmeno na pozici 'pozice'
		String zacatek = new String(text.substring(0,pozice)); // vezmu začátek řetězce až k onomu znaku
		int zac_delka = g.getFontMetrics().stringWidth(zacatek); // a spočtu, kde je ten znak; bohužel to funguje spolehlivě jen u neproporc. fontu
		int pozvednuti = 0; // u velkých písmen bude nenulové, háček či kroužek musí být výš
		char znak = text.charAt(pozice); // tento znak se bude označovat diakritikou
		if (znak >= 'C' && znak <= 'Z') pozvednuti = 2; // pozvednutí u velkých písmen
		g.scale(0.5,0.5); // abych kreslil tenčí čáru
		if (znak=='u' || znak=='U') { // kreslí se kroužek
			g.drawOval(2*(x+zac_delka+2)+1, 2*(y-font_size+2-pozvednuti), 4, 4);
		}
		else { // kreslí se háček
			g.drawLine(2*(x+zac_delka+4)-1, 2*(y-font_size+4-pozvednuti), 2*(x+zac_delka+3)-1, 2*(y-font_size+3-pozvednuti));
			g.drawLine(2*(x+zac_delka+4)-1, 2*(y-font_size+4-pozvednuti), 2*(x+zac_delka+5)-1, 2*(y-font_size+3-pozvednuti));
		}
		g.scale(2.0,2.0); // vrátím lupu zpátky
	}*/

	/*public static void kresliHackyKrouzky (Graphics2D g, String text, String puvodni, int x, int y, int font_size) { // namaluje háčky a kroužky nad písmena, která se liší v 'text' a 'puvodni'
		int pocet_znaku = text.length();
		for (int i=0; i<pocet_znaku; i++) { // přes celý řetězec
			if (text.charAt(i) != puvodni.charAt(i)) {
				//debug ("\nOháčkovávám znak " + text.charAt(i) + "; výška fontu je: " + font_size);
				kresliHacekKrouzek (g, text, x, y, font_size, i);
			}
		}
	}*/

	/*public static void kresliObrazkemText (Graphics g, String text, int x, int y, String font_family, int font_size, Color font_color, int zvetseni) {
		// vytvoří z textu obrázek a nakreslí na udané pozice x,y udaným fontem a velikostí; pro vyhlazení fontu použije udané zvětšení
	    Font font = new Font (font_family, Font.PLAIN, font_size*zvetseni); // vyberu velký font, aby byl hodně podrobný; pak ho zmenším
	    Rectangle2D velikost = font.getStringBounds(text,new FontRenderContext(new AffineTransform(),false,false)); // takto veliký bude ten text
	    int sirka = (int)velikost.getWidth();
		int vyska = (int)velikost.getHeight()+zvetseni*4; // přičteno pro části znaků pod řádkem a háčky nad velkými znaky

		BufferedImage image = new BufferedImage(sirka,vyska,BufferedImage.TYPE_INT_ARGB);
	    Graphics2D gr = image.createGraphics();
		gr.setBackground(new Color(16777215,true));
		//gr.clearRect(0,0,sirka,vyska); // vybarvím podklad transparentní bílou

		gr.setPaint(font_color);

		gr.setFont(font);
		gr.drawString(text,0,vyska-(int)(zvetseni*2.4)); //!!!
		g.drawImage(image,x,y-vyska/zvetseni+2,sirka/zvetseni,vyska/zvetseni,null); // zmensim obrazek do spravne velikosti
	}*/

    /**
     * Sets the color for printing a reference. May be black and white for printing on a printer, that is why the function is overridden here.
     * @param node
     * @param pattern a reference pattern
     * @param value_depend_value the value of the attribute that may influence the color
     * @return the color
     */
    protected Color determineColor(TNode node, ReferencePattern pattern, String value_depend_value) {
        Color color = pattern.getGeneralColor();
        //debug("\nNGTreeView.determineColor: Getting value dependent color for value '" + value_depend_value + "'");
        if (value_depend_value != null) {
            if (value_depend_value.length() > 0) { // atribut má neprázdnou hodnotu
                color = pattern.getValueDependentColor(value_depend_value);
            }
        }
        if (this.getPrintProperties().getBlackWhite()) { // for black and white printing
            color = Color.GRAY;
        }
        //debug("\nNGTreeView.determineColor: the color is set to: " + color);
        return color;
    }


}
