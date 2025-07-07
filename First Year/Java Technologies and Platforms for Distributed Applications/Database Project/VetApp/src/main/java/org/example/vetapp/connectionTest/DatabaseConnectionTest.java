package org.example.vetapp.connectionTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        testDatabaseConnection("mssql-unit", "Microsoft SQL Server");
        testDatabaseConnection("postgresql-unit", "PostgreSQL");
        testDatabaseConnection("oracle-unit", "Oracle");
    }

    private static void testDatabaseConnection(String persistenceUnitName, String databaseName) {
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory(persistenceUnitName);
            em = emf.createEntityManager();

            System.out.println("The app has successfully connected to the " + databaseName + " database!");
        } catch (Exception e) {
            System.err.println("The app could not connect to the " + databaseName + " database...");
            e.printStackTrace();
        } finally {
            if (em != null) {
                em.close();
            }
            if (emf != null) {
                emf.close();
            }
        }
    }
}
