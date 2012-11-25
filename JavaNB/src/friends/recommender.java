/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package friends;
import java.util.logging.Level;
import java.util.logging.Logger;
import misc.userLogsException;
import userLogsExtractor.userLogsExtractor;

/**
 *
 * @author srivid
 */
public class recommender {
    private String uuid;
    private double avgFrequency;
    private double avgTimeSpent;
    private double frequencySD;
    private double timespentSD;
    
    public recommender(String uuid) {
        this.uuid = uuid;
    }
}
