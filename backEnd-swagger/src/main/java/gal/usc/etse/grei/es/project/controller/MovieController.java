package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("films")
@CrossOrigin
@Tag(name = "Film API", description = "Film related operations")
@SecurityRequirement(name = "JWT")
public class MovieController {
    private final MovieService movies;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public MovieController(MovieService movies, LinkRelationProvider relationProvider) {
        this.movies = movies;
        this.relationProvider = relationProvider;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "getAllFilms",
            summary = "Get the list of films, paginated"
    ) @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    }) ResponseEntity<?> get(
            @Parameter(name = "Number of page", required = false) @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "Page size", required = false) @RequestParam(name = "size", defaultValue = "14") int size,
            @Parameter(name = "Sorting value", required = false) @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @Parameter(name = "Palabras clave", required = false) @RequestParam(name = "clave", required = false) List<String> clave,
            @Parameter(name = "Genero pelicula", required = false) @RequestParam(name = "genre", required = false) List<String> genre,
            @Parameter(name = "Fecha de Estreno", required = false) @RequestParam(name = "estreno", required = false) String estreno,
            @Parameter(name = "Titulo", required = false) @RequestParam(name = "title", required = false) String title) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("a")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Optional<Page<Film>> filmFound;
        try {
            if (estreno != null) {
                String[] parts = estreno.split("/"); //si hay date que mire si está bien escrito
            }
            filmFound = movies.get(page, size, Sort.by(criteria), clave, genre, estreno, title);
            if (filmFound.isPresent()) {
                Page<Film> data = filmFound.get();
                Pageable metadata = data.getPageable();
                Link self = linkTo(
                        methodOn(MovieController.class).get(page, size, sort, clave, genre, estreno, title)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(MovieController.class).get(metadata.first().getPageNumber(), size, sort, clave, genre, estreno, title)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(MovieController.class).get(data.getTotalPages() - 1, size, sort, clave, genre, estreno, title)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(MovieController.class).get(metadata.next().getPageNumber(), size, sort, clave, genre, estreno, title)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(MovieController.class).get(metadata.previousOrFirst().getPageNumber(), size, sort, clave, genre, estreno, title)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link one = linkTo(
                        methodOn(MovieController.class).get(null)
                ).withRel(relationProvider.getItemResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, one.toString())
                        .body(filmFound.get());
            } else {
                throw new notFoundExecption("FILMS NOT FOUND");
            }

        } catch (NumberFormatException e) {
            throw new badRequestException("WRONG DATE FORMAT. USE day/month/year.");
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }


    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "getOneFilm",
            summary = "Get a single film details",
            description = "Get the details for a given film. To see the film details you must be the logged in."
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The film details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content
            ),
    }) ResponseEntity<?> get(@Parameter(name = "Film ID", required = true) @PathVariable("id") String id) {
        Optional<Film> filmFound;
        try {
            filmFound = movies.get(id);

            if (filmFound.isPresent()) {
                Link self = linkTo(
                        methodOn(MovieController.class).get(id)
                ).withSelfRel();
                Link todas = linkTo(
                        methodOn(MovieController.class).get(0, 20, null, null, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todas.toString())
                        .body(filmFound.get());
            } else {
                throw new notFoundExecption("FILM NOT FOUND");
            }
        } catch (notFoundExecption e) {
            throw new notFoundExecption(e.getMessage());
        }catch (Exception e) {
            throw new serverErrorException(e.getMessage());
        }

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PostFilm",
            summary = "Create a new film",
            description = "Creates a new object film. To create the film you must be logged in as an admin."
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "New Film Detail",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Film already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Errors on Body",
                    content = @Content
            ),
    }) @io.swagger.v3.oas.annotations.parameters.RequestBody()
    ResponseEntity<?> post(@RequestBody Film film) {
        Optional<Film> filmFound;
        try {
            filmFound = movies.post(film);

            if (filmFound.isPresent()) {
                Link self = linkTo(
                        methodOn(MovieController.class).post(film)
                ).withSelfRel();
                Link todas = linkTo(
                        methodOn(MovieController.class).get(0, 20, null, null, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todas.toString())
                        .body(filmFound.get());
            } else {
                throw new serverErrorException("INTERNAL SERVER ERROR"); //algo ha pasado que no se ha creado la peli
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PutFilm",
            summary = "Modify an existing film",
            description = "you must be logged as an administrator"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified Film Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error on body",
                    content = @Content),
    })    @io.swagger.v3.oas.annotations.parameters.RequestBody()
    ResponseEntity<?> put(@Parameter(name = "film id") @PathVariable("id") String id, @RequestBody Film film) {
        Optional<Film> filmFound;
        try {
            filmFound = movies.put(id, film);
            if (filmFound.isPresent()) {
                Link self = linkTo(
                        methodOn(MovieController.class).put(id, film)
                ).withSelfRel();
                Link todas = linkTo(
                        methodOn(MovieController.class).get(0, 20, null, null, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todas.toString())
                        .body(filmFound.get());
            } else {
                throw new notFoundExecption("FILM NOT FOUND");
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }



    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "DeleteFilm",
            summary = "Delete an existing film",
            description = "you must be logged as an administrator"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deleted Film Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content),
    })ResponseEntity<?> delete(@Parameter(name = "film id") @PathVariable("id") String id) {
        Optional<Film> filmFound;
        try {
            filmFound = movies.delete(id);
            if (filmFound.isPresent()) {
                Link todas = linkTo(
                        methodOn(MovieController.class).get(0, 20, null, null, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, todas.toString())
                        .body(filmFound.get());
            } else {
                throw new notFoundExecption("NOT FOUND");
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }

    }

    @PreAuthorize("hasRole('ADMIN')")
    @CrossOrigin
    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PatchFilm",
            summary = "Modify an existing film"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified Film Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Film not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "Error on body",
                    content = @Content),
    })@io.swagger.v3.oas.annotations.parameters.RequestBody()
    ResponseEntity<?> patchFilm(@Parameter(name = "film id") @PathVariable("id") String id, @RequestBody  List<Map<String, Object>>  changes) {
        Optional<Film> filmsFound;
        try {
            filmsFound = movies.patchFilm(id, changes);
            if (filmsFound.isPresent()) {
                Link self = linkTo(
                        methodOn(MovieController.class).patchFilm(id, changes)
                ).withSelfRel();
                Link todas = linkTo(
                        methodOn(MovieController.class).get(0, 20, null, null, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todas.toString())
                        .body(filmsFound.get());
            } else {
                throw new notFoundExecption("FILM NOT FOUND");
            }

        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }

    }
}
