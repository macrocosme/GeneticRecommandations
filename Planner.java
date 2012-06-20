/*
 * Created by Dany Vohl and Kim Poirier-Champagne
 * Copyright 2012
 * 
 * Melting pot of methods to make a recommandation to a user
 * based on his ratings for a product and the ratings of other users.
 * 
 * This was part of a research project in Advanced Concepts 
 * for Intelligent Systems. I haven't had time to clean it up.
 * 
 * Two techniques are used: Pearson Correlation (commented sections) and 
 * a mix of Pearson Correlation and genetic algorithms.
 * 
 * This could be upgraded so we simply use the genetic algorithms without
 * Pearson. 
 * 
 */

package planner;

import java.util.*; 
import java.io.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

import java.util.Collections;
import java.util.Comparator;
// ça a trouvé et sauvegardé pearson en 
//13433904 millisecondes.
// soit environ 3h45min.

//  CONNEXION À LA BD
// *************************
public class Planner {

    //  CORPS DE L'APPLICATION
    // *************************
    public static void main(String[] args) throws SQLException {
        try {
            long StartTime;
            long EndTime;
            long temps;

            user_rating u1;
            user_rating u2;
            user_rating u3;

            Planner s = new Planner();
            StartTime = new Date().getTime();


//            for (int i = 2; i <= 135259; i++) {
//                
//                u1 = s.evaluationsCommunes(1, i);
//                u2 = s.evaluationsCommunes(i, 1);
//            
//                
//                if (!u1.getVs().isEmpty()) {
//                    //Si l'utisateur est des ratings en commun, calculer Pearson
//                    float pearson = s.Pearson(u1.getIdUser(), u2.getIdUser(), u1.getVs(), u2.getVs());
//                    float pearson2point5 = (float) (pearson * Math.pow(Math.abs(pearson), 1.5));
//                    
//                    //Sauve dans DB les valeurs de pearsons calculés
//                    s.save(u1, u2, pearson, pearson2point5);
//                }
//            }

            // 1000 est le nombre d'usager à comparer avec notre usager
            //float result = s.moyennePondere(1, 1000, 133);

            //System.out.println("Résultat: " + result);

            //Algo génétique
            //Evalue par rapport a 
            int id1 = 1;

            //Creer une population contenant 100 individues aléatoirement choisis dans la base
            //de données mais qui ont voté pour 133
            Population population = s.GetTirageSQL(1, 1000, 133);

            //Mettre en ordre décroissant de pearson cette population
            s.Sort(population);

            Population populationPondere = new Population();
            
            //Génération 1
            for (int i = 1; i < population.size(); i += 2) {
                //Choix de 2 parents
                int id2 = population.getId(i - 1);
                int id3 = population.getId(i);

                
                
                // Trouve les votes communs entre u1, u2 et u3 - A refaire sans BD
                u1 = s.evaluationsCommunesGenetiques(id2, id1, id3);
                u2 = s.evaluationsCommunesGenetiques(id1, id2, id3);
                u3 = s.evaluationsCommunesGenetiques(id1, id3, id2);

                //trouver le vote pour 133
                int ratingUser1 = s.findRating(u2, 133);
                int ratingUser2 = s.findRating(u3, 133);
                    
                System.out.println(i-1);
                System.out.println(i);
                
                //Enregister les enfants dans la population
                if (i==1){
                    Vector id = new Vector();
                    id.add(u2.getIdUser());
                    
                    Vector rated = new Vector();
                    rated.add(133);
                    
                    Vector rating = new Vector();
                    rating.add(ratingUser1);
                    
                    Vector pearson = new Vector();
                    pearson.add(population.getPearson(i-1));
                    
                    Vector pearson2p5 = new Vector();
                    pearson2p5.add(population.getPearson2p5(i-1));
                    
                    populationPondere = new Population(id, rated, rating, pearson, pearson2p5, u2.getVs());
                    populationPondere.add(u3.getIdUser(), 133, ratingUser2, population.getPearson(i), population.getPearson2p5(i), u3.getVs());
                } else {
                    populationPondere.add(u2.getIdUser(), 133, ratingUser1, population.getPearson(i-1), population.getPearson2p5(i-1), u2.getVs());
                    populationPondere.add(u3.getIdUser(), 133, ratingUser2, population.getPearson(i), population.getPearson2p5(i), u3.getVs());
                }
                
                if (u1.size() > 0)
                {
                    //Croisement des résultats de deux usagers
                    Vector v = s.Croisement(u2, u3);

                    //Va chercher enfant1 et enfant2.
                    user_rating enfant1 = (user_rating) v.get(0);
                    user_rating enfant2 = (user_rating) v.get(1);

                    //Ajout de mutation dans les résultats des 2 enfants
                    double rand = Math.random();
                    
                    if (rand < 0.4) {
                        s.Mutation(enfant1, enfant2);
                    }

                    //Calcul v_barre
                    enfant1 = s.VBarreEnfant(enfant1);
                    enfant2 = s.VBarreEnfant(enfant2);

                    //Calcul Pearson pour enfant1 et enfant2
                    float pearsonEnfant1 = s.Pearson(u1.getIdUser(), enfant1.getIdUser(), u1.getVs(), enfant1.getVs());
                    float pearsonEnfant12p5 = (float) (pearsonEnfant1 * Math.pow(Math.abs(pearsonEnfant1), 1.5));
                    float pearsonEnfant2 = s.Pearson(u1.getIdUser(), enfant2.getIdUser(), u1.getVs(), enfant2.getVs());
                    float pearsonEnfant22p5 = (float) (pearsonEnfant2 * Math.pow(Math.abs(pearsonEnfant2), 1.5));;

                    
                    //trouver le vote pour 133
                    ratingUser1 = s.findRating(enfant1, 133);
                    ratingUser2 = s.findRating(enfant2, 133);
                    
                    //Enregister les enfants dans la population
                    populationPondere.add(22222 + i, 133, ratingUser1, pearsonEnfant1, pearsonEnfant12p5, enfant1.getVs());
                    populationPondere.add(22222 + i + 1, 133, ratingUser2, pearsonEnfant2, pearsonEnfant22p5, enfant2.getVs());
                }

            }  //Fin de la génération

            //Calcule la moyenne pondérée - a refaire sans BD
            float result = s.moyennePondereGenetique(populationPondere);

            System.out.println("Résultat avec génétique: " + result);

            EndTime = new Date().getTime();
            temps = EndTime - StartTime;
            System.out.println("Temps d'exécution: " + temps);

        }
        catch(SQLException e){
            System.out.println("Failed");
            e.printStackTrace();
        }

    }
    
