package com.example;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {
            runCLI(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Todo: Starting point for your code
    }

    private void runCLI(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Username: ");
            String username = scanner.nextLine();

            System.out.println("Password: ");
            String password = scanner.nextLine();

            if(isValidLogin(connection, username, password)) {
                break;
            }

            System.out.println("Invalid username or password");
            System.out.printf("0) Exit ");
            String options = scanner.nextLine();
            if ("0".equals(options)) {
                return;
            }
        }

        while (true) {
            prinMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    listMoonMissions(connection);
                    break;
                case "2":
                    break;
                case "3":
                    break;
                case "4":
                    break;
                case "5":
                    break;
                case "6":
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void listMoonMissions(Connection connection) throws SQLException {
        String sql = "SELECT spacecraft FROM moon_mission ORDER BY spacecraft";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("spacecraft");
                System.out.println(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list moon missions", e);
        }
    };

    private boolean isValidLogin(Connection connection, String username, String password) {
        String sql = "SELECT COUNT(*) FROM account WHERE name = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == 1;
                }
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Login query failed", e);
        }

        return false;
    }

    private void prinMenu() {
        System.out.println("1) List moon missions");
        System.out.println("2) Get a moon mission by mission_id");
        System.out.println("3) Count missions for a given year");
        System.out.println("4) Create an account");
        System.out.println("5) Update an account password");
        System.out.println("6) Delete an account");
        System.out.println("0) Exit");
    }


    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
