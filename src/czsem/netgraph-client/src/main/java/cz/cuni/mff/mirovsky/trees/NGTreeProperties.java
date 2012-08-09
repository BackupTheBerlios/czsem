package cz.cuni.mff.mirovsky.trees;

import java.awt.*;
import javax.swing.DefaultListModel;
import cz.cuni.mff.mirovsky.CharCode;

/**
 * A class that keeps information how to display trees.
 */


public class NGTreeProperties extends Object {

    final static private float dash1[] = {7.0f};
    final static private float dot_and_dash1[] = {7.0f,5.0f,2.0f,5.0f};
    final static private float dots1[] = {2.0f,5.0f};
    final static private BasicStroke stroke_dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 7.0f, dash1, 0.0f);
    final static private BasicStroke stroke_line = new BasicStroke();
    final static private BasicStroke stroke_dot_and_dashed = new BasicStroke(1.0f, // Width
                               BasicStroke.CAP_BUTT,    // End cap
                               BasicStroke.JOIN_MITER,    // Join style
                               7.0f,                     // Miter limit
                               dot_and_dash1,            // Dash pattern
                               0.0f);                     // Dash phase
    final static private BasicStroke stroke_dotted = new BasicStroke(1.0f, // Width
                               BasicStroke.CAP_BUTT,    // End cap
                               BasicStroke.JOIN_MITER,    // Join style
                               7.0f,                     // Miter limit
                               dots1,                    // Dash pattern
                               0.0f);                     // Dash phase

    /** a type of stroke for references - a solid line */
    public final static int STROKE_LINE = 0;
    /** a type of stroke for references - a dashed line */
    public final static int STROKE_DASHED = 1;
    /** a type of stroke for references - a dotted line */
    public final static int STROKE_DOTTED = 2;
    /** a type of stroke for references - a dotted and dashed line */
    public final static int STROKE_DOT_AND_DASHED = 3;

    /** a color scheme for painting trees - a default colored scheme */
    public final static int COLOR_SCHEME_DEFAULT = 0;
    /** a color scheme for painting trees - a black and white scheme for printing on black and white printers */
    public final static int COLOR_SCHEME_BLACK_AND_WHITE = 1;
    /** a color scheme for painting trees - a dim scheme for displaying a previously found tree after a new query is launched */
    public final static int COLOR_SCHEME_DIM = 2;

    private int font_size;

    private int color_scheme;

    private int coding_in_printing; // kódování, které se má použít při tisku stromu na tiskárnu
	private boolean use_ascii_in_tree; // má se při kreslení stromu na obrazovku tento kreslit bez háčků a čárek?

    private int direction; // smer vykreslovani vrcholu ve stromu (levo-pravy/pravo-levy)

    /** a left-right direction for ordering nodes (e.g. for Czech, English) */
    static final public int DIRECTION_LEFT_RIGHT = 1; // napr. cestina, anglictina
    /** a right-left direction for ordering nodes (e.g. for Arabic) */
    static final public int DIRECTION_RIGHT_LEFT = 2; // napr. arabstina

	private int odsazeni; // horizontalni posunuti zacatku slov ve stromu
	private int diameter; //prumer; // prumer kolecka zobrazeneho vrcholu
	private int diameter_chosen; //prumer_vybrany; // prumer kolecka vybraneho vrcholu
	private int diameter_multi; // průměr kružnice, která se přimaluje navíc u vrcholů s více sadami atributů
	private int diameter_multi_chosen; // průměr kružnice, která se přimaluje navíc u vrcholů s více sadami atributů - u vybraného vrcholu
	private int space_above_text; // vertikální mezera mezi vrcholem a textem nad textem
	private int space_below_text; // vertikální mezera mezi vrcholem a textem pod textem
	private int space_above_tree; // odsazení stromu od horního okraje
	private int space_below_tree; // odsazení stromu od spodního okraje
	private int vertical_space_between_texts; // vertikální mezera mezi dvěma popisky jednoho vrcholu
	private int space_above_divider; // vertikální mezera před oddělovačem více sad atributů
	private int space_below_divider; // vertikální mezera za oddělovačem více sad atributů

	private boolean show_attr_names; // mají se zobrazovat i jména atributů?
	private boolean show_null_values; // mají nullové hodnoty atributů zabírat místo? - neimplementováno
	private boolean show_multiple_mark; // mají se graficky zvýrazňovat vrcholy s více sadami atributů?
	private boolean show_multiple_mark_chosen; // má se graficky zvýrazňovat vrchol s více sadami atributů alespoň, když je vybraný?
	private boolean show_multiple_sets; // mají se zobrazovat najednou všechny sady u všech vrcholů?
	private boolean show_multiple_sets_chosen; // mají se zobrazovat najednou všechny sady alespoň u vybraného vrcholu?
	private boolean show_multiple_values; // mají se zobrazovat alternativní hodnoty atributu u všech vrcholů?
	private boolean show_multiple_values_chosen; // mají se zobrazovat alternativní hodnoty atributu alespoň u vybraného vrcholu?
	private boolean show_lemma_variants; // mají se zobrazovat varianty lemmat?
	private boolean show_lemma_comments; // mají se zobrazovat varianty a vysvětlivky lemmat?
    private boolean show_hidden_nodes; // mají se zobrazovat skrývané vrcholy?
    private boolean highlight_optional_nodes; // mají se zvýrazňovat optional vrcholy?
    private boolean highlight_zero_occurrence_nodes; // mají se zvýrazňovat zero-occurrence vrcholy?
    private boolean highlight_transitive_edges; // mají se zvýrazňovat tranzitivní hrany?

    private BasicStroke stroke_transitive_true; // typ čáry hrany s _transitive=true
    private BasicStroke stroke_transitive_exclusive; // typ čáry hrany s _transitive=exclusive

    private Color color_optional_node; // barva zvýraznění optional vrcholu
    private Color color_zero_occurrence_node; // barva zvýraznění zero-occurrence vrcholu
    private Color color_background; // barva pozadi pod stromem
	private Color color_writing; // barva písma
	private Color color_edge; // barva hran stromu
	private Color color_edge_matching; // barva hran stromu v části matchující s dotazem
	private Color color_edge_hidden; // barva hran stromu vedoucích k skrývaným vrcholům
	private Color color_edge_hidden_matching; // barva hran stromu vedoucích k skrývaným vrcholům v části matchující s dotazem
	private Color color_circle; // barva kruznice kolem kolecka vrcholu
	private Color color_fullcircle; // barva kolecka vrcholu
	private Color color_circle_matching; // barva kruznice kolem kolecka vrcholu matchujícího s dotazem
	private Color color_fullcircle_matching; // barva kolecka vrcholu matchujícího s dotazem
	private Color color_circle_chosen; // barva kruznice u vybraneho vrcholu
	private Color color_fullcircle_chosen; // barva kolecka u vybraneho vrcholu
	private Color color_circle_hidden; // barva kruznice u skryvaneho vrcholu
	private Color color_fullcircle_hidden; // barva kolecka u skryvaneho vrcholu
	private Color color_circle_hidden_matching; // barva kruznice u skryvaneho vrcholu matchujícího s dotazem
	private Color color_fullcircle_hidden_matching; // barva kolecka u skryvaneho vrcholu matchujícího s dotazem
	private Color color_circle_hidden_chosen; // barva kruznice u vybraneho skryvaneho vrcholu
	private Color color_fullcircle_hidden_chosen; // barva kolecka u vybraneho skryvaneho vrcholu
	private Color color_circle_multiple; // barva další kružnice vrcholu s více sadami atributů
	private Color color_circle_multiple_matching; // barva další kružnice vrcholu s více sadami atributů, matchujícího s dotazem
	private Color color_circle_multiple_chosen; // barva další kružnice u vybraného vrcholu
	private Color color_circle_multiple_hidden; // barva další kružnice skrývaného vrcholu s více sadami atributů
	private Color color_circle_multiple_hidden_matching; // barva další kružnice skrývaného vrcholu s více sadami atributů, matchujícího s dotazem
	private Color color_circle_multiple_hidden_chosen; // barva další kružnice u vybraného skrývaného vrcholu
	private Color color_multiple_sets_divider; // barva čáry oddělující více sad vrcholů

    /**
     * Creates a new object for tree properties.
     */
	public NGTreeProperties () { // konstruktor
	    setCodingInPrinting (CharCode.coding_unicode);
	    setUseAsciiInTree(false);
        setDirection(DIRECTION_LEFT_RIGHT);
        setDiameterChosen(10);
		setDiameter(8);
		setDiameterMultiChosen(6);
		setDiameterMulti(2);
		setFontSize(12); // velikost fontu
		setOdsazeni(20);

        setColorScheme(COLOR_SCHEME_DEFAULT);

        setShowAttrNames(false);
	    setShowNullValues(true);
		setSpaceAboveText(11);
		setSpaceBelowText(15);
		setSpaceAboveTree(18);
		setSpaceBelowTree(10);
		setVerticalSpaceBetweenTexts(1);
		setSpaceAboveDivider(4);
		setSpaceBelowDivider(2);

		setShowMultipleMark(true);
		setShowMultipleMarkChosen(true);

		setShowMultipleSets(false);
		setShowMultipleSetsChosen(true);
		setShowMultipleValues(true);
		setShowMultipleValuesChosen(true);

		setShowLemmaVariants(true);
		setShowLemmaComments(true);
        setShowHiddenNodes(false);

        setHighlightOptionalNodes(true); // mají se zvýrazňovat optional vrcholy?
        setHighlightZeroOccurrenceNodes(true); // mají se zvýrazňovat zero-occurrence vrcholy?
        setHighlightTransitiveEdges(true); // mají se zvýrazňovat tranzitivní hrany?

        setStrokeTransitiveEdgeTrue(STROKE_DOTTED); // typ čáry hrany s _transitive=true
        setStrokeTransitiveEdgeExclusive(STROKE_DASHED); // typ čáry hrany s _transitive=exclusive
    }

    /**
     * Returns the actual color scheme for painting trees.
     * Possible return values are:
     * <br>&nbsp; &nbsp; COLOR_SCHEME_DEFAULT - a colored color scheme for printing on black and white printers
     * <br>&nbsp; &nbsp; COLOR_SCHEME_BLACK_AND_WHITE - a black and white color scheme for printing on black and white printers
     * <br>&nbsp; &nbsp; COLOR_SCHEME_DIM - a dim color scheme for printing dimmed trees (a previously found tree after a new query is launched)
     */
    public int getColorScheme() {
        return color_scheme;
    }

    /**
     * Sets a color scheme for painting trees.
     * @param scheme a color scheme. Possible values are:
     * <br>&nbsp; &nbsp; COLOR_SCHEME_DEFAULT - a colored color scheme for printing on black and white printers
     * <br>&nbsp; &nbsp; COLOR_SCHEME_BLACK_AND_WHITE - a black and white color scheme for printing on black and white printers
     * <br>&nbsp; &nbsp; COLOR_SCHEME_DIM - a dim color scheme for printing dimmed trees (a previously found tree after a new query is launched)
     */
    public void setColorScheme(int scheme) { // sets a predefined color scheme
        color_scheme = scheme;
        switch (scheme) {
            case COLOR_SCHEME_DEFAULT:
                setColorBackground(Color.white);
                setColorWriting(Color.black);
                setColorEdge(Color.blue);
                setColorEdgeHidden(Color.gray);
                setColorEdgeMatching(new Color(100,240,100));
                setColorEdgeHiddenMatching(Color.magenta);
                setColorCircle(Color.blue);
                setColorCircleMatching(new Color(10,140,0));
                setColorFullcircle(Color.cyan);
                setColorFullcircleMatching(new Color(100,240,100));
                setColorCircleChosen(Color.red);
                setColorFullcircleChosen(Color.yellow);
                setColorCircleHidden(new Color(90,90,90));
                setColorCircleHiddenMatching(new Color(120,90,90));
                setColorFullcircleHidden(new Color(200,200,200));
                setColorFullcircleHiddenMatching(new Color(230,200,200));
                setColorCircleHiddenChosen(Color.red);
                setColorFullcircleHiddenChosen(Color.green);
                setColorCircleMultiple(Color.blue);
                setColorCircleMultipleMatching(Color.red);
                setColorCircleMultipleChosen(Color.red);
                setColorCircleMultipleHidden(new Color(90,90,90));
                setColorCircleMultipleHiddenMatching(new Color(120,90,90));
                setColorCircleMultipleHiddenChosen(new Color(200,200,200));
                setColorMultipleSetsDivider(new Color(50,100,50));
                setColorOptionalNode(getColorCircle());
                setColorZeroOccurrenceNode(getColorCircle());
                break;
            case COLOR_SCHEME_BLACK_AND_WHITE:
                setColorBackground(Color.white);
                setColorCircle(Color.black);
                setColorCircleChosen(Color.black);
                setColorCircleHidden(Color.gray);
                setColorCircleHiddenChosen(Color.gray);
                setColorCircleHiddenMatching(Color.gray);
                setColorCircleMatching(Color.black);
                setColorEdge(Color.gray);
                setColorEdgeHidden(Color.gray);
                setColorEdgeHiddenMatching(Color.gray);
                setColorEdgeMatching(Color.black);
                setColorFullcircle(Color.gray);
                setColorFullcircleChosen(Color.white);
                setColorFullcircleHidden(Color.gray);
                setColorFullcircleHiddenChosen(Color.white);
                setColorFullcircleHiddenMatching(Color.white);
                setColorFullcircleMatching(Color.white);
                setColorMultipleSetsDivider(Color.black);
                setColorWriting(Color.black);
                setColorCircleMultiple(Color.black);
                setColorCircleMultipleChosen(Color.black);
                setColorCircleMultipleHidden(Color.gray);
                setColorCircleMultipleHiddenChosen(Color.gray);
                setColorCircleMultipleHiddenMatching(Color.gray);
                setColorCircleMultipleMatching(Color.black);
                setColorOptionalNode(getColorCircle());
                setColorZeroOccurrenceNode(getColorCircle());
                break;
            case COLOR_SCHEME_DIM:
                setColorBackground(new Color(245,245,245));
                setColorCircle(Color.darkGray);
                setColorCircleChosen(Color.darkGray);
                setColorCircleHidden(Color.gray);
                setColorCircleHiddenChosen(Color.gray);
                setColorCircleHiddenMatching(Color.gray);
                setColorCircleMatching(Color.darkGray);
                setColorEdge(Color.gray);
                setColorEdgeHidden(Color.gray);
                setColorEdgeHiddenMatching(Color.gray);
                setColorEdgeMatching(Color.black);
                setColorFullcircle(Color.gray);
                setColorFullcircleChosen(Color.lightGray);
                setColorFullcircleHidden(Color.gray);
                setColorFullcircleHiddenChosen(Color.lightGray);
                setColorFullcircleHiddenMatching(Color.lightGray);
                setColorFullcircleMatching(Color.lightGray);
                setColorMultipleSetsDivider(Color.darkGray);
                setColorWriting(Color.darkGray);
                setColorCircleMultiple(Color.darkGray);
                setColorCircleMultipleChosen(Color.darkGray);
                setColorCircleMultipleHidden(Color.gray);
                setColorCircleMultipleHiddenChosen(Color.gray);
                setColorCircleMultipleHiddenMatching(Color.gray);
                setColorCircleMultipleMatching(Color.darkGray);
                setColorOptionalNode(getColorCircle());
                setColorZeroOccurrenceNode(getColorCircle());
                break;
            default: setColorScheme(COLOR_SCHEME_DEFAULT);
        }
    }

    public void setHighlightOptionalNodes(boolean highlight) { // mají se zvýrazňovat optional vrcholy?
        highlight_optional_nodes = highlight;
    }
    public boolean getHighlightOptionalNodes() { // mají se zvýrazňovat optional vrcholy?
        return highlight_optional_nodes;
    }

    public void setHighlightZeroOccurrenceNodes(boolean highlight) { // mají se zvýrazňovat zero-occurrence vrcholy?
        highlight_zero_occurrence_nodes = highlight;
    }
    public boolean getHighlightZeroOccurrenceNodes() { // mají se zvýrazňovat zero-occurrence vrcholy?
        return highlight_zero_occurrence_nodes;
    }

    public void setHighlightTransitiveEdges(boolean highlight) { // mají se zvýrazňovat tranzitivní hrany?
        highlight_transitive_edges = highlight;
    }
    public boolean getHighlightTransitiveEdges() { // mají se zvýrazňovat tranzitivní hrany?
        return highlight_transitive_edges;
    }

    public void setStrokeTransitiveEdgeTrue(int stroke) { // typ čáry hrany s _transitive=true
        stroke_transitive_true = getStroke(stroke);
    }
    public BasicStroke getStrokeTransitiveEdgeTrue() {
        return stroke_transitive_true;
    }

    public void setStrokeTransitiveEdgeExclusive(int stroke) { // typ čáry hrany s _transitive=exclusive
        stroke_transitive_exclusive = getStroke(stroke);
    }
    public BasicStroke getStrokeTransitiveEdgeExclusive() {
        return stroke_transitive_exclusive;
    }

    private BasicStroke getStroke(int stroke) { // vrátí čáru na základě jejího pojmenování
        switch (stroke) {
            case STROKE_LINE:
                return stroke_line;
            case STROKE_DASHED:
                return stroke_dashed;
            case STROKE_DOTTED:
                return stroke_dotted;
            case STROKE_DOT_AND_DASHED:
                return stroke_dot_and_dashed;
            default:
                return stroke_line;
        }
    }

    public void setColorOptionalNode(Color color) {
        color_optional_node = color;
    }
    public Color getColorOptionalNode() {
        return color_optional_node;
    }

    public void setColorZeroOccurrenceNode(Color color) {
        color_zero_occurrence_node = color;
    }
    public Color getColorZeroOccurrenceNode() {
        return color_zero_occurrence_node;
    }

    public void setCodingInPrinting (int coding) { // nastaví kódování při tisku na tiskárnu
		coding_in_printing = coding;
	}
	public int getCodingInPrinting() { // vrátí kódování při tisku na tiskárnu
		return coding_in_printing;
	}

	public void setUseAsciiInTree (boolean use_ascii) { // nastaví, zda se má strom vykreslovat bez háčků a čárek
		use_ascii_in_tree = use_ascii;
	}
	public boolean getUseAsciiInTree() { // má se strom vykreslovat bez háčků a čárek?
		return use_ascii_in_tree;
	}

	public void setDiameter(int new_diameter) { // nastaví nový průměr kolečka vrcholu
		diameter = new_diameter;
	}
	public int getDiameter() { // vrátí průměr kolečka vrcholu
		return diameter;
	}

        public void setDirection(int new_direction) { // nastaví levo-pravou nebo pravo-levou orientaci razeni uzlu
                direction = new_direction;
        }
        public int getDirection() { // vrátí nastavenou levo-pravou nebo pravo-levou orientaci razeni uzlu
                return direction;
        }

	public void setDiameterChosen(int new_diameter) { // nastaví nový průměr kolečka vybraného vrcholu
		diameter_chosen = new_diameter;
	}
	public int getDiameterChosen() { // vrátí průměr kolečka vybraného vrcholu
		return diameter_chosen;
	}

	public void setDiameterMulti(int new_diameter) { // nastaví nový průměr přidaného kolečka vrcholu s více sadami atributů
		diameter_multi = new_diameter;
	}
	public int getDiameterMulti() { // vrátí průměr přidaného kolečka vrcholu s více sadami atributů
		return diameter_multi;
	}

	public void setDiameterMultiChosen(int new_diameter) { // nastaví nový průměr přidaného kolečka vrcholu s více sadami atributů - pro vybraný vrchol
		diameter_multi_chosen = new_diameter;
	}
	public int getDiameterMultiChosen() { // vrátí průměr přidaného kolečka vybraného vrcholu s více sadami atributů
		return diameter_multi_chosen;
	}

	public void setShowAttrNames(boolean new_show) { // mají se před hodnotami zobrazovat jména atributů?
		show_attr_names = new_show;
	}
	public boolean getShowAttrNames() { // vrátí true, pokud se před hodnotami mají zobrazovat jména atributů
		return show_attr_names;
	}

	public void setShowNullValues(boolean new_show) { // mají prázdné hodnoty zabírat místo ve vykresleném stromě?
		show_null_values = new_show;
	}
	public boolean getShowNullValues() { // vrátí true, pokud prázdné hodnoty mají zabírat místo ve vykresleném stromě
		return show_null_values;
	}

	public void setShowMultipleMark(boolean new_show) { // mají se graficky zvýrazňovat vrcholy s více sadami atributů?
		show_multiple_mark = new_show;
	}
	public boolean getShowMultipleMark() { // vrátí true, pokud se mají graficky zvýrazňovat vrcholy s více sadami atributů
		return show_multiple_mark;
	}

	public void setShowMultipleMarkChosen(boolean new_show) { // má se graficky zvýrazňovat alespoň vybraný vrchol s více sadami atributů?
		show_multiple_mark_chosen = new_show;
	}
	public boolean getShowMultipleMarkChosen() { // vrátí true, pokud se má graficky zvýrazňovat alespoň vybraný vrchol s více sadami atributů
		return show_multiple_mark_chosen;
	}

	public void setShowMultipleSets(boolean new_show) { // mají se zobrazovat všechny sady atributů u všech vrcholů?
		show_multiple_sets = new_show;
	}
	public boolean getShowMultipleSets() { // vrátí true, pokud se mají zobrazovat všechny sady atributů u všech vrcholů
		return show_multiple_sets;
	}

	public void setShowMultipleSetsChosen(boolean new_show) { // mají se zobrazovat všechny sady atributů alespoň u vybraného vrcholu?
		show_multiple_sets_chosen = new_show;
	}
	public boolean getShowMultipleSetsChosen() { // vrátí true, pokud se mají zobrazovat všechny sady atributů alespoň u vybraného vrcholu
		return show_multiple_sets_chosen;
	}

	public void setShowMultipleValues(boolean new_show) { // mají se zobrazovat všechny hodnoty atributu u všech vrcholů?
		show_multiple_values = new_show;
	}
	public boolean getShowMultipleValues() { // vrátí true, pokud se mají zobrazovat všechny hodnoty atributu u všech vrcholů
		return show_multiple_values;
	}

	public void setShowMultipleValuesChosen(boolean new_show) { // mají se zobrazovat všechny hodnoty atributu u vybraného vrcholu?
		show_multiple_values_chosen = new_show;
	}
	public boolean getShowMultipleValuesChosen() { // vrátí true, pokud se mají zobrazovat všechny hodnoty atributu u vybraného vrcholu
		return show_multiple_values_chosen;
	}

	public void setShowLemmaVariants(boolean new_show) { // mají se zobrazovat varianty lemmat?
		show_lemma_variants = new_show;
		if (! new_show) { // pokud se nemají zobrazovat varianty, nemohou se zobrazovat ani vysvětlivky
			show_lemma_comments = false;
		}
	}
	public boolean getShowLemmaVariants() { // vrátí true, pokud se mají zobrazovat varianty lemmat
		return show_lemma_variants;
	}

	public void setShowLemmaComments(boolean new_show) { // mají se zobrazovat vysvětlivky a varianty lemmat?
		show_lemma_comments = new_show;
		if (new_show) { // pokud se mají zobrazovat vysvětlivky, musejí se zobrazovat i varianty
			show_lemma_variants = true;
		}
	}
	public boolean getShowLemmaComments() { // vrátí true, pokud se mají zobrazovat vysvětlivky a varianty lemmat
		return show_lemma_comments;
	}

        public void setShowHiddenNodes(boolean show) { // mají se zobrazovat skrývané vrcholy?
            show_hidden_nodes = show;
        }
        public boolean getShowHiddenNodes() { // vrátí true, pokud se mají zobrazovat skrývané vrcholy
                return show_hidden_nodes;
        }


	public void setSpaceAboveText(int new_space) { // nastaví vertikální mezeru nad textem od vrcholu
		space_above_text = new_space;
	}
	public int getSpaceAboveText() { // vrátí vertikální mezeru mezi textem a vrcholem
		return space_above_text;
	}

	public void setSpaceBelowText(int new_space) { // nastaví vertikální mezeru nad textem od vrcholu
		space_below_text = new_space;
	}
	public int getSpaceBelowText() { // vrátí vertikální mezeru mezi textem a vrcholem
		return space_below_text;
	}

	public void setSpaceAboveTree(int new_space) { // nastaví odsazení stromu od horního okraje
		space_above_tree = new_space;
	}
	public int getSpaceAboveTree() { // vrátí odsazení stromu od horního okraje
		return space_above_tree;
	}

	public void setSpaceBelowTree(int new_space) { // nastaví odsazení stromu od spodního okraje
		space_below_tree = new_space;
	}
	public int getSpaceBelowTree() { // vrátí odsazení stromu od horního okraje
		return space_below_tree;
	}

	public void setVerticalSpaceBetweenTexts(int new_space) { // nastaví mezeru mezi popisky jednoho vrcholu
		vertical_space_between_texts = new_space;
	}
	public int getVerticalSpaceBetweenTexts() { // vrátí vertikální mezeru mezi popisky jednoho vrcholu
		return vertical_space_between_texts;
	}

	public void setSpaceAboveDivider(int new_space) { // nastaví mezeru před oddělovačem více sad atributů
		space_above_divider = new_space;
	}
	public int getSpaceAboveDivider() { // vrátí vertikální mezeru před oddělovačem více sad atributů
		return space_above_divider;
	}

	public void setSpaceBelowDivider(int new_space) { // nastaví mezeru za oddělovačem více sad atributů
		space_below_divider = new_space;
	}
	public int getSpaceBelowDivider() { // vrátí vertikální mezeru za oddělovačem více sad atributů
		return space_below_divider;
	}

	/**
	 * Sets the new value of font_size
	 */
	public void setFontSize (int new_font_size) {
		font_size = new_font_size;
	}
	/**
	 * Returns the current value of font_size
	 */
	public int getFontSize() {
		return font_size;
	}

	/**
	 * Sets the new value of fullcircle color of a selected node
	 */
	public void setColorFullcircleChosen (Color new_color) {
		color_fullcircle_chosen = new_color;
	}
	/**
	 * Returns the current value of fullcircle color of a selected node
	 */
	public Color getColorFullcircleChosen() {
		return color_fullcircle_chosen;
	}

	/**
	 * Sets the new value of circle color of a selected node
	 */
	public void setColorCircleChosen (Color new_color) {
		color_circle_chosen = new_color;
	}
	/**
	 * Returns the current value of circle color of a selected node
	 */
	public Color getColorCircleChosen() {
		return color_circle_chosen;
	}

	/**
	 * Sets the new value of fullcircle color
	 */
	public void setColorFullcircle (Color new_color) {
		color_fullcircle = new_color;
	}
	/**
	 * Returns the current value of fullcircle color
	 */
	public Color getColorFullcircle() {
		return color_fullcircle;
	}

	/**
	 * Sets the new value of fullcircle color of a node matching with a query
	 */
	public void setColorFullcircleMatching (Color new_color) {
		color_fullcircle_matching = new_color;
	}
	/**
	 * Returns the current value of fullcircle color of a node matching with a query
	 */
	public Color getColorFullcircleMatching() {
		return color_fullcircle_matching;
	}

	/**
	 * Sets the new value of circle color
	 */
	public void setColorCircle (Color new_color) {
		color_circle = new_color;
	}
	/**
	 * Returns the current value of circle color
	 */
	public Color getColorCircle() {
		return color_circle;
	}

	/**
	 * Sets the new value of circle color of a node matching with a query
	 */
	public void setColorCircleMatching (Color new_color) {
		color_circle_matching = new_color;
	}
	/**
	 * Returns the current value of circle color of a node matching with a query
	 */
	public Color getColorCircleMatching() {
		return color_circle_matching;
	}

	/**
	 * Sets the new value of multiple circle color
	 */
	public void setColorCircleMultiple (Color new_color) {
		color_circle_multiple = new_color;
	}
	/**
	 * Returns the current value of the multiple circle color
	 */
	public Color getColorCircleMultiple() {
		return color_circle_multiple;
	}

	/**
	 * Sets the new value of multiple circle color of a node matching with a query
	 */
	public void setColorCircleMultipleMatching (Color new_color) {
		color_circle_multiple_matching = new_color;
	}
	/**
	 * Returns the current value of the multiple circle color of a node matching with a query
	 */
	public Color getColorCircleMultipleMatching() {
		return color_circle_multiple_matching;
	}

	/**
	 * Sets the new value of the multiple circle color of a selected node
	 */
	public void setColorCircleMultipleChosen(Color new_color) {
		color_circle_multiple_chosen = new_color;
	}
	/**
	 * Returns the current value of the multiple circle color of a selected node
	 */
	public Color getColorCircleMultipleChosen() {
		return color_circle_multiple_chosen;
	}

	/**
	 * Sets the new value of multiple circle color of a hidden node
	 */
	public void setColorCircleMultipleHidden (Color new_color) {
		color_circle_multiple_hidden = new_color;
	}
	/**
	 * Returns the current value of the multiple circle color of a hidden node
	 */
	public Color getColorCircleMultipleHidden() {
		return color_circle_multiple_hidden;
	}

	/**
	 * Sets the new value of multiple circle color of a hidden node matching with a query
	 */
	public void setColorCircleMultipleHiddenMatching (Color new_color) {
		color_circle_multiple_hidden_matching = new_color;
	}
	/**
	 * Returns the current value of the multiple circle color of a hidden node matching with a query
	 */
	public Color getColorCircleMultipleHiddenMatching() {
		return color_circle_multiple_hidden_matching;
	}

	/**
	 * Sets the new value of multiple circle color of a selected hidden node
	 */
	public void setColorCircleMultipleHiddenChosen (Color new_color) {
		color_circle_multiple_hidden_chosen = new_color;
	}
	/**
	 * Returns the current value of the multiple circle color of a chosen hidden node
	 */
	public Color getColorCircleMultipleHiddenChosen() {
		return color_circle_multiple_hidden_chosen;
	}

	/**
	 * Sets the new colour of multiple sets divider
	 */
	public void setColorMultipleSetsDivider (Color new_color) {
		color_multiple_sets_divider = new_color;
	}
	/**
	 * Returns the current colour of multiple sets divider
	 */
	public Color getColorMultipleSetsDivider() {
		return color_multiple_sets_divider;
	}

	/**
	 * Sets the new value of circle color of a hidden node
	 */
	public void setColorCircleHidden (Color new_color) {
		color_circle_hidden = new_color;
	}
	/**
	 * Returns the current value of circle color of a hidden node
	 */
	public Color getColorCircleHidden() {
		return color_circle_hidden;
	}

	/**
	 * Sets the new value of circle color of a hidden node matching with a query
	 */
	public void setColorCircleHiddenMatching (Color new_color) {
		color_circle_hidden_matching = new_color;
	}
	/**
	 * Returns the current value of circle color of a hidden node matching with a query
	 */
	public Color getColorCircleHiddenMatching() {
		return color_circle_hidden_matching;
	}

	/**
	 * Sets the new value of circle color of a hidden selected node
	 */
	public void setColorCircleHiddenChosen (Color new_color) {
		color_circle_hidden_chosen = new_color;
	}
	/**
	 * Returns the current value of circle color of a hidden selected node
	 */
	public Color getColorCircleHiddenChosen() {
		return color_circle_hidden_chosen;
	}

	/**
	 * Sets the new value of fullcircle color of a hidden node
	 */
	public void setColorFullcircleHidden (Color new_color) {
		color_fullcircle_hidden = new_color;
	}
	/**
	 * Returns the current value of fullcircle color of a hidden node
	 */
	public Color getColorFullcircleHidden() {
		return color_fullcircle_hidden;
	}

	/**
	 * Sets the new value of fullcircle color of a hidden node matching with a query
	 */
	public void setColorFullcircleHiddenMatching (Color new_color) {
		color_fullcircle_hidden_matching = new_color;
	}
	/**
	 * Returns the current value of fullcircle color of a hidden node matching with a query
	 */
	public Color getColorFullcircleHiddenMatching() {
		return color_fullcircle_hidden_matching;
	}

	/**
	 * Sets the new value of fullcircle color of a chosen hidden node
	 */
	public void setColorFullcircleHiddenChosen (Color new_color) {
		color_fullcircle_hidden_chosen = new_color;
	}
	/**
	 * Returns the current value of fullcircle color of a chosen hidden node
	 */
	public Color getColorFullcircleHiddenChosen() {
		return color_fullcircle_hidden_chosen;
	}

	/**
	 * Sets the new value of edge color
	 */
	public void setColorEdge (Color new_color) {
		color_edge = new_color;
	}
	/**
	 * Returns the current value of edge color
	 */
	public Color getColorEdge() {
		return color_edge;
	}

	/**
	 * Sets the new value of edge color, part of a subtree matching with a query
	 */
	public void setColorEdgeMatching (Color new_color) {
		color_edge_matching = new_color;
	}
	/**
	 * Returns the current value of edge color, part of a subtree matching with a query
	 */
	public Color getColorEdgeMatching() {
		return color_edge_matching;
	}

	/**
	 * Sets the new value of color of an edge to a hidden node
	 */
	public void setColorEdgeHidden (Color new_color) {
		color_edge_hidden = new_color;
	}
	/**
	 * Returns the current value of color of an edge to a hidden node
	 */
	public Color getColorEdgeHidden() {
		return color_edge_hidden;
	}

	/**
	 * Sets the new value of color of an edge to a hidden node, part of a subtree matching with a query
	 */
	public void setColorEdgeHiddenMatching (Color new_color) {
		color_edge_hidden_matching = new_color;
	}
	/**
	 * Returns the current value of color of an edge to a hidden node, part of a subtree matching with a query
	 */
	public Color getColorEdgeHiddenMatching() {
		return color_edge_hidden_matching;
	}

	/**
	 * Sets the new value of writing color
	 */
	public void setColorWriting (Color new_color) {
		color_writing = new_color;
	}
	/**
	 * Returns the current value of writing color
	 */
	public Color getColorWriting() {
		return color_writing;
	}

	/**
	 * Sets the new value of background color
	 */
	public void setColorBackground (Color new_color) {
		color_background = new_color;
	}
	/**
	 * Returns the current value of background color
	 */
	public Color getColorBackground() {
		return color_background;
	}

	/**
	 * Sets the new value of odsazeni
	 */
	public void setOdsazeni (int new_odsazeni) {
		odsazeni = new_odsazeni;
	}
	/**
	 * Returns the current value of odsazeni
	 */
	public int getOdsazeni() {
		return odsazeni;
	}

} // class NGTreeProperties

