package exo.robot;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitRobotEvent;
import robocode.RobotStatus;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

public class FirstHero extends FirstDroid
{
   {
      movRadius = 110;
   }
   
   public static class BearComparator implements Comparator<FirstHero.Bearing>
   {
      public Bearing currBearing;

      public BearComparator setCurrBearing(Bearing currBearing)
      {
         this.currBearing = currBearing;
         return this;
      }

      @Override
      public int compare(Bearing o1, Bearing o2)
      {
         double test = Math.abs(o1.bearing) - Math.abs(o2.bearing);
         if (Math.abs(test) < 10 * Math.PI / 180 && currBearing != null && o1.forward == currBearing.forward
            && o2.forward != o1.forward)
         {
            return -1;
         }
         if (test < 0)
            return -1;
         if (test > 0)
            return 1;
         return 0;
      }
   }
   
   private int hittedCount;
   
   protected void attack(ScannedRobotEvent target)
   {
      boolean cancelFire = false;
      double eDistance = target.getDistance();
      if (eDistance > 150)
         cancelFire = true;
      
      double eBearing = status.getHeadingRadians() + target.getBearingRadians();
      double center = getMinBearing(getGunHeadingRadians(), eBearing);
      setTurnGunRightRadians(center);
      double power = hittedCount > 1 ? 2 : 1.5;
      if (eDistance < 100)
      {         
         power = 3;
      }
      if (!cancelFire) setFire(power);
   }   
   
   @Override
   public void onBulletHit(BulletHitEvent event)
   {
      if (!event.getName().contains("FirstDroid"))
         ++hittedCount;
   }

   @Override
   public void onBulletMissed(BulletMissedEvent event)
   {
      hittedCount = --hittedCount < 0 ? 0 : hittedCount; 
   }

   public static void updateRobotStatus(TeamRobot sender, ScannedRobotEvent robot)
   {
      try
      {
         String[] teamMates = sender.getTeammates();
         if (teamMates != null)
            sender.sendMessage(
               teamMates[0],
               new WrappedEvent(new MiniRobotStatus(sender.getX(), sender.getY(), sender.getEnergy(), sender
                  .getHeadingRadians(), sender.getVelocity(), sender.getName()), robot));
      }
      catch (IOException e)
      {
      }
   }

   public static ScannedRobotEvent getRealEvent(MiniRobotStatus sender, RobotStatus receiver, ScannedRobotEvent robot)
   {
      if (robot == null)
      {
         double distance = Point2D.distance(sender.x, sender.y, receiver.getX(), receiver.getY());
         double eBearing =
            Math.acos((sender.y - receiver.getY()) / distance) * ((sender.x - receiver.getX()) > 0 ? 1 : -1);
         return new ScannedRobotEvent(sender.name, sender.energy,
            getMinBearing(receiver.getHeadingRadians(), eBearing), distance, sender.headingRadians, sender.velocity);

      }
      double tmpBearing = sender.headingRadians + robot.getBearingRadians();
      Double ePoint = getPoint(new Double(sender.x, sender.y), robot.getDistance(), tmpBearing);
      double distance = Point2D.distance(ePoint.x, ePoint.y, receiver.getX(), receiver.getY());
      double eBearing =
         Math.acos((ePoint.getY() - receiver.getY()) / distance) * ((ePoint.getX() - receiver.getX()) > 0 ? 1 : -1);
      return new ScannedRobotEvent(robot.getName(), robot.getEnergy(), getMinBearing(receiver.getHeadingRadians(),
         eBearing), distance, robot.getHeadingRadians(), robot.getVelocity());
   }

   protected void moveRadar(ScannedRobotEvent target)
   {
      double headBearing = status.getHeadingRadians();
      double nextRadarHeading =
         FirstHero.getMinBearing(getRadarHeadingRadians(), headBearing + target.getBearingRadians());
      setTurnRadarRightRadians(nextRadarHeading + 20 * (Math.PI / 180) * (nextRadarHeading > 0 ? 1 : -1));
   }

   public static class WrappedEvent implements Serializable
   {
      ScannedRobotEvent robot;

      MiniRobotStatus sender;

      public WrappedEvent(MiniRobotStatus sender, ScannedRobotEvent robot)
      {
         this.sender = sender;
         this.robot = robot;
      }
   }

   public static class MiniRobotStatus implements Serializable
   {
      double x, y, energy, headingRadians, velocity;

      String name;

      public MiniRobotStatus(double x, double y, double energy, double headingRadians, double velocity, String name)
      {
         this.x = x;
         this.y = y;
         this.energy = energy;
         this.headingRadians = headingRadians;
         this.velocity = velocity;
         this.name = name;
      }
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
   public void onHitRobot(HitRobotEvent event)
   {
      robots.put(event.getName(), new ScannedRobotEvent(event.getName(), event.getEnergy(), event.getBearingRadians(),
         0, 0, 0));
   }

   public static int getBestRadiusForDroid(String name, ScannedRobotEvent target)
   {
      if (name.contains("FirstDroid") && !target.getName().contains("FirstHero"))
         return target.getEnergy() != 999 && target.getDistance() < 120 ? 1 : 140;
      return 140;
   }

   public static ScannedRobotEvent getBestTarget(String name, Map<String, ScannedRobotEvent> robots)
   {
      ScannedRobotEvent target = null;
      double minDistance = 100;
      for (ScannedRobotEvent rb : robots.values())
      {
         if (!rb.getName().equals(name) && rb.getDistance() < minDistance)
         {
            if (!name.contains("FirstHero") || !rb.getName().contains("FirstDroid"))
            {
               target = rb;
               minDistance = rb.getDistance();
            }
         }
      }
      return target;
   }
}
