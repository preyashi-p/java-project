import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

// Parent class: Inheritance
abstract class LibraryItem {
    protected int id;
    protected String title;

    public LibraryItem(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public abstract void display();
}

public class LibraryManagementSystem {

    private static final String FILE_NAME = "library_books.txt";

    // Inner Book class prevents conflicts with an old external Book.class
    static class Book extends LibraryItem {
        private String author;
        private String category;
        private boolean available;
        private double price;
        private char categoryCode;

        public Book(int id, String title, String author,
                    String category, boolean available, double price) {
            super(id, title);
            this.author = author;
            this.category = category;
            this.available = available;
            this.price = price;
            this.categoryCode = category.isEmpty()
                    ? 'U' : Character.toUpperCase(category.charAt(0));
        }

        // Method overriding
        @Override
        public void display() {
            System.out.println(
                "ID: " + id +
                " | Title: " + title +
                " | Author: " + author +
                " | Category: " + category +
                " | Available: " + (available ? "Yes" : "No") +
                " | Price: Rs." + String.format("%.2f", price)
            );
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
        public boolean isAvailable() { return available; }
        public double getPrice() { return price; }
        public char getCategoryCode() { return categoryCode; }

        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }

        public void setCategory(String category) {
            this.category = category;
            this.categoryCode = category.isEmpty()
                    ? 'U' : Character.toUpperCase(category.charAt(0));
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String toFileRecord() {
            return id + "|" + clean(title) + "|" + clean(author) +
                   "|" + clean(category) + "|" + available + "|" + price;
        }

        private static String clean(String value) {
            return value.replace("|", "/")
                        .replace("\n", " ")
                        .replace("\r", " ");
        }

        public static Book fromFileRecord(String record) {
            String[] p = record.split("\\|", -1);

            if (p.length != 6) {
                throw new IllegalArgumentException("Invalid record");
            }

            int id = Integer.parseInt(p[0]);
            String title = p[1];
            String author = p[2];
            String category = p[3];
            boolean available = Boolean.parseBoolean(p[4]);
            double price = Double.parseDouble(p[5]);

            if (id <= 0 || title.trim().isEmpty()
                    || !Double.isFinite(price) || price < 0
                    || !(p[4].equals("true") || p[4].equals("false"))) {
                throw new IllegalArgumentException("Invalid book data");
            }

            return new Book(id, title, author, category, available, price);
        }
    }

    private final ArrayList<Book> books = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    // Array
    private final String[] categories = {
        "Programming", "Science", "Mathematics",
        "Literature", "History", "Other"
    };

    public LibraryManagementSystem() {
        loadBooks();
    }

    public static void main(String[] args) {
        LibraryManagementSystem library = new LibraryManagementSystem();
        library.runMenu();
    }

    // Main interactive menu
    private void runMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search Book");
            System.out.println("4. Issue Book");
            System.out.println("5. Return Book");
            System.out.println("6. Update Book");
            System.out.println("7. Delete Book");
            System.out.println("8. Library Statistics");
            System.out.println("9. Count Available Books (Recursion)");
            System.out.println("10. Show Categories");
            System.out.println("0. Exit");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1: addBook(); break;
                case 2: viewAllBooks(); break;
                case 3: searchMenu(); break;
                case 4: issueBook(); break;
                case 5: returnBook(); break;
                case 6: updateBook(); break;
                case 7: deleteBook(); break;
                case 8: showStatistics(); break;
                case 9:
                    System.out.println("Available books: " +
                            countAvailableRecursive(0));
                    break;
                case 10: showCategories(); break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using the library!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        scanner.close();
    }

    // Safe input methods and exception handling
    private int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private String readText(String message) {
        while (true) {
            System.out.print(message);
            String text = scanner.nextLine().trim();

            if (!text.isEmpty()) return text;

            System.out.println("This field cannot be empty.");
        }
    }

    private double readPrice(String message) {
        while (true) {
            System.out.print(message);
            try {
                double price = Double.parseDouble(scanner.nextLine().trim());

                if (Double.isFinite(price) && price >= 0) return price;

                System.out.println("Enter a non-negative price.");
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid decimal number.");
            }
        }
    }

    // Add a book
    private void addBook() {
        int id = readInt("Enter book ID: ");

        if (id <= 0 || findBook(id) != null) {
            System.out.println("Invalid or duplicate book ID.");
            return;
        }

        Book book = new Book(
            id,
            readText("Enter title: "),
            readText("Enter author: "),
            readText("Enter category: "),
            true,
            readPrice("Enter price: ")
        );

        books.add(book);

        if (saveBooks()) {
            System.out.println("Book added successfully.");
        } else {
            books.remove(book);
            System.out.println("Save failed. Book was not added.");
        }
    }

    // View books: StringBuilder, lambda expression and stream
    private void viewAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("\n----- BOOK LIST -----\n");

        books.stream()
             .sorted((a, b) ->
                     a.getTitle().compareToIgnoreCase(b.getTitle()))
             .forEach(book -> report.append("ID: ")
                 .append(book.getId())
                 .append(" | Title: ").append(book.getTitle())
                 .append(" | Author: ").append(book.getAuthor())
                 .append(" | Category: ").append(book.getCategory())
                 .append(" | Available: ")
                 .append(book.isAvailable() ? "Yes" : "No")
                 .append(" | Price: Rs.")
                 .append(String.format("%.2f", book.getPrice()))
                 .append("\n"));

        System.out.print(report);
    }

