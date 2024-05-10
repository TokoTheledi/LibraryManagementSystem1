import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Book {
    private String title;
    private String author;
    private LocalDate dueDate;
    private double overdueFine;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(int loanPeriod) {
        this.dueDate = LocalDate.now().plusDays(loanPeriod);
    }

    public double getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(double overdueFine) {
        this.overdueFine = overdueFine;
    }
}

class Member {
    private String name;
    private List<Book> borrowedBooks;

    public Member(String name) {
        this.name = name;
        this.borrowedBooks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrowBook(Book book, int loanPeriod) {
        book.setDueDate(loanPeriod);
        borrowedBooks.add(book);
    }
}

class OverdueFineCalculator {
    private static final double dailyFineRate = 0.5;

    public static double calculateOverdueFine(Book book) {
        if (book.getDueDate() == null) {
            return 0.0;
        }

        LocalDate today = LocalDate.now();
        if (!today.isAfter(book.getDueDate())) {
            return 0.0;
        }

        long overdueDays = book.getDueDate().until(today).getDays();
        return overdueDays * dailyFineRate;
    }
}

class NotificationService {
    public static void sendNotification(Member member, Book book) {
        System.out.println("Notification sent to " + member.getName() + " for book '" +
                book.getTitle() + "' due on " + book.getDueDate());
    }
}

class LibraryManagementSystem {
    private static List<Member> members = new ArrayList<>();
    private static final String dataFilePath = "library_data.json";

    public static void main(String[] args) {
        loadData();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenu();
            String input = scanner.nextLine();

            switch (input.toLowerCase()) {
                case "1":
                    addMember(scanner);
                    break;
                case "2":
                    borrowBook(scanner);
                    break;
                case "3":
                    checkDueDates();
                    break;
                case "4":
                    viewFines();
                    break;
                case "5":
                    manageNotifications();
                    break;
                case "6":
                    saveData();
                    return;
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }
        }
    }

    static void displayMenu() {
        System.out.println("Library Management System");
        System.out.println("1. Add Member");
        System.out.println("2. Borrow Book");
        System.out.println("3. Check Due Dates");
        System.out.println("4. View Fines");
        System.out.println("5. Manage Notifications");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    static void loadData() {
        try {
            if (Files.exists(Paths.get(dataFilePath))) {
                String jsonData = new String(Files.readAllBytes(Paths.get(dataFilePath)));
                members = JsonUtils.fromJsonToList(jsonData, Member.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(LibraryManagementSystem::processOverdueFines);
        executor.submit(LibraryManagementSystem::sendNotifications);
        executor.shutdown();
    }

    static void saveData() {
        String jsonData = JsonUtils.fromListToJson(members);
        try {
            Files.write(Paths.get(dataFilePath), jsonData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void addMember(Scanner scanner) {
        System.out.print("Enter member name: ");
        String name = scanner.nextLine();
        Member member = new Member(name);
        members.add(member);
        System.out.println("Member '" + name + "' added successfully.");
    }

    static void borrowBook(Scanner scanner) {
        System.out.print("Enter member name: ");
        String name = scanner.nextLine();
        Member member = members.stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);

        if (member == null) {
            System.out.println("Member not found.");
            return;
        }

        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();
        System.out.print("Enter loan period (in days): ");
        int loanPeriod = Integer.parseInt(scanner.nextLine());

        Book book = new Book(title, author);
        member.borrowBook(book, loanPeriod);
        System.out.println("Book '" + title + "' by '" + author + "' borrowed successfully.");
    }

    static void checkDueDates() {
        System.out.println("Due Dates:");
        for (Member member : members) {
            System.out.println("Member: " + member.getName());
            for (Book book : member.getBorrowedBooks()) {
                if (book.getDueDate() != null) {
                    System.out.println("Book: " + book.getTitle() + ", Due Date: " + book.getDueDate());
                }
            }
            System.out.println();
        }
    }

    static void viewFines() {
        System.out.println("Overdue Fines:");
        for (Member member : members) {
            System.out.println("Member: " + member.getName());
            double totalFine = 0;
            for (Book book : member.getBorrowedBooks()) {
                double fine = book.getOverdueFine();
                totalFine += fine;
                System.out.println("Book: " + book.getTitle() + ", Fine: $" + fine);
            }
            System.out.println("Total Fine: $" + totalFine);
            System.out.println();
        }
    }

    static void manageNotifications() {
        System.out.println("Manage Notifications:");
        System.out.println("1. Enable Notifications");
        System.out.println("2. Disable Notifications");
        System.out.print("Enter your choice: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        switch (input) {
            case "1":
                enableNotifications();
                break;
            case "2":
                disableNotifications();
                break;
            default:
                System.out.println("Invalid input.");
                break;
        }
    }

    static void enableNotifications() {
        // Enable the notification task
        System.out.println("Notifications enabled.");
    }

    static void disableNotifications() {
        // Disable the notification task
        System.out.println("Notifications disabled.");
    }

    static void processOverdueFines() {
        while (true) {
            for (Member member : members) {
                for (Book book : member.getBorrowedBooks()) {
                    book.setOverdueFine(OverdueFineCalculator.calculateOverdueFine(book));
                }
            }

            System.out.println("Overdue fines updated.");
            try {
                Thread.sleep(60000); // Wait for 1 minute
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void sendNotifications() {
        while (true) {
            for (Member member : members) {
                for (Book book : member.getBorrowedBooks()) {
                    if (book.getDueDate() != null && book.getDueDate().equals(LocalDate.now())) {
                        NotificationService.sendNotification(member, book);
                    }
                }
            }

            try {
                Thread.sleep(86400000); // Wait for 1 day
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class JsonUtils {
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        return null;
        // implementation of deserialization from JSON to list of objects
    }

    public static String fromListToJson(List<?> list) {
        return null;
        // implementation of serialization from list of objects to JSON
    }
}
