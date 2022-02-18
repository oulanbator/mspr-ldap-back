package epsi.mspr.ldapback.exception;


import org.springframework.security.authentication.AccountStatusException;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author GraafiK
 */
public class BlockedException extends AccountStatusException {

    public BlockedException(String msg) {
        super(msg);
    }
    

    public BlockedException(String msg, Throwable cause) {
        super(msg,cause);
    }
}