    // Method overloading: search by ID
    private Book searchBook(int id) {
        return findBook(id);
    }

    // Method overloading: search by title
    private List<Book> searchBook(String title) {
        String query = title.toLowerCase(Locale.ROOT);

        return books.stream()
            .filter(b -> b.getTitle().toLowerCase(Locale.ROOT)
                    .contains(query))
            .collect(Collectors.toList());
    }

    private void searchMenu() {
        System.out.println("1. Search by ID");
        System.out.println("2. Search by title");

        int choice = readInt("Choose option: ");

        if (choice == 1) {
            Book book = searchBook(readInt("Enter ID: "));

            if (book == null) System.out.println("Book not found.");
            else book.display();

        } else if (choice == 2) {
            List<Book> results =
                    searchBook(readText("Enter title: "));

            if (results.isEmpty()) {
                System.out.println("No matching books.");
            } else {
                results.forEach(Book::display);
            }
        } else {
            System.out.println("Invalid search option.");
        }
    }

    private Book findBook(int id) {
        for (Book book : books) {
            if (book.getId() == id) return book;
        }
        return null;
    }

    // Issue a book
    private void issueBook() {
        Book book = findBook(readInt("Enter book ID to issue: "));

        if (book == null) {
            System.out.println("Book not found.");
        } else if (!book.isAvailable()) {
            System.out.println("Book is already issued.");
        } else {
            book.setAvailable(false);

            if (saveBooks()) {
                System.out.println("Book issued successfully.");
            } else {
                book.setAvailable(true);
                System.out.println("Could not save the change.");
            }
        }
    }

    // Return a book
    private void returnBook() {
        Book book = findBook(readInt("Enter book ID to return: "));

        if (book == null) {
            System.out.println("Book not found.");
        } else if (book.isAvailable()) {
            System.out.println("This book is already available.");
        } else {
            book.setAvailable(true);

            if (saveBooks()) {
                System.out.println("Book returned successfully.");
            } else {
                book.setAvailable(false);
                System.out.println("Could not save the change.");
            }
        }
    }

    // Update book details
    private void updateBook() {
        Book b = findBook(readInt("Enter book ID to update: "));

        if (b == null) {
            System.out.println("Book not found.");
            return;
        }

        String oldTitle = b.getTitle();
        String oldAuthor = b.getAuthor();
        String oldCategory = b.getCategory();
        double oldPrice = b.getPrice();

        b.setTitle(readText("Enter new title: "));
        b.setAuthor(readText("Enter new author: "));
        b.setCategory(readText("Enter new category: "));
        b.setPrice(readPrice("Enter new price: "));

        if (saveBooks()) {
            System.out.println("Book updated successfully.");
        } else {
            b.setTitle(oldTitle);
            b.setAuthor(oldAuthor);
            b.setCategory(oldCategory);
            b.setPrice(oldPrice);
            System.out.println("Save failed. Previous details restored.");
        }
    }

    // Delete book
    private void deleteBook() {
        Book b = findBook(readInt("Enter book ID to delete: "));

        if (b == null) {
            System.out.println("Book not found.");
            return;
        }

        if (!b.isAvailable()) {
            System.out.println("Return the book before deleting it.");
            return;
        }

        books.remove(b);

        if (saveBooks()) {
            System.out.println("Book deleted successfully.");
        } else {
            books.add(b);
            System.out.println("Save failed. Book restored.");
        }
    }

    // StringBuffer, collections and streams
    private void showStatistics() {
        long total = books.size();
        long available = books.stream()
                .filter(Book::isAvailable).count();
        long issued = total - available;
        double value = books.stream()
                .mapToDouble(Book::getPrice).sum();

        StringBuffer report = new StringBuffer();
        report.append("\n----- LIBRARY STATISTICS -----\n");
        report.append("Total books: ").append(total).append("\n");
        report.append("Available books: ").append(available).append("\n");
        report.append("Issued books: ").append(issued).append("\n");
        report.append("Total book value: Rs.")
              .append(String.format("%.2f", value)).append("\n");

        System.out.println(report);
    }

    // Recursion
    private int countAvailableRecursive(int index) {
        if (index >= books.size()) return 0;

        int current = books.get(index).isAvailable() ? 1 : 0;
        return current + countAvailableRecursive(index + 1);
    }

    private void showCategories() {
        System.out.println("\nSuggested categories:");

        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i]);
        }
    }

    // File handling: save records
    private boolean saveBooks() {
        Path path = Paths.get(FILE_NAME);
        Path temp = Paths.get(FILE_NAME + ".tmp");

        try (BufferedWriter writer = Files.newBufferedWriter(temp)) {
            for (Book b : books) {
                writer.write(b.toFileRecord());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("File save error: " + e.getMessage());
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {}
            return false;
        }

        try {
            try {
                Files.move(temp, path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, path,
                    StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            System.out.println("File update error: " + e.getMessage());
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {}
            return false;
        }
    }

    // File handling: load records
    private void loadBooks() {
        Path path = Paths.get(FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("No saved file. Starting a new library.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    Book b = Book.fromFileRecord(line);

                    if (findBook(b.getId()) == null) {
                        books.add(b);
                    } else {
                        System.out.println("Skipping duplicate book ID.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Skipping invalid saved record.");
                }
            }

            System.out.println("Loaded " + books.size() + " saved book(s).");

        } catch (IOException e) {
            System.out.println("Could not load books: " + e.getMessage());
        }
    }
}
