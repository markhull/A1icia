/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.api.shared;

/**
 * A utility class to allow functions to return both a message and an explanation.
 * 
 * @author hulles
 */
public final class Response {
    private String message;
    private String explanation;

    public Response() {
    }
    public Response(String message) {
        
        SharedUtils.checkNotNull(message);
        setMessage(message);
    }
    public Response(String message, String explanation) {
        this(message);
        
        SharedUtils.checkNotNull(explanation);
        setExplanation(explanation);
    }
    
    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        SharedUtils.checkNotNull(message);
        this.message = message;
    }

    public String getExplanation() {

        return explanation;
    }

    public void setExplanation(String explanation) {

        SharedUtils.nullsOkay(explanation);
        this.explanation = explanation;
    }


}
