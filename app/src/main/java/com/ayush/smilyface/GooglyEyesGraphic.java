/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ayush.smilyface;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.ayush.smilyface.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

/**
 * Graphics class for rendering Googly Eyes on a graphic overlay given the current eye positions.
 */
class GooglyEyesGraphic extends GraphicOverlay.Graphic {
    private static final float EYE_RADIUS_PROPORTION = 0.45f;
    private static final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;

    private Paint mEyeWhitesPaint;
    private Paint mEyeIrisPaint;
    private Paint mEyeOutlinePaint;
    private Paint mEyeLidPaint;
    private Canvas mCanvas;
    private Face mFace;
    // Keep independent physics state for each eye.
    private EyePhysics mLeftPhysics = new EyePhysics();
    private EyePhysics mRightPhysics = new EyePhysics();

    private volatile PointF mLeftPosition;
    private volatile boolean mLeftOpen;

    private volatile PointF mRightPosition;
    private volatile boolean mRightOpen;

    PointF bottomMouthPos, leftCheekPos, leftEarPos,  leftEarTipPos,  leftMouthPos,  nodeBasePos,  rightCheekPos,  rightEarPos,  rightEarTipPos,  rightMouthPos;
    //==============================================================================================
    // Methods
    //==============================================================================================

    GooglyEyesGraphic(GraphicOverlay overlay) {
        super(overlay);

        mEyeWhitesPaint = new Paint();
        mEyeWhitesPaint.setColor(Color.WHITE);
        mEyeWhitesPaint.setStyle(Paint.Style.FILL);

        mEyeLidPaint = new Paint();
        mEyeLidPaint.setColor(Color.YELLOW);
        mEyeLidPaint.setStyle(Paint.Style.FILL);

        mEyeIrisPaint = new Paint();
        mEyeIrisPaint.setColor(Color.BLACK);
        mEyeIrisPaint.setStyle(Paint.Style.FILL);

        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(Color.BLACK);
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(5);
    }

    /**
     * Updates the eye positions and state from the detection of the most recent frame.  Invalidates
     * the relevant portions of the overlay to trigger a redraw.
     */
    void updateEyes(Face face, PointF leftPosition, boolean leftOpen,
                    PointF rightPosition, boolean rightOpen) {
        this.mFace = face;
        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

//        postInvalidate();
    }


