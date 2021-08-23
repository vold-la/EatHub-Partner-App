package com.alam.eathub_staff.Model;

import java.util.List;

public class ShipperOrderModel {
    private boolean success;
    private String message;
    private List<ShipperOrder> result;

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

    public List<ShipperOrder> getResult() {
        return result;
    }

    public void setResult(List<ShipperOrder> result) {
        this.result = result;
    }
}
