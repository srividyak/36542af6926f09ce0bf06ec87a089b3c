/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyWorldWebService.User;

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
            return res.toString();
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
        newUser.insertUser(obj);
        return userInfo;
    }
}
