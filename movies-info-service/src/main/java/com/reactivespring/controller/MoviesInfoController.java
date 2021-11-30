package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MovieInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {

    @Autowired
    private MovieInfoService movieInfoService;

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(value = "year", required = false) Integer year) {

        log.info("Year is : {} ", year);

        if (year != null) {
            return movieInfoService.getMovieInfoByYear(year);
        }

        return movieInfoService.getAllMovieInfos();

    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {

        return movieInfoService.getMovieInfoById(id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @GetMapping("/movieinfosname/{name}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoByName(@PathVariable String name) {

        return movieInfoService.getMovieInfoByName(name)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {

        return movieInfoService.addMovieInfo(movieInfo);

    }

    @PutMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody @Valid MovieInfo updatedMovieInfo, @PathVariable String id) {

        return movieInfoService.updateMovieInfo(updatedMovieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable String id) {

        return movieInfoService.deleteMovieInfoById(id);

    }
}
