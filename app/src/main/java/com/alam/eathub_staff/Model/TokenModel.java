package com.alam.eathub_staff.Model;

import java.util.List;

public class TokenModel {
    private boolean success;
    private  String  message;
    private List<MyToken> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MyToken> getResult() {
        return result;
    }

    public void setResult(List<MyToken> result) {
        this.result = result;
    }
}
