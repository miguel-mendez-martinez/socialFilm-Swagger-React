package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.service.AssessementService;
import gal.usc.etse.grei.es.project.service.UserService;
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
@RequestMapping("assessements")
@CrossOrigin
@Tag(name = "Assessment API", description = "Assessment related operations")
@SecurityRequirement(name = "JWT")
public class AssessementController {
    private final AssessementService assessements;
    private final UserService users;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public AssessementController(AssessementService assessements, UserService users, LinkRelationProvider relationProvider){
        this.assessements = assessements;
        this.users = users;
        this.relationProvider = relationProvider;
    }

    @PreAuthorize("(#user!=Null and (#user == principal or @friendshipService.areFriends(#user, principal))) or (isAuthenticated())")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) @Operation(
            operationId = "getAllAssessments",
            summary = "Get the list of assessments, paginated",
            description = "you must be logged as the user of the assassments, as a friend or an administrator if you want to search by user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of assessments details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessments not found",
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
    })ResponseEntity<?> get(
            @Parameter(name = "Number of page", required = false) @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "Page size", required = false) @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(name = "Sorting value", required = false) @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @Parameter(name = "User's email", required = false) @RequestParam(name = "user", required = false, defaultValue = "") String user,
            @Parameter(name = "Film's id", required = false) @RequestParam(name = "film", required = false, defaultValue = "") String film
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Optional<Page<Assessment>> assessmentsFound;
        try {
            assessmentsFound = assessements.get(page, size, Sort.by(criteria), user, film);
            if (assessmentsFound.isPresent()) {
                //volvemos a tener 2 opciones, buscar por pelicula o por usuario
                //solo se diferencian en una cosa con lo cual es lo unico que haremos dentro de los ifs
                Page<Assessment> data = assessmentsFound.get();
                Pageable metadata = data.getPageable();
                Link first = linkTo(
                        methodOn(AssessementController.class).get(metadata.first().getPageNumber(), size, sort, user, film)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(AssessementController.class).get(data.getTotalPages() - 1, size, sort, user, film)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(AssessementController.class).get(metadata.next().getPageNumber(), size, sort, user, film)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(AssessementController.class).get(metadata.previousOrFirst().getPageNumber(), size, sort, user, film)
                ).withRel(IanaLinkRelations.PREVIOUS);

                //creamos la respues de manera que si se busca por pelicula o por usuario se añade ese campo
                ResponseEntity.BodyBuilder respuesta = ResponseEntity.ok();
                respuesta.header(HttpHeaders.LINK, first.toString());
                respuesta.header(HttpHeaders.LINK, last.toString());
                respuesta.header(HttpHeaders.LINK, next.toString());
                respuesta.header(HttpHeaders.LINK, previous.toString());
                if(user != null){
                    //se ha buscado por user
                    Link usuario = linkTo(
                            methodOn(UsersController.class).get(user)
                    ).withRel(IanaLinkRelations.ITEM);
                    respuesta.header(HttpHeaders.LINK, usuario.toString());
                }
                if(film != null){
                    //se ha buscado por pelicula
                    Link pelicula = linkTo(
                            methodOn(MovieController.class).get(film)
                    ).withRel(IanaLinkRelations.ITEM);
                    respuesta.header(HttpHeaders.LINK, pelicula.toString());
                }
                return respuesta.body(assessmentsFound.get());
            } else {
                throw new notFoundExecption("ASSESSMENTS NOT FOUND");
            }
        }catch (notFoundExecption e){
            throw new serverErrorException(e.getMessage());
        }
        catch (Exception e) {
            throw new serverErrorException(e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @CrossOrigin("http://localhost:3000")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PostAssessment",
            summary = "Create a new assessment by an user for a movie"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "New Assessment Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Assessment already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Errors on Body",
                    content = @Content
            ),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody()
    ResponseEntity<?> post(@RequestBody Assessment assessment ) {
        Optional<Assessment> assessmentsFound;
        try {
            assessmentsFound = assessements.post(assessment);
            if (assessmentsFound.isPresent()) {
                Link pelicula = linkTo(
                        methodOn(MovieController.class).get(assessmentsFound.get().getMovie().getId())
                ).withRel(IanaLinkRelations.ITEM);
                Link todos = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, null, assessmentsFound.get().getMovie().getId())
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, pelicula.toString())
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(assessmentsFound.get());
            } else {
                throw new serverErrorException("INTERNAL SERVER ERROR");
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }

    }

    @PreAuthorize("@assessementService.get(#id).get().getUser().getEmail()")
    @PutMapping(
            path = "{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "PutUser",
            summary = "Modify an existing assessment",
            description = "you must be logged as the user creator of the assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified Assessment Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
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
    ResponseEntity<?> put( @Parameter(name = "assessment id") @PathVariable("id") String id, @RequestBody Assessment assessment) {
        Optional<Assessment> assessmentsFound;
        try {
            assessmentsFound = assessements.put(id, assessment);

            if (assessmentsFound.isPresent()) {
                Link self = linkTo(
                        methodOn(AssessementController.class).put(id, assessment)
                ).withSelfRel();
                Link todosPeli = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, null, assessmentsFound.get().getMovie().getId())
                ).withRel(IanaLinkRelations.NEXT);
                Link todosUser = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, assessmentsFound.get().getUser().getEmail(), null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todosUser.toString())
                        .header(HttpHeaders.LINK, todosPeli.toString())
                        .body(assessmentsFound.get());
            } else {
                throw new notFoundExecption("ASSESSMENT NOT FOUND");
            }

        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }

    }

    @PreAuthorize("hasRole('ADMIN') or @assessementService.get(#id).get().getUser().getEmail() == principal")
    @DeleteMapping(
            path = "{id}"
    )@Operation(
            operationId = "DeleteAssessment",
            summary = "Delete an existing assessment",
            description = "you must be logged in as the user creator of the assessment or an administrator"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deleted Assessment Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content),
    })ResponseEntity<?> delete(@Parameter(name = "assessment id") @PathVariable("id") String id) {
        Optional<Assessment> assessmentsFound;
        try {
            assessmentsFound = assessements.delete(id);

            if (assessmentsFound.isPresent()) {
                Link todosPeli = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, null, assessmentsFound.get().getMovie().getId())
                ).withRel(IanaLinkRelations.NEXT);
                Link todosUser = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, assessmentsFound.get().getUser().getEmail(), null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, todosUser.toString())
                        .header(HttpHeaders.LINK, todosPeli.toString())
                        .body(assessmentsFound.get());
            } else {
                throw new notFoundExecption("ASSESSMENT NOT FOUND");
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }

    }

    @PreAuthorize("@assessementService.get(#id).get().getUser().getEmail() == principal")
    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PatchAssessment",
            summary = "Modify an existing assessment",
            description = "you must be logged as the user who created the assessment"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified Assessment Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Assessment not found",
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
    ResponseEntity<?> patchAssessement(@Parameter( name = "assessment id") @PathVariable("id") String id, @RequestBody  List<Map<String, Object>>  changes) {
        Optional<Assessment> assessmentFound;
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonPatch operations = mapper.convertValue(changes, JsonPatch.class);

            if(operations.toString().contains("user") || operations.toString().contains("film")){
                throw new badRequestException("CANT EDIT USER OR FILM");
            }
            assessmentFound = assessements.patchAssessment(id, changes);
            if (assessmentFound.isPresent()) {
                Link self = linkTo(
                        methodOn(AssessementController.class).patchAssessement(id, changes)
                ).withSelfRel();
                Link todosPeli = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, null, assessmentFound.get().getMovie().getId())
                ).withRel(IanaLinkRelations.NEXT);
                Link todosUser = linkTo(
                        methodOn(AssessementController.class).get(0, 20, null, assessmentFound.get().getUser().getEmail(), null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todosUser.toString())
                        .header(HttpHeaders.LINK, todosPeli.toString())
                        .body(assessmentFound.get());
            } else {
                throw new notFoundExecption("FILM NOT FOUND");
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }
    }
}
