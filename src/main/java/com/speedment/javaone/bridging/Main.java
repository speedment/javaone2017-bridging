/**
 *
 * Copyright (c) 2006-2017, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.javaone.bridging;

import com.company.sakila.SakilaApplication;
import com.company.sakila.SakilaApplicationBuilder;
import com.company.sakila.sakila.sakila.film.Film;
import com.company.sakila.sakila.sakila.film.FilmManager;
import com.company.sakila.sakila.sakila.film.generated.GeneratedFilm.Rating;
import com.company.sakila.sakila.sakila.language.Language;
import com.company.sakila.sakila.sakila.language.LanguageManager;
import com.speedment.runtime.core.ApplicationBuilder.LogType;
import java.util.List;
import java.util.function.Function;
import static java.util.stream.Collectors.*;
import java.util.stream.Stream;

/**
 *
 * @author Per Minborg
 */
public class Main {

    public static void main(String[] args) {
        SakilaApplication app = startSpeedment();
        FilmManager films = app.getOrThrow(FilmManager.class);
        LanguageManager languages = app.getOrThrow(LanguageManager.class);

        List.of(0, 1, 2).stream().count();

        films.stream().count();

        // PAUSE
        films.stream().filter(Film.RATING.equal(Rating.PG13)).filter(Film.LENGTH.greaterThan(75)).count();

        films.stream().filter(Film.RATING.equal(Rating.PG13)).filter(Film.LENGTH.greaterThan(75)).map(Film::getTitle).sorted().count();

        films.stream().limit(2).map(Film.TITLE.getter()).forEach(System.out::println);

        System.out.println(films.stream().collect(groupingBy(Film.RATING.getter(), counting())));

        System.out.println(films.stream().collect(groupingBy(Film.RATING.getter(), summingInt(Film.LENGTH.getter()))));

        languages.stream().filter(Language.NAME.equal("English")).flatMap(films.finderBackwardsBy(Film.LANGUAGE_ID)).limit(2).forEach(System.out::println);

        languages.stream()
            .filter(Language.NAME.equal("English"))
            .flatMap(films.finderBackwardsBy(
                Film.LANGUAGE_ID))
            .forEach(System.out::println);

        films.stream().filter(Film.RATING.equal("PG-13")).filter(Film.LENGTH.greaterThan(75)).map(Film::getTitle).sorted().count();

        System.out.println("**** JSON ");

        System.out.println("["
            + films.stream()
                .filter(Film.RATING.equal("PG-13"))
                .sorted(Film.TITLE.comparator())
                .skip(10)
                .limit(10) // JVM from here…
                .map(myToJsonMapper())
                .collect(joining(", "))
            + "]");

        System.out.println("**** PAGING");

        serveFilms(films, "PG-13", 10).forEach(System.out::println);

    }

    private static final int PAGE_SIZE = 50;

    private static Stream<Film> serveFilms(FilmManager films, String rating, int page) {
        Stream<Film> stream = films.stream();
        if (rating != null) {
            stream = stream.filter(Film.RATING.equal(rating));
        }
        return stream
            .sorted(Film.LENGTH.comparator())
            .skip(page * PAGE_SIZE)
            .limit(PAGE_SIZE);
    }

    public static Function<Film, String> myToJsonMapper() {
        return f -> String.format("{\"title\":\"%s\",\"rating\":\"%s\"}", f.getTitle(), f.getRating());
    }

    public static SakilaApplication startSpeedment() {
        return new SakilaApplicationBuilder().withPassword("sakila-password").withLogging(LogType.STREAM).build();
    }

}