    /**
     * Draws the current eye state to the supplied canvas.  This will draw the eyes at the last
     * reported position from the tracker, and the iris positions according to the physics
     * simulations for each iris given motion and other forces.
     */
    @Override
    public void draw(Canvas canvas) {

        PointF rightMouthCord = null, leftMouthCord = null, bottomMouthCord = null, rightCheekCord = null, leftCheekCord = null, nodeBaseCord = null;
        float x = translateX(mFace.getPosition().x + mFace.getWidth() / 2);
        float y = translateY(mFace.getPosition().y + mFace.getHeight() / 2);
        float xOffset = scaleX(mFace.getWidth() / 2.0f);
        float yOffset = scaleY(mFace.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        RectF rect = new RectF(left, top, right, bottom);

        Paint backGroundpaint = new Paint();
        backGroundpaint.setColor(Color.YELLOW);
        backGroundpaint.setStyle(Paint.Style.FILL);

        if(rect.width()>rect.height())
            canvas.drawCircle(rect.centerX(),rect.centerY(),rect.width()/2-10,backGroundpaint);
        else
            canvas.drawCircle(rect.centerX(),rect.centerY(),rect.height()/2-10,backGroundpaint);


        Paint backGroundpaintStroke = new Paint();
        backGroundpaintStroke.setColor(Color.BLACK);
        backGroundpaintStroke.setStyle(Paint.Style.STROKE);
        backGroundpaintStroke.setStrokeWidth(3);
        if(rect.width()>rect.height())
            canvas.drawCircle(rect.centerX(),rect.centerY(),rect.width()/2-7,backGroundpaintStroke);
        else
            canvas.drawCircle(rect.centerX(),rect.centerY(),rect.height()/2-7,backGroundpaintStroke);


        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            return;
        }

        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        mCanvas = canvas;

        if(rightCheekPos != null)
        {
            rightCheekCord =
                    new PointF(translateX(rightCheekPos.x), translateY(rightCheekPos.y));
//            drawCircle(leftCheekCord);
        }

        if(leftCheekPos != null) {
            leftCheekCord =
                    new PointF(translateX(leftCheekPos.x), translateY(leftCheekPos.y));
//            drawCircle(leftCheekCord);
        }
        if(leftEarPos != null) {
            PointF leftEarCord =
                    new PointF(translateX(leftEarPos.x), translateY(leftEarPos.y));
//            drawCircle(leftEarCord);
        }
        if(leftEarTipPos != null) {
            PointF leftEarTipCord =
                    new PointF(translateX(leftEarTipPos.x), translateY(leftEarTipPos.y));
//            drawCircle(leftEarTipCord);
        }
        if(leftMouthPos != null) {
            leftMouthCord =
                    new PointF(translateX(leftMouthPos.x), translateY(leftMouthPos.y));
//            drawCircle(leftMouthCord);
//            Log.d("Ayush leftMouth", ""+leftMouthCord.x+", "+leftMouthCord.y);
        }
        if(nodeBasePos != null) {
            nodeBaseCord =
                    new PointF(translateX(nodeBasePos.x), translateY(nodeBasePos.y));
//            drawCircle(nodeBaseCord);
        }
        if(rightEarPos != null) {
            PointF rightEarCord =
                    new PointF(translateX(rightEarPos.x), translateY(rightEarPos.y));
//            drawCircle(rightEarCord);
        }
        if(rightEarTipPos != null) {
            PointF rightEarTipCord =
                    new PointF(translateX(rightEarTipPos.x), translateY(rightEarTipPos.y));
//            drawCircle(rightEarTipCord);
        }
        if(rightMouthPos != null) {
            rightMouthCord =
                    new PointF(translateX(rightMouthPos.x), translateY(rightMouthPos.y));
//            drawCircle(rightMouthCord);
//            Log.d("Ayush rightMouth", ""+rightMouthCord.x+", "+rightMouthCord.y);
        }
        if(bottomMouthPos != null) {
            bottomMouthCord =
                    new PointF(translateX(bottomMouthPos.x), translateY(bottomMouthPos.y));
//            drawCircle(bottomMouthCord);
//            Log.d("Ayush bottomMouth", ""+bottomMouthCord.x+", "+bottomMouthCord.y);
        }


        // Use the inter-eye distance to set the size of the eyes.
        float distance = (float) Math.sqrt(
                Math.pow(rightPosition.x - leftPosition.x, 2) +
                Math.pow(rightPosition.y - leftPosition.y, 2));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
        float irisRadius = IRIS_RADIUS_PROPORTION * distance;

        // Advance the current left iris position, and draw left eye.
        PointF leftIrisPosition =
                mLeftPhysics.nextIrisPosition(leftPosition, eyeRadius, irisRadius);
        drawEye(canvas, leftPosition, eyeRadius, leftIrisPosition, irisRadius, mLeftOpen);

        // Advance the current right iris position, and draw right eye.
        PointF rightIrisPosition =
                mRightPhysics.nextIrisPosition(rightPosition, eyeRadius, irisRadius);
        drawEye(canvas, rightPosition, eyeRadius, rightIrisPosition, irisRadius, mRightOpen);


        if(leftMouthCord != null && bottomMouthCord != null && rightMouthCord != null) {

            PointF upperLipCord = new PointF((leftMouthCord.x+rightMouthCord.x)/2, (leftMouthCord.y+rightMouthCord.y)/2 - 20);
//            drawCircle(upperLipCord);

            float difference = (bottomMouthCord.y - upperLipCord.y);

            Paint paintMouthLine = new Paint();
            paintMouthLine.setColor(Color.BLACK);
            paintMouthLine.setStyle(Paint.Style.FILL);

            Log.d("my laugh",""+mFace.getIsSmilingProbability());

            float upperLipMeasurementFromCenter =   -rect.centerY() + upperLipCord.y;
            float lowerLipMeasurementFromCenter =   -rect.centerY() + bottomMouthCord.y;
//            if((leftMouthCord.x - rightMouthCord.x) < 50)
            float tempRatio = ((rect.width()/2) / (rightMouthCord.x - leftMouthCord.x));
            Log.d("lips kiss","value: "+leftMouthCord.y+" "+rightMouthCord.y+" "+tempRatio);
//            check laughing face

            if(mFace.getIsSmilingProbability() > .60)
            {
//                Path mPath = new Path();
//                RectF rectf = new RectF();
//                rectf.set(leftMouthCord.x,upperLipCord.y+40, rightMouthCord.x, bottomMouthCord.y);
//                mPath.arcTo(rectf,0, 140, true);
//                canvas.drawPath(mPath, paintMouthLine);

                paintMouthLine.setStyle(Paint.Style.STROKE);
                paintMouthLine.setStrokeWidth(10);
                // Setting the color of the circle
                paintMouthLine.setColor(Color.TRANSPARENT);

                // Draw the circle at (x,y) with radius 250
                int radius = (int)(-leftMouthCord.x + upperLipCord.x);
                Log.d("radius","draw: "+ radius);
                canvas.drawCircle(upperLipCord.x, upperLipCord.y + 40, radius, paintMouthLine);

                paintMouthLine.setColor(Color.TRANSPARENT);
                paintMouthLine.setDither(true);                    // set the dither to true
                paintMouthLine.setStyle(Paint.Style.STROKE);       // set to STOKE
                paintMouthLine.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
                paintMouthLine.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
                paintMouthLine.setPathEffect(new CornerPathEffect(50) );   // set the path effect when they join.
                paintMouthLine.setAntiAlias(true);

                RectF oval = new RectF(upperLipCord.x - radius, upperLipCord.y - radius - 20, upperLipCord.x + radius, upperLipCord.y + radius - 20);
                canvas.drawArc(oval, -90, 90, false, paintMouthLine);
                paintMouthLine.setColor(Color.BLACK);
                canvas.drawArc(oval, 30, 119, false, paintMouthLine);


                oval = new RectF(upperLipCord.x + 30 - radius, upperLipCord.y - radius , upperLipCord.x + radius - 30, upperLipCord.y + radius );
                paintMouthLine.setColor(Color.RED);
                paintMouthLine.setStyle(Paint.Style.FILL);
                canvas.drawArc(oval, 30, 119, false, paintMouthLine);

                oval = new RectF(upperLipCord.x + 40 - radius, upperLipCord.y - radius + 10, upperLipCord.x + radius - 40, upperLipCord.y + radius + 10);
                paintMouthLine.setColor(Color.BLACK);
                paintMouthLine.setStyle(Paint.Style.STROKE);
                canvas.drawArc(oval, 30, 119, false, paintMouthLine);

                if(leftCheekCord != null && rightCheekCord != null)
                {
                    Paint paint = new Paint();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.RED);
                    paint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));
                    mCanvas.drawCircle(leftCheekCord.x - 80, leftCheekCord.y+20, 20, paint);
                    mCanvas.drawCircle(rightCheekCord.x+ 80, rightCheekCord.y+20, 20, paint);
                }
            }
            else if((rect.width()/2)/(lowerLipMeasurementFromCenter -upperLipMeasurementFromCenter) < 2.8)
            {
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.BLACK);
                canvas.drawOval(leftMouthCord.x + 20, upperLipCord.y - 40, rightMouthCord.x - 20, bottomMouthCord.y, paint);
            }
//            else if(mFace.getIsSmilingProbability() == -1.0 && tempRatio < 1.7)
//            {
//                Paint paint = new Paint();
//                paint.setStyle(Paint.Style.FILL);
//                paint.setColor(Color.BLACK);
//                canvas.drawArc(upperLipCord.x, upperLipCord.y, rightMouthCord.x, bottomMouthCord.y, 90, -120, false, paint);
//            }
            else
            {

//                Path mPath = new Path();
//                RectF rectf = new RectF();
//                rectf.set(leftMouthCord.x,upperLipCord.y+40, rightMouthCord.x, bottomMouthCord.y);
//                mPath.arcTo(rectf, 10, 100, true);
//                canvas.drawPath(mPath, paintMouthLine);
//                not laughing

                paintMouthLine.setStyle(Paint.Style.STROKE);
                paintMouthLine.setStrokeWidth(10);
                // Setting the color of the circle
                paintMouthLine.setColor(Color.TRANSPARENT);

                // Draw the circle at (x,y) with radius 250
                int radius = (int)(-leftMouthCord.x + upperLipCord.x);
                Log.d("radius","draw: "+ radius);
                canvas.drawCircle(upperLipCord.x, upperLipCord.y + 40, radius, paintMouthLine);

                paintMouthLine.setColor(Color.TRANSPARENT);
                paintMouthLine.setDither(true);                    // set the dither to true
                paintMouthLine.setStyle(Paint.Style.STROKE);       // set to STOKE
                paintMouthLine.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
                paintMouthLine.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
                paintMouthLine.setPathEffect(new CornerPathEffect(50) );   // set the path effect when they join.
                paintMouthLine.setAntiAlias(true);

                RectF oval = new RectF(upperLipCord.x - radius, upperLipCord.y - radius - 20, upperLipCord.x + radius, upperLipCord.y + radius - 20);
                canvas.drawArc(oval, -90, 90, false, paintMouthLine);
                paintMouthLine.setColor(Color.BLACK);
                canvas.drawArc(oval, 30, 119, false, paintMouthLine);

            }




