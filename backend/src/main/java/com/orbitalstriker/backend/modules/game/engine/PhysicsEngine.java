package com.orbitalstriker.backend.modules.game.engine;

import com.orbitalstriker.backend.modules.game.model.Disc;
import com.orbitalstriker.backend.modules.game.model.GameState;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PhysicsEngine {

    private static final double MAX_VELOCITY = 50.0;

    // kapu
    public static final double FIELD_WIDTH = 800.0;
    public static final double FIELD_HEIGHT = 600.0;
    public static final double GOAL_TOP_Y = 250.0;
    public static final double GOAL_BOTTOM_Y = 350.0;
    public static final double GOAL_DEPTH = 40.0; // Milyen mély a háló
    public static final double POST_RADIUS = 8.0; // Kapufa vastagsága

    // Kapufák pozíciói
    private final double[][] posts = {
            {0.0, GOAL_TOP_Y},      // Bal felső
            {0.0, GOAL_BOTTOM_Y},   // Bal alsó
            {FIELD_WIDTH, GOAL_TOP_Y},    // Jobb felső
            {FIELD_WIDTH, GOAL_BOTTOM_Y}  // Jobb alsó
    };

    public void update(GameState state) {
        List<Disc> discs = state.getDiscs();

        // 1. MOZGÁS ÉS FALAK
        for (Disc disc : discs) {
            
            if (disc.getVelocity().length() > MAX_VELOCITY) {
                disc.setVelocity(disc.getVelocity().normalize().multiply(MAX_VELOCITY));
            }

            
            disc.setPosition(disc.getPosition().add(disc.getVelocity()));
            
          
            disc.setVelocity(disc.getVelocity().multiply(disc.getDamping()));

           
            if (disc.getVelocity().length() < 0.15) {
                disc.setVelocity(new Vector2D(0, 0));
            }

           
            checkPostCollisions(disc);

         
            checkWallCollision(disc, FIELD_WIDTH, FIELD_HEIGHT);
        }

       
        for (int i = 0; i < discs.size(); i++) {
            for (int j = i + 1; j < discs.size(); j++) {
                checkDiscCollision(discs.get(i), discs.get(j));
            }
        }
    }

  
    private void checkPostCollisions(Disc disc) {
        for (double[] post : posts) {
            double postX = post[0];
            double postY = post[1];

            double dx = disc.getPosition().getX() - postX;
            double dy = disc.getPosition().getY() - postY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double minDist = disc.getRadius() + POST_RADIUS;

          
            if (distance < minDist) {
               
                double nx = dx / distance;
                double ny = dy / distance;

          
                double overlap = minDist - distance;
                disc.getPosition().setX(disc.getPosition().getX() + nx * overlap);
                disc.getPosition().setY(disc.getPosition().getY() + ny * overlap);

      
                double vx = disc.getVelocity().getX();
                double vy = disc.getVelocity().getY();
                double dotProduct = vx * nx + vy * ny;

       
                disc.getVelocity().setX((vx - 2 * dotProduct * nx) * 0.7);
                disc.getVelocity().setY((vy - 2 * dotProduct * ny) * 0.7);
            }
        }
    }

   
    private void checkWallCollision(Disc disc, double width, double height) {
        double r = disc.getRadius();
        double x = disc.getPosition().getX();
        double y = disc.getPosition().getY();
        double vx = disc.getVelocity().getX();
        double vy = disc.getVelocity().getY();
        double bounce = 0.7;

      
        if (y + r > height) {
            disc.getPosition().setY(height - r);
            disc.getVelocity().setY(vy * -1 * bounce);
        } else if (y - r < 0) {
            disc.getPosition().setY(r);
            disc.getVelocity().setY(vy * -1 * bounce);
        }

      
        if (x - r < 0) { boolean inGoalMouth = (y > GOAL_TOP_Y && y < GOAL_BOTTOM_Y);

            if (inGoalMouth) {
            
                if (x - r < -GOAL_DEPTH) {
                    disc.getPosition().setX(-GOAL_DEPTH + r);
                    disc.getVelocity().setX(vx * -1 * 0.5); 
                }
              
                if (y - r < GOAL_TOP_Y + 5) { // Felső háló
                    disc.getPosition().setY(GOAL_TOP_Y + 5 + r);
                    disc.getVelocity().setY(Math.abs(vy) * 0.5);
                }
                if (y + r > GOAL_BOTTOM_Y - 5) { // Alsó háló
                    disc.getPosition().setY(GOAL_BOTTOM_Y - 5 - r);
                    disc.getVelocity().setY(-Math.abs(vy) * 0.5);
                }
            } else {
        
                disc.getPosition().setX(r);
                disc.getVelocity().setX(vx * -1 * bounce);
            }
        }

       
        else if (x + r > width) {
            boolean inGoalMouth = (y > GOAL_TOP_Y && y < GOAL_BOTTOM_Y);

            if (inGoalMouth) {
        
                if (x + r > width + GOAL_DEPTH) {
                    disc.getPosition().setX(width + GOAL_DEPTH - r);
                    disc.getVelocity().setX(vx * -1 * 0.5);
                }
               
                if (y - r < GOAL_TOP_Y + 5) {
                    disc.getPosition().setY(GOAL_TOP_Y + 5 + r);
                    disc.getVelocity().setY(Math.abs(vy) * 0.5);
                }
                if (y + r > GOAL_BOTTOM_Y - 5) {
                    disc.getPosition().setY(GOAL_BOTTOM_Y - 5 - r);
                    disc.getVelocity().setY(-Math.abs(vy) * 0.5);
                }
            } else {
             
                disc.getPosition().setX(width - r);
                disc.getVelocity().setX(vx * -1 * bounce);
            }
        }
    }

    private void checkDiscCollision(Disc d1, Disc d2) {
        double dx = d2.getPosition().getX() - d1.getPosition().getX();
        double dy = d2.getPosition().getY() - d1.getPosition().getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double minDistance = d1.getRadius() + d2.getRadius();

        if (distance < minDistance && distance > 0) {
            double overlap = minDistance - distance;
            double offsetX = (dx / distance) * (overlap / 2.0);
            double offsetY = (dy / distance) * (overlap / 2.0);

            d1.getPosition().setX(d1.getPosition().getX() - offsetX);
            d1.getPosition().setY(d1.getPosition().getY() - offsetY);
            d2.getPosition().setX(d2.getPosition().getX() + offsetX);
            d2.getPosition().setY(d2.getPosition().getY() + offsetY);

            Vector2D normal = new Vector2D(dx / distance, dy / distance);
            Vector2D tangent = new Vector2D(-normal.getY(), normal.getX());

            double dpTan1 = d1.getVelocity().getX() * tangent.getX() + d1.getVelocity().getY() * tangent.getY();
            double dpTan2 = d2.getVelocity().getX() * tangent.getX() + d2.getVelocity().getY() * tangent.getY();
            double dpNorm1 = d1.getVelocity().getX() * normal.getX() + d1.getVelocity().getY() * normal.getY();
            double dpNorm2 = d2.getVelocity().getX() * normal.getX() + d2.getVelocity().getY() * normal.getY();

            double m1 = d1.getMass();
            double m2 = d2.getMass();
            double restitution = 0.9;

            double mom1 = (dpNorm1 * (m1 - m2) + 2.0 * m2 * dpNorm2) / (m1 + m2) * restitution;
            double mom2 = (dpNorm2 * (m2 - m1) + 2.0 * m1 * dpNorm1) / (m1 + m2) * restitution;

            d1.setVelocity(tangent.multiply(dpTan1).add(normal.multiply(mom1)));
            d2.setVelocity(tangent.multiply(dpTan2).add(normal.multiply(mom2)));
        }
    }
}