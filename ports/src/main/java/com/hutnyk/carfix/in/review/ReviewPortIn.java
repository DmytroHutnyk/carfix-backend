package com.hutnyk.carfix.in.review;

import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.review.Review;

public interface ReviewPortIn {

    /**
     * Stores the review and refreshes the branch's cached rating in the same transaction.
     */
    Review addReview(AddReviewCommand command);
}
