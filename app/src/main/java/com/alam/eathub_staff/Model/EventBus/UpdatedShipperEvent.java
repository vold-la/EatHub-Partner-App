package com.alam.eathub_staff.Model.EventBus;

import com.alam.eathub_staff.Model.Shipper;

public class UpdatedShipperEvent {
    private Shipper shipper;
    private boolean isActive;

    public UpdatedShipperEvent(Shipper shipper, boolean isActive) {
        this.shipper = shipper;
        this.isActive = isActive;
    }

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
