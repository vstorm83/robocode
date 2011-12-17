package exo.robot;

import java.awt.geom.*;
import java.awt.geom.Point2D.Double;
import java.util.*;

import robocode.*;
import exo.robot.FirstHero.Bearing;

public class FirstDroid extends AdvancedRobot
{
   public RobotStatus status;

   public Rectangle2D.Double battle;

   public double size, maxX, maxY;

   public int movRadius = 130;

   public Bearing currentBearing;

   public Bearing revertBearing;

   public boolean inBattle;

   public Map<String, ScannedRobotEvent> robots = new HashMap<String, ScannedRobotEvent>();

   public int needStop;

   @Override
   public void run()
   {
      size = Math.max(getWidth(), getHeight()) + 20;
      maxX = getBattleFieldWidth() - size;
      maxY = getBattleFieldHeight() - size;
      battle = new Rectangle2D.Double(size, size, getBattleFieldWidth() - size * 2, getBattleFieldHeight() - size * 2);

      setAdjustRadarForGunTurn(true);
      setAdjustRadarForRobotTurn(true);
      setAdjustGunForRobotTurn(true);
      turnRadarRight(360);
      while (true)
      {
         doRun();
         turnRadarRight(360);
      }
   }

   private void doRun()
   {
      ScannedRobotEvent target = selectTarget();
      if (target == null)
         return;
      moveRobot(target);
      attack(target);
      moveRadar(target);
   }

   private void attack(ScannedRobotEvent target)
   {
      if (target.getEnergy() == 0)
      {
         double eBearing = status.getHeadingRadians() + target.getBearingRadians();
         turnRightRadians(FirstHero.getMinBearing(status.getHeadingRadians(), eBearing));
         ahead(target.getDistance());
      }
   }

   private ScannedRobotEvent selectTarget()
   {
      return robots.values().iterator().next();
   }

   private void moveRadar(ScannedRobotEvent target)
   {
      double headBearing = status.getHeadingRadians();
      double nextRadarHeading =
         FirstHero.getMinBearing(getRadarHeadingRadians(), headBearing + target.getBearingRadians());
      setTurnRadarRightRadians(nextRadarHeading + 20 * (Math.PI / 180) * (nextRadarHeading > 0 ? 1 : -1));
   }

   private void moveRobot(ScannedRobotEvent target)
   {
      if (target.getEnergy() == 0)
      {
         stop();
         return;
      }
      double headBearing = status.getHeadingRadians();
      double eDistance = target.getDistance();
      double eBearing = headBearing + target.getBearingRadians();

      Double ePoint = FirstHero.getPoint(new Double(getX(), getY()), eDistance, eBearing);
      Double point = new Double(getX(), getY());

      double sin = movRadius / eDistance;
      double movBearing = eBearing + Math.asin(sin < 1 ? sin : 1);
      double movBearing2 = eBearing - Math.asin(sin < 1 ? sin : 1);

      List<Bearing> bearings = new ArrayList<Bearing>();
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing, movBearing), movBearing, 1, 1));
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing, movBearing2), movBearing2, 1, 2));
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing + Math.PI, movBearing), movBearing, -1, 1));
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing + Math.PI, movBearing2), movBearing2, -1, 2));
      Collections.sort(bearings);

      printOut(bearings.get(0).bearing, bearings.get(0).forward, bearings.get(1).bearing, bearings.get(1).forward);
      if (point.x <= size || point.x >= maxX || point.y <= size || point.y >= maxY)
      {
         if (revertBearing == null || (revertBearing != null && inBattle))
         {
            revertBearing = bearings.get(0);
            inBattle = false;
         }
      }
      if (point.x > size && point.x < maxX && point.y > size && point.y < maxY)
      {
         inBattle = true;
      }
      if (point.x >= size + 30 && point.x <= maxX - 30 && point.y >= size + 30 && point.y <= maxY - 30)
      {
         revertBearing = null;
      }

      if (revertBearing != null && bearings.get(0).type == revertBearing.type
         && bearings.get(0).forward == revertBearing.forward)
      {
         Bearing test = bearings.remove(0);
         printOut("Removed", test.bearing, test.forward, test.type);
      }

      currentBearing = bearings.get(0);
      double movDistance =
         Math.sqrt(Math.pow(eDistance <= movRadius + 5 ? movRadius + 10 : eDistance, 2) - Math.pow(movRadius, 2));
      Double movPoint1 = FirstHero.getPoint(point, movDistance, bearings.get(0).relBearing);
      // Double movPoint2 = getPoint(new Double(getX(), getY()), movDistance,
      // movBearing2);
      movPoint1.x = movPoint1.x <= size ? size : movPoint1.x >= maxX ? maxX : movPoint1.x;
      movPoint1.y = movPoint1.y <= size ? size : movPoint1.y >= maxY ? maxY : movPoint1.y;
      // movPoint2.x = movPoint2.x < size ? size : movPoint2.x > maxX ? maxX :
      // movPoint2.x;
      // movPoint2.y = movPoint2.y < size ? size : movPoint2.y > maxY ? maxY :
      // movPoint2.y;

      // System.out.println(movPoint1 + "  :   " + movPoint2 + " : " + battle);

      movDistance = Point2D.distance(point.x, point.y, movPoint1.x, movPoint1.y) * bearings.get(0).forward;
      setTurnRightRadians(bearings.get(0).bearing);
      setAhead(movDistance);

      Line2D.Double line = new Line2D.Double(point, movPoint1);
      getGraphics().draw(line);

      // Line2D.Double line2 = new Line2D.Double(new Double(getX(), getY()),
      // movPoint2);
      // getGraphics().draw(line2);

      getGraphics().draw(battle);
   }

   @Override
   public void onScannedRobot(ScannedRobotEvent event)
   {
      robots.put(event.getName(), event);
      if ((needStop == 3 && new Random().nextInt(2) == 1) || (needStop < 3 && --needStop > 0))
      {
         stop();
         moveRadar(event);
      }
      else
      {
         needStop = 0;
         doRun();
      }
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

   @Override
   public void onHitByBullet(HitByBulletEvent event)
   {
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
