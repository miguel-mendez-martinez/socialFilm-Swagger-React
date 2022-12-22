package gal.usc.etse.grei.es.project.service;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.Resource;
import gal.usc.etse.grei.es.project.model.ResourceType;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movies;
    private final PatchUtils patcher;

    @Autowired
    public MovieService(MovieRepository movies, PatchUtils patcher) {
        this.patcher = patcher;
        this.movies = movies;
    }

    public Optional<Page<Film>> get(int page, int size, Sort sort, List<String> clave, List<String> genero, String estreno, String title) {
        Pageable request = PageRequest.of(page, size, sort);

        Film peliBuscar = new Film();

        peliBuscar.setKeywords(clave);
        peliBuscar.setGenres(genero);
        peliBuscar.setTitle(title);


        if(estreno != null){
            String[] parts = estreno.split("/");
            Date fechaEstreno = new Date();
            fechaEstreno.setDay(Integer.parseInt(parts[0]));
            fechaEstreno.setMonth(Integer.parseInt(parts[1]));
            fechaEstreno.setYear(Integer.parseInt(parts[2]));

            peliBuscar.setReleaseDate(fechaEstreno);
        }else{
            Date fechaEstreno = new Date();
            peliBuscar.setReleaseDate(fechaEstreno);
        }


        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withMatcher("genres", match -> match.transform(source -> Optional.of(((List) source.get()).iterator().next())).ignoreCase().contains())
                .withMatcher("keywords", match -> match.transform(source -> Optional.of(((List) source.get()).iterator().next())).ignoreCase().contains())
                .withMatcher("crew", match -> match.transform(source -> Optional.of(((List) source.get()).iterator().next())).ignoreCase().contains())
                .withMatcher("cast", match -> match.transform(source -> Optional.of(((List) source.get()).iterator().next())).ignoreCase().contains())
                .withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Film> filter = Example.of(
                peliBuscar,
                matcher
        );

        Page<Film> result = movies.findAll(filter, request);

        if (result.isEmpty())
            return Optional.empty();
        else{
            Page<Film> finalResult = result.map(film -> {
                film.setTagline(null);
                film.setCollection(null);
                film.setKeywords(null);
                film.setProducers(null);
                film.setCrew(null);
                film.setCast(null);
                film.setBudget(null);
                film.setStatus(null);
                film.setRuntime(null);
                film.setRevenue(null);

                //se debe mirar si tiene un poster, si no se le asocia uno de stock
                if(film.getResources() != null){
                    //si hay algo en resources miramos si hay un poster
                }else{
                    //no hay recursos, le asociamos un cartel por defecto
                    Resource posterStock = new Resource(ResourceType.POSTER, "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.shutterstock.com%2Fsearch%2Fmovie-poster&psig=AOvVaw0fYY5CYIkpWhnHkBor1oYF&ust=1670580471195000&source=images&cd=vfe&ved=0CBAQjRxqFwoTCMihrq3j6fsCFQAAAAAdAAAAABAE");
                }

                return film;
            });
            return Optional.of(finalResult);
        }
    }

    public Optional<Film> get(String id) {
        return movies.findById(id);
    }
    public Optional<Film> post(Film film) {
        Film nuevaFilm = movies.save(film);

        return Optional.of(nuevaFilm);
    }
    public Optional<Film> put(String id, Film film) {
        //primero debemos saber que usuario es buscandolo con el id
        Optional<Film> filmPut = this.get(id);

        //si esta vacío el ID esta mal y debemos no pasarle nada de respuesta al controlador
        if(!filmPut.isPresent()){
            return Optional.empty();
        }

        //si se encuentra hacemos los cambios
        filmPut.get().setCast(film.getCast());
        filmPut.get().setBudget(film.getBudget());
        filmPut.get().setCollection(film.getCollection());
        filmPut.get().setCrew((film.getCrew()));
        filmPut.get().setGenres(film.getGenres());
        filmPut.get().setKeywords(film.getKeywords());
        filmPut.get().setOverview(film.getOverview());
        filmPut.get().setProducers(film.getProducers());
        filmPut.get().setReleaseDate(film.getReleaseDate());
        filmPut.get().setResources(film.getResources());
        filmPut.get().setRevenue(film.getRevenue());
        filmPut.get().setRuntime(film.getRuntime());
        filmPut.get().setStatus(film.getStatus());
        filmPut.get().setTagline(film.getTagline());
        filmPut.get().setTitle(film.getTitle());

        Film savedFilm = movies.save(filmPut.get());

        return Optional.of(savedFilm);
    }
    public Optional<Film> delete(String id) {

        Optional<Film> filmDelete = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if(!filmDelete.isPresent()){
            return Optional.empty();
        }

        movies.delete(filmDelete.get());

        //si va bien devolvemos el usuario eliminado para que el controlador sepa que ha sido eliminado
        return filmDelete;
    }

    public Optional<Film> patchFilm(String id, List<Map<String, Object>> changes) {
        Optional<Film> filmPatch = this.get(id);
        //debemos ver si existe en la base de datos, si no existe devolvemos vacio
        if (!filmPatch.isPresent()) {
            return Optional.empty();
        }

        Film filmPatched;
        try {
            filmPatched = patcher.patch(filmPatch.get(), changes);

            Film filmSaved = movies.save(filmPatched);

            return Optional.of(filmSaved);
        } catch (JsonPatchException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