    //  CONNEXION À LA BD
    // *************************

    private Statement statement;
    
    public Planner() throws SQLException{
        makeStatement();
    }
    public Statement makeStatement() throws SQLException{
        
        Connect c = new Connect();
        Connection conn = c.makeConnection();
        statement = conn.createStatement();
        
        return statement;
    }

    //  FONCTIONS STATISTIQUE
    // *************************    
    private boolean save(user_rating u1, user_rating u2, float pearson, float pearson2point5) {
        try {
            statement.execute(("INSERT INTO pearson VALUES(" + u1.getIdUser()
                    + "," + u2.getIdUser() + ", " + pearson + ", "
                    + pearson2point5 + ")"));
            System.out.println("user2: " + u2.getIdUser() + " : " + pearson);
            return true;
        } catch (SQLException ex) {
            System.out.println("non sauvegardé: " + ex.getMessage());
            return false;
        }
    }

    public user_rating evaluationsCommunes(int idUser1, int idUser2) {
        try {

            ResultSet user =
                    statement.executeQuery(
                    "SELECT r.RatedUserID, r.rating"
                    + " FROM ratings as r"
                    + " WHERE r.idUser=" + idUser1
                    + " AND r.RatedUserID IN"
                    + " (SELECT RatedUserID from ratings as ra"
                    + " WHERE ra.idUser=" + idUser2 + ")"
                    + " ORDER BY r.RatedUserID");

            user.next();
            Vector ratings = new Vector();
            Vector rated = new Vector();

            if (!user.wasNull()) {
                // Mettre en mémoire les résultats
                do {
                    rated.add((Integer) user.getInt(1));
                    ratings.add((Integer) user.getInt(2));
                } while (user.next());
            } else {
                return null;
            }
            user.close();


            //Calcul de v
            
            ResultSet vbarre = statement.executeQuery("SELECT vbarre"
                    + " FROM user_vbarre WHERE idUser=" + idUser1);

            vbarre.next();
            float vb = vbarre.getFloat(1);

            vbarre.close();

            Vector v = new Vector();
            float tmp;
            for (int j = 0; j < ratings.size(); j++) {
                tmp = (Integer) ratings.get(j) - vb;
                v.add(tmp);
            }

            user_rating u = new user_rating(idUser1, rated, ratings, v);
            return u;

        } catch (SQLException ex) {
            System.out.println("Erreur de SQL" + ex.getMessage());
            return null;
        }
    }
    
