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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bson.Document;

/**
 *
 * @author sidya
 */
public class MongoDBController implements DBControllerInterface {
    public List<Movie>getMoviesByUser(Integer user_id){
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
        
    public List<Rating>getRatinByUser(Integer user_id){
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

    public void updateMovieRating(Rating rating) {
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

    public List<Rating> ProcessRecommendationV1(Integer user_id) {
        MongoDatabase db = MongoDBConnector.getInstance().getConnexion();
        List<Rating> ratings_target_user = this.getRatinByUser(user_id);
        
        MongoCursor<Document> cursor = db.getCollection("users").find().iterator();
        List<Rating> best_user_ratings = new ArrayList<Rating>();
        while (cursor.hasNext()) {
                Document user = cursor.next();
                if(Objects.equals(user.getInteger("_id"), user_id))
                    continue;
                List<Rating> ratings_other_user = this.getRatinByUser(user.getInteger("_id"));
                Integer ratings_intersection = 0;
                for(Rating ro : ratings_other_user)
                    for(Rating rt : ratings_target_user)
                        if( ro.getMovieId() == rt.getMovieId())
                        {
                            ratings_intersection += 1;
                            break;
                        }
               
                if (ratings_intersection > best_user_ratings.size())
                    best_user_ratings = new ArrayList<Rating>(ratings_other_user);
                ratings_other_user = null;
                System.gc();
        }
        
        List<Rating> return_ratings = new ArrayList<Rating>(best_user_ratings);
        for(Rating rb : best_user_ratings)
            for(Rating rt : ratings_target_user)
                if( rb.getMovieId() == rt.getMovieId())
                {
                    return_ratings.remove(rb);
                    break;
                }
        Collections.sort(return_ratings, Rating.CompareScoreDesc);
        return return_ratings;
    }

    public List<Rating> ProcessRecommendationV2(Integer user_id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public List<Rating> ProcessRecommendationV3(Integer user_id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
