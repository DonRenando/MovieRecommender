/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camillepradel.movierecommender.controller;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 *
 * @author sidya
 */
public class MongoDBConnector {
    private static MongoDBConnector instance = null;
    private MongoClient c ;
    private MongoDatabase db;
   
    private MongoDBConnector() {
        this.c = new MongoClient("localhost",27017);
        this.db = c.getDatabase("movies");
    }
   
    public static MongoDBConnector getInstance() {
       if(instance == null) {
          instance = new MongoDBConnector();
       }
       return instance;
    }
    
    public MongoDatabase getConnexion() {
        if (this.db == null || this.c == null)
            MongoDBConnector.getInstance();
        return this.db;
    }
    
    public void close() {
        this.c.close();
        this.db = null;
        this.c=null;
    }
}
