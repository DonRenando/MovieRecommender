package com.camillepradel.movierecommender.controller;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestMethod;

import com.camillepradel.movierecommender.model.Movie;
import com.camillepradel.movierecommender.model.Rating;

@Controller
public class MainController {
	String message = "Welcome to Spring MVC!";
 
        //Commenter et Decommenter en fonction de BD que l'on veut utiliser
        //DBControllerInterface db = new MongoDBController();
        DBControllerInterface db = new Neo4JController();
                
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
                
		List<Movie> movies = db.getMoviesByUser(userId);

		ModelAndView mv = new ModelAndView("movies");
		mv.addObject("userId", userId);
		mv.addObject("movies", movies);
		return mv;
	}
        

	@RequestMapping(value = "/movieratings", method = RequestMethod.GET)
	public ModelAndView showMoviesRattings(
			@RequestParam(value = "user_id", required = true) Integer userId) {
		System.out.println("GET /movieratings for user " + userId);

		// write query to retrieve all movies from DB
                List<Movie> allMovies = db.getMoviesByUser(null);

		// write query to retrieve all ratings from the specified user       
                List<Rating> ratings=db.getRatinByUser(userId);
                
		ModelAndView mv = new ModelAndView("movieratings");
		mv.addObject("userId", userId);
		mv.addObject("allMovies", allMovies);
		mv.addObject("ratings", ratings);

		return mv;
	}

	@RequestMapping(value = "/movieratings", method = RequestMethod.POST)
	public String saveOrUpdateRating(@ModelAttribute("rating") Rating rating) {
		System.out.println("POST /movieratings for user " + rating.getUserId()
											+ ", movie " + rating.getMovie().getTitle()
											+ ", score " + rating.getScore());
                db.updateMovieRating(rating);
		return "redirect:/movieratings?user_id=" + rating.getUserId();
	}

	@RequestMapping(value = "/recommendations", method = RequestMethod.GET)
	public ModelAndView ProcessRecommendations(
			@RequestParam(value = "user_id", required = true) Integer userId,
			@RequestParam(value = "processing_mode", required = false, defaultValue = "0") Integer processingMode){
		System.out.println("GET /movieratings for user " + userId);

		List<Rating> recommendations = new LinkedList<Rating>();

                if (processingMode.equals(1)){
                    recommendations = db.ProcessRecommendationV1(userId);
                }
		else if (processingMode.equals(2)){
                    recommendations = db.ProcessRecommendationV2(userId);
                }
                
		ModelAndView mv = new ModelAndView("recommendations");
		mv.addObject("recommendations", recommendations);

		return mv;
	}
}
