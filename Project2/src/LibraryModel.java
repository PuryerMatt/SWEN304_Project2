/*
 * LibraryModel.java
 * Author:
 * Created on:
 */



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.*;

public class LibraryModel {

    // For use in creating dialogs and making them modal
    private JFrame dialogParent;
    private Connection connection;

    public LibraryModel(JFrame parent, String userid, String password){
	dialogParent = parent;
	try {
		Class.forName("org.postgresql.Driver");
	} catch (ClassNotFoundException e) {

		e.printStackTrace();
	}

	String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/" + userid + "_jdbc";
	try {
		connection = DriverManager.getConnection(url, userid, password);
	} catch (SQLException e){
		e.printStackTrace();
	}
	System.out.print("DATABASE CONNECT");
    }

    /**
     * Querys a book based on an isbn number and returns the title, edition_no, numofcop, surnames of authors, numleft
     * @param isbn
     * @return
     */
    public String bookLookup(int isbn) {

    	    	String query = ""
    			+ "SELECT title, edition_no, numofcop, surname, numleft "
    			+ "FROM book_author "
    			+ "NATURAL JOIN book "
    			+ "NATURAL JOIN author "
    			+ "WHERE isbn = " + isbn
    			+ "ORDER BY authorseqno";

    	try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);



			String title = "";
			int edition_no = -1;
			int numofcop = -1;
			int numleft = -1;
			ArrayList<String> authors = new ArrayList<String>();


			/**
			 * Check for authorless books, if none do full query
			 */
			if(!rs.isBeforeFirst()){
				query = ""
		    			+ "SELECT title, edition_no, numofcop, numleft "
		    			+ "FROM book "
		    			+ "WHERE isbn = " + isbn;
				statement = connection.createStatement();
				rs = statement.executeQuery(query);

				if(!rs.isBeforeFirst()){
					return "no such ISBN: " + isbn;
				}
				while(rs.next()){
						title = rs.getString("title");
						edition_no = rs.getInt("edition_no");
						numofcop = rs.getInt("numofcop");
						numleft = rs.getInt("numleft");
				}
				String result = ""
		    			+ "Book Lookup:\n"
		    			+ "\t" + isbn + ": " + title + "\n"
		    			+ "\tEdition: " + edition_no + " - Number of copies: " + numofcop + " - Copies left: " + numleft + "\n"
						+ "\tAuthors: " + "no authors";

				return result;

			} else {

			boolean initial = true;
			while(rs.next()){
				if(initial){
					title = rs.getString("title");
					edition_no = rs.getInt("edition_no");
					numofcop = rs.getInt("numofcop");
					numleft = rs.getInt("numleft");
					initial = false;
				}
				authors.add(rs.getString("surname"));

			}
			String authorString = "";
			initial = true;
			for(String author: authors){
				if(initial) {authorString = author.trim(); initial = false;}
				else{authorString = authorString + ", " + author.trim();}
			}
			String result = ""
	    			+ "Book Lookup:\n"
	    			+ "\t" + isbn + ": " + title + "\n"
	    			+ "\tEdition: " + edition_no + " - Number of copies: " + numofcop + " - Copies left: " + numleft + "\n"
					+ "\tAuthors: " + authorString;

			return result;

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	return "Failed Book LookUp query";
    }

    /**
     * returns every single book in the database, and there authors
     * @return
     */
    public String showCatalogue() {

    	String query = ""
    			+ "SELECT DISTINCT title, edition_no, numofcop, numleft, isbn "
    			+ "FROM book "
    			+ "ORDER BY isbn";

    	try {
    		ArrayList<Book> books = new ArrayList<Book>();
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);

    		String title = "";
    		int edition_no = -1;
    		int numofcop = -1;
    		int numleft = -1;
    		int isbn = -1;

    		String result = "Show Catalogue:\n";
    		while(rs.next()){
    			title = rs.getString("title");
    			edition_no = rs.getInt("edition_no");
    			numofcop = rs.getInt("numofcop");
    			numleft = rs.getInt("numleft");
    			isbn = rs.getInt("isbn");
    			books.add(new Book(isbn, title, edition_no, numofcop, numleft));

    		}
    		statement.close();
    		rs.close();

    		for(Book book: books){

    			query = ""
    					+ "SELECT surname "
    					+ "FROM book_author "
    					+ "NATURAL JOIN author "
    					+ "WHERE isbn = " + book.getIsbn()
    					+ " ORDER BY authorseqno";
    			statement = connection.createStatement();
    			rs = statement.executeQuery(query);

    			result = result + ""
    					+ book.getIsbn() + ": " + book.getTitle() + "\n"
    					+ "\tEdition: " + book.getEdition_no() + " - Number of copies: " + book.getNumofcop() + " - Copies left: " + book.getNumleft() + "\n"
    					+ "\tAuthors: ";
    			if(!rs.isBeforeFirst()){
    				result = result + "No author";
    			} else{
    				while(rs.next()){
        				result = result + rs.getString("surname").trim() + ", ";
        			}

    			}
    			if(result.substring(result.length()-2).equals(", ")){
    				result = result.substring(0, result.length()-2);
    			}

    			result = result + "\n";

    		}


    		return result;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed Catalogue Query";
    }




