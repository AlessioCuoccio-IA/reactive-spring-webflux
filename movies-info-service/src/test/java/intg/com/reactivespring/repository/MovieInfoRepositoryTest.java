package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos)
                .blockLast();
    }

    @AfterEach
    void tearDown() {

        movieInfoRepository.deleteAll().block();

    }

    @Test
    void findAllTest() {

        var moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void findByIdTest() {

        var moviesInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();

    }

    @Test
    void findInfoMovieByYearTest() {

        var moviesInfoMono = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(moviesInfoMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void findInfoMovieByNameTest() {

        var moviesInfoMono = movieInfoRepository.findByName("Dark Knight Rises").log();

        StepVerifier.create(moviesInfoMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void saveTest() {
        var movieInfo = new MovieInfo(null, "Spiderman",
                        2005, List.of("Tobie Maquire", "Willem Dafoe"), LocalDate.parse("2005-06-15"));

        var moviesInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals("Spiderman", movieInfo1.getName());
                })
                .verifyComplete();

    }

    @Test
    void updateTest() {

        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2021);

        var moviesInfoMono = movieInfoRepository.save(movieInfo).log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoId());
                    assertEquals(2021, movieInfo1.getYear());
                })
                .verifyComplete();

    }

    @Test
    void deleteTest() {

        movieInfoRepository.deleteById("abc").block();

        var moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(2)
                .verifyComplete();

    }
}