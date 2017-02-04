package rs.ac.bg.etf.dm130240d.poligon.crtanje_poligona;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.ArrayList;

import rs.ac.bg.etf.dm130240d.poligon.ParameterCord;

public class DrawingView extends ImageView {

	protected DrawingController controller;

	protected Paint startHolePaint, startHolePaint2;
	protected Paint trueHolePaint, trueHolePaint2;
	protected Paint falseHolePaint, falseHolePaint2;
	protected Paint zidPaint, zidPaint2;

	protected void DrawViewInit() {
		this.controller = null;

		startHolePaint = new Paint();
		startHolePaint.setColor(Color.rgb(0, 115, 230));

		startHolePaint2 = new Paint();
		startHolePaint2.setColor(Color.rgb(102, 179, 255));


		trueHolePaint = new Paint();
		trueHolePaint.setColor(Color.rgb(0, 102, 0));

		trueHolePaint2 = new Paint();
		trueHolePaint2.setColor(Color.GREEN);

		falseHolePaint = new Paint();
		falseHolePaint.setColor(Color.rgb(153, 0, 0));

		falseHolePaint2 = new Paint();
		falseHolePaint2.setColor(Color.RED);

		zidPaint = new Paint();
		zidPaint.setColor(Color.rgb(102, 102, 153));

		zidPaint2 = new Paint();
		zidPaint2.setColor(Color.rgb(51, 51, 77));
	}

	public DrawingView(Context context) {
		super(context);
		DrawViewInit();
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		DrawViewInit();
	}

	public DrawingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		DrawViewInit();
	}

	public void setController(DrawingController controller) {
		this.controller = controller;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if(controller.getStartHole().x != 0 && controller.getStartHole().y != 0) {
			canvas.drawCircle(controller.getStartHole().x, controller.getStartHole().y, 90, startHolePaint);
			canvas.drawCircle(controller.getStartHole().x, controller.getStartHole().y, 75, startHolePaint2);
		}

		if(controller.getTrueHole().x != 0 && controller.getTrueHole().y != 0) {
			canvas.drawCircle(controller.getTrueHole().x, controller.getTrueHole().y, 90, trueHolePaint);
			canvas.drawCircle(controller.getTrueHole().x, controller.getTrueHole().y, 75, trueHolePaint2);
		}

		if(controller.getFalseHoles().size() > 0)
			for(ParameterCord pc: controller.getFalseHoles()) {
				canvas.drawCircle(pc.x, pc.y, 90, falseHolePaint);
				canvas.drawCircle(pc.x, pc.y, 75, falseHolePaint2);
			}

		if(controller.getStartWall().size() > 0){
			ArrayList<ParameterCord> startWall = controller.getStartWall();
			ArrayList<ParameterCord> endWall = controller.getEndWall();
			for(int i = 0; i < startWall.size(); i++){

				float left = startWall.get(i).x > endWall.get(i).x ? endWall.get(i).x : startWall.get(i).x;
				float top = startWall.get(i).y > endWall.get(i).y ? endWall.get(i).y : startWall.get(i).y;
				float right = startWall.get(i).x > endWall.get(i).x ? startWall.get(i).x : endWall.get(i).x;
				float bottom = startWall.get(i).y > endWall.get(i).y ? startWall.get(i).y : endWall.get(i).y;

				canvas.drawRect(left, top, right, bottom, zidPaint2);
				canvas.drawRect(left+15, top+15, right-15, bottom-15, zidPaint);
			}
		}

		super.onDraw(canvas);
	}

}