//            draw a lips curve
//            Rect rect1 = new Rect((int)leftMouthCord.x, (int)leftMouthCord.y + 5, (int)bottomMouthCord.x, (int)bottomMouthCord.y + 5);
//            canvas.drawArc(new RectF(rect1),90 ,120,false, paintMouthLine);

//            draw over differnce
//            if(difference >= 105 && !((leftMouthCord.x - rightMouthCord.x) > 170))
//            canvas.drawOval(leftMouthCord.x, upperLipCord.y, rightMouthCord.x, bottomMouthCord.y,paintMouthLine);
//
//            else {
//                canvas.drawLine(leftMouthCord.x, leftMouthCord.y + 5, bottomMouthCord.x, bottomMouthCord.y + 5, paintMouthLine);
//                canvas.drawLine(leftMouthCord.x, leftMouthCord.y + 3, upperLipCord.x, upperLipCord.y + 3, paintMouthLine);
//                canvas.drawLine(upperLipCord.x, upperLipCord.y + 3, rightMouthCord.x, rightMouthCord.y + 3, paintMouthLine);
//                canvas.drawLine(bottomMouthCord.x, bottomMouthCord.y + 7, rightMouthCord.x, rightMouthCord.y + 7, paintMouthLine);
//            }




        }

    }



    private void drawCircle(PointF bottomMouthCord) {

        mCanvas.drawCircle(bottomMouthCord.x, bottomMouthCord.y, 7, mEyeWhitesPaint);
    }

    /**
     * Draws the eye, either closed or open with the iris in the current position.
     */
    private void drawEye(Canvas canvas, PointF eyePosition, float eyeRadius,
                         PointF irisPosition, float irisRadius, boolean isOpen) {
        if (isOpen) {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeWhitesPaint);
            canvas.drawCircle(irisPosition.x, irisPosition.y, irisRadius, mEyeIrisPaint);
        } else {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeLidPaint);
            float y = eyePosition.y;
            float start = eyePosition.x - eyeRadius;
            float end = eyePosition.x + eyeRadius;
            canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
        }

        canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
        float y = eyePosition.y;
        float start = eyePosition.x - eyeRadius;
        float end = eyePosition.x + eyeRadius;
        canvas.drawLine(start+40, (float) (y- (1.5*eyeRadius)), end-40, (float) (y- (1.5*eyeRadius)), mEyeOutlinePaint);


    }



    public void updateFaceFeatures(PointF bottomMouthPos, PointF leftCheekPos, PointF leftEarPos, PointF leftEarTipPos, PointF leftMouthPos, PointF nodeBasePos, PointF rightCheekPos, PointF rightEarPos, PointF rightEarTipPos, PointF rightMouthPos) {
        this.bottomMouthPos = bottomMouthPos;
        this.leftCheekPos = leftCheekPos;
        this.leftEarPos = leftEarPos;
        this.leftEarTipPos = leftEarTipPos;
        this.leftMouthPos = leftMouthPos;
        this.nodeBasePos = nodeBasePos;
        this.rightCheekPos = rightCheekPos;
        this.rightEarPos = rightEarPos;
        this.rightEarTipPos = rightEarTipPos;
        this.rightMouthPos = rightMouthPos;


        postInvalidate();
    }
}
