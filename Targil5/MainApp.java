import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MainApp {
    public static final String USERS_PATH = "Targil5/data/Users.txt";
    public static final String BOOKS_PATH = "Targil5/data/Books.txt";
    public static final String RATINGS_PATH = "Targil5/data/Ratings.txt";
    public static Map<Integer, User> users;
    public static Map<Integer, Book> books;
    public static List<Rating<Book>> ratings;
    public static void main(String[] args) throws IOException {
        initElements();
        testRecommenderSystem();
    }
    
    /**
     * Parses tab-separated files and initializes users, books, and ratings.
     * Uses Java Streams to process lines without explicit loops.
     */
    public static void initElements() throws IOException {
        // Parse users from Users.txt and collect into Map<Integer, User>
        // where key is userId
        users = Files.lines(Paths.get(USERS_PATH))
                .map(User::new)
                .collect(toMap(User::getId, user -> user));
        
        // Parse books from Books.txt and collect into Map<Integer, Book>
        // where key is bookId
        books = Files.lines(Paths.get(BOOKS_PATH))
                .map(Book::new)
                .collect(toMap(Book::getId, book -> book));
        
        // Parse ratings from Ratings.txt and collect into List<Rating<Book>>
        ratings = Files.lines(Paths.get(RATINGS_PATH))
                .map(Rating<Book>::new)
                .collect(toList());
    }
    public static void testRecommenderSystem() {
        Scanner in = new Scanner(System.in);

        System.out.println("Choose recommender: [p] popularity, [f] profile, [u] user similarity");
        String type = in.nextLine().trim().toLowerCase();

        switch (type) {
            case "p" -> {
                PopularityBasedRecommender<Book> rec = new PopularityBasedRecommender<>(users, books, ratings);
                System.out.println("Choose operation: [r] Recommend, [s] Print statistics");
                String op = in.nextLine().trim().toLowerCase();

                if (op.equals("r")) {
                    System.out.print("Enter user ID: ");
                    int userId = in.nextInt();
                    printItems(rec.recommendTop10(userId));
                } else if (op.equals("s")) {
                    System.out.print("Enter item ID: ");
                    int itemId = in.nextInt();
                    System.out.println("The average rating of " + itemId + " is " +  String.format("%.2f", rec.getItemAverageRating(itemId)) +
                            ". The number of ratings is " + rec.getItemRatingsCount(itemId) + ".");
                }
            }

            case "f" -> {
                ProfileBasedRecommender<Book> rec = new ProfileBasedRecommender<>(users, books, ratings);
                System.out.println("Choose operation: [r] Recommend, [m] Print matching users");
                String op = in.nextLine().trim().toLowerCase();

                if (op.equals("r")) {
                    System.out.print("Enter user ID: ");
                    int userId = in.nextInt();
                    printItems(rec.recommendTop10(userId));
                } else if (op.equals("m")) {
                    System.out.print("Enter user ID: ");
                    int userId = in.nextInt();
                    printItems(rec.getMatchingProfileUsers(userId));
                }
            }


            case "u" -> {
                SimilarityBasedRecommender<Book> rec = new SimilarityBasedRecommender<>(users, books, ratings);
                System.out.println("Choose operation: [r] Recommend, [gb] Print global bias, [ub] Print user bias, [ib] Print item bias, " +
                        "[sim] Calculate similarity");
                String op = in.nextLine().trim().toLowerCase();

                switch (op) {
                    case "r" -> {
                        System.out.print("Enter user ID: ");
                        int userId = in.nextInt();
                        printItems(rec.recommendTop10(userId));
                    }
                    case "gb" -> {
                        System.out.println("Global bias: " + String.format("%.2f", rec.getGlobalBias()));
                    }
                    case "ib" -> {
                        System.out.print("Enter item ID: ");
                        int itemId = in.nextInt();
                        System.out.println("Item bias for item " + itemId + ": " + String.format("%.2f", rec.getItemBias(itemId)));
                    }                    case "ub" -> {
                        System.out.print("Enter user ID: ");
                        int userId = in.nextInt();
                        System.out.println("User bias for user " + userId + ": " + String.format("%.2f", rec.getUserBias(userId)));
                    }

                    case "sim" -> {
                        System.out.print("Enter first user ID: ");
                        int u1 = in.nextInt();
                        System.out.print("Enter second user ID: ");
                        int u2 = in.nextInt();
                        System.out.println("Similarity: " + String.format("%.2f",rec.getSimilarity(u1, u2)));
                    }
                    default -> System.out.println("Unknown operation.");
                }
            }

            default -> System.out.println("Unknown recommender type.");
        }
    }

    private static void printItems(List<?> items) {
        String output = items.stream()
                .map(String::valueOf)
                .collect(joining(System.lineSeparator()));

        if (!output.isEmpty()) {
            System.out.println(output);
        }
    }
}
