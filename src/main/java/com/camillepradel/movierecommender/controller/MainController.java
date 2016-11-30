package com.camillepradel.movierecommender.controller;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMethod;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.Movie;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import org.bson.Document;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import com.camillepradel.movierecommender.model.Rating;

@Controller
public class MainController {
	String message = "Welcome to Spring MVC!";
 
	@RequestMapping("/hello")
	public ModelAndView showMessage(
			@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
		System.out.println("in controller");
 
		ModelAndView mv = new ModelAndView("helloworld");
		mv.addObject("message", message);
		mv.addObject("name", name);
		return mv;
	}

	@RequestMapping("/movies")
	public ModelAndView showMovies(
			@RequestParam(value = "user_id", required = false) Integer userId) {
                
		//List<Movie> movies = getMoviesNeo4JByUser(userId);
                List<Movie> movies = getMoviesMongoDBByUser(userId);

		ModelAndView mv = new ModelAndView("movies");
		mv.addObject("userId", userId);
		mv.addObject("movies", movies);
		return mv;
	}
        public List<Movie>getMoviesNeo4JByUser (Integer userId){
            List<Movie> movies = new LinkedList<Movie>();
            String oldMovie=null;
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
        
        
        public List<Movie>getMoviesMongoDBByUser(Integer user_id){
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

	@RequestMapping(value = "/movieratings", method = RequestMethod.GET)
	public ModelAndView showMoviesRattings(
			@RequestParam(value = "user_id", required = true) Integer userId) {
		System.out.println("GET /movieratings for user " + userId);

		// TODO: write query to retrieve all movies from DB
		List<Movie> allMovies = new LinkedList<Movie>();
		Genre genre0 = new Genre(0, "genre0");
		Genre genre1 = new Genre(1, "genre1");
		Genre genre2 = new Genre(2, "genre2");
		allMovies.add(new Movie(0, "Titre 0", Arrays.asList(new Genre[] {genre0, genre1})));
		allMovies.add(new Movie(1, "Titre 1", Arrays.asList(new Genre[] {genre0, genre2})));
		allMovies.add(new Movie(2, "Titre 2", Arrays.asList(new Genre[] {genre1})));
		allMovies.add(new Movie(3, "Titre 3", Arrays.asList(new Genre[] {genre0, genre1, genre2})));

		// TODO: write query to retrieve all ratings from the specified user
		List<Rating> ratings = new LinkedList<Rating>();
		ratings.add(new Rating(new Movie(0, "Titre 0", Arrays.asList(new Genre[] {genre0, genre1})), userId, 3));
		ratings.add(new Rating(new Movie(2, "Titre 2", Arrays.asList(new Genre[] {genre1})), userId, 4));

		ModelAndView mv = new ModelAndView("movieratings");
		mv.addObject("userId", userId);
		mv.addObject("allMovies", allMovies);
		mv.addObject("ratings", ratings);

		return mv;
	}

	@RequestMapping(value = "/movieratings", method = RequestMethod.POST)
	public String saveOrUpdateRating(@ModelAttribute("rating") Rating rating) {
		System.out.println("POST /movieratings for user " + rating.getUserId()
											+ ", movie " + rating.getMovie().getId()
											+ ", score " + rating.getScore());

		// TODO: add query which
		//         - add rating between specified user and movie if it doesn't exist
		//         - update it if it does exist

		return "redirect:/movieratings?user_id=" + rating.getUserId();
	}

	@RequestMapping(value = "/recommendations", method = RequestMethod.GET)
	public ModelAndView ProcessRecommendations(
			@RequestParam(value = "user_id", required = true) Integer userId,
			@RequestParam(value = "processing_mode", required = false, defaultValue = "0") Integer processingMode){
		System.out.println("GET /movieratings for user " + userId);

		// TODO: process recommendations for specified user exploiting other users ratings
		//       use different methods depending on processingMode parameter
		Genre genre0 = new Genre(0, "genre0");
		Genre genre1 = new Genre(1, "genre1");
		Genre genre2 = new Genre(2, "genre2");
		List<Rating> recommendations = new LinkedList<Rating>();
		String titlePrefix;
		if (processingMode.equals(0))
			titlePrefix = "0_";
		else if (processingMode.equals(1))
			titlePrefix = "1_";
		else if (processingMode.equals(2))
			titlePrefix = "2_";
		else
			titlePrefix = "default_";
		recommendations.add(new Rating(new Movie(0, titlePrefix + "Titre 0", Arrays.asList(new Genre[] {genre0, genre1})), userId, 5));
		recommendations.add(new Rating(new Movie(1, titlePrefix + "Titre 1", Arrays.asList(new Genre[] {genre0, genre2})), userId, 5));
		recommendations.add(new Rating(new Movie(2, titlePrefix + "Titre 2", Arrays.asList(new Genre[] {genre1})), userId, 4));
		recommendations.add(new Rating(new Movie(3, titlePrefix + "Titre 3", Arrays.asList(new Genre[] {genre0, genre1, genre2})), userId, 3));

		ModelAndView mv = new ModelAndView("recommendations");
		mv.addObject("recommendations", recommendations);

		return mv;
	}
}
