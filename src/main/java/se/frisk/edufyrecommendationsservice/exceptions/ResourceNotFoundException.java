package se.frisk.edufyrecommendationsservice.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message){

        super(message);
    }
}
