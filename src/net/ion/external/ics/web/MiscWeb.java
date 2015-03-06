package net.ion.external.ics.web;

import com.google.common.base.Function;
import net.ion.craken.Craken;
import net.ion.craken.node.*;
import net.ion.craken.node.crud.ChildQueryResponse;
import net.ion.craken.tree.PropertyId;
import net.ion.external.ics.common.ExtMediaType;
import net.ion.external.ics.web.misc.PropertyInfo;
import net.ion.external.ics.web.misc.ThreadDumpInfo;
import net.ion.framework.parse.gson.JsonArray;
import net.ion.framework.parse.gson.JsonObject;
import net.ion.framework.parse.gson.JsonParser;
import net.ion.framework.parse.gson.JsonPrimitive;
import net.ion.framework.util.DateUtil;
import net.ion.framework.util.StringUtil;
import net.ion.radon.core.ContextParam;
import org.apache.lucene.queryparser.classic.ParseException;
import org.jboss.resteasy.spi.HttpRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Path("/misc")
public class MiscWeb implements WebApp{


    private ReadSession rsession;
    public MiscWeb(@ContextParam(Craken.EntryName) Craken rentry) throws IOException {
        this.rsession = rentry.login() ;
    }

    @GET
    @Path("/thread")
    public JsonObject listThreadDump() throws IOException{
        return new ThreadDumpInfo().list() ;
    }


    @GET
    @Path("/properties")
    public JsonObject listProperties(){
        return new PropertyInfo().list() ;

    }

//    @GET
//    @Path("/shutdown")
//    public String shutdown(@Context HttpRequest request,
//                           @DefaultValue("") @QueryParam("password") String password,
//                           @DefaultValue("1000") @QueryParam("time") final int time, @ContextParam("net.ion.niss.NissServer") final NissServer server){
//
//        if (! password.equals(server.config().serverConfig().password())) {
//            return "not matched password" ;
//        }
//
//        new Thread(){
//            public void run(){
//                try {
//                    Thread.sleep(time);
//                    server.shutdown() ;
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//
//        return "bye after " + time;
//    }


    @GET
    @Path("/history")
    @Produces(ExtMediaType.APPLICATION_JSON_UTF8)
    public JsonObject logHistory(@DefaultValue("") @QueryParam("searchQuery") String query) throws IOException, ParseException {

        JsonArray jarray = rsession.ghostBy("/events/loaders").childQuery(query).descending("time").offset(1000).find().transformer(new Function<ChildQueryResponse, JsonArray>(){
            @Override
            public JsonArray apply(ChildQueryResponse res) {
                List<ReadNode> nodes = res.toList() ;
                JsonArray his = new JsonArray() ;
                for(ReadNode node : nodes){
                    JsonArray row = new JsonArray() ;
                    row.add(new JsonPrimitive(node.fqn().name()))
                            .add(new JsonPrimitive(DateUtil.timeMilliesToDay(node.property("time").asLong(0))))
                            .add(new JsonPrimitive(node.propertyId(PropertyId.refer("loader")).asString()))
                            .add(new JsonPrimitive(node.property("status").asString())) ;
                    his.add(row) ;
                }
                return his;
            }
        }) ;

        JsonObject result = new JsonObject() ;
        result.add("history", jarray);
        result.put("schemaName", JsonParser.fromString("[{'title':'Id'},{'title':'Time'},{'title':'LoaderId'},{'title':'Status'}]").getAsJsonArray()) ;
        result.put("info", rsession.ghostBy("/menus/loaders").property("history").asString());
        return result ;
    }



    @GET
    @Path("/users")
    @Produces(ExtMediaType.APPLICATION_JSON_UTF8)
    public JsonObject userList() throws IOException, ParseException{

        JsonArray jarray = rsession.ghostBy("/users").children().transform(new Function<Iterator<ReadNode>, JsonArray>(){
            @Override
            public JsonArray apply(Iterator<ReadNode> iter) {
                JsonArray result = new JsonArray() ;
                while(iter.hasNext()){
                    ReadNode node = iter.next() ;
                    JsonArray userProp = new JsonArray() ;
                    userProp.add(new JsonPrimitive(node.fqn().name())) ;
                    userProp.add(new JsonPrimitive(node.property("name").asString())) ;
                    result.add(userProp) ;
                }

                return result;
            }
        }) ;
        return new JsonObject().put("info", rsession.ghostBy("/menus/misc").property("user").asString())
                .put("schemaName", JsonParser.fromString("[{'title':'Id'},{'title':'Name'}]").getAsJsonArray())
                .put("users", jarray) ;
    }

    @POST
    @Path("/users/{uid}")
    public String addUser(@PathParam("uid") final String userId, @FormParam("name") final String name, @FormParam("password") final String password){
        rsession.tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                wsession.pathBy("/users/" + userId)
                        .property("name", name)
                        .property("password", password)
                        .property("registered", System.currentTimeMillis()) ;
                return null ;
            }
        }) ;

        return "registered " + userId ;
    }

    @POST
    @Path("/profile/{uid}")
    public String editUser(@PathParam("uid") final String userId, @Context HttpRequest request){
        final MultivaluedMap<String, String> formParam = request.getDecodedFormParameters() ;

        rsession.tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                WriteNode found = wsession.pathBy("/users/" + userId) ;
                for (String key : formParam.keySet()) {
                    found.property(key, formParam.getFirst(key)) ;
                }
                return null;
            }
        }) ;

        return "edited " + userId ;
    }



    @POST
    @Path("/users_remove")
    public String removeUsers(@FormParam("users") final String users){
        rsession.tran(new TransactionJob<Void>() {
            @Override
            public Void handle(WriteSession wsession) throws Exception {
                String[] targets = StringUtil.split(users, ",") ;
                for (String userId : targets) {
                    wsession.pathBy("/users/" + userId).removeSelf() ;
                }
                return null ;
            }
        });
        return "removed " + users ;
    }



    @DELETE
    @Path("/users/{uid}")
    public String removeUser(@PathParam("uid") final String userId) throws InterruptedException, ExecutionException{
        Boolean removed = rsession.tran(new TransactionJob<Boolean>() {
            @Override
            public Boolean handle(WriteSession wsession) throws Exception {
                return wsession.pathBy("/users/" + userId).removeSelf() ;
            }
        }).get() ;

        return removed ? "removed " + userId : "";
    }


}