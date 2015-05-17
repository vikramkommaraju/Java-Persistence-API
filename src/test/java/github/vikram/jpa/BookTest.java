package github.vikram.jpa;

import github.vikram.jpa.Book;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class BookTest {
	
	public static void main(String[] args) {
		
		System.out.println("Creating a new book...");
		Book b = new Book("H2G2", 29F, "The Hitchhiker's Guide to the Galaxy", "1-84023-742", 354, false);
		System.out.println("Created book: " + b);
		
		 // Obtains an entity manager and a transaction
	    EntityManagerFactory emf = Persistence.createEntityManagerFactory("persistence-unit");
	    EntityManager em = emf.createEntityManager();
	    
	    System.out.println("Created entityManager.");
	    
	    
	    // Persists the book to the database
	    EntityTransaction tx = em.getTransaction();
	    tx.begin();
	    System.out.println("Transaction started. Persisting book...");
	    try {
			em.persist(b);
			System.out.println("Book persisted...");
		    tx.commit();
		} catch (Exception e) {
			System.out.println("Exception in persisting book. Rollback the transaction");
			tx.rollback();
		} finally {
			 // Closes the entity manager and the factory
		    em.close();
		    emf.close();
		    
		    System.out.println("Entity manager closed...");
			
		}
	    
	   
	    
		
	}

}
