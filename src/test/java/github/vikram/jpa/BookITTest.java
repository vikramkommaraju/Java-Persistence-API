package github.vikram.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import github.vikram.jpa.Book;

import java.util.Set;

import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BookITTest {
	
	private static EntityManagerFactory emf;
	private static EntityManager em;
	private static EntityTransaction tx;
	
	private static ValidatorFactory vf;
	private static Validator validator;
	
	
	@BeforeClass
	public static void initData() {
		emf = Persistence.createEntityManagerFactory("persistence-unit-test");
	    em = emf.createEntityManager();
	    tx = em.getTransaction();
	    
	    vf = Validation.buildDefaultValidatorFactory();
		validator = vf.getValidator();
		
	}
	
	@AfterClass
	public static void cleanUp() {
		em.close();
		emf.close();
		vf.close();
		
		if(tx.isActive()) {
			System.out.println("Transaction is still active!");
			tx.commit();
		}
		
	}
	
	@Test
	public void shouldFindJavaEE7Book(){
		//Entity name in query should match the actual entity name in code.
		//Book != BOOK
		Query query = em.createQuery("SELECT b FROM Book b where b.title='Beginning Java EE 7'");
		int numOfBooks = query.getResultList().size();
		System.out.println("Num of books is: " + numOfBooks);
		Book b = (Book) query.getSingleResult();
		System.out.println("Printing book...");
		System.out.println(b);
		assertEquals("Beginning Java EE 7", b.getTitle());
	}
	
	@Test
	public void shouldCreateH2G2() {
		Book b = new Book("H2G2", 29F, "The Hitchhiker's Guide to the Galaxy", "1-84023-742", 354, false);
		System.out.println("Created book: " + b);
		
		//Persist book to DB
		tx.begin();
		try {
			em.persist(b);
			assertNotNull("ID should not be null!", b.getId());
			System.out.println("Book persisted...Commit the transaction");
			tx.commit();
			
			//Now fetch the book with title H2G2
			TypedQuery<Book> query = em.createNamedQuery("findBookH2G2", Book.class);
			Book queriedBook = query.getSingleResult();
			
			System.out.println("Got book from DB. Printing book...");
			System.out.println(queriedBook);
			assertEquals(b.getId(), queriedBook.getId());
			
			
		} catch (Exception e) {
			System.out.println("Exception in creating book!");
			fail();
		}
		
	}
	
	public void printViolations(Set<ConstraintViolation<Book>> violations) {
		
		
		for(ConstraintViolation<Book> v : violations) {
			System.out.println("####################");
			System.out.println("Violation Property: " + v.getPropertyPath());
			System.out.println("Violation Message: " + v.getMessage());
			System.out.println("Violation Bean: " + v.getRootBean().getClass());
			System.out.println("####################");
		}
		
	}
	
	@Test(expected = ConstraintViolationException.class)
	public void shouldRaiseExceptionForNullTitle() throws Exception {
		Book b = new Book(null, 29F, "The Hitchhiker's Guide to the Galaxy", "1-84023-742", 354, false);
		System.out.println("Created book with null title: " + b);
		
		tx.begin();
		try {
			em.persist(b); //This will throw ConstraintViolationException because
						  //book is validated in prePersist function
						  //before peristsing it to DB. Check Book class
						  //for interceptor to prepersist.
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} 
		
		Set<ConstraintViolation<Book>> violations = validator.validate(b, Default.class);
		
		//print the violation
		System.out.println("Printing violations...");
		printViolations(violations);
		
		
		//There should be one violation
		assertEquals(1, violations.size());
		
		
		
	}
	
	@Test
	public void shouldRaiseViolationForDescriptionLength() {
		Book b = new Book("Random", 29F, "Desc", "1-84023-742", 354, false);
		System.out.println("Created book with short description length: " + b);
		
		tx.begin();
		try {
			em.persist(b); //This will throw ConstraintViolationException because
						  //book is validated in prePersist function
						  //before peristsing it to DB. Check Book class
						  //for interceptor to prepersist.
			tx.commit();
		} catch (Exception e) {
			System.out.println("Exception in persisting book. Exception type: " + e.getClass());
			tx.rollback();
		}
		
		Set<ConstraintViolation<Book>> violations = validator.validate(b);
		
		//print the violation
		System.out.println("Printing violations...");
		printViolations(violations);
		
		
		//There should be one violation
		assertEquals(1, violations.size());
		
		
	}
	

}
