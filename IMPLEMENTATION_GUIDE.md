# מערכת המלצות גנרית בתכנות פונקציונלי

## תיאור כללי
מערכת זו מיישמת מערכת המלצות גנרית המשתמשת בפרדיגמה פונקציונלית של Java Streams.
המערכת מונעת לחלוטין לולאות מפורשות ותומכת בעבודה עם כל סוג של פריטים (Items) באמצעות Generics.

---

## שלב 1: ניתוח נתונים (Data Parsing) ב-MainApp.java

### הסבר בעברית:
בשלב זה אנו קוראים שלוש קבצים בפורמט tab-separated ומחזירים:
- **Map<Integer, User>**: מפה של משתמשים עם מזהה כמפתח
- **Map<Integer, Book>**: מפה של ספרים עם מזהה כמפתח
- **List<Rating<Book>>**: רשימה של דירוגים

### הקבצים:
1. **Users.txt**: `<user_id>` `<name>` `<age>` `<gender>`
   - דוגמה: `5702563	Bill Krayer	87	male`

2. **Books.txt**: `<book_id>` `<book_name>` `<author>` `<pages>`
   - דוגמה: `3	Harry Potter and the Sorcerer's Stone	J.K. Rowling	309`

3. **Ratings.txt**: `<user_id>` `<item_id>` `<rating>`
   - דוגמה: `1001	3	5`

### קוד מלא:

```java
/**
 * Parse tab-separated files and initialize users, books, and ratings.
 * Uses Java Streams to process lines without explicit loops.
 * 
 * Hebrew Explanation (הסבר בעברית):
 * אנחנו קוראים שלוש קבצים בפורמט tab-separated:
 * 1. Users.txt: מזהה משתמש, שם, גיל, מין
 * 2. Books.txt: מזהה ספר, שם הספר, מחבר, מספר עמודים
 * 3. Ratings.txt: מזהה משתמש, מזהה ספר, דירוג (1-5)
 * 
 * עבור כל קובץ, אנו:
 * - קוראים את השורות באמצעות Files.lines()
 * - מנתחים כל שורה ויוצרים את האובייקטים המתאימים
 * - אוספים את התוצאות ל-Map או List באמצעות collectors
 * 
 * זה מונע לולאות מפורשות ומשתמש בפרדיגמה פונקציונלית.
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
```

### עיקרי הטכניקה:
- **Files.lines()**: קורא את השורות מהקובץ בצורת Stream
- **map()**: מעביר כל שורה (String) לאובייקט (User, Book, Rating)
- **collect(toMap())**: אוסף את התוצאות למפה עם קונקטור וקונקטור ערך
- **collect(toList())**: אוסף את התוצאות לרשימה
- **אין לולאות**: כל התהליך הוא פונקציונלי 100%

---

## שלב 2: בניית ההנדסה הבסיסית ב-RecommenderSystem.java

### הסבר בעברית:
בשלב זה אנו יוצרים את המבנים הנתונים העזר כדי להגביר את הביצועים:

1. **ratingsByUserId**: מפה המקבצת את כל הדירוגים לפי מזהה משתמש
   - מאפשרת גישה O(1) לכל דירוגי משתמש נתון
   - במקום לחפש בכל רשימת הדירוגים (O(N))

2. **ratingsByItemId**: מפה המקבצת את כל הדירוגים לפי מזהה פריט
   - מאפשרת גישה O(1) לכל דירוגי פריט נתון
   - חיוני לחישוב ממוצעי דירוגים ותכונות פופולריות

### סיבוך עשתוני (Time-Space Tradeoff):
- **ביצוע Preprocessing**: בקונסטרוקטור אנו משקיעים זמן כדי ליצור את המפות
- **קבלת Performance**: בעת שימוש בשיטות האחרות, אנו מקבלים גישה מהירה מאוד

### קוד מלא:

```java
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/** Abstract generic recommender system. */
abstract class RecommenderSystem<T extends Item> {
    protected final Map<Integer, User> users;
    protected final Map<Integer, T> items;
    protected final List<Rating<T>> ratings;
    
    // Auxiliary data structures for efficient lookups
    // Grouping ratings by userId for O(1) access to a user's ratings
    protected final Map<Integer, List<Rating<T>>> ratingsByUserId;
    
    // Grouping ratings by itemId for O(1) access to an item's ratings
    protected final Map<Integer, List<Rating<T>>> ratingsByItemId;
    
    protected final int NUM_OF_RECOMMENDATIONS = 10;

    /**
     * Constructor initializes the recommender system with users, items, and ratings.
     * Creates auxiliary data structures to optimize rating lookups.
     * 
     * Hebrew Explanation (הסבר בעברית):
     * בקונסטרוקטור אנו:
     * 1. שומרים את המפות והרשימה המקוריות
     * 2. יוצרים ratingsByUserId - קיבוץ של דירוגים לפי מזהה משתמש
     *    זה מאפשר לנו לקבל במהירות את כל הדירוגים של משתמש מסוים
     * 3. יוצרים ratingsByItemId - קיבוץ של דירוגים לפי מזהה פריט
     *    זה מאפשר לנו לקבל במהירות את כל הדירוגים של פריט מסוים
     * 
     * זה מחליף מהירות עיבוד (preprocessing) בביצועים של queries מהירים יותר.
     * במקום O(N) חיפוש בכל פעם, אנו עושים preprocessing פעם אחת
     * ואז נוכל לגשת ל-O(1) בממוצע.
     */
    protected RecommenderSystem(Map<Integer, User> users,
                                Map<Integer, T> items,
                                List<Rating<T>> ratings) {
        this.users = users;
        this.items = items;
        this.ratings = ratings;
        
        // Group ratings by userId using Streams
        // This creates a Map where each userId maps to a List of its ratings
        this.ratingsByUserId = ratings.stream()
                .collect(groupingBy(Rating::getUserId));
        
        // Group ratings by itemId using Streams
        // This creates a Map where each itemId maps to a List of its ratings
        this.ratingsByItemId = ratings.stream()
                .collect(groupingBy(Rating::getItemId));
    }

    /** @return top‑10 recommended items for the given user, sorted best‑first. */
    public abstract List<T> recommendTop10(int userId);
}
```

