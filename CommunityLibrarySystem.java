import java.util.ArrayList;
import java.util.Scanner;

// --- BOOK CLASS ---
class Book {
    int bookId;
    String bookTitle;
    char category;
    boolean available;

    public Book(int bookId, String bookTitle, char category) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.category = Character.toUpperCase(category);
        this.available = true; 
    }

    public String getCategoryName() {
        switch (this.category) {
            case 'A': return "Fiction";
            case 'B': return "Non-Fiction";
            case 'C': return "Reference";
            default: return "Unknown";
        }
    }
}

// --- MEMBER CLASS ---
class Member {
    int memberId;
    String name;
    String email;
    int borrowedCount;

    // Constructor to initialize member records
    public Member(int memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.borrowedCount = 0; 
    }
}

// --- MAIN LIBRARY SYSTEM CLASS ---
public class Main {
    // Dynamic In-Memory Databases using ArrayLists
    private static ArrayList<Book> bookDatabase = new ArrayList<>();
    private static ArrayList<Member> memberDatabase = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Seeding initial sample data for demonstration
        seedInitialData();

        System.out.println("=================================================");
        System.out.println("   WELCOME TO COMMUNITY LIBRARY MANAGEMENT SYSTEM ");
        System.out.println("=================================================");

        int choice;
        // Do-While loop keeps the interactive menu active until execution is exited
        do {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. View All Books");
            System.out.println("2. Search for a Book");
            System.out.println("3. Register New Member");
            System.out.println("4. Borrow a Book");
            System.out.println("5. Return a Book & Check Fine");
            System.out.println("6. Exit");
            System.out.print("Enter your selection (1-6): ");
            
            choice = scanner.nextInt();
            scanner.nextLine(); // Clear scanner buffer

            switch (choice) {
                case 1:
                    displayAllBooks();
                    break;
                case 2:
                    searchBookWorkflow();
                    break;
                case 3:
                    registerMemberWorkflow();
                    break;
                case 4:
                    borrowBookWorkflow();
                    break;
                case 5:
                    returnBookWorkflow();
                    break;
                case 6:
                    System.out.println("\nThank you for using the Community Library Management System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid entry! Please enter a value between 1 and 6.");
            }
        } while (choice != 6);
    }
    private static boolean checkAvailability(Book book) {
        return book.available;
    }
    private static double calculateFine(int overdueDays) {
        if (overdueDays > 30) {
            return 100.00;
        } else if (overdueDays > 15) {
            return 50.00;
        } else if (overdueDays > 0) {
            return overdueDays * 5.00; // Rs. 5 per day standard rate
        } else {
            return 0.00;
        }
    }
    private static void registerMember(int id, String name, String email) {
        Member newMember = new Member(id, name, email);
        memberDatabase.add(newMember);
        System.out.println("Success: Member '" + name + "' registered successfully with ID: " + id);
    }

    private static void displayAllBooks() {
        System.out.println("\n--- CURRENT BOOK INVENTORY ---");
        System.out.printf("%-7s %-25s %-15s %-12s\n", "ID", "Title", "Category", "Availability");
        System.out.println("----------------------------------------------------------------");
        
        // For Loop cycling through the dynamic inventory list
        for (Book book : bookDatabase) {
            String availabilityStatus = book.available ? "Available" : "Borrowed";
            System.out.printf("%-7d %-25s %-15s %-12s\n", 
                book.bookId, book.bookTitle, book.getCategoryName(), availabilityStatus);
        }
    }

    private static void searchBookWorkflow() {
        System.out.print("\nEnter the Book Title or keyword to search: ");
        String query = scanner.nextLine().toLowerCase();
        boolean found = false;

        System.out.println("\n--- Search Results ---");
        for (Book book : bookDatabase) {
            if (book.bookTitle.toLowerCase().contains(query)) {
                String status = checkAvailability(book) ? "Available" : "Borrowed";
                System.out.println("[" + book.bookId + "] " + book.bookTitle + " | Category: " + book.getCategoryName() + " | Status: " + status);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching books found matching your query.");
        }
    }

    private static void registerMemberWorkflow() {
        System.out.println("\n--- Register New Member ---");
        int nextId = 200 + memberDatabase.size() + 1; // Auto generate an incremental ID string
        System.out.print("Enter Member Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine();

        registerMember(nextId, name, email);
    }

    private static void borrowBookWorkflow() {
        System.out.println("\n--- Borrow Transaction Processing ---");
        System.out.print("Enter Member ID: ");
        int mId = scanner.nextInt();
        
        Member member = findMemberById(mId);
        if (member == null) {
            System.out.println("Error: Transaction rejected. Member record not found.");
            return;
        }
        if (member.borrowedCount >= 5) {
            System.out.println("Limit reached: Member has already borrowed 5 books and cannot take more.");
            return;
        } else {
            System.out.println("Verification successful: Member can borrow more items.");
        }

        System.out.print("Enter Book ID to borrow: ");
        int bId = scanner.nextInt();
        Book book = findBookById(bId);

        if (book == null) {
            System.out.println("Error: Book item code does not exist.");
            return;
        }
        if (checkAvailability(book)) {
            book.available = false; // Toggle record state flag
            member.borrowedCount++; // Increment user allocations
            System.out.println("Transaction Completed! '" + book.bookTitle + "' has been issued to " + member.name + ".");
        } else {
            System.out.println("Sorry: The requested book is currently checked out by another member.");
        }
    }

    private static void returnBookWorkflow() {
        System.out.println("\n--- Return Transaction Processing ---");
        System.out.print("Enter Book ID being returned: ");
        int bId = scanner.nextInt();
        Book book = findBookById(bId);

        if (book == null) {
            System.out.println("Error: Valid book entity not found.");
            return;
        }

        if (book.available) {
            System.out.println("Anomaly: This book record shows it is already in the library database.");
            return;
        }

        System.out.print("Enter Member ID returning the book: ");
        int mId = scanner.nextInt();
        Member member = findMemberById(mId);

        System.out.print("How many days overdue is this return? (Enter 0 if on time): ");
        int overdueDays = scanner.nextInt();

        double fineAmount = calculateFine(overdueDays);
        if (fineAmount > 0) {
            System.out.printf("Action Required: Book is overdue. Fine Assessed: Rs. %.2f\n", fineAmount);
        } else {
            System.out.println("Confirmation: Returned on schedule. No fine assessed.");
        }
        book.available = true;
        if (member != null && member.borrowedCount > 0) {
            member.borrowedCount--;
        }
        System.out.println("Success: Database updated. Item safely returned to storage inventory racks.");
    }

    // --- HELPER LOOKUP FUNCTIONS ---
    private static Book findBookById(int id) {
        for (Book b : bookDatabase) {
            if (b.bookId == id) return b;
        }
        return null;
    }

    private static Member findMemberById(int id) {
        for (Member m : memberDatabase) {
            if (m.memberId == id) return m;
        }
        return null;
    }

    private static void seedInitialData() {
    
        bookDatabase.add(new Book(101, "Java Programming", 'B'));
        bookDatabase.add(new Book(102, "Introduction to Algorithms", 'B'));
