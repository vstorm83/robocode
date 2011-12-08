package ahf;
import robocode.*;
import java.awt.geom.Rectangle2D;
import robocode.util.Utils;

public class Acero extends AdvancedRobot
{
	static double direction;
	
	public void run(){
			direction=1;
			setAdjustRadarForGunTurn(true);
			do{
				turnRadarRightRadians(1);
			}
			while(true);
	}
	public void onScannedRobot(ScannedRobotEvent e){ 
		double enemyAbsoluteBearing;
		double distance;
		double goalDirection =(enemyAbsoluteBearing=e.getBearingRadians()+getHeadingRadians())-(Math.PI/2+((distance=e.getDistance())>=600?0:0.4  ))*direction;
	
		while (!new Rectangle2D.Double(19.0,19.0,762.0,562.0).contains(getX()+Math.sin(goalDirection)*120,getY()+Math.cos(goalDirection)*120)){
			goalDirection = goalDirection+direction*.1;	
			if (Math.random()<.01)
				direction=-direction;
		}
		goalDirection = Utils.normalRelativeAngle(goalDirection-getHeadingRadians());
		setTurnRightRadians(Math.tan(goalDirection));
	    setAhead(100*(Math.abs(goalDirection) > Math.PI/2 ?-1:1));
		if (Math.random()<.04)
			direction=-direction;
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing-getGunHeadingRadians()));
		setFire(2);
		setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) );
    }
}	
      				