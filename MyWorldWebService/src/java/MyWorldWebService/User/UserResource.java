/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyWorldWebService.User;

import MyWorldWebService.jsonRenderer;
import UGCThreads.board;
import entity.entity;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * REST Web Service
 *
 * @author srivid
 */
@Path("user")
public class UserResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of UserResource
     */
    public UserResource() {
    }

    @GET
    @Produces("application/json")
    @Path("/boards")
    public String getBoards(@QueryParam("uuid") String uuid, @QueryParam("offset") int offset, @QueryParam("count") int count) {
        entity entity = new entity();
        entity.setUuid(uuid);
        try {
            List<board> list = entity.getAllBoards();
            return jsonRenderer.toJSON(list);
        } catch (userException ex) {
            Logger.getLogger(UserResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * Retrieves representation of an instance of
     * MyWorldWebService.User.UserResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces("application/json")
    public String getJson(@QueryParam("uuid") String uuid) {
        //TODO return proper representation object
        try {
            user existingUser = new user(uuid);
            existingUser.fetchEntity();
            JSONObject res = existingUser.getUserDetails();
            System.out.println(res);
            return res.toString();
//            new sqlpractice.SQLPractice();
        } catch (userException ex) {
            return ex.getErrorMsg();
        }
    }

    /**
     * PUT method for updating or creating an instance of UserResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Path("/update")
//    @Consumes("application/x-www-form-urlencoded")
    @Consumes("application/json")
    @Produces("application/json")
    public String putJson(String userInfo) throws userException {
        JSONObject obj = (JSONObject) JSONSerializer.toJSON(userInfo);
        if (obj.containsKey("uuid")) {
            user newUser = new user();
            newUser.updateUser(obj);
            return userInfo;
        } else {
            throw new userException("uuid is mandatory as part of updating a user's info");
        }
    }

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public String postJson(String userInfo) throws userException {
        user newUser = new user();
        JSONObject obj = (JSONObject) JSONSerializer.toJSON(userInfo);
        String uuid = newUser.insertUser(obj);
        return uuid;
    }
}
