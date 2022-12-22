package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.FriendshipService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("users")
@CrossOrigin
@Tag(name = "User API", description = "User related operations")
@SecurityRequirement(name = "JWT")
public class UsersController {
    private final UserService users;
    private final FriendshipService friends;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public UsersController(UserService users, FriendshipService friends, LinkRelationProvider relationProvider) {
        this.users = users;
        this.friends = friends;
        this.relationProvider = relationProvider;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            operationId = "getAllUsers",
            summary = "Get the list of users, paginated"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
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
    })ResponseEntity<?> getWEmail(
            @Parameter(name = "Number of page", required = false) @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "Page size", required = false) @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(name = "Sorting value", required = false) @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @Parameter(name = "User email", required = false) @RequestParam(name = "email", required = false) String email,
            @Parameter(name = "User name", required = false) @RequestParam(name = "name", required = false) String name
    ){
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Optional<String> notFound = Optional.of("NOT FOUND");

        Optional<Page<User>> usersFound;
        try {
            usersFound = users.get(page, size, Sort.by(criteria), email, name);
            if (usersFound.isPresent()) {
                Page<User> data = usersFound.get();
                Pageable metadata = data.getPageable();
                Link self = linkTo(
                        methodOn(UsersController.class).getWEmail(page, size, sort, email, name)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(UsersController.class).getWEmail(metadata.first().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(UsersController.class).getWEmail(data.getTotalPages() - 1, size, sort, email, name)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(UsersController.class).getWEmail(metadata.next().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(UsersController.class).getWEmail(metadata.previousOrFirst().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link one = linkTo(
                        methodOn(UsersController.class).get(null)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, one.toString())
                        .body(usersFound.get());
            } else {
                throw new notFoundExecption("USERS NOT FOUND.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("INTERNAL SERVER ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') or #id == principal or @friendshipService.areFriends(#id, principal)")
    @Operation(
            operationId = "getOneUser",
            summary = "Get a single user details",
            description = "Get the details for a given user. To see the user details " +
                    "you must be the requested user, his friend, or have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
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
    }) ResponseEntity<?> get(@Parameter(name = "User email", required = true)
                                 @PathVariable("id") String id) {
        Optional<User> usersFound;
        try {
            usersFound = users.get(id);
            if (usersFound.isPresent()) {
                Link self = linkTo(
                        methodOn(UsersController.class).get(id)
                ).withSelfRel();
                Link todos = linkTo(
                        methodOn(UsersController.class).getWEmail(0, 20, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(usersFound.get());
            } else {
                throw new notFoundExecption("USERS NOT FOUND.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>("INTERNAL SERVER ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PostUser",
            summary = "Create a new user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "New User Detail",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "User already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Errors on Body",
                    content = @Content
            ),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody()
    ResponseEntity<?> post( @RequestBody User user) {
        String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        try{
            Boolean emailValid = Pattern.compile(regexPattern).matcher(user.getEmail()).matches();
            if(!emailValid){
                throw new badRequestException("EMAIL NOT VALID");
            }
            Optional<User> usersFound = users.post(user);
            if(usersFound.isPresent()){
                Link self = linkTo(
                        methodOn(UsersController.class).post(user)
                ).withSelfRel();
                Link todos = linkTo(
                        methodOn(UsersController.class).getWEmail(0, 20, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);
                URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().pathSegment(usersFound.get().getEmail()).build().toUri();
                return ResponseEntity.created(uri)
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(usersFound.get());
            }else{
                throw new serverErrorException("INTERNAL SERVER ERROR."); //reutilizo esta excepcion para que la pille el catch y lance el internal error
            }
        }catch (badRequestException e){
            throw new badRequestException(e.getMessage());
        }catch (Exception e){
            throw new serverErrorException(e.getMessage());
        }
    }

    @PreAuthorize("#id == principal")
    @PutMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PutUser",
            summary = "Modify an existing user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified User Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
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
    ResponseEntity<?> put(@Parameter(name = "user email") @PathVariable("id") String id, @RequestBody User user) {
        Optional<User> usersFound;
        try {
            usersFound = users.put(id, user);
            if (usersFound.isPresent()) {
                Link self = linkTo(
                        methodOn(UsersController.class).put(id, user)
                ).withSelfRel();
                Link todos = linkTo(
                        methodOn(UsersController.class).getWEmail(0, 20, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(usersFound.get());
            } else {
                throw new notFoundExecption("USER NOT FOUND.");
            }

        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }

    }

    @PreAuthorize("#id == principal")
    @Operation(
            operationId = "DeleteUser",
            summary = "Delete an existing user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deleted User Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content),
    })
    @DeleteMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )ResponseEntity<?> delete(@Parameter(name = "user email") @PathVariable("id") String id) {
        Optional<User> usersFound;
        try {
            usersFound = users.delete(id);

            if (usersFound.isPresent()) {
                Link todos = linkTo(
                        methodOn(UsersController.class).getWEmail(0, 20, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(usersFound.get());
            } else {
                throw new notFoundExecption("USER NOT FOUND");
            }
        } catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }
    }

    @PreAuthorize("#id == principal")
    @Operation(
            operationId = "PatchUser",
            summary = "Modify an existing user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Modified User Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
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
    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )ResponseEntity<?> patchUser(@Parameter(name = "user email") @PathVariable("id") String id, @RequestBody  List<Map<String, Object>>  changes) {
        Optional<User> usersFound;
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonPatch operations = mapper.convertValue(changes, JsonPatch.class);

            if(operations.toString().contains("email") || operations.toString().contains("birthday")){
                throw new badRequestException("CANT EDIT EMAIL OR DATE");
            }

            usersFound = users.patchUser(id, changes);
            if (usersFound.isPresent()) {
                Link self = linkTo(
                        methodOn(UsersController.class).patchUser(id, changes)
                ).withSelfRel();
                Link todos = linkTo(
                        methodOn(UsersController.class).getWEmail(0, 20, null, null, null)
                ).withRel(IanaLinkRelations.NEXT);
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(usersFound.get());
            } else {
                throw new notFoundExecption("USER NOT FOUND");
            }
        }catch (badRequestException e){
            throw new badRequestException("CANT EDIT EMAIL OR DATE.");
        }
        catch (Exception e) {
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }
    }


    //aqui tod lo relacionado con friends


    //metodo para obtener todos los amigos de un usuario
    @PreAuthorize("(#idUser==Null and #idUser == principal) or (#idUser!=Null and (@friendshipService.areFriends(#idUser, principal)) or #idUser == principal)")
    @GetMapping(
            path = "{idUser}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "getAllFriends",
            summary = "Get the list of friend of an user, paginated",
            description = "You have to be logged on as the user, a friend of the user or an administrator "
    ) @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of friend details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Users arent friends",
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
    })ResponseEntity<?> getFriends(@PathVariable("idUser") String idUser,
                                   @Parameter(name = "Number of page", required = false) @RequestParam(name = "page", defaultValue = "0") int page,
                                   @Parameter(name = "Page size", required = false) @RequestParam(name = "size", defaultValue = "20") int size,
                                   @Parameter(name = "Sorting value", required = false) @RequestParam(name = "sort", defaultValue = "") List<String> sort,
                                   @Parameter(name = "Friend email", required = false) @RequestParam(name = "user", required = false, defaultValue = "") String user){
        List<Sort.Order> criteria = sort.stream().map(string -> {
                    if (string.startsWith("+")) {
                        return Sort.Order.asc(string.substring(1));
                    } else if (string.startsWith("-")) {
                        return Sort.Order.desc(string.substring(1));
                    } else return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Optional<List<Friendship>> friendsFound;
        try {
            friendsFound = friends.get(page, size, Sort.by(criteria), idUser, user);
            if (friendsFound.isPresent()) {
                //va a haber 2 casos, en el que se obtenga 1 amigo o donde se obtengan varios
                if(user == null){
                    //no se ha buscado por usuario, por lo que es una busqueda de todos

                    Link self = linkTo(
                            methodOn(UsersController.class).getFriends(idUser, page, size, sort, user)
                    ).withSelfRel();

                    return ResponseEntity.ok()
                            .header(HttpHeaders.LINK, self.toString())
                            .body(friendsFound.get());

                }else{
                    //se ha buscado un amigo en concreto
                    Link self = linkTo(
                            methodOn(UsersController.class).getFriends(idUser, page, size, sort, user)
                    ).withSelfRel();
                    Link todos = linkTo(
                            methodOn(UsersController.class).getFriends(idUser, 0, 20, null, null)
                    ).withRel(IanaLinkRelations.NEXT);
                    Link usuario = linkTo(
                            methodOn(UsersController.class).get(idUser)
                    ).withRel(IanaLinkRelations.ITEM);
                    Link amigo = linkTo(
                            methodOn(UsersController.class).get(user)
                    ).withRel(IanaLinkRelations.ITEM);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.LINK, self.toString())
                            .header(HttpHeaders.LINK, todos.toString())
                            .header(HttpHeaders.LINK, usuario.toString())
                            .header(HttpHeaders.LINK, amigo.toString())
                            .body(friendsFound.get());
                }
            } else {
                return ResponseEntity.ok().body(Optional.empty());
            }
        } catch (Exception e) {
            return new ResponseEntity<>("INTERNAL SERVER ERROR " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //añadir amigos
    @PreAuthorize("#idUser == principal")
    @PostMapping(
            path = "{idUser}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "PostFriend",
            summary = "Send a friend request to an user",
            description = "Send from the user in the path variable to the user on the body a new friend request."
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "New unconfirmed friendship Detail",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "The users already are friends",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Errors on Body",
                    content = @Content
            ),
    }) @io.swagger.v3.oas.annotations.parameters.RequestBody()
    ResponseEntity<?> postFriendship(@Parameter(name = "User email") @PathVariable("idUser") String idUser,@RequestBody Friendship friendship){
        try{
            Optional<Friendship> friendShip = friends.post(idUser, friendship);

            if(friendShip.isPresent()){
                Link self = linkTo(
                        methodOn(UsersController.class).postFriendship(idUser, friendship)
                ).withSelfRel();
                Link todos = linkTo(
                        methodOn(UsersController.class).getFriends(idUser, 0, 20, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(friendShip.get());
            }else{
                throw new serverErrorException("INTERNAL SERVER ERROR."); //reutilizo esta excepcion para que la pille el catch y lance el internal error
            }
        }catch (badRequestException e){
            throw new badRequestException(e.getMessage());
        }catch (Exception e){
            throw new serverErrorException("INTERNAL SERVER ERROR");
        }
    }

    //eliminar amigos
    @PreAuthorize("#idUser == principal")
    @Operation(
            operationId = "DeleteFriend",
            summary = "Delete an existing friend from the users friends",
            description = "you must be logged as the user searched"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Deleted Friendship Details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "FriendShip not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Not enough privileges",
                    content = @Content),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content),
    }) @DeleteMapping(
            path = "{idUser}/friends/{idFriendship}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )ResponseEntity<?> deleteFriendship(@Parameter(name = "User email") @PathVariable("idUser") String idUser, @Parameter(name = "Friendship id") @PathVariable("idFriendship") String idFriendship){
        try{
            Optional<Friendship> amistad =  friends.delete(idUser, idFriendship);

            if(amistad.isPresent()){
                Link todos = linkTo(
                        methodOn(UsersController.class).getFriends(idUser, 0, 20, null, null)
                ).withRel(IanaLinkRelations.NEXT);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, todos.toString())
                        .body(amistad.get());
            }else{
                throw new serverErrorException("INTERNAL SERVER ERROR");
            }
        }catch (badRequestException e){
            throw new badRequestException(e.getMessage());
        }catch (Exception e){
            throw new serverErrorException(e.getMessage());
        }
    }

    //confirmar la amistad
    @PreAuthorize("#idUser == principal")
    @PatchMapping(
            path = "{idUser}/friends/{idFriendship}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )@Operation(
            operationId = "ConfirmFriendShip",
            summary = "Confirm a friendship"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Confirmed friendship details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Friendship not found",
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
    ResponseEntity<?> patchFriendship(@Parameter(name = "user email") @PathVariable("idUser") String idUser, @Parameter(name = "friendship id") @PathVariable("idFriendship") String idFriendship, @RequestBody  List<Map<String, Object>>  changes) {
        try{
            ObjectMapper mapper = new ObjectMapper();

            JsonPatch operations = mapper.convertValue(changes, JsonPatch.class);

            if(operations.toString().contains("confirmed")){
                Optional<Friendship> amistad = friends.patchFriendship(idFriendship, changes);
                if(amistad.isPresent()){
                    Link self = linkTo(
                            methodOn(UsersController.class).patchFriendship(idUser, idFriendship, changes)
                    ).withSelfRel();
                    Link todos = linkTo(
                            methodOn(UsersController.class).getFriends(idUser, 0, 20, null, null)
                    ).withRel(IanaLinkRelations.NEXT);
                    Link usuario = linkTo(
                            methodOn(UsersController.class).get(amistad.get().getUser())
                    ).withRel(IanaLinkRelations.ITEM);
                    Link amigo = linkTo(
                            methodOn(UsersController.class).get(amistad.get().getFriend())
                    ).withRel(IanaLinkRelations.ITEM);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.LINK, self.toString())
                            .header(HttpHeaders.LINK, todos.toString())
                            .header(HttpHeaders.LINK, usuario.toString())
                            .header(HttpHeaders.LINK, amigo.toString())
                            .body(amistad.get());
                }else{
                    throw new serverErrorException("INTERNAL SERVER ERROR");
                }
            }else{
                throw new badRequestException("CAN NOT EDIT THOSE FIELDS");
            }
        }catch (badRequestException e){
            throw new badRequestException(e.getMessage());
        }catch (Exception e){
            throw new serverErrorException(e.getMessage());
        }
    }
}
