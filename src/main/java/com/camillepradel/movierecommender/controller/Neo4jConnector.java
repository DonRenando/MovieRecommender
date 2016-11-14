/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camillepradel.movierecommender.controller;
import java.net.ConnectException;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.Neo4jException;

/**
 *
 * @author renando
 */
public class Neo4jConnector {

    
    private static Neo4jConnector INSTANCE = null;
    private static Session session = null;
    private static Driver driver = null;

    private Neo4jConnector() {

    }  

    public static synchronized Neo4jConnector getInstance()
    {			
            if (INSTANCE == null)
            { 
                INSTANCE = new Neo4jConnector();
                driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "root1234" ) );
                session = driver.session();
            }
            return INSTANCE;
    }

    public Session getConnection() throws ConnectException {
        if (session == null || driver == null){
            Neo4jConnector.getInstance();
        }
        return this.session;
    }

    public boolean close() throws ConnectException {
            boolean etat = false;
            if (session != null) {
                    try {
                        session.close();
                        driver.close();
                        etat = true;
                    } catch (Neo4jException e) {
                        etat = false;
                    }
            } else {
                    throw new ConnectException();

            }

            return etat;
    }

}