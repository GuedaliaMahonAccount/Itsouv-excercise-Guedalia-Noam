import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

/** Similarity‑based recommender with bias correction. */
class SimilarityBasedRecommender<T extends Item> extends RecommenderSystem<T> {
    private final double globalBias;
    private final Map<Integer, Double> itemBiases;
    private final Map<Integer, Double> userBiases;

    public SimilarityBasedRecommender(Map<Integer, User> users,
                                      Map<Integer, T> items,
                                      List<Rating<T>> ratings) {
        super(users, items, ratings);
        globalBias = ratings.stream()
                .mapToDouble(Rating::getRating)
                .average()
                .orElse(0.0);

        itemBiases = ratings.stream()
                .collect(groupingBy(Rating::getItemId,
                        averagingDouble(rating -> rating.getRating() - globalBias)));

        userBiases = ratings.stream()
                .collect(groupingBy(Rating::getUserId,
                        averagingDouble(rating -> rating.getRating()
                                - globalBias
                                - itemBiases.getOrDefault(rating.getItemId(), 0.0))));
    }

    /** Dot‑product similarity; 0 if <10 shared items. */
    public double getSimilarity(int u1, int u2) {
        Set<Integer> sharedItemIds = sharedItemIds(u1, u2);
        if (sharedItemIds.size() < 10) {
            return 0.0;
        }

        return sharedItemIds.stream()
                .mapToDouble(itemId -> centeredRating(u1, itemId) * centeredRating(u2, itemId))
                .sum();
    }

    @Override
    public List<T> recommendTop10(int userId) {
        if (users.get(userId) == null) {
            return Collections.emptyList();
        }

        Set<Integer> targetRatedItemIds = ratedItemIds(userId);
        List<Neighbor> neighbors = topSimilarUsers(userId);
        Set<Integer> candidateItemIds = candidateItemCounts(targetRatedItemIds, neighbors)
                .entrySet().stream()
                .filter(entry -> entry.getValue() >= 5L)
                .map(Map.Entry::getKey)
                .collect(toSet());

        return candidateItemIds.stream()
                .map(items::get)
                .filter(item -> item != null)
                .map(item -> new ScoredItem<>(item, predictedRating(userId, item.getId(), neighbors)))
                .sorted(scoredItemComparator())
                .limit(NUM_OF_RECOMMENDATIONS)
                .map(ScoredItem::item)
                .collect(toList());
    }

    public void printGlobalBias() {
        System.out.println("Global bias: " + String.format("%.2f", getGlobalBias()));
    }

    public void printItemBias(int itemId) {
        System.out.println("Item bias for item " + itemId + ": " + String.format("%.2f", getItemBias(itemId)));
    }

    public void printUserBias(int userId) {
        System.out.println("User bias for user " + userId + ": " + String.format("%.2f", getUserBias(userId)));
    }

    public double getGlobalBias() {
        return globalBias;
    }

    public double getItemBias(int itemId) {
        return itemBiases.getOrDefault(itemId, 0.0);
    }

    public double getUserBias(int userId) {
        return userBiases.getOrDefault(userId, 0.0);
    }

    private Set<Integer> ratedItemIds(int userId) {
        return ratingsByUserId.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(Rating::getItemId)
                .collect(toSet());
    }

    private Set<Integer> sharedItemIds(int u1, int u2) {
        Set<Integer> firstUserItems = ratedItemIds(u1);
        Set<Integer> secondUserItems = ratedItemIds(u2);

        return firstUserItems.stream()
                .filter(secondUserItems::contains)
                .collect(toSet());
    }

    private double centeredRating(int userId, int itemId) {
        return ratingFor(userId, itemId) - getGlobalBias() - getItemBias(itemId) - getUserBias(userId);
    }

    private double biasFreeRating(int userId, int itemId, double rating) {
        return rating - getGlobalBias() - getItemBias(itemId) - getUserBias(userId);
    }

