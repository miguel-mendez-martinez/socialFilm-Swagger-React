package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.controller.badRequestException;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.FriendShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FriendshipService {
    private final FriendShipRepository friends;
    private final UserService users;
    private final PatchUtils patcher;

    @Autowired
    public FriendshipService(FriendShipRepository friends, PatchUtils patcher, UserService users) {
        this.patcher = patcher;
        this.friends = friends;
        this.users = users;
    }

    public Optional<List<Friendship>> get(int page, int size, Sort sort, String idUser, String userFriend){

        Friendship friendship = new Friendship();
        friendship.setUser(idUser);
        friendship.setFriend(userFriend);

        Friendship friendship2 = new Friendship();
        friendship.setUser(idUser);
        friendship.setFriend(userFriend);

        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withMatcher("user", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("friend", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<Friendship> filter = Example.of(friendship, matcher);
        Example<Friendship> filter2 = Example.of(friendship2, matcher);

        List<Friendship> result = friends.findAll(filter);
        List<Friendship> result2 = friends.findAll(filter2);

        List<Friendship> finalResult = Stream.concat(result.stream(), result2.stream()).collect(Collectors.toList());

        List<Friendship> finalResultFinal = finalResult.stream().filter((friend) -> (friend.getUser().equals(idUser) || friend.getFriend().equals(idUser))).collect(Collectors.toList());

        finalResultFinal = finalResultFinal.stream()
                .distinct()
                .collect(Collectors.toList());
        if(!userFriend.equals("")){
            finalResultFinal = finalResultFinal.stream().filter((friend) -> (friend.getUser().equals(userFriend) || friend.getFriend().equals(userFriend))).collect(Collectors.toList());
        }

        if(finalResultFinal.isEmpty()){
            return Optional.empty();
        }else{
            return Optional.of(finalResultFinal);
        }
    }

    public Optional<Friendship> get(String id) {
        Optional<Friendship> result = friends.findById(id);
        if (!result.isPresent())
            return Optional.empty();

        else return result;
    }

    public Boolean areFriends(String user1, String user2) {

        Example<Friendship> filter = Example.of(new Friendship().setUser(user1).setFriend(user2), ExampleMatcher.matchingAll().withIgnoreCase());
        Example<Friendship> filter2 = Example.of(new Friendship().setUser(user2).setFriend(user1), ExampleMatcher.matchingAll().withIgnoreCase());
        List<Friendship> result1 = friends.findAll(filter);
        List<Friendship> result2 = friends.findAll(filter2);

        if(result1.isEmpty() && result2.isEmpty()){
            //no hemos encontrado relacion de amigos de nuestros 2 usuarios
            return false;
        }else{
            return true;
        }
    }

    public Optional<Friendship> post(String idUser, Friendship friend) {

        //primero debemos comprobar que ambos usuarios existan en la base de datos
        Optional<User> solicitante = users.get(idUser);
        if(solicitante.isPresent()){
            //el solicitante existe pasamos al que se le hace la peticion
            Optional<User> amigo = users.get(friend.getFriend());
            if(amigo.isPresent()){
                //existen ambos usuarios, debemos comprobar si son amigos
                if(this.areFriends(solicitante.get().getEmail(), amigo.get().getEmail())){
                    throw new badRequestException("THE USERS ARE ALREADY FRIENDS");
                }else{
                    //no son amigos asi que procedemos a hacer la nueva amistad
                    return Optional.of(friends.save(friend));
                }
            }else{
                throw new badRequestException("ONE OF THE USERS DOES NOT EXIST");
            }
        }else{
            throw new badRequestException("ONE OF THE USERS DOES NOT EXIST");
        }
    }

    public Optional<Friendship> delete(String idUser, String idFriendship){

        //primero debemos saber si existe el usuario

        Optional<User> solicitante = users.get(idUser);
        if(solicitante.isPresent()){
            //el solicitante existe pasamos a ver si la amistad existe
            Optional<Friendship> amistad = this.get(idFriendship);
            if(amistad.isPresent()){
                //son amigos asi que procedemos a deshacer
                friends.delete(amistad.get());
                return amistad;

            }else{
                throw new badRequestException("ERROR IN FRIENDSHIP ID");
            }
        }else{
            throw new badRequestException("THE USER DOES NOT EXISt");
        }
    }

    public Optional<Friendship> patchFriendship(String id, List<Map<String, Object>> changes) {
        Optional<Friendship> frienship = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if (!frienship.isPresent()) {
            return Optional.empty();
        }

        if(frienship.get().getConfirmed() == Boolean.TRUE){
            //ya esta confirmada no se dejará volver a hacerlo
            throw new badRequestException("THE FRIENDSHIP IS ALREADY CONFIRMED");
        }

        Friendship friendshipPatched;
        try {
            friendshipPatched = patcher.patch(frienship.get(), changes);

            LocalDate now = java.time.LocalDate.now();
            Date ahora = new Date(now.getDayOfMonth(), now.getMonth().getValue(), now.getYear());

            friendshipPatched.setSince(ahora);

            Friendship friendShipSaved = friends.save(friendshipPatched);

            return Optional.of(friendShipSaved);
        } catch (JsonPatchException e) {
            throw new RuntimeException(e);
        }
    }
}
