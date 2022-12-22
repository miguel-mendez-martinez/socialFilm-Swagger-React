package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository users;
    private final PatchUtils patcher;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository user, PatchUtils patcher, PasswordEncoder encoder) {
        this.users = user;
        this.patcher = patcher;
        this.encoder = encoder;
    }

    public Optional<Page<User>> get(int page, int size, Sort sort, String email, String name) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withMatcher("email", ExampleMatcher.GenericPropertyMatchers.exact().ignoreCase())
                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
                //.withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<User> filter = Example.of(
                new User().setEmail(email).setName(name),
                matcher
        );
        Page<User> result = users.findAll(filter, request);

        Page<User> finalResult = result.map(user -> user.setPassword(null));

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(finalResult);
    }

    public Optional<User> get(String id) {
        Optional<User> result = users.findById(id);
        if (!result.isPresent())
            return Optional.empty();

        else return result;
    }

    public Optional<User> post(User user) {

        if(!users.existsById(user.getEmail())) {
            // Modificamos o contrasinal para gardalo codificado na base de datos
            user.setPassword(encoder.encode(user.getPassword()));
            List<String> rolUser = new ArrayList<>();
            rolUser.add("ROLE_USER");
            user.setRoles(rolUser);
            return Optional.of(users.save(user));
        }else{
            return Optional.empty();
        }
    }

    public Optional<User> put(String id, User user) {
        //primero debemos saber que usuario es buscandolo con el id
        Optional<User> userPut = this.get(id);

        //si esta vacío el ID esta mal y debemos no pasarle nada de respuesta al controlador
        if(!userPut.isPresent()){
            return Optional.empty();
        }

        //si se encuentra hacemos los cambios
        userPut.get().setName(user.getName());
        userPut.get().setCountry(user.getCountry());
        userPut.get().setPicture(user.getPicture());

        User result = users.save(userPut.get());

        result.setPassword(null);

        return Optional.of(result);
    }

    public Optional<User> delete(String id) {

        Optional<User> userDelete = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if(!userDelete.isPresent()){
            return Optional.empty();
        }

        users.delete(userDelete.get());

        userDelete.get().setPassword(null);

        //si va bien devolvemos el usuario eliminado para que el controlador sepa que ha sido eliminado
        return userDelete;
    }

    public Optional<User> patchUser(String id, List<Map<String, Object>> changes) {
        Optional<User> userPatch = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if (!userPatch.isPresent()) {
            return Optional.empty();
        }

        User userPatched;
        try {
            userPatched = patcher.patch(userPatch.get(), changes);

            User userSaved = users.save(userPatched);

            userSaved.setPassword(null);

            return Optional.of(userSaved);
        } catch (JsonPatchException e) {
            throw new RuntimeException(e);
        }
    }

}
