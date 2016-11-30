package com.camillepradel.movierecommender.controller;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.camillepradel.movierecommender.model.Genre;
import com.camillepradel.movierecommender.model.Movie;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

@Controller
public class MainController {
	
	@RequestMapping("/hello")
	public ModelAndView showMessage(
			@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
		System.out.println("in controller");
 
		ModelAndView mv = new ModelAndView("helloworld");
		mv.addObject("name", name);
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
        

        
        
	@RequestMapping("/movies")
	public ModelAndView showMovies(
			@RequestParam(value = "user_id", required = false) Integer userId) {
                
		List<Movie> movies = getMoviesNeo4JByUser(userId);

		ModelAndView mv = new ModelAndView("movies");
		mv.addObject("userId", userId);
		mv.addObject("movies", movies);
		return mv;
	}
}
