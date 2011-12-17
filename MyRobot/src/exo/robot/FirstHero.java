package exo.robot;

import java.awt.geom.Point2D;
import java.util.Random;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.StatusEvent;
import robocode.util.Utils;

public class FirstHero extends FirstDroid
{
   
   private void attack(ScannedRobotEvent target)
   {
      if (target.getEnergy() == 0)
      {         
         double eBearing = status.getHeadingRadians() + target.getBearingRadians();
         turnRightRadians(getMinBearing(status.getHeadingRadians(), eBearing));
         ahead(target.getDistance());
      }
   }

   private ScannedRobotEvent selectTarget()
   {
      return robots.values().iterator().next();
   }

   public static class Bearing
   {
      public double bearing;

      public double relBearing;

      public int forward;

      public int type;

      public Bearing(double bearing, double relBearing, int forward, int type)
      {
         this.bearing = bearing;
         this.relBearing = relBearing;
         this.forward = forward;
         this.type = type;
      }

//      public int compareTo(Bearing o)
//      {
//         double test = Math.abs(bearing) - Math.abs(o.bearing);
//         if (Math.abs(test) < 10 * Math.PI / 180 && currentBearing != null && forward == currentBearing.forward
//            && o.forward != forward)
//         {
//            printOut("test", bearing, forward, currentBearing.bearing, currentBearing.forward, o.bearing, o.forward);
//            return -1;
//         }
//         if (test < 0)
//            return -1;
//         if (test > 0)
//            return 1;
//         return 0;
//      }
   }

   private void printOut(Object... params)
   {
      StringBuilder builder = new StringBuilder();
      for (Object obj : params)
      {
         builder.append(obj + "  :  ");
      }
      System.out.println(builder.toString());
   }

   public static double getMinBearing(double from, double to)
   {
      double bearing = Utils.normalAbsoluteAngle(to) - from;
      return Math.abs(bearing) > Math.PI ? bearing - Math.PI * 2 * (bearing > 0 ? 1 : -1) : bearing;
   }

   public static Point2D.Double getPoint(Point2D.Double from, double distance, double bearing)
   {
      double toX = from.x + distance * Math.sin(bearing);
      double toY = from.y + distance * Math.cos(bearing);
      return new Point2D.Double(toX, toY);
   }

   @Override
   public void onHitByBullet(HitByBulletEvent event)
   {
      movRadius = radius_array[new Random().nextInt(3)];
      needStop = 3;
   }

   @Override
   public void onHitRobot(HitRobotEvent event)
   {
      
   }

   @Override
   public void onHitWall(HitWallEvent event)
   {
      revertBearing = null;
   }

   @Override
   public void onStatus(StatusEvent e)
   {
      this.status = e.getStatus();
   }
}