### המושגים הטכניים:
- **groupingBy()**: Collector שמקבץ את האלמנטים לפי מפתח (Key)
- **Generics <T extends Item>**: מבטיח שכל סוג פריט יוצא מהממשק Item
- **Protected fields**: נגישים לכיתות המורישות (RecommenderSystem)
- **Stream Pipeline**: כל groupingBy() הוא Stream pipeline שלם

---

## שלב 3: לוגיקה פופולריות ב-PopularityBasedRecommender.java

### הסבר בעברית:
בשלב זה אנו ממשים שתי שיטות עזר לחישוב סטטיסטיקות פופולריות:

1. **getItemAverageRating(int itemId)**:
   - מחזירה את הדירוג הממוצע של פריט
   - משתמשת ב-averagingDouble() כדי לחשב ממוצע בצורה אלגנטית

2. **getItemRatingsCount(int itemId)**:
   - מחזירה את מספר הדירוגים של פריט
   - משתמשת ב-count() כדי לספור את הדירוגים

### המבנה:
- שתי השיטות משתמשות ב-ratingsByItemId המיוצר בקונסטרוקטור
- זה מבטיח גישה מהירה בלי חיפוש של O(N)

### קוד מלא:

```java
import java.util.*;

import static java.util.stream.Collectors.*;

/** Popularity‑based recommender implementation. */
class PopularityBasedRecommender<T extends Item> extends RecommenderSystem<T> {
    private static final int POPULARITY_THRESHOLD = 100;
    
    public PopularityBasedRecommender(Map<Integer, User> users,
                                      Map<Integer, T> items,
                                      List<Rating<T>> ratings) {
        super(users, items, ratings);
    }

    @Override
    public List<T> recommendTop10(int userId) {
        // TODO: implement
        return null;
    }

    /**
     * Calculates the average rating for a given item.
     * 
     * Hebrew Explanation (הסבר בעברית):
     * אנו משתמשים ב-ratingsByItemId המאוחסן בכיתה המוריש כדי להשיג
     * את רשימת הדירוגים של פריט מסוים. אם אין דירוגים, אנו מחזירים 0.
     * 
     * אנו משתמשים ב-Stream כדי לחשב את סכום הדירוגים ולחלק במספר הדירוגים.
     * זה דומה ל-averagingDouble() collector שכן מחשב את הממוצע בדרך אחת.
     * 
     * @param itemId the ID of the item to get average rating for
     * @return the average rating of the item, or 0 if no ratings exist
     */
    public double getItemAverageRating(int itemId) {
        // Get the list of ratings for this item from the auxiliary data structure
        // Use getOrDefault to handle case where item has no ratings
        return ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .collect(averagingDouble(Rating::getRating));
    }
    
    /**
     * Counts the total number of ratings for a given item.
     * 
     * Hebrew Explanation (הסבר בעברית):
     * אנו משתמשים ב-ratingsByItemId כדי להשיג את רשימת הדירוגים של פריט.
     * אם אין דירוגים, אנו מחזירים 0.
     * 
     * אנו משתמשים ב-Stream count() כדי לספור את מספר הדירוגים.
     * זה יעיל יותר מאשר לעבור על כל הדירוגים ב-O(N).
     * 
     * @param itemId the ID of the item to count ratings for
     * @return the total number of ratings for the item
     */
    public int getItemRatingsCount(int itemId) {
        // Get the list of ratings for this item and count them using Stream
        // Use getOrDefault to handle case where item has no ratings
        return (int) ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .count();
    }
}
```

### Collectors המשמשים:
- **averagingDouble(Rating::getRating)**: מחשב את הממוצע של הדירוגים
- **count()**: סופר את מספר האלמנטים בStream
- **getOrDefault()**: מטפל במקרה שפריט אין לו דירוגים

---

## ריכוז טכניקות Stream לא-סדרתיות (No-Loop Techniques)

### 1. **Map Transformation**
```java
ratings.stream()
    .map(Rating<Book>::new)  // transform each element
    .collect(toList());
```

### 2. **Grouping (Aggregation)**
```java
ratings.stream()
    .collect(groupingBy(Rating::getUserId))  // group by key
```

### 3. **Counting**
```java
ratingsByItemId.get(itemId).stream()
    .count()  // count elements (no loop!)
```

### 4. **Averaging**
```java
ratingsByItemId.get(itemId).stream()
    .collect(averagingDouble(Rating::getRating))  // compute average
```

---

## סיכום

| שלב | מטרה | טכניקה | סיבוכיות |
|------|------|---------|-----------|
| 1 | ניתוח קבצים | Files.lines() + map + collect | O(N) |
| 2 | בניית מבנים | groupingBy() | O(N) |
| 3 | חישוב סטטיסטיקות | stream().count() + averagingDouble() | O(M) כאשר M = גודל קבוצה |

כאשר N = מספר הדירוגים הכולל

