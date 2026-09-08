package com.hutnyk.carfix.in.review;

import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.review.Review;

public interface ReviewPortIn {

    Review addReview(String customerEmail, AddReviewCommand command);
}
