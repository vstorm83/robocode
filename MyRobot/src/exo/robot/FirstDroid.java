package exo.robot;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.*;

import robocode.*;
import exo.robot.FirstHero.Bearing;
import exo.robot.FirstHero.WrappedEvent;

public class FirstDroid extends TeamRobot
{
   public RobotStatus status;

   public Rectangle2D.Double battle;

   public double size, maxX, maxY, revertingType;

   public int movRadius = 140, needStop;

   public Bearing currentBearing;

   public Map<String, ScannedRobotEvent> robots = new HashMap<String, ScannedRobotEvent>();

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
      FirstHero.updateRobotStatus(this, null);
      ScannedRobotEvent target = selectTarget();
      if (target == null)
         return;
      moveRadar(target);
      moveRobot(target);
      attack(target);
   }

   protected void attack(ScannedRobotEvent target)
   {
      if (target.getEnergy() == 0)
      {
         double eBearing = status.getHeadingRadians() + target.getBearingRadians();
         turnRightRadians(FirstHero.getMinBearing(status.getHeadingRadians(), eBearing));
         ahead(target.getDistance());
      }
   }

   protected ScannedRobotEvent selectTarget()
   {
      for (ScannedRobotEvent robot : robots.values())
      {
         if (!isTeammate(robot.getName()))
         {
            return robot;
         }
      }
      return null;
   }

   protected void moveRadar(ScannedRobotEvent target)
   {
      setTurnRadarRight(360);
   }

   private int revertTime;
   
   private void moveRobot(ScannedRobotEvent target)
   {
      ScannedRobotEvent tmp = FirstHero.getBestTarget(getName(), robots);
      target = tmp != null ? tmp : target;
      movRadius = FirstHero.getBestRadiusForDroid(getName(), target);
     
      double headBearing = status.getHeadingRadians();
      double eDistance = target.getDistance();
      double eBearing = headBearing + target.getBearingRadians();

      Double point = new Double(getX(), getY());

      double sin = movRadius / eDistance;
      double movBearing = eBearing + Math.asin(sin < 1 ? sin : 1);
      double movBearing2 = eBearing - Math.asin(sin < 1 ? sin : 1);

      List<Bearing> bearings = new ArrayList<Bearing>();
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing, movBearing), movBearing, 1, 1));
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing, movBearing2), movBearing2, 1, 2));
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing + Math.PI, movBearing), movBearing, -1, 1));
      bearings.add(new Bearing(FirstHero.getMinBearing(headBearing + Math.PI, movBearing2), movBearing2, -1, 2));
      Collections.sort(bearings, new FirstHero.BearComparator().setCurrBearing(currentBearing));

      if (point.x <= size || point.x >= maxX || point.y <= size || point.y >= maxY)
      {
         if (revertingType == 0 && currentBearing != null) 
         {
            revertingType = currentBearing.type;
            revertTime = 6;
         }
      }
      if (revertTime-- == 0)
      {
         revertingType = 0;         
      }

      boolean done = true;
      double movDistance;
      Double movPoint1;
      do
      {
         currentBearing = bearings.remove(0);
         movDistance =
            Math.sqrt(Math.pow(eDistance <= movRadius + 5 ? movRadius + 10 : eDistance, 2) - Math.pow(movRadius, 2));
         movPoint1 = FirstHero.getPoint(point, movDistance, currentBearing.relBearing);
         done = !(currentBearing.type == revertingType);
         movPoint1.x = movPoint1.x <= size ? size : movPoint1.x >= maxX ? maxX : movPoint1.x;
         movPoint1.y = movPoint1.y <= size ? size : movPoint1.y >= maxY ? maxY : movPoint1.y;

         movDistance = Point2D.distance(point.x, point.y, movPoint1.x, movPoint1.y) * currentBearing.forward;
      }
      while (revertingType != 0 && !done);
      setTurnRightRadians(currentBearing.bearing);
      setAhead(movDistance);

      Line2D.Double line = new Line2D.Double(point, movPoint1);
      getGraphics().draw(line);
      getGraphics().draw(battle);
   }

   @Override
   public void onScannedRobot(ScannedRobotEvent event)
   {
      robots.put(event.getName(), event);
      FirstHero.updateRobotStatus(this, event);

      if ((needStop == 3 && new Random().nextInt(3) == 1) || (needStop < 3 && --needStop > 0))
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

   @Override
   public void onMessageReceived(MessageEvent event)
   {
      WrappedEvent msg = (WrappedEvent)event.getMessage();
      ScannedRobotEvent robot = FirstHero.getRealEvent(msg.sender, status, msg.robot);
      robots.put(robot.getName(), robot);
   }

   @Override
   public void onHitByBullet(HitByBulletEvent event)
   {
      needStop = 3;
      if (currentBearing != null && revertingType == 0)
      {
         revertingType = currentBearing.type;
         revertTime = 10;
      }      
   }

   @Override
   public void onRobotDeath(RobotDeathEvent event)
   {
      robots.remove(event.getName());
   }

   @Override
   public void onHitWall(HitWallEvent event)
   {
      revertingType = currentBearing.type;
   }

   @Override
   public void onStatus(StatusEvent e)
   {
      this.status = e.getStatus();
   }
}
