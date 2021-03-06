package edu.berkeley.gcweb.servlet;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;

@Path("/gamesman")
public class GamesmanServlet {
    
    @GET @Path("/{game}/getMoveValue")
    @Produces("text/plain")
    public String getMoveValue(@PathParam("game") String game,
                               @MatrixParam("width") int width,
                               @MatrixParam("height") int height,
                               @MatrixParam("position") String position,
                               @Context UriInfo uri) {
        
        String response = null;
        try {
//            Map<String, String> moveValue =
//                Game.getMoveValue(game, width, height, position);
//            JSONObject json = new JSONObject("{status: 'OK'}");
//            json = new JSONObject(moveValue);
//            response = json.toString(4);
        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject("{status: 'error'}");
                json.put("message", e.getMessage());
                response = json.toString(4);
            } catch (JSONException je) {
                response = "{status: 'error', message: 'A JSON error occurred while handling an exception.'}";
            }
        }
        
        return response;
    }
    
    /**
     * Handles requests in the form of:
     *   /gamesman/{game}/getNextMoveValues;width={width};height={height};
     *                                     position={position};
     *                                     opt0={opt0};...;optN={optN}
     * @param game the internal name of the game; for example, "ttt"
     * @param option the number specifying the variant of the game
     * @param hash the hash of the game's current position
     * @return A list JSON-encoded move-values. Each move-value object will
     *         contain the hash of the position that it represents, the
     *         win-loss-tie value, the remoteness, and either the win-by value
     *         or the mex value, depending on the game.
     */
    @GET @Path("/{game}/getNextMoveValues")
    @Produces("text/plain")
    public String getNextMoveValues(@PathParam("game") String game,
                                    @MatrixParam("width") int width,
                                    @MatrixParam("height") int height,
                                    @MatrixParam("position") String position,
                                    @Context UriInfo uri) {
        
        String response = null;
        try {
//            JSONArray json = new JSONArray();
//            Map<String, String>[] nextMoveValues =
//                getNextMoveValues(game, width, height, position);
//            for (Map<String, String> moveValue : nextMoveValues) {
//                JSONObject jsonMoveValue = new JSONObject(moveValue);
//                jsonMoveValue.put("status", "OK");
//                json.put(jsonMoveValue);
//            }
//            response = json.toString(4);
        } catch (Exception e) {
            try {
                JSONObject json = new JSONObject();
                json.put("status", "error");
                json.put("message", e.getMessage());
                response = json.toString(4);
            } catch (JSONException je) {
                response = "{status: 'error', message: 'A JSON error occurred while handling an exception.'}";
            }
        }
        
        return response;
    }
    
    public static MultivaluedMap<String, String> getMatrixParameters(UriInfo uri) {
        MultivaluedMap<String, String> parameters;
        List<PathSegment> segments = uri.getPathSegments();
        if (segments.isEmpty()) {
            parameters = null;
        } else {
            // all path segments should share the same matrix parameters
            PathSegment tailSegment = segments.get(segments.size() - 1);
            parameters = tailSegment.getMatrixParameters();
        }
        return parameters;
    }
}
