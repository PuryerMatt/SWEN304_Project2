
public class Book {
	private int isbn;
	private String title;
	private int edition_no;
	private int numofcop;
	private int numleft;

	public Book(int i, String t, int e, int numo, int numl){
		isbn = i;
		title = t;
		edition_no = e;
		numofcop = numo;
		numleft = numl;
	}

	public int getIsbn() {
		return isbn;
	}

	public String getTitle() {
		return title;
	}

	public int getEdition_no() {
		return edition_no;
	}

	public int getNumofcop() {
		return numofcop;
	}

	public int getNumleft() {
		return numleft;
	}
}
