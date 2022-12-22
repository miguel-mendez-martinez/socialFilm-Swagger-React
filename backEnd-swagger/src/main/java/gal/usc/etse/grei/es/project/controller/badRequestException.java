package gal.usc.etse.grei.es.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        value = HttpStatus.BAD_REQUEST
)
public class badRequestException extends RuntimeException{
    public badRequestException(String T){
        super(T);

    }
}
