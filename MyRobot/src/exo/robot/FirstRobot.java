/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package exo.robot;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 * 
 */
public class FirstRobot extends AdvancedRobot
{
   private double lastEX;

   private double lastEY;

   private double lastEBearing;

   private Rectangle2D.Double battle;
   
   @Override
   public void run()
   {
      double size = Math.max(getWidth(), getHeight()) + 5;
      battle = new Rectangle2D.Double(size, size, getBattleFieldWidth() - size * 2, getBattleFieldHeight() - size * 2);
           
      setAdjustRadarForGunTurn(true);
      setAdjustRadarForRobotTurn(true);
      setAdjustGunForRobotTurn(true);
      turnRadarRight(360);

      while (true)
      {
         if (getRadarTurnRemaining() == 0)
         {
            turnRadarRight(360);
         }
      }
   }

   @Override
   public void onScannedRobot(ScannedRobotEvent event)
   {
      double eBearing = getHeadingRadians() + event.getBearingRadians();
      double nextRadarHeading = getRadarHeadingRadians() - Utils.normalAbsoluteAngle(eBearing);

      double eX = getX() + event.getDistance() * Math.sin(eBearing);
      double eY = getY() + event.getDistance() * Math.cos(eBearing);

      Rectangle2D target = new Rectangle2D.Double(eX - 180, eY - 180, 360, 360);
//      getGraphics().draw(target);
//      getGraphics().draw(battle);

      int lr = nextRadarHeading > 0 ? 1 : -1;
      nextRadarHeading = Math.abs(nextRadarHeading) > Math.PI ? nextRadarHeading - Math.PI * 2 * lr : nextRadarHeading;
      setTurnRadarLeftRadians(nextRadarHeading + 20 * (Math.PI / 180) * lr);
   }

   @Override
   public void onBulletHit(BulletHitEvent event)
   {
      // TODO Auto-generated method stub
      super.onBulletHit(event);
   }

   @Override
   public void onBulletMissed(BulletMissedEvent event)
   {
      // TODO Auto-generated method stub
      super.onBulletMissed(event);
   }

   @Override
   public void onHitByBullet(HitByBulletEvent event)
   {
      // TODO Auto-generated method stub
      super.onHitByBullet(event);
   }

   @Override
   public void onHitRobot(HitRobotEvent event)
   {
      // TODO Auto-generated method stub
      super.onHitRobot(event);
   }

   @Override
   public void onHitWall(HitWallEvent event)
   {
      // TODO Auto-generated method stub
      super.onHitWall(event);
   }

   public Point2D.Double getPoint(Point2D.Double from, double distance, double bearing)
   {
      double eX = from.x + distance * Math.sin(bearing);
      double eY = from.y + distance * Math.cos(bearing);
      return new Point2D.Double(eX, eY);
   }   
}
