package main.api.response;

public class ResponseResultTrue extends AbstractResponse{

    boolean result = true;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