    public String showLoanedBooks() {
    	return "SHOW LOADNED BOOK";
    }

    public String showAuthor(int authorID) {
    	String query = ""
    			+ "SELECT name, surname "
    			+ "FROM author "
    			+ "WHERE authorid = " + authorID;

    	try {
    		Author author = new Author(authorID,"No name", "No surname");
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);

    		String name = "";
    		String surname = "";

    		String result = "Show Author:\n";
    		if(!rs.isBeforeFirst()){
				return "invalid authorID";
    		} else{
	    		while(rs.next()){
	    			author.setName(rs.getString("name"));
	    			author.setSurname(rs.getString("surname"));
	    			
	    		}
    		}
    		statement.close();
    		rs.close();

    			query = ""
    					+ "SELECT isbn, title "
    					+ "FROM book_author "
    					+ "NATURAL JOIN book "
    					+ "WHERE authorid = " + authorID
    					+ " ORDER BY isbn";
    			statement = connection.createStatement();
    			rs = statement.executeQuery(query);

    			result = result + ""
    					+ "\t" + authorID + " - " + author.getName().trim() + " " + author.getSurname() +  "\n"
    					+ "\tBooks Written:" + "\n";

    				while(rs.next()){
        				result = result + "\t\t" + rs.getString("isbn").trim() + " - " + rs.getString("title") + "\n";
        			}

    				return result;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed ShowAuthor Query";
    }



    public String showAllAuthors() {
    	String query = ""
    			+ "SELECT authorid, name, surname "
    			+ "FROM author "
    			+ "ORDER BY authorid" ;

    	try {
    		ArrayList<Author> authors = new ArrayList<Author>();
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);



    		String result = "Show All Authors:\n";
    		while(rs.next()){
    			int authorid = rs.getInt("authorid");
    			String name = rs.getString("name");
    			String surname = rs.getString("surname");

    			authors.add(new Author(authorid, name, surname));

    		}
    		for(Author author: authors){
    			result = result + "\t" + author.getAuthorId() + ": " + author.getSurname() + "," + author.getName() + "\n";
    		}
    		statement.close();
    		rs.close();

    		return result;


    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed ShowAuthor Query";
    }

    public String showCustomer(int customerID) {
    	String query = ""
    			+ "SELECT f_name, l_name, city "
    			+ "FROM customer "
    			+ "WHERE customerid = " + customerID;

    	try {
    		Customer customer = new Customer(customerID,"No name","no surname","No city");
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);

    		String name = "";
    		String surname = "";
    		String city = "";

    		String result = "Show Customer:\n";
    		if(!rs.isBeforeFirst()){
				return "invalid customerID";
			} else {
	    		while(rs.next()){
	    			customer.setCustomerId(customerID);
	    			customer.setLastName(rs.getString("l_name"));
	    			customer.setFirstName(rs.getString("f_name"));
	    			customer.setCity(rs.getString("city"));
	
	
	    		}
			}
    		statement.close();
    		rs.close();

    			query = ""
    					+ "SELECT isbn, title "
    					+ "FROM cust_book "
    					+ "NATURAL JOIN book "
    					+ "WHERE customerid = " + customerID
    					+ " ORDER BY isbn";
    			statement = connection.createStatement();
    			rs = statement.executeQuery(query);

    			result = result + ""
    					+ "\t" + customerID + " - " + customer.getLastName().trim() + " " + customer.getFirstName().trim() + " - " + customer.getCity() + "\n";
    				if(!rs.isBeforeFirst()){
    					result = result + "\t(no books borrowed)";
    				} else {
    					result = result + "\tBooks Borrowed:" + "\n";
	    				while(rs.next()){
	        				result = result + "\t\t" + rs.getString("isbn").trim() + " - " + rs.getString("title").trim() + "\n";
	        			}
    				}

    				return result;
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed ShowAuthor Query";
    }

    public String showAllCustomers() {
	return "Show All Customers Stub";
    }

    public String borrowBook(int isbn, int customerID,
			     int day, int month, int year) {
	return "Borrow Book Stub";
    }

    public String returnBook(int isbn, int customerid) {
	return "Return Book Stub";
    }

    public void closeDBConnection() {
    }

    public String deleteCus(int customerID) {
    	return "Delete Customer";
    }

    public String deleteAuthor(int authorID) {
    	return "Delete Author";
    }

    public String deleteBook(int isbn) {
    	return "Delete Book";
    }
}