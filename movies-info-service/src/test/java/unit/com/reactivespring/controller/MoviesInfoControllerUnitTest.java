package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoService;

    static String MOVIES_INFO_URL = "/v1/movieinfos";

    @Test
    void getAllMoviesInfoTest() {

        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        when(movieInfoService.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoByIdTest() {

        var movieInfoId = "abc";

        var movieInfo = new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(movieInfoService.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(movieInfo));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var movieInfoInDb = movieInfoEntityExchangeResult.getResponseBody();
                    assertNotNull(movieInfoInDb);

                });
    }

    @Test
    void addMovieInfoTest() {

        var movieInfo = new MovieInfo("mockId", "Batman Begins1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoService.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());

                });
    }

    @Test
    void addMovieInfo_validation_Test() {

        var movieInfo = new MovieInfo(null, "",
                -2005, List.of(""), LocalDate.parse("2005-06-15"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("Responsebody : " + responseBody);
                    assert responseBody != null;
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a positive value";
                    assertEquals(expectedErrorMessage, responseBody);
                });

    }

    @Test
    void updateMovieInfoTest() {

        var movieInfoId = "abc";

        var movieInfo = new MovieInfo(null, "Another Batman's film",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(movieInfoService.updateMovieInfo(isA(MovieInfo.class), isA(String.class))).thenReturn(Mono.just(
                new MovieInfo(movieInfoId, "Another Batman's film",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))
        ));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL+"/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {

                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assertEquals("Another Batman's film", updatedMovieInfo.getName());

                });
    }

    @Test
    void deleteMovieInfoByIdTest() {
        var movieInfoId = "abc";

        when(movieInfoService.deleteMovieInfoById(isA(String.class))).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
