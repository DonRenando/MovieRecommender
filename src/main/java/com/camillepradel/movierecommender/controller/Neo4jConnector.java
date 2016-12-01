/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camillepradel.movierecommender.controller;
import java.net.ConnectException;
import org.neo4j.driver.v1.*;

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

            }
            return INSTANCE;
    }

    public Session getConnection() throws ConnectException {
                this.driver = GraphDatabase.driver( "bolt://localhost", AuthTokens.basic( "neo4j", "root1234" ) );
                this.session = driver.session();
        return this.session;
    }

    public void close()  {
                        this.session.close();
                        this.driver.close();
    }

}