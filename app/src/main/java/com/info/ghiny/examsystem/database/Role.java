package com.info.ghiny.examsystem.database;

/**
 * Created by FOONG on 12/12/2016.
 */

public enum Role {
    CHIEF,
    INVIGILATOR,
    IN_CHARGE;

    @Override
    public String toString() {
        return super.toString();
    }

    public static Role parseRole(String role){
        switch(role){
            case "CHIEF":
                return CHIEF;
            case "IN_CHARGE":
                return IN_CHARGE;
            default:
                return INVIGILATOR;
        }
    }
}
