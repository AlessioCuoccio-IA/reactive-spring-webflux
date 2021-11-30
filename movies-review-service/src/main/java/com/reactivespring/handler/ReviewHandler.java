package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ReviewHandler {

    @Autowired
    private Validator validator;

    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;



    public Mono<ServerResponse> addReview(ServerRequest request) {

        return request.bodyToMono(Review.class)
                .doOnNext(this::validate)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);

    }

    private void validate(Review review) {

        var constraintsViolations = validator.validate(review);
        log.info("Constraints violations : {} ", constraintsViolations);

        if(constraintsViolations.size() > 0) {
            var errorMessage = constraintsViolations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining(","));
            throw new ReviewDataException(errorMessage);
        }

    }

    public Mono<ServerResponse> getAllReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");

        if(movieInfoId.isPresent()) {
           var reviewsFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
           return buildReviewsResponse(reviewsFlux);
        } else {
            var reviewsFlux = reviewReactiveRepository.findAll();
            return buildReviewsResponse(reviewsFlux);
        }
    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");

        var exisitingReview = reviewReactiveRepository.findById(reviewId)
                .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for the given Review id ")));

        return exisitingReview
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return review;
                        })
                        .flatMap(reviewReactiveRepository::save)
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        var reviewId = request.pathVariable("id");

        var exisitingReview = reviewReactiveRepository.findById(reviewId);

        return exisitingReview.flatMap(review -> reviewReactiveRepository.deleteById(reviewId)
                .then(ServerResponse.noContent().build()));
    }
}
