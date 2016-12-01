/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camillepradel.movierecommender.controller;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.Movie;
import com.camillepradel.movierecommender.model.Rating;
import java.net.ConnectException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

/**
 *
 * @author renando
 */
public class Neo4JController {
    
            public static List<Movie>getMoviesNeo4JByUser (Integer userId){
            List<Movie> movies = new LinkedList<Movie>();
            int id=0;
            StatementResult result = null;
            try {
            if (userId != null && userId >=0 ){
               
                result = Neo4jConnector.getInstance().getConnection().run( "MATCH (me:User { id: "+userId+" })-[:RATED]->(movie:Movie) -[:CATEGORIZED_AS]->(g:Genre) RETURN movie.title as movies,  collect(g.name) as genre;" );
            }
            else{
                result = Neo4jConnector.getInstance().getConnection().run( "MATCH (m:Movie) -[:CATEGORIZED_AS]->(g:Genre) RETURN m.title as movies, collect(g.name) as genre;" );
            }
            } catch (ConnectException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
             finally{
                  Neo4jConnector.getInstance().close();
             }
            while ( result.hasNext() )
            {
               Record record = result.next();
               String[] listGenre = record.get("genre").toString().split(",");
                List<Genre> listTypeGenre = new LinkedList<Genre>();
                for(String g : listGenre){
                    if(g != null)
                        listTypeGenre.add(new Genre(id, g.replace("\"", "").replace("[", "").replace("]", "")));
                    
                }
               movies.add(new Movie(id, record.get("movies").asString(), (listTypeGenre.isEmpty())?null:listTypeGenre ) );
            id+=1;
            }
            return movies;
        }
            
            
        public static List<Rating>GetMoviesRatingNeo4JByUser(Integer userId){
            List<Rating> moviesRating = new LinkedList<Rating>();

            StatementResult result = null;
            try {
            if (userId != null && userId >=0 ){
               
                result = Neo4jConnector.getInstance().getConnection().run( "MATCH (u:User{id:"+userId+"})-[r:RATED]->(m:Movie)-[:CATEGORIZED_AS]->(g:Genre) RETURN m.id AS id, m.title AS title, collect(g.name) AS genre, r.note AS note ORDER BY id" );
            }
            else{
                return null;
            }
            } catch (ConnectException ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
             finally{
                  Neo4jConnector.getInstance().close();
             }
            Movie movie = null;
            Rating rate = null;
            
            while ( result.hasNext() )
            {
               Record record = result.next();
                System.out.println(record.get("p").toString());
                String[] listGenre = record.get("genre").toString().split(",");
                List<Genre> listTypeGenre = new LinkedList<Genre>();
                for(String g : listGenre){
                    if(g != null)
                        listTypeGenre.add(new Genre(0, g.replace("\"", "").replace("[", "").replace("]", "")));
                    
                }
                movie = new Movie(record.get("id").asInt(), record.get("title").asString(),listTypeGenre );
                rate = new Rating(movie, userId, record.get("note").asInt());
                moviesRating.add(rate);
            }
            return moviesRating;
             
        }
        
        public static void updateMovieRatingNeo4J(Rating note){

            Integer user_id = note.getUserId();
            try {
            if (user_id != null && user_id >=0 ){
               
               Neo4jConnector.getInstance().getConnection().run( "MATCH (u:User{id:"+user_id+"})-[r:RATED]->(m:Movie)\n" +
                    "where m.id  ="+note.getMovie().getId()+"\n" +
                    "delete r" );
            }

            } catch (ConnectException ex) {
                    System.out.println(ex);
            }
             finally{
                  Neo4jConnector.getInstance().close();
             } 
            
            
              try {
                 Neo4jConnector.getInstance().getConnection().run( "MATCH (u:User{id:"+user_id+"}),(m:Movie)\n" +
                    "where m.id  = "+note.getMovie().getId()+"\n" +
                    "CREATE (u)-[r:RATED{note:"+note.getScore()+", timestamp: "+System.currentTimeMillis()+"}]->(m) \n" +
                    "RETURN *" );

            } catch (ConnectException ex) {
                   System.out.println(ex);
            }
             finally{
                  Neo4jConnector.getInstance().close();
             } 
        }
    
}