    private double ratingFor(int userId, int itemId) {
        return ratingsByUserId.getOrDefault(userId, Collections.emptyList())
                .stream()
                .filter(rating -> rating.getItemId() == itemId)
                .map(Rating::getRating)
                .findFirst()
                .orElse(0.0);
    }

    private double predictedRating(int userId, int itemId, List<Neighbor> neighbors) {
        double baseline = getGlobalBias() + getItemBias(itemId) + getUserBias(userId);
        List<NeighborRating> neighborRatings = neighborRatings(itemId, neighbors);

        double denominator = neighborRatings.stream()
                .mapToDouble(neighborRating -> Math.abs(neighborRating.similarity()))
                .sum();

        if (denominator == 0.0) {
            return baseline;
        }

        double numerator = neighborRatings.stream()
                .mapToDouble(neighborRating -> neighborRating.similarity()
                        * biasFreeRating(neighborRating.userId(), itemId, neighborRating.rating()))
                .sum();

        return baseline + numerator / denominator;
    }

    private Comparator<ScoredItem<T>> scoredItemComparator() {
        return Comparator.comparingDouble(ScoredItem<T>::score)
                .reversed()
                .thenComparing(Comparator.comparingInt((ScoredItem<T> scoredItem) -> itemRatingCount(scoredItem.item().getId())).reversed())
                .thenComparing(scoredItem -> scoredItem.item().getName());
    }

    private List<Neighbor> topSimilarUsers(int userId) {
        return users.keySet().stream()
                .filter(candidateUserId -> candidateUserId != userId)
                .map(candidateUserId -> new Neighbor(candidateUserId, getSimilarity(userId, candidateUserId)))
                .sorted(Comparator.comparingDouble(Neighbor::similarity)
                        .reversed()
                        .thenComparingInt(Neighbor::userId))
                .limit(NUM_OF_RECOMMENDATIONS)
                .collect(toList());
    }

    private Map<Integer, Long> candidateItemCounts(Set<Integer> targetRatedItemIds,
                                                   List<Neighbor> neighbors) {
        return neighbors.stream()
                .flatMap(neighbor -> ratingsByUserId.getOrDefault(neighbor.userId(), Collections.emptyList()).stream())
                .map(Rating::getItemId)
                .filter(itemId -> !targetRatedItemIds.contains(itemId))
                .collect(groupingBy(itemId -> itemId, counting()));
    }

    private List<NeighborRating> neighborRatings(int itemId, List<Neighbor> neighbors) {
        Map<Integer, Double> similarityByUserId = neighbors.stream()
                .collect(toMap(Neighbor::userId, Neighbor::similarity));

        return ratingsByItemId.getOrDefault(itemId, Collections.emptyList())
                .stream()
                .filter(rating -> similarityByUserId.containsKey(rating.getUserId()))
                .map(rating -> new NeighborRating(
                        rating.getUserId(),
                        similarityByUserId.get(rating.getUserId()),
                        rating.getRating()))
                .collect(toList());
    }

    private int itemRatingCount(int itemId) {
        return ratingsByItemId.getOrDefault(itemId, Collections.emptyList()).size();
    }

    private static final class ScoredItem<U extends Item> {
        private final U item;
        private final double score;

        private ScoredItem(U item, double score) {
            this.item = item;
            this.score = score;
        }

        private U item() {
            return item;
        }

        private double score() {
            return score;
        }
    }

    private static final class Neighbor {
        private final int userId;
        private final double similarity;

        private Neighbor(int userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }

        private int userId() {
            return userId;
        }

        private double similarity() {
            return similarity;
        }
    }

    private static final class NeighborRating {
        private final int userId;
        private final double similarity;
        private final double rating;

        private NeighborRating(int userId, double similarity, double rating) {
            this.userId = userId;
            this.similarity = similarity;
            this.rating = rating;
        }

        private int userId() {
            return userId;
        }

        private double similarity() {
            return similarity;
        }

        private double rating() {
            return rating;
        }
    }
}

