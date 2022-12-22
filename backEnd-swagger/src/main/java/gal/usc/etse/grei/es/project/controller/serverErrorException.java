package gal.usc.etse.grei.es.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.INTERNAL_SERVER_ERROR
)
public class serverErrorException extends RuntimeException{
    public serverErrorException(String T){
        super(T);

    }
}
