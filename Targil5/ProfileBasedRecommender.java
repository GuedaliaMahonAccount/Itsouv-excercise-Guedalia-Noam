import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/** Profile‑based recommender implementation. */
class ProfileBasedRecommender<T extends Item> extends RecommenderSystem<T> {
    public ProfileBasedRecommender(Map<Integer, User> users,
                                   Map<Integer, T> items,
                                   List<Rating<T>> ratings) {
        super(users, items, ratings);
    }

    @Override
    public List<T> recommendTop10(int userId) {
        User targetUser = users.get(userId);
        if (targetUser == null) {
            return Collections.emptyList();
        }

        Set<Integer> targetRatedItemIds = ratedItemIds(userId);
        Set<Integer> matchingUserIds = getMatchingProfileUsers(userId).stream()
                .map(User::getId)
                .collect(toSet());

        Map<Integer, Double> averageRatings = matchingRatings(matchingUserIds)
                .collect(groupingBy(Rating::getItemId, averagingDouble(Rating::getRating)));

        Map<Integer, Long> ratingsCounts = matchingRatings(matchingUserIds)
                .collect(groupingBy(Rating::getItemId, counting()));

        return items.values().stream()
                .filter(item -> !targetRatedItemIds.contains(item.getId()))
                .filter(item -> ratingsCounts.getOrDefault(item.getId(), 0L) >= 5L)
                .sorted(profileComparator(averageRatings, ratingsCounts))
                .limit(NUM_OF_RECOMMENDATIONS)
                .collect(toList());
    }

    public List<User> getMatchingProfileUsers(int userId) {
        User targetUser = users.get(userId);
        if (targetUser == null) {
            return Collections.emptyList();
        }

        return users.values().stream()
                .filter(user -> matchesProfile(targetUser, user))
                .sorted(Comparator.comparingInt(User::getId))
                .collect(toList());
    }

    private boolean matchesProfile(User referenceUser, User candidateUser) {
        return referenceUser.getGender().equalsIgnoreCase(candidateUser.getGender())
                && Math.abs(referenceUser.getAge() - candidateUser.getAge()) <= 5;
    }

    private Set<Integer> ratedItemIds(int userId) {
        return ratingsByUserId.getOrDefault(userId, Collections.emptyList())
                .stream()
                .map(Rating::getItemId)
                .collect(toSet());
    }

    private Stream<Rating<T>> matchingRatings(Set<Integer> matchingUserIds) {
        return matchingUserIds.stream()
                .flatMap(id -> ratingsByUserId.getOrDefault(id, Collections.emptyList()).stream());
    }

    private Comparator<T> profileComparator(Map<Integer, Double> averageRatings,
                                            Map<Integer, Long> ratingsCounts) {
        return Comparator.comparingDouble((T item) -> averageRatings.getOrDefault(item.getId(), 0.0))
                .reversed()
                .thenComparing(Comparator.comparingLong((T item) -> ratingsCounts.getOrDefault(item.getId(), 0L)).reversed())
                .thenComparing(T::getName);
    }
}
