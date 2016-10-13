
public class Book {
	private int isbn;
	private String title;
	private int editionNo;
	private int numOfCop;
	private int numLeft;



	public Book(int i, String t, int e, int numo, int numl){
		isbn = i;
		title = t;
		editionNo = e;
		numOfCop = numo;
		numLeft = numl;
	}

	public int getIsbn() {
		return isbn;
	}

	public String getTitle() {
		return title;
	}

	public int getEditionNo() {
		return editionNo;
	}

	public int getNumOfCop() {
		return numOfCop;
	}

	public int getNumLeft() {
		return numLeft;
	}

	public void setIsbn(int isbn) {
		this.isbn = isbn;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setEditionNo(int edition_no) {
		this.editionNo = edition_no;
	}

	public void setNumOfCop(int numOfCop) {
		this.numOfCop = numOfCop;
	}

	public void setNumleft(int numLeft) {
		this.numLeft = numLeft;
	}
}
