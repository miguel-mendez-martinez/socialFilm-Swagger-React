package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.AssessementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssessementService {
    private final AssessementRepository assements;
    private final PatchUtils patcher;

    @Autowired
    public AssessementService(AssessementRepository assements, PatchUtils patcher){
        this.assements = assements;
        this.patcher = patcher;
    }

    public Optional<Page<Assessment>> get(int page, int size, Sort sort, String user, String film){
        Pageable request = PageRequest.of(page, size, sort);
        Assessment comment = new Assessment().setUser(new User().setEmail(user)).setMovie(new Film().setId(film));
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withMatcher("user", match -> match.transform(source -> Optional.of(source.get())).ignoreCase().exact())
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);
        Example<Assessment> filter = Example.of(
                comment,
                matcher
        );

        //si no viene nada en la request, ni user ni film devolvemos vacio
        if(user == "" && film == ""){
            return Optional.empty();
        }

        Page<Assessment> result = assements.findAll(filter, request);

        if (result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Assessment> get(String id) {
        Optional<Assessment> result = assements.findById(id);
        if (!result.isPresent())
            return Optional.empty();

        else return result;
    }

    public Optional<Assessment> post(Assessment assessment){
        Assessment nuevoAssessment = assements.save(assessment);

        return Optional.of(nuevoAssessment);
    }
    public Optional<Assessment> put(String id, Assessment assessment){
        //primero debemos saber que usuario es buscandolo con el id
        Optional<Assessment> assessmentPut = this.get(id);

        //si esta vacío el ID esta mal y debemos no pasarle nada de respuesta al controlador
        if(!assessmentPut.isPresent()){
            return Optional.empty();
        }

        //si se encuentra hacemos los cambios
        assessmentPut.get().setRating(assessment.getRating());
        assessmentPut.get().setComment(assessment.getComment());

        Assessment result = assements.save(assessmentPut.get());

        return Optional.of(result);
    }

    public Optional<Assessment> delete(String id){
        Optional<Assessment> assessmentDelete = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if(!assessmentDelete.isPresent()){
            return Optional.empty();
        }

        assements.delete(assessmentDelete.get());

        //si va bien devolvemos el usuario eliminado para que el controlador sepa que ha sido eliminado
        return assessmentDelete;
    }
    public Optional<Assessment> patchAssessment(String id, List<Map<String, Object>> changes) {
        Optional<Assessment> assessmentPatch = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if (!assessmentPatch.isPresent()) {
            return Optional.empty();
        }

        Assessment assessmentPatched;
        try {
            assessmentPatched = patcher.patch(assessmentPatch.get(), changes);

            Assessment filmSaved = assements.save(assessmentPatched);

            return Optional.of(filmSaved);
        } catch (JsonPatchException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }


}
