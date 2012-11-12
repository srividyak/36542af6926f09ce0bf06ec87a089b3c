/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.userpackage;

/**
 *
 * @author srivid
 */
public class userException extends Exception {
    private String errorMsg;
    public String getErrorMsg() {
        return errorMsg;
    }
    public userException(String msgString) {
        errorMsg = msgString;
        System.out.println(msgString);
    }
    
}