    public user_rating evaluationsCommunesGenetiques(int idUser1, int idUser2, int idUser3) {
        try {

            ResultSet user =
                    statement.executeQuery(
                    "SELECT r.RatedUserID, r.rating"
                    + " FROM ratings as r"
                    + " WHERE r.idUser=" + idUser2
                    + " AND r.RatedUserID IN"
                    + " (SELECT RatedUserID from ratings as ra"
                    + " WHERE ra.idUser=" + idUser1 + ")"
                    + " AND r.RatedUserID IN "
                    + " (SELECT RatedUserID from ratings as ra"
                    + " WHERE ra.idUser=" + idUser3 + ")"
                    + " ORDER BY r.RatedUserID");

            user.next();
            Vector ratings = new Vector();
            Vector rated = new Vector();

            if (!user.wasNull()) {
                // Mettre en mémoire les résultats
                do {
//                    System.out.println("id: " + idUser2);
//                    System.out.println("Rated: " + (Integer) user.getInt(1));
//                    System.out.println("Rating: " + (Integer) user.getInt(2));
//                    System.out.println();
                    rated.add((Integer) user.getInt(1));
                    ratings.add((Integer) user.getInt(2));
                } while (user.next());
                
            } else {
                return null;
            }
            user.close();


            //Calcul de v
            
            ResultSet vbarre = statement.executeQuery("SELECT vbarre"
                    + " FROM user_vbarre WHERE idUser=" + idUser1);

            vbarre.next();
            float vb = vbarre.getFloat(1);

            vbarre.close();

            Vector v = new Vector();
            float tmp;
            for (int j = 0; j < ratings.size(); j++) {
                tmp = (Integer) ratings.get(j) - vb;
                v.add(tmp);
            }

            user_rating u = new user_rating(idUser2, rated, ratings, v);
            return u;

        } catch (SQLException ex) {
            System.out.println("Erreur de SQL" + ex.getMessage());
            return null;
        }
    }

    public float Pearson(int id1, int id2, Vector v1, Vector v2) {
        float result;
        float sum_sq_x = 0;
        float sum_sq_y = 0;
        float sum_coproduct = 0;

        for (int i = 0; i < v1.size(); i++) {
            sum_sq_x += (Float) v1.get(i) * (Float) v1.get(i);
            sum_sq_y += (Float) v2.get(i) * (Float) v2.get(i);
            sum_coproduct += (Float) v1.get(i) * (Float) v2.get(i);
        }

        result = sum_coproduct / ((float) Math.sqrt(sum_sq_x) * (float) Math.sqrt(sum_sq_y));

        return result;
    }

