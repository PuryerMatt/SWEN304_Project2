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
import java.util.HashMap;
import java.util.Map;

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
    					+ "\tEdition: " + book.getEditionNo() + " - Number of copies: " + book.getNumOfCop() + " - Copies left: " + book.getNumLeft()+ "\n"
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

    /**
     * Shows all the loaned books, there authors and the customers that are borrowing the books
     * @return
     */
    public String showLoanedBooks() {
    	String query = ""
    			+ "SELECT isbn, customerid "
    			+ "FROM cust_book "
    			+ "ORDER By isbn";

    	try {
    		ArrayList<LoanedBook> loanedBooks = new ArrayList<LoanedBook>();
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);


    		if(!rs.isBeforeFirst()){
    			return "No books loaned";
    		} else{
    			while(rs.next()){
    				int isbn = rs.getInt("isbn");
    				int customerId = rs.getInt("customerid");

    				//check if book already added, if so add customer to that book (book would already have at least one borrower at this point)
    				boolean preAdded = false;
    				for(LoanedBook storedBook: loanedBooks){
    					if(storedBook.getIsbn() == isbn){
    						storedBook.getBorrowers().add(new Customer(customerId, "No LastName", "No FirstName", "no City"));
    						preAdded = true;
    						break;
    					}
    				}
    				//book not yet added to list, add book to list along with the customer
    				if(preAdded == false){
    					LoanedBook book = new LoanedBook(isbn, "no title", -1, -1, -1);
    					book.getBorrowers().add(new Customer(customerId, "No LastName", "No FirstName", "no City"));
    					loanedBooks.add(book);
    				}


    			}
    		}
    		statement.close();
    		rs.close();

    		//At this point we have a list of books, for each book we have a list of borrowers
    		for(LoanedBook loanedBook : loanedBooks){

    			//for each book, we want to query its details
    			query = ""
    					+ "SELECT title, edition_no, numofcop, numleft "
    					+ "FROM book "
    					+ "WHERE isbn = " + loanedBook.getIsbn();
    			statement = connection.createStatement();
    			rs = statement.executeQuery(query);
    			if(!rs.isBeforeFirst()){
    				return "No books from loaned books exist in books";
    			} else{
    				while(rs.next()){
    					loanedBook.setTitle(rs.getString("title"));
    					loanedBook.setEditionNo(rs.getInt("edition_no"));
    					loanedBook.setNumOfCop(rs.getInt("numofcop"));
    					loanedBook.setNumleft(rs.getInt("numleft"));
    				}
    			}

    			//For each book we want to query its authors
    			query = ""
    					+ "SELECT authorid, name, surname "
    					+ "FROM author "
    					+ "NATURAL JOIN book_author "
    					+ "WHERE isbn = " + loanedBook.getIsbn()
    					+ " ORDER By authorseqno";
    			statement = connection.createStatement();
    			rs = statement.executeQuery(query);
    			while(rs.next()){
    				int authorId = rs.getInt("authorid");
    				String name = rs.getString("name");
    				String surname = rs.getString("surname");
    				loanedBook.getAuthors().add(new Author(authorId,name,surname));
    			}

    			//For each book we want to query its borrowers which we gathered prior
    			for(Customer borrower: loanedBook.getBorrowers()){
    				query = ""
        					+ "SELECT l_name, f_name, city "
        					+ "FROM customer "
        					+ "WHERE customerid = " + borrower.getCustomerId();
        			statement = connection.createStatement();
        			rs = statement.executeQuery(query);
        			while(rs.next()){
        				borrower.setLastName(rs.getString("l_name"));
        				borrower.setFirstName(rs.getString("f_name"));
        				borrower.setCity(rs.getString("city"));
        			}
    			}

    		}

    		//We have everything we need to print the result now
    		String result = "Show Loaned books: \n";
    		for(LoanedBook loanedBook: loanedBooks){
    			result = result + "\n"
    					+ loanedBook.getIsbn() + ": " + loanedBook.getTitle() + "\n"
    					+ "\tEdition: "  + loanedBook.getEditionNo() + " - Number of copies: " + loanedBook.getNumOfCop() + " - Copies left: " + loanedBook.getNumLeft() + "\n"
    					+ "\tAuthors: ";
    					for(Author author : loanedBook.getAuthors()){
    						 result = result + author.getSurname().trim() + ", ";
    					}

    					result = result + "\n\tBorrowers:\n";
    					for(Customer borrower : loanedBook.getBorrowers()){
    						result = result + "\t\t" + borrower.getCustomerId() + ": " + borrower.getLastName().trim() + ", " + borrower.getFirstName().trim() + " - " + borrower.getCity() + "\n";
    					}
    		}
    		return result;

    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed ShowAuthor Query";
    }

    /**
     * Retrieves an authors name, surname and the books they've written based off an id
     * @param authorID
     * @return
     */
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

    /**
     * Retrieves every author in the database
     * @return
     */
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

    /**
     * Shows a customers first/last name, their city and the books they are borrowing based off there id
     * @param customerID
     * @return
     */
    public String showCustomer(int customerID) {
    	String query = ""
    			+ "SELECT f_name, l_name, city "
    			+ "FROM customer "
    			+ "WHERE customerid = " + customerID;

    	try {
    		Customer customer = new Customer(customerID,"No name","no surname","No city");
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);

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
    	return "Failed ShowCustomer Query";
    }

    /**
     * Shows all the customers in the database
     * @return
     */
    public String showAllCustomers() {
    	String query = ""
    			+ "SELECT customerid, l_name, f_name, city "
    			+ "FROM customer "
    			+ "ORDER BY customerid" ;

    	try {
    		ArrayList<Customer> customers = new ArrayList<Customer>();
    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);



    		String result = "Show All Customers:\n";
    		while(rs.next()){
    			int customerid = rs.getInt("customerid");
    			String firstName = rs.getString("f_name");
    			String lastName = rs.getString("l_name");
    			String city = rs.getString("city");

    			customers.add(new Customer(customerid, firstName, lastName, city));

    		}
    		for(Customer customer: customers){
    			result = result + "\t" + customer.getCustomerId() + ": " + customer.getLastName().trim() + ", " + customer.getFirstName().trim() + " - " + customer.getCity() +  "\n";
    		}
    		statement.close();
    		rs.close();

    		return result;


    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed ShowAuthor Query";
    }

    /**
     * Checks to see if the customer and books exists (and that there are copies available to borrow),
     * then checks if the customer has'nt already borrowed the book. If everything is fine the relevent
     * details will be added to the cust_book table and the relevent tuple in the book table wil have
     * one deducted from the numleft column.
     * @param isbn
     * @param customerID
     * @param day
     * @param month
     * @param year
     * @return
     */
    public String borrowBook(int isbn, int customerID, int day, int month, int year) {
    	String query = ""
    			+ "SELECT l_name, f_name "
    			+ "FROM customer "
    			+ "WHERE customerid = " + customerID;

    	try {

    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);

    		String lastName = "";
    		String firstName = "";
    		//Check if customer exists
    		if(!rs.isBeforeFirst()){
				return "No such customer ID : " + customerID;
			} else {
				while(rs.next()){
					lastName = rs.getString("l_name");
					firstName = rs.getString("f_name");
				}
			}
    		statement.close();
    		rs.close();

    		query = ""
        			+ "SELECT title "
        			+ "FROM book "
        			+ "WHERE isbn = " + isbn + " "
        			+ "AND numleft > 0";

    		statement = connection.createStatement();
    		rs = statement.executeQuery(query);

    		String bookTitle = "";
    		//Check if book exists and
    		if(!rs.isBeforeFirst()){
				return "Book not available for booking: " + isbn;
			} else {
				while(rs.next()){
					bookTitle = rs.getString("title");
				}
			}
    		statement.close();
    		rs.close();

    		query = ""
        			+ "SELECT * "
        			+ "FROM cust_book "
        			+ "WHERE customerid = " + customerID + " "
        			+ "AND isbn = " + isbn;

    		statement = connection.createStatement();
    		rs = statement.executeQuery(query);

    		//Check if book already borrowed by customer
    		if(rs.isBeforeFirst()){
				return "Book: " + isbn + ", is already being borrowed by the customer";
			}
    		statement.close();
    		rs.close();


    		String update = ""
    				+ "INSERT INTO cust_book (isbn, duedate, customerid) "
    				+ "VALUES ('" + isbn + "', '"+year+ "-" + month + "-" + day +"', '"+ customerID +"')";
    		statement = connection.createStatement();

    		//0 = failure, 1 = success
    		int updateResult = statement.executeUpdate(update);

    		if(updateResult != 1){
    			return "book could not be borrowed (problem with insert)";
    		} else  {
    			statement.close();

    			update = ""
        				+ "UPDATE book SET numleft = numleft-1 "
        				+ "WHERE isbn = " + isbn;
    			statement = connection.createStatement();

    			updateResult = statement.executeUpdate(update);
    			if(updateResult != 1){
    				return "Failed to alter number of books availble to borrow";
    			} else {
    				return "Borrow Book:\n"
    						+ "\tBook: " + isbn + " (" + bookTitle.trim() + ")\n"
    						+ "\tLoaned to: " + customerID + " (" + firstName.trim() + " " + lastName.trim() + ")\n"
    						+ "\tDue Date: " + day + "," + month + "," + year;
    			}

    		}





    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed borrowBook Query";
    }

    /**
     * Checks to see if the book is bveing borrowed by the customer and exists,
     * if yes it removes the book from cust_book and increments the relevent numleft
     * column in book.
     * @param isbn
     * @param customerid
     * @return
     */
    public String returnBook(int isbn, int customerid) {
    	String query = ""
    			+ "SELECT * "
    			+ "FROM cust_book "
    			+ "WHERE isbn = " + isbn + " "
    			+ "AND customerid = " + customerid;

    	try {

    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);


    		//Check if book is being borrowed
    		if(!rs.isBeforeFirst()){
				return "Book (isbn =" + isbn +  ") is not being borrowed by customer: " + customerid;
			}
    		statement.close();
    		rs.close();

    		query = ""
        			+ "SELECT * "
        			+ "FROM book "
        			+ "WHERE isbn = " + isbn + " ";

    		statement = connection.createStatement();
    		rs = statement.executeQuery(query);


    		//Check if book exists in book table
    		if(!rs.isBeforeFirst()){
				return "Book does not exist in the book table";
			}
    		statement.close();
    		rs.close();


        	String delete = ""
			+ "DELETE "
			+ "FROM cust_book "
			+ "WHERE isbn = " + isbn + " "
			+ "AND customerid = " + customerid;

        	statement = connection.createStatement();

    		//0 = failure, 1 = success
    		int deleteResult = statement.executeUpdate(delete);

    		//check if book was successfully removed from cust_book table
    		if(deleteResult != 1){
    			return "unable to delete booking from cust_book table ";
    		}
    		statement.close();

    		String update = ""
    				+ "UPDATE book SET numleft = numleft+1 "
    				+ "WHERE isbn = " + isbn;
			statement = connection.createStatement();

			int updateResult = statement.executeUpdate(update);

			//check if book was successfully updated in book table
			if(updateResult != 1){
				return "Failed to alter number of books availble to borrow";
			} else {
				return "Return book:\n"
						+ "\tBook: " + isbn + " returned for customer " + customerid;

			}

    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed returnBook Query";
    }

    /**
     * Exit function
     */
    public void closeDBConnection() {
    	try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * If a customer exist and is'nt borrowing a book, delete them
     * @param customerID
     * @return
     */
    public String deleteCus(int customerID) {
    	String query = ""
    			+ "SELECT * "
    			+ "FROM cust_book "
    			+ "WHERE customerid = " + customerID;

    	try {

    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);


    		//Check if customer is borrowing a book
    		if(rs.isBeforeFirst()){
				return "Cannot delete customer as they are borrowing a book";
			}
    		statement.close();
    		rs.close();

    		String delete = ""
        			+ "DELETE "
        			+ "FROM customer "
        			+ "WHERE customerid = " + customerID;

    		statement = connection.createStatement();

    		int deleteResult = statement.executeUpdate(delete);

    		//check if customer was successfully removed from the database
    		if(deleteResult != 1){
    			return "unable to delete customer ";
    		}  else {
    			return "Customer delete:\n"
						+ "\tCustomer deleted (ID: "+ customerID + ")";

			}

    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed delete Customer Query";
    }

    /**
     * Deletes an author based on their ID
     * @param authorID
     * @return
     */
    public String deleteAuthor(int authorID) {
    	String delete = ""
    			+ "DELETE "
    			+ "FROM author "
    			+ "WHERE authorid = " + authorID;

    	try {

    		Statement statement = connection.createStatement();

    		int deleteResult = statement.executeUpdate(delete);

    		//check if author was successfully deleted
    		if(deleteResult != 1){
    			return "unable to delete author ";
    		}  else {
    			return "Author delete:\n"
						+ "\tAuthor deleted (ID: "+ authorID + ")";

			}

    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed delete Author Query";
    }

    public String deleteBook(int isbn) {
    	String query = ""
    			+ "SELECT * "
    			+ "FROM cust_book "
    			+ "WHERE isbn = " + isbn;

    	try {

    		Statement statement = connection.createStatement();
    		ResultSet rs = statement.executeQuery(query);


    		//Check if book is being borrowed
    		if(rs.isBeforeFirst()){
				return "Book can't be deleted as a copy is being borrowed";
			}
    		statement.close();
    		rs.close();

    		String delete = ""
        			+ "DELETE "
        			+ "FROM book "
        			+ "WHERE isbn = " + isbn;

    		statement = connection.createStatement();

    		int deleteResult = statement.executeUpdate(delete);

    		//check if book was successfully removed from the database
    		if(deleteResult != 1){
    			return "unable to delete book ";
    		}  else {
    			return "Book delete:\n"
						+ "\tBook deleted (ISBN: "+ isbn + ")";

			}

    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return "Failed delete book Query";
    }
}