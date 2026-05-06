# סיכום יישום מערכת המלצות גנרית

## מידע כללי
- **שם מטלה**: יישום מערכת המלצות גנרית בתכנות פונקציונלי
- **שפה**: Java
- **פרדיגמה**: Streams + Functional Programming
- **אין לולאות**: כל הקוד משתמש רק ב-Stream API

---

## שלוש שלבים יושמו בהצלחה

### ✅ שלב 1: ניתוח נתונים (Data Parsing) - MainApp.java

#### הסבר בעברית:
שיטת `initElements()` קוראת שלוש קבצים בפורמט tab-separated ויוצרת:
- `Map<Integer, User>` - רשימת משתמשים מזוהה לפי ID
- `Map<Integer, Book>` - רשימת ספרים מזוהה לפי ID
- `List<Rating<Book>>` - רשימת כל הדירוגים

הפעולה משתמשת ב-Streams ללא לולאות מפורשות:
```
Files.lines() → map(Constructor) → collect(toMap/toList)
```

**קבצים המעובדים:**
- Users.txt: ID, שם, גיל, מין
- Books.txt: ID, שם, מחבר, עמודים
- Ratings.txt: userID, bookID, דירוג

---

### ✅ שלב 2: בניית ההנדסה הבסיסית - RecommenderSystem.java

#### הסבר בעברית:
קונסטרוקטור ההקבה מאתחל שני מבנים עזר חיוניים:

**ratingsByUserId:**
- מקבצת דירוגים לפי מזהה משתמש
- מאפשרת קבלה מהירה של כל דירוגי משתמש נתון
- סיבוכיות: O(1) בממוצע לאחרי preprocessing

**ratingsByItemId:**
- מקבצת דירוגים לפי מזהה פריט (ספר)
- מאפשרת קבלה מהירה של כל דירוגי פריט נתון
- חיוני לחישוב סטטיסטיקות

```java
this.ratingsByUserId = ratings.stream()
    .collect(groupingBy(Rating::getUserId));

this.ratingsByItemId = ratings.stream()
    .collect(groupingBy(Rating::getItemId));
```

**יתרונות:**
- ביצוע Preprocessing בקונסטרוקטור (O(N) פעם אחת)
- queries מהירות לאחר מכן (O(1) בממוצע)
- עיצוב בסיסי חזק לכל ה-Recommenders

---

### ✅ שלב 3: לוגיקה פופולריות - PopularityBasedRecommender.java

#### שיטה 1: getItemAverageRating(int itemId)

**הסבר בעברית:**
מחזירה את ממוצע הדירוגים לפריט נתון. משתמשת ב-`averagingDouble()` Collector
שמחשב את הממוצע בדרך אחת ויעילה.

```java
public double getItemAverageRating(int itemId) {
    return ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
            .stream()
            .collect(averagingDouble(Rating::getRating));
}
```

**דוגמה:**
- אם ספר מספר 3 קיבל דירוגים: [5.0, 4.0, 5.0]
- התוצאה תהיה: 4.67

---

#### שיטה 2: getItemRatingsCount(int itemId)

**הסבר בעברית:**
מחזירה את מספר הדירוגים הכולל לפריט נתון. משתמשת ב-`count()` Stream
שסופרת את האלמנטים ללא לולאה מפורשת.

```java
public int getItemRatingsCount(int itemId) {
    return (int) ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
            .stream()
            .count();
}
```

**דוגמה:**
- אם ספר מספר 3 קיבל דירוגים: [5.0, 4.0, 5.0]
- התוצאה תהיה: 3

---

## טכניקות Streams המשמשות

### 1. Files.lines()
קורא שורות מקובץ בצורת Stream (טיפול יעיל בקבצים גדולים)

### 2. map()
מעביר כל אלמנט מסוג אחד לסוג אחר
- `String` → `User`, `Book`, `Rating`

### 3. collect()
אוסף את התוצאות לסוג הנתונים הרצוי
- `toMap()` - מפה
- `toList()` - רשימה
- `groupingBy()` - ריבוץ

### 4. averagingDouble()
Collector שמחשב את הממוצע של ערכים double

### 5. count()
Collector שסופר את מספר האלמנטים בStream

### 6. getOrDefault()
מטפל במקרה שמפתח לא קיים (אין דירוגים)

---

## סיבוכיות זמן-מרחב

| פעולה | זמן | מרחב | הערות |
|-------|------|------|--------|
| initElements() | O(N + M + K) | O(N + M + K) | N=users, M=books, K=ratings |
| Constructor | O(K) | O(K) | K = מספר דירוגים |
| getItemAverageRating | O(m) | O(1) | m = דירוגים לפריט |
| getItemRatingsCount | O(m) | O(1) | m = דירוגים לפריט |

---

## אילוצים המתקיימים

✅ **אין לולאות מפורשות** - רק Streams
✅ **אין forEach()** - רק collectors
✅ **Generics <T extends Item>** - עם כל סוג פריט
✅ **הסברים בעברית** - בתוך התוכנה
✅ **קוד באנגלית** - כל התגובות בעברית הן בעברית

---

## קבצים שתוקנו

1. **MainApp.java**
   - הוסיפו: `initElements()` method
   - עודכנו: נתיבי הקבצים

2. **RecommenderSystem.java**
   - הוסיפו: `ratingsByUserId` ו-`ratingsByItemId`
   - הוסיפו: קוד initialization בקונסטרוקטור

3. **PopularityBasedRecommender.java**
   - הוסיפו: `getItemAverageRating()` method
   - הוסיפו: `getItemRatingsCount()` method

---

## דוגמת שימוש

```java
// Step 1: Parse data
MainApp.initElements();

// Step 2: Create recommender
PopularityBasedRecommender<Book> rec = 
    new PopularityBasedRecommender<>(users, books, ratings);

// Step 3: Get statistics
double avgRating = rec.getItemAverageRating(3);      // ממוצע דירוג ספר 3
int ratingsCount = rec.getItemRatingsCount(3);        // כמה דירגו את ספר 3

System.out.println("Book 3: " + avgRating + " stars, " + ratingsCount + " ratings");
// Output: Book 3: 4.67 stars, 123 ratings
```

---

## ערכות ה-Stream API המשמשות

```
java.util.stream.Collectors:
  - toMap()
  - toList()
  - groupingBy()
  - averagingDouble()
  - count()

java.nio.file.Files:
  - lines()

java.nio.file.Paths:
  - get()
```

---

## מסקנה

המערכת מיישמת מערכת המלצות מודרנית ויעילה המשתמשת ב:
- ✅ Stream API בלבד (לא לולאות)
- ✅ Generics חזקים
- ✅ עיצוב יעיל בזמן ומרחב
- ✅ קוד קריא ותחזוקה


