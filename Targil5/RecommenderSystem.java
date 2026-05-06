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
     * Initializes the recommender system and builds lookup tables for ratings by user and item.
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
