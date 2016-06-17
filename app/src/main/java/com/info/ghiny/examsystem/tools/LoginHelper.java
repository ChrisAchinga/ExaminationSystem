package com.info.ghiny.examsystem.tools;

import com.info.ghiny.examsystem.database.Identity;

/**
 * Created by GhinY on 15/06/2016.
 *
 * Login Helper is a tools to execute all back end logic for --- MainLoginActivity ---
 * without touching the UI interface. This allow automated test without using a hardware
 * mobile.
 */
public class LoginHelper {
    //This method take in an id and check if the id is an invigilator identity
    public static void checkInvigilator(Identity invglt) throws CustomException{
        if(invglt == null){
            throw new CustomException("Not an Identity", CustomException.ERR_NULL_IDENTITY,
                    IconManager.WARNING);
        } else {
            if(!invglt.getEligible())
                throw new CustomException("Unauthorized Invigilator",
                        CustomException.ERR_ILLEGAL_IDENTITY, IconManager.WARNING);
        }
    }

    //This method check whether the input password was the password of the invglt
    public static void checkInputPassword(Identity invglt, String pw) throws CustomException{
        if(invglt == null)
            throw new CustomException("Input ID is null", CustomException.ERR_NULL_IDENTITY,
                    IconManager.WARNING);

        if(pw == null || pw.isEmpty()) {
            throw new CustomException("Please enter a password to proceed",
                    CustomException.ERR_EMPTY_PASSWORD, IconManager.MESSAGE);
        } else {
            if (!invglt.matchPassword(pw))
                throw new CustomException("Input password is incorrect",
                        CustomException.ERR_WRONG_PASSWORD, IconManager.WARNING);
        }
    }

}
