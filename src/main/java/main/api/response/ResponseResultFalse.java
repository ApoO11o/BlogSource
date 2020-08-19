package main.api.response;

import java.util.HashMap;

public class ResponseResultFalse extends AbstractResponse {

    boolean result = false;
    HashMap errors;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public HashMap getErrors() {
        return errors;
    }

    public void setErrors(HashMap errors) {
        this.errors = errors;
    }
}
