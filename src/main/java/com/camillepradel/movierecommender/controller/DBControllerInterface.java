/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camillepradel.movierecommender.controller;

import com.camillepradel.movierecommender.model.Movie;
import com.camillepradel.movierecommender.model.Rating;
import java.util.List;

/**
 *
 * @author pomme
 */
public interface DBControllerInterface {
    public  abstract List<Movie>getMoviesByUser(Integer user_id);

    public abstract List<Rating>getRatinByUser(Integer user_id);

    public abstract void updateMovieRating(Rating rating);
    
    public abstract List<Rating> ProcessRecommendationV1(Integer userId);

    public abstract List<Rating> ProcessRecommendationV2(Integer userId);
    
    public abstract List<Rating> ProcessRecommendationV3(Integer userId);
}
