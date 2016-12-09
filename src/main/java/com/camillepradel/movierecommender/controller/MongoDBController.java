/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camillepradel.movierecommender.controller;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.Movie;
import com.camillepradel.movierecommender.model.Rating;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.bson.Document;

/**
 *
 * @author sidya
 */
public class MongoDBController {
    public  static List<Movie>getMoviesMongoDBByUser(Integer user_id){
            ArrayList<Movie> listMovies= new  ArrayList<Movie>();
            MongoCursor<Document> cursor;
            
            MongoDatabase db = MongoDBConnector.getInstance().getConnexion();
            MongoCollection<Document> movies = db.getCollection("movies");
            if(user_id != null)
            {
                MongoCollection<Document> users = db.getCollection("users");
                BasicDBObject whereQuery = new BasicDBObject();
                whereQuery.put("_id", user_id);
                
                cursor = users.find(whereQuery).iterator();
                Document user;
                try {
                    user = cursor.next();
                } catch(Exception e)
                {
                    return new ArrayList<Movie>();
                }
                ArrayList<Document> user_movies;
                user_movies = (ArrayList) user.get("movies");
                BasicDBObject inQuery = new BasicDBObject();
                List<Integer> list = new ArrayList<Integer>();
                for(Document movie : user_movies)
                    list.add(movie.getInteger("movieid"));

                inQuery.put("_id", new BasicDBObject("$in", list));
                cursor = movies.find(inQuery).iterator();
            }
            else
            {
                cursor = movies.find().iterator();
            }
            
            while (cursor.hasNext()) {
                Document movie = cursor.next();
                String[] genres;
                genres = movie.getString("genres").split("\\|");
                ArrayList<Genre> listGenres = new ArrayList<Genre>();
                for(String genre : genres){
                    listGenres.add(new Genre(1,genre));
                }
                
                listMovies.add(new Movie(movie.getInteger("_id"), movie.getString("title"), listGenres));
            }
            return listMovies;
        }
        
    public static List<Rating>getRatingMongoDBByUser(Integer user_id){
            ArrayList<Rating> listRating= new  ArrayList<Rating>();
            MongoCursor<Document> cursor;
            
            MongoDatabase db = MongoDBConnector.getInstance().getConnexion();
            MongoCollection<Document> users = db.getCollection("users");
            MongoCollection<Document> movies = db.getCollection("movies");
            
            BasicDBObject whereQuery = new BasicDBObject();
            whereQuery.put("_id", user_id);

            cursor = users.find(whereQuery).iterator();
            Document user;
            try {
                user = cursor.next();
            } catch(Exception e)
            {
                return new ArrayList<Rating>();
            }
            
            ArrayList<Document> user_movies = (ArrayList) user.get("movies");
            BasicDBObject inQuery = new BasicDBObject();
            HashMap<Integer,Integer> list = new HashMap<Integer,Integer>();
            for(Document movie : user_movies)
                list.put(movie.getInteger("movieid"),movie.getInteger("rating"));

            inQuery.put("_id", new BasicDBObject("$in", list.keySet()));
            cursor = movies.find(inQuery).iterator();
          
            while (cursor.hasNext()) {
                Document movie = cursor.next();
                String[] genres;
                genres = movie.getString("genres").split("\\|");
                ArrayList<Genre> listGenres = new ArrayList<Genre>();
                for(String genre : genres){
                    listGenres.add(new Genre(1,genre));
                }
                
                listRating.add(new Rating(new Movie(movie.getInteger("_id"), movie.getString("title"), 
                        listGenres),user_id, list.get(movie.getInteger("_id"))));
            }
            return listRating;
        }

    public static void updateMovieRating(Rating rating) {
        ArrayList<Rating> listRating= new  ArrayList<Rating>();
        MongoCursor<Document> cursor;

        MongoDatabase db = MongoDBConnector.getInstance().getConnexion();
        MongoCollection<Document> users = db.getCollection("users");

        BasicDBObject updateQuery = new BasicDBObject();
        BasicDBObject newDocument = new BasicDBObject();
        BasicDBObject deleteDocument = new BasicDBObject();
        
        deleteDocument.append("$pull", new BasicDBObject()
            .append("movies", new BasicDBObject()
                .append("movieid", rating.getMovieId())
            )
        );
        
        newDocument.append("$push", new BasicDBObject()
            .append("movies", new BasicDBObject()
                .append("movieid", rating.getMovieId())
                .append("rating", rating.getScore())
                .append("timestamp", (int) (new Date().getTime()/1000))
            )
        );
        
        BasicDBObject searchQuery = new BasicDBObject()
            .append("_id", rating.getUserId());
        
        FindIterable<Document> movie = users.find(searchQuery);
        

        users.updateOne(searchQuery,deleteDocument);
        users.updateOne(searchQuery, newDocument);
        
    }
}
