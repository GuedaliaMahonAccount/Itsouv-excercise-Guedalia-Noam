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
        Set<Integer> ratedItemIds = ratedItemIds(userId);

        return items.values().stream()
                .filter(item -> !ratedItemIds.contains(item.getId()))
                .filter(this::isPopular)
                .sorted(popularityComparator())
                .limit(NUM_OF_RECOMMENDATIONS)
                .collect(toList());
    }

    /** Returns the average rating for the given item, or 0 if none exist. */
    public double getItemAverageRating(int itemId) {
        // Get the list of ratings for this item from the auxiliary data structure
        // Use getOrDefault to handle case where item has no ratings
        return ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .collect(averagingDouble(Rating::getRating));
    }
    
    /** Returns the total number of ratings for the given item. */
    public int getItemRatingsCount(int itemId) {
        return (int) ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .count();
    }

    private Set<Integer> ratedItemIds(int userId) {
        return ratingsByUserId.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(Rating::getItemId)
                .collect(toSet());
    }

    private boolean isPopular(T item) {
        return getItemRatingsCount(item.getId()) >= POPULARITY_THRESHOLD;
    }

    private Comparator<T> popularityComparator() {
        return Comparator.comparingDouble((T item) -> getItemAverageRating(item.getId()))
                .reversed()
                .thenComparing(Comparator.comparingInt((T item) -> getItemRatingsCount(item.getId())).reversed())
                .thenComparing(T::getName);
    }

}
