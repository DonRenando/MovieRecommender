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
public class Neo4JController implements DBControllerInterface {

    public List<Movie> getMoviesByUser(Integer userId) {
        List<Movie> movies = new LinkedList<Movie>();
        int id = 0;
        StatementResult result = null;
        try {
            if (userId != null && userId >= 0) {

                result = Neo4jConnector.getInstance().getConnection().run("MATCH (me:User { id: " + userId + " })-[:RATED]->(movie:Movie) -[:CATEGORIZED_AS]->(g:Genre) RETURN movie.title as movies,  collect(g.name) as genre;");
            } else {
                result = Neo4jConnector.getInstance().getConnection().run("MATCH (m:Movie) -[:CATEGORIZED_AS]->(g:Genre) RETURN m.title as movies, collect(g.name) as genre;");
            }
        } catch (ConnectException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Neo4jConnector.getInstance().close();
        }
        while (result.hasNext()) {
            Record record = result.next();
            String[] listGenre = record.get("genre").toString().split(",");
            List<Genre> listTypeGenre = new LinkedList<Genre>();
            for (String g : listGenre) {
                if (g != null) {
                    listTypeGenre.add(new Genre(id, g.replace("\"", "").replace("[", "").replace("]", "")));
                }

            }
            movies.add(new Movie(id, record.get("movies").asString(), (listTypeGenre.isEmpty()) ? null : listTypeGenre));
            id += 1;
        }
        return movies;
    }

    public List<Rating> getRatinByUser(Integer userId) {
        List<Rating> moviesRating = new LinkedList<Rating>();

        StatementResult result = null;
        try {
            if (userId != null && userId >= 0) {

                result = Neo4jConnector.getInstance().getConnection().run("MATCH (u:User{id:" + userId + "})-[r:RATED]->(m:Movie)-[:CATEGORIZED_AS]->(g:Genre) RETURN m.id AS id, m.title AS title, collect(g.name) AS genre, r.note AS note ORDER BY id");
            } else {
                return null;
            }
        } catch (ConnectException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Neo4jConnector.getInstance().close();
        }
        Movie movie = null;
        Rating rate = null;

        while (result.hasNext()) {
            Record record = result.next();
            System.out.println(record.get("p").toString());
            String[] listGenre = record.get("genre").toString().split(",");
            List<Genre> listTypeGenre = new LinkedList<Genre>();
            for (String g : listGenre) {
                if (g != null) {
                    listTypeGenre.add(new Genre(0, g.replace("\"", "").replace("[", "").replace("]", "")));
                }

            }
            movie = new Movie(record.get("id").asInt(), record.get("title").asString(), listTypeGenre);
            rate = new Rating(movie, userId, record.get("note").asInt());
            moviesRating.add(rate);
        }
        return moviesRating;

    }

    public void updateMovieRating(Rating note) {

        Integer user_id = note.getUserId();
        try {
            if (user_id != null && user_id >= 0) {

                Neo4jConnector.getInstance().getConnection().run("MATCH (u:User{id:" + user_id + "})-[r:RATED]->(m:Movie)\n"
                        + "where m.id  =" + note.getMovie().getId() + "\n"
                        + "delete r");
            }

        } catch (ConnectException ex) {
            System.out.println(ex);
        } finally {
            Neo4jConnector.getInstance().close();
        }

        try {
            Neo4jConnector.getInstance().getConnection().run("MATCH (u:User{id:" + user_id + "}),(m:Movie)\n"
                    + "where m.id  = " + note.getMovie().getId() + "\n"
                    + "CREATE (u)-[r:RATED{note:" + note.getScore() + ", timestamp: " + System.currentTimeMillis() + "}]->(m) \n"
                    + "RETURN *");

        } catch (ConnectException ex) {
            System.out.println(ex);
        } finally {
            Neo4jConnector.getInstance().close();
        }
    }

    public List<Rating> ProcessRecommendationV1(Integer userId) {

        List<Rating> ratings = new LinkedList<Rating>();
        StatementResult result = null;

        try {
            result = Neo4jConnector.getInstance().getConnection().run("MATCH (target_user:User {id : " + userId + "})-[:RATED]->(m:Movie)<-[:RATED]-(other_user:User)\n"
                    + "WITH other_user, count(distinct m.title) AS num_common_movies, target_user\n"
                    + "ORDER BY num_common_movies DESC\n"
                    + "LIMIT 1\n"
                    + "MATCH (other_user)-[rat_other_user:RATED]->(m2:Movie)\n"
                    + "WHERE NOT ((target_user)-[:RATED]->(m2))\n"
                    + "RETURN m2.id AS mov_id, m2.title AS rec_movie_title, rat_other_user.note AS rating, other_user.id AS watched_by\n"
                    + "ORDER BY rat_other_user.note DESC");
        } catch (ConnectException ex) {
            Logger.getLogger(Neo4JController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Neo4jConnector.getInstance().close();
        }
        Record record = null;
        int idMovie = 0;
        String titre = "";
        int note = 0;
        while (result.hasNext()) {
            record = result.next();
            idMovie = record.get("mov_id").asInt();
            titre = record.get("rec_movie_title").asString();
            note = record.get("rating").asInt();
            System.out.println(titre);
            ratings.add(new Rating(new Movie(idMovie, titre,null), userId, note));
        }
        return ratings;

    }

    public List<Rating> ProcessRecommendationV2(Integer userId) {

        List<Rating> ratings = new LinkedList<Rating>();
        StatementResult result = null;

        try {

            result = Neo4jConnector.getInstance().getConnection().run("MATCH (target_user:User {id : " + userId + "})-[:RATED]->(m:Movie)<-[:RATED]-(other_user:User)\n"
                    + "WITH other_user, count(distinct m.title) AS num_common_movies, target_user\n"
                    + "ORDER BY num_common_movies DESC\n"
                    + "LIMIT 5\n"
                    + "MATCH (other_user)-[rat_other_user:RATED]->(m2:Movie)\n"
                    + "WHERE NOT ((target_user)-[:RATED]->(m2))\n"
                    + "RETURN m2.id AS mov_id, m2.title AS rec_movie_title, AVG(rat_other_user.note) AS rating, COUNT(other_user.id) AS watched_by\n"
                    + "ORDER BY AVG(rat_other_user.note) DESC, COUNT(other_user.id) DESC");

        } catch (ConnectException ex) {
            Logger.getLogger(Neo4JController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Neo4jConnector.getInstance().close();
        }
        Record record = null;
        int idMovie = 0;
        String titre = "";
        Double noteMoyenneDouble = 0.0;
        Integer noteMoyenneInt = 0;
        while (result.hasNext()) {
            record = result.next();
            idMovie = record.get("mov_id").asInt();
            titre = record.get("rec_movie_title").asString();
            noteMoyenneDouble = record.get("rating").asDouble();
            noteMoyenneInt = noteMoyenneDouble.intValue();

            ratings.add(new Rating(new Movie(idMovie, titre,null), userId, noteMoyenneInt));
        }
        return ratings;

    }

    public List<Rating> ProcessRecommendationV3(Integer userId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