    public float moyennePondere(int idUser1, int nbusers, int RatedUserIDtbd) {

        ResultSet vbarre;
        float result;
        float vb;
        float numerateur = 0, denominateur = 0;

        try {
            vbarre = statement.executeQuery("SELECT vbarre"
                    + " FROM user_vbarre WHERE idUser=" + idUser1);
            vbarre.next();
            vb = vbarre.getFloat(1);

            vbarre.close();

            // Faire une requête sur les tables pearson et ratings pour tous les 
            // utilisateurs compris dans nbusers

            ResultSet pearsonSQL;
            pearsonSQL = statement.executeQuery(
                    "SELECT p.idUser2, r.RatedUserID, r.rating, "
                    + "p.pearson, p.pearson2point5, u.vbarre "
                    + "FROM ratings_trou as r JOIN pearson as p "
                    + "ON r.idUser = p.idUser2 "
                    + "JOIN user_vbarre as u ON u.idUser = r.idUser "
                    + "WHERE r.RatedUserID = " + RatedUserIDtbd + " "
                    + "AND r.idUser <> " + idUser1 + " "
                    + "AND p.pearson2point5  > 0.9 "
                    + "ORDER BY p.idUser2 "
                    + "LIMIT " + nbusers);

            pearsonSQL.next();

            do {
                numerateur += ((pearsonSQL.getInt(3))
                        * pearsonSQL.getFloat(5));
                denominateur += (Math.abs(vb) + Math.abs(pearsonSQL.getFloat(5)));
            } while (pearsonSQL.next());

            result = vb; //initialisation de la fonction avec v_barre.
            result += numerateur / denominateur;
            System.out.println("numerateur: " + numerateur);
            System.out.println("denominateur: " + denominateur);

            System.out.println("moyenne pondérée: " + result);

            return result;

        } catch (SQLException ex) {
            Logger.getLogger(Planner.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    private float moyennePondereGenetique(Population population) throws SQLException {
        float result;
        float vb;
        float numerateur = 0, denominateur = 0;

        //Aller chercher vb
        ResultSet vbarre = statement.executeQuery("SELECT vbarre"
                + " FROM user_vbarre WHERE idUser=" + 1);
        vbarre.next();
        vb = vbarre.getFloat(1);

        vbarre.close();

        for (int i = 0; i < population.size(); i++) {
            Vector ratings = population.getRatings();
            int rating = (Integer) ratings.get(i);
            
            if (Math.abs(population.getPearson2p5(i)) >= 0){
                numerateur += population.getPearson2p5(i) * rating ;
                denominateur += (Math.abs(vb) + Math.abs(rating));
//                System.out.println("i: " + i);
//                System.out.println("rating: " + rating);
//                System.out.println("numerateur: " + numerateur);
//                System.out.println("denominateur: " + denominateur);
//                System.out.println("population.getPearson(i): " + population.getPearson(i));
                System.out.println();
            }
        }

        result = vb; //initialisation de la fonction avec v_barre.
        result += numerateur / denominateur;
        
        System.out.println("moyenne pondérée: " + result);

        return result;

    }

    public Population GetTirageSQL(int idUser, int nbIndividus, int IDaEvaluer) throws SQLException {

        //retourne une population random (sauf l'individu qui nous intÃ©resse)
        ResultSet pop = statement.executeQuery(
                "SELECT r.idUser, r.RatedUserID, r.rating, "
                + "p.pearson, p.pearson2point5 "
                + "FROM ia.ratings as r JOIN ia.pearson as p "
                + "ON r.idUser = p.idUser2 "
                + "WHERE r.idUser <>  " + idUser + " "
                + "AND r.idUser IN "
                + "    (SELECT ra.idUser "
                + "    FROM ia.ratings as ra "
                + "    WHERE ra.RatedUserID = " + IDaEvaluer + " ) "
                + "ORDER BY rand() "
                + "LIMIT " + nbIndividus);

        pop.next();
        Vector ids = new Vector();
        Vector rated = new Vector();
        Vector rating = new Vector();
        Vector pearsons = new Vector();
        Vector pearson2p5s = new Vector();

        int i = 0;
        int idTmp = -1;
        boolean first = true;

        // Mettre en mÃ©moire les rÃ©sultats
        do {
            ids.add((Integer) pop.getInt(1));
            rated.add((Integer) pop.getInt(2));
            rating.add((Integer) pop.getInt(3));
            pearsons.add((Float) pop.getFloat(4));
            pearson2p5s.add((Float) pop.getFloat(5));
        } while (pop.next());
        
        pop.close();
   
         //Calcul de v    
        ResultSet vbarre = statement.executeQuery("SELECT vbarre"
                + " FROM user_vbarre WHERE idUser= 1");

        vbarre.next();
        float vb = vbarre.getFloat(1);

        vbarre.close();

        Vector v = new Vector();
        float tmp;
        for (int j = 0; j < rating.size(); j++) {
            tmp = (Integer) rating.get(j) - vb;
            v.add(tmp);
        }
        
        Population population = new Population(ids, rated, rating, pearsons, pearson2p5s, v);
        return population;
    }

    public Population Sort(Population tirage) {

        Comparator comparator = Collections.reverseOrder();
        Collections.sort(tirage.getPearsons(), comparator);

        for (int i = 0; i < tirage.size(); i++) {
        }

        return tirage;
    }

    public Vector Mutation(user_rating u1, user_rating u2) {
        u1.setRatingOfUseri((int) (Math.floor(Math.random() * u1.getRatedUserIDs().size())),
                (int) (Math.floor(Math.random() * 10)));
        u2.setRatingOfUseri((int) (Math.floor(Math.random() * u2.getRatedUserIDs().size())),
                (int) (Math.floor(Math.random() * 10)));

        Vector v = new Vector();
        v.add(u1);
        v.add(u2);
        return v;
    }

    //Croise les données des deux users
    public Vector Croisement(user_rating u1, user_rating u2) {
        // établie la ligne de sélection
        double selection = Math.floor(u1.getRatedUserIDs().size() / 2);
        //System.out.println("Size: " + u1.getRatedUserIDs().size() + " \t Selection: " + selection);
        int tmp;

        for (int i = 0; i < selection; i++) {
//            System.out.println("idUser1: " + u1.getIdUser());
//            System.out.println("idUser2: " + u2.getIdUser());
//            System.out.println(" \t i \t u1 \t u2");
//            System.out.println("AVANT \t" + i + "\t" + u1.getRatings(i) + " \t" + u2.getRatings(i));
            //Swap records
            tmp = u1.getRatings(i);
            u1.setRatingOfUseri(i, u2.getRatings(i));
            u2.setRatingOfUseri(i, tmp);

//            System.out.println("");
//            System.out.println(" \t i \t u1 \t u2");
//            System.out.println("APRÈS \t" + i + "\t" + u1.getRatings(i) + " \t" + u2.getRatings(i));
//            System.out.println("-------");
        }
        Vector v = new Vector();
        v.add(u1);
        v.add(u2);
        return v;
    }

    public int findRating(user_rating user, int id){
        
        for (int i=0; i<user.getRatedUserIDs().size(); i++){
            if (user.getRatedUserID(i)==id)
                return user.getRatings(i);
        }
        
        return 0;
    }
    
    public user_rating VBarreEnfant(user_rating enfant){
        try{
            float tmp = 0;
            
            for (int j = 0; j < enfant.getRatings().size(); j++) {
                tmp += (Integer)enfant.getRatings(j);
            }
            
            
            float vb = tmp/enfant.getRatings().size();

            Vector v = new Vector();
            
            for (int j = 0; j < enfant.getRatings().size(); j++) {
                tmp = (Integer)enfant.getRatings(j) - vb;
                enfant.setVofUseri(j,tmp);
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }
        
        return enfant;
    }
    
    //  CLASSES user_rating       
    static class user_rating {

        protected int idUser;
        protected Vector RatedUserID;
        protected Vector ratings;
        protected Vector v;

        public user_rating(int idUser, Vector RatedUserID, Vector ratings, Vector v) {
            this.idUser = idUser;
            this.RatedUserID = RatedUserID;
            this.ratings = ratings;
            this.v = v;
        }
        
        public int size() {
            return this.RatedUserID.size();
        }

        public int getIdUser() {
            return this.idUser;
        }

        public Vector getRatedUserIDs() {
            return this.RatedUserID;
        }

        public int getRatedUserID(int i) {
            return (Integer) this.RatedUserID.get(i);
        }

        //GET
        public int getRatings(int i) {
            return (Integer) this.ratings.get(i);
        }

        public Vector getRatings() {
            return this.ratings;
        }

        //SET
        public void setRatingOfUseri(int i, int rated) {
            this.ratings.set(i, rated);
        }
        
        public void setVofUseri(int i, float v){
            this.v.set(i, v);
        }

        public Vector getVs() {
            return this.v;
        }

        public float getV(int i) {
            return (Float) this.v.get(i);
        }
    }

    static class Population implements Comparable {

        protected Vector ids;
        protected Vector Rateds;
        protected Vector ratings;
        protected Vector v;
        protected Vector pearson;
        protected Vector pearson2p5;

        public Population(Vector ids, Vector Rateds, Vector ratings,
                Vector pearson, Vector pearson2p5, Vector v) {
            this.ids = ids;
            this.Rateds = Rateds;
            this.ratings = ratings;
            this.pearson = pearson;
            this.pearson2p5 = pearson2p5;
            this.v = v;
        }

        public Population() {
            
        }
        
        public int size() {
            return this.getIds().size();
        }

        public Vector getIds() {
            return this.ids;
        }

        public int getId(int i) {
            return (Integer) this.ids.get(i);
        }

        public Vector getRateds() {
            return this.Rateds;
        }

        public Vector getRatings() {
            return this.ratings;
        }

        public int getRatings2(int i) {
            return (Integer) this.ratings.get(i);
        }

        public float getPearson(int i) {
            return (Float) this.pearson.get(i);
        }

        public Vector getPearsons() {
            return this.pearson;
        }

        public float getPearson2p5(int i) {
            return (Float) this.pearson2p5.get(i);
        }

        public Vector getPearson2p5s() {
            return this.pearson2p5;
        }
        
        public Vector getVs(){
            return this.v;
        }

        @Override
        public int compareTo(Object t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void add(int id, int rated, int rating,
                float pearson, float pearson2p5, Vector v) {
            this.ids.add(id);
            this.Rateds.add(rated);
            this.ratings.add(rating);
            this.pearson.add(pearson);
            this.pearson2p5.add(pearson2p5);
            this.v.add(v);
        }

        public int getpositionVoulue() {
            int i = 0;
            while ((Integer) Rateds.get(i) != 133) {
                i++;
            }

            return i;
        }
    }
}