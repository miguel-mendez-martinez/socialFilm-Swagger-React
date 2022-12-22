package gal.usc.etse.grei.es.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.NOT_FOUND
)
public class notFoundExecption extends RuntimeException{
    public notFoundExecption(String T){
        super(T);

    }
}
