package com.avatartalk.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;

import com.avatartalk.app.anim.Pose;
import com.avatartalk.app.anim.PosePlayer;

/**
 * Draws a cartoon humanoid from a Pose using Canvas. Uses forward kinematics
 * for both arms (shoulder -> upper arm -> elbow -> forearm -> wrist -> palm -> fingers).
 */
public class AvatarView extends View implements Choreographer.FrameCallback {

    private final PosePlayer player;
    private Choreographer choreo;
    private boolean attached = false;

    private final Paint pFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pMouth = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pEye = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pBg = new Paint();
    private final Path path = new Path();
    private final RectF tmpRect = new RectF();

    private int colSky1 = 0xFFBDE0FE;
    private int colSky2 = 0xFFEEF4FD;
    private int colSkin = 0xFFF2C9A6;
    private int colSkinShade = 0xFFD9A77D;
    private int colHair = 0xFF2A2630;
    private int colShirt = 0xFF4A6CF7;
    private int colShirtDark = 0xFF2E4CBE;
    private int colPants = 0xFF333C57;
    private int colMouth = 0xFF8B2F3A;
    private int colEye = 0xFF1A1F2E;
    private int colOutline = 0xFF101827;

    public AvatarView(Context c) { this(c, null); }
    public AvatarView(Context c, AttributeSet a) { this(c, a, 0); }
    public AvatarView(Context c, AttributeSet a, int d) {
        super(c, a, d);
        player = new PosePlayer();
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeCap(Paint.Cap.ROUND);
        pStroke.setStrokeJoin(Paint.Join.ROUND);
        pMouth.setColor(colMouth);
        pMouth.setStyle(Paint.Style.FILL);
        pEye.setColor(colEye);
        pEye.setStyle(Paint.Style.FILL);
    }

    public PosePlayer getPlayer() { return player; }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        choreo = Choreographer.getInstance();
        choreo.postFrameCallback(this);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
    }

    @Override public void doFrame(long frameTimeNanos) {
        if (!attached) return;
        invalidate();
        choreo.postFrameCallback(this);
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        pBg.setShader(new LinearGradient(0, 0, 0, h, colSky1, colSky2, Shader.TileMode.CLAMP));
    }

    @Override protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        canvas.drawPaint(pBg);

        Pose pose = player.tick();

        float figureH = Math.min(h * 0.9f, w * 1.6f);
        float unit = figureH / 7.5f;
        float cx = w / 2f;
        float groundY = h * 0.95f;

        drawFigure(canvas, cx, groundY, unit, pose);
    }

    private void drawFigure(Canvas c, float cx, float groundY, float u, Pose pose) {
        float torsoBottomY = groundY - u * 3.4f;
        float torsoTopY    = torsoBottomY - u * 2.2f * (1f + pose.torsoBreathe * 0.04f);
        float neckBottomY  = torsoTopY - u * 0.1f;
        float neckTopY     = neckBottomY - u * 0.35f;
        float headCenterY  = neckTopY - u * 0.55f;
        float headR        = u * 0.75f;

        drawLegs(c, cx, torsoBottomY, groundY, u);
        drawTorso(c, cx, torsoTopY, torsoBottomY, u);

        float shoulderOffsetX = u * 1.05f;
        float shoulderY = torsoTopY + u * 0.15f;
        float lShX = cx - shoulderOffsetX;
        float rShX = cx + shoulderOffsetX;

        drawArm(c, lShX, shoulderY, u, pose, true);
        drawArm(c, rShX, shoulderY, u, pose, false);

        pFill.setColor(colSkin);
        pFill.setStyle(Paint.Style.FILL);
        tmpRect.set(cx - u * 0.3f, neckTopY, cx + u * 0.3f, neckBottomY + u * 0.1f);
        c.drawRect(tmpRect, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStrokeWidth(u * 0.06f);
        c.drawRect(tmpRect, pStroke);

        drawHead(c, cx, headCenterY, headR, u, pose);
    }

    private void drawLegs(Canvas c, float cx, float topY, float groundY, float u) {
        pFill.setColor(colPants);
        pStroke.setColor(colOutline);
        pStroke.setStrokeWidth(u * 0.08f);

        float legW = u * 0.6f;
        float gap = u * 0.08f;

        tmpRect.set(cx - legW - gap, topY, cx - gap, groundY);
        c.drawRoundRect(tmpRect, u * 0.2f, u * 0.2f, pFill);
        c.drawRoundRect(tmpRect, u * 0.2f, u * 0.2f, pStroke);
        tmpRect.set(cx + gap, topY, cx + legW + gap, groundY);
        c.drawRoundRect(tmpRect, u * 0.2f, u * 0.2f, pFill);
        c.drawRoundRect(tmpRect, u * 0.2f, u * 0.2f, pStroke);

        pFill.setColor(colOutline);
        float shoeH = u * 0.22f;
        tmpRect.set(cx - legW - gap - u * 0.15f, groundY - shoeH, cx - gap + u * 0.1f, groundY + u * 0.05f);
        c.drawRoundRect(tmpRect, u * 0.14f, u * 0.14f, pFill);
        tmpRect.set(cx + gap - u * 0.1f, groundY - shoeH, cx + legW + gap + u * 0.15f, groundY + u * 0.05f);
        c.drawRoundRect(tmpRect, u * 0.14f, u * 0.14f, pFill);
    }

    private void drawTorso(Canvas c, float cx, float topY, float bottomY, float u) {
        float topW = u * 2.2f;
        float botW = u * 1.9f;

        path.reset();
        path.moveTo(cx - topW / 2f, topY);
        path.lineTo(cx + topW / 2f, topY);
        path.lineTo(cx + botW / 2f + u * 0.1f, bottomY);
        path.lineTo(cx - botW / 2f - u * 0.1f, bottomY);
        path.close();

        pFill.setColor(colShirt);
        c.drawPath(path, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStrokeWidth(u * 0.09f);
        c.drawPath(path, pStroke);

        path.reset();
        path.moveTo(cx + topW * 0.1f, topY + u * 0.1f);
        path.lineTo(cx + topW / 2f - u * 0.08f, topY + u * 0.1f);
        path.lineTo(cx + botW / 2f, bottomY - u * 0.1f);
        path.lineTo(cx + botW * 0.08f, bottomY - u * 0.1f);
        path.close();
        pFill.setColor(colShirtDark);
        pFill.setAlpha(80);
        c.drawPath(path, pFill);
        pFill.setAlpha(255);

        path.reset();
        path.moveTo(cx - u * 0.35f, topY + u * 0.02f);
        path.lineTo(cx, topY + u * 0.5f);
        path.lineTo(cx + u * 0.35f, topY + u * 0.02f);
        pStroke.setStrokeWidth(u * 0.06f);
        c.drawPath(path, pStroke);
    }

    private void drawArm(Canvas c, float sx, float sy, float u, Pose pose, boolean isLeft) {
        float shPitch, shYaw, elbow, wrPitch, wrRoll;
        float[] fingers;
        float signFlip;

        if (isLeft) {
            shPitch = pose.lShoulderPitch;
            shYaw   = pose.lShoulderYaw;
            elbow   = pose.lElbow;
            wrPitch = pose.lWristPitch;
            wrRoll  = pose.lWristRoll;
            fingers = pose.lFingers;
            signFlip = -1f;
        } else {
            shPitch = pose.rShoulderPitch;
            shYaw   = pose.rShoulderYaw;
            elbow   = pose.rElbow;
            wrPitch = pose.rWristPitch;
            wrRoll  = pose.rWristRoll;
            fingers = pose.rFingers;
            signFlip = 1f;
        }

        float armAngleDeg = 180f - shPitch;
        if (isLeft) armAngleDeg = 180f + shPitch;

        float yawRad = (float) Math.toRadians(shYaw);
        float yawPushX = signFlip * (float) Math.sin(yawRad) * u * 0.25f;
        float yawPushY = -(1f - (float) Math.cos(yawRad)) * u * 0.1f;

        float sxAdj = sx + yawPushX;
        float syAdj = sy + yawPushY;

        float armAngleRad = (float) Math.toRadians(armAngleDeg);
        float upperLen = u * 1.35f;
        float ex = sxAdj + (float) Math.sin(armAngleRad) * upperLen;
        float ey = syAdj + (float) Math.cos(armAngleRad) * upperLen;

        float forearmAngleDeg = armAngleDeg - signFlip * elbow;
        float forearmAngleRad = (float) Math.toRadians(forearmAngleDeg);
        float forearmLen = u * 1.2f;
        float wx = ex + (float) Math.sin(forearmAngleRad) * forearmLen;
        float wy = ey + (float) Math.cos(forearmAngleRad) * forearmLen;

        float handAngleDeg = forearmAngleDeg - signFlip * wrPitch;

        pStroke.setColor(colOutline);
        pStroke.setStrokeWidth(u * 0.55f);
        c.drawLine(sxAdj, syAdj, ex, ey, pStroke);
        pStroke.setStrokeWidth(u * 0.42f);
        pStroke.setColor(colShirt);
        c.drawLine(sxAdj, syAdj, ex, ey, pStroke);

        pStroke.setStrokeWidth(u * 0.42f);
        pStroke.setColor(colOutline);
        c.drawLine(ex, ey, wx, wy, pStroke);
        pStroke.setStrokeWidth(u * 0.3f);
        pStroke.setColor(colSkin);
        c.drawLine(ex, ey, wx, wy, pStroke);

        pFill.setColor(colShirt);
        c.drawCircle(ex, ey, u * 0.22f, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStrokeWidth(u * 0.06f);
        pStroke.setStyle(Paint.Style.STROKE);
        c.drawCircle(ex, ey, u * 0.22f, pStroke);

        pFill.setColor(colShirtDark);
        c.drawCircle(sxAdj, syAdj, u * 0.3f, pFill);
        c.drawCircle(sxAdj, syAdj, u * 0.3f, pStroke);

        drawHand(c, wx, wy, handAngleDeg, wrRoll, u, fingers, signFlip);
    }

    private void drawHand(Canvas c, float wx, float wy, float handAngleDeg, float wristRoll,
                          float u, float[] fingers, float signFlip) {
        float handAngleRad = (float) Math.toRadians(handAngleDeg);
        float palmLen = u * 0.55f;
        float palmCx = wx + (float) Math.sin(handAngleRad) * palmLen * 0.5f;
        float palmCy = wy + (float) Math.cos(handAngleRad) * palmLen * 0.5f;

        pFill.setColor(colSkin);
        c.drawCircle(palmCx, palmCy, u * 0.35f, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeWidth(u * 0.055f);
        c.drawCircle(palmCx, palmCy, u * 0.35f, pStroke);

        float fingerBaseDist = u * 0.42f;
        float fbX = wx + (float) Math.sin(handAngleRad) * fingerBaseDist;
        float fbY = wy + (float) Math.cos(handAngleRad) * fingerBaseDist;

        float latX = (float) Math.cos(handAngleRad);
        float latY = -(float) Math.sin(handAngleRad);
        latX *= signFlip; latY *= signFlip;

        float[] lateralOffsets = { 0.22f, 0.07f, -0.08f, -0.22f };
        float[] lengths        = { 0.55f, 0.62f, 0.60f, 0.50f };

        for (int i = 0; i < 4; i++) {
            float lx = fbX + latX * u * lateralOffsets[i];
            float ly = fbY + latY * u * lateralOffsets[i];
            int base = (i + 1) * 3;
            drawFinger(c, lx, ly, handAngleDeg,
                    fingers[base], fingers[base + 1], fingers[base + 2],
                    u * lengths[i], signFlip);
        }

        float thumbBaseX = palmCx + latX * u * 0.32f;
        float thumbBaseY = palmCy + latY * u * 0.32f;
        float thumbBaseAngle = handAngleDeg - signFlip * 60f;
        drawFinger(c, thumbBaseX, thumbBaseY, thumbBaseAngle,
                fingers[0], fingers[1], fingers[2],
                u * 0.45f, signFlip);
    }

    private void drawFinger(Canvas c, float bx, float by, float baseAngleDeg,
                            float a0, float a1, float a2, float segLen, float signFlip) {
        float ang = baseAngleDeg - signFlip * a0;
        float r = (float) Math.toRadians(ang);
        float x1 = bx + (float) Math.sin(r) * segLen;
        float y1 = by + (float) Math.cos(r) * segLen;

        float ang2 = ang - signFlip * a1;
        float r2 = (float) Math.toRadians(ang2);
        float seg2 = segLen * 0.78f;
        float x2 = x1 + (float) Math.sin(r2) * seg2;
        float y2 = y1 + (float) Math.cos(r2) * seg2;

        float ang3 = ang2 - signFlip * a2;
        float r3 = (float) Math.toRadians(ang3);
        float seg3 = segLen * 0.62f;
        float x3 = x2 + (float) Math.sin(r3) * seg3;
        float y3 = y2 + (float) Math.cos(r3) * seg3;

        float thick = segLen * 0.32f;
        pStroke.setStrokeCap(Paint.Cap.ROUND);
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setColor(colOutline);
        pStroke.setStrokeWidth(thick + segLen * 0.08f);
        c.drawLine(bx, by, x1, y1, pStroke);
        c.drawLine(x1, y1, x2, y2, pStroke);
        c.drawLine(x2, y2, x3, y3, pStroke);
        pStroke.setColor(colSkin);
        pStroke.setStrokeWidth(thick);
        c.drawLine(bx, by, x1, y1, pStroke);
        c.drawLine(x1, y1, x2, y2, pStroke);
        c.drawLine(x2, y2, x3, y3, pStroke);

        pFill.setColor(colSkinShade);
        c.drawCircle(x3, y3, thick * 0.3f, pFill);
    }

    private void drawHead(Canvas c, float cx, float cy, float r, float u, Pose pose) {
        c.save();
        c.rotate(pose.headRoll + pose.headYaw * 0.3f, cx, cy);

        pFill.setColor(colHair);
        pFill.setStyle(Paint.Style.FILL);
        c.drawCircle(cx, cy - r * 0.15f, r * 1.08f, pFill);

        pFill.setColor(colSkin);
        c.drawCircle(cx, cy, r, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeWidth(u * 0.06f);
        c.drawCircle(cx, cy, r, pStroke);

        pFill.setColor(colHair);
        path.reset();
        tmpRect.set(cx - r, cy - r * 1.1f, cx + r, cy + r * 0.1f);
        path.addArc(tmpRect, 190f, 160f);
        path.lineTo(cx + r * 0.9f, cy - r * 0.1f);
        path.quadTo(cx, cy - r * 0.55f, cx - r * 0.9f, cy - r * 0.1f);
        path.close();
        c.drawPath(path, pFill);

        pFill.setColor(colSkin);
        c.drawCircle(cx - r * 1.0f, cy + r * 0.1f, r * 0.18f, pFill);
        c.drawCircle(cx + r * 1.0f, cy + r * 0.1f, r * 0.18f, pFill);
        c.drawCircle(cx - r * 1.0f, cy + r * 0.1f, r * 0.18f, pStroke);
        c.drawCircle(cx + r * 1.0f, cy + r * 0.1f, r * 0.18f, pStroke);

        float browOffsetY = cy - r * 0.35f;
        pStroke.setColor(colHair);
        pStroke.setStrokeWidth(u * 0.14f);
        drawBrow(c, cx - r * 0.4f, browOffsetY - pose.browL * r * 0.1f, r * 0.3f, pose.browL);
        drawBrow(c, cx + r * 0.4f, browOffsetY - pose.browR * r * 0.1f, r * 0.3f, pose.browR);

        float eyeY = cy - r * 0.1f;
        drawEye(c, cx - r * 0.4f, eyeY, r * 0.16f, pose.eyeLidL, u);
        drawEye(c, cx + r * 0.4f, eyeY, r * 0.16f, pose.eyeLidR, u);

        pStroke.setColor(colSkinShade);
        pStroke.setStrokeWidth(u * 0.1f);
        c.drawLine(cx - r * 0.08f, cy + r * 0.15f, cx + r * 0.08f, cy + r * 0.2f, pStroke);

        float mouthY = cy + r * 0.45f;
        drawMouth(c, cx, mouthY, r, pose.mouthOpen, pose.mouthWide, u);

        c.restore();
    }

    private void drawBrow(Canvas c, float cx, float cy, float w, float raise) {
        pStroke.setStrokeCap(Paint.Cap.ROUND);
        path.reset();
        float lift = raise * 4f;
        path.moveTo(cx - w / 2f, cy + Math.max(0, -lift));
        path.quadTo(cx, cy - 6f - lift, cx + w / 2f, cy + Math.max(0, lift));
        c.drawPath(path, pStroke);
    }

    private void drawEye(Canvas c, float cx, float cy, float r, float lid, float u) {
        float visible = 1f - lid;
        if (visible <= 0.02f) {
            pStroke.setColor(colOutline);
            pStroke.setStrokeWidth(u * 0.1f);
            pStroke.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(cx - r, cy, cx + r, cy, pStroke);
            return;
        }
        pFill.setColor(Color.WHITE);
        tmpRect.set(cx - r, cy - r * visible, cx + r, cy + r * visible);
        c.drawOval(tmpRect, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeWidth(u * 0.055f);
        c.drawOval(tmpRect, pStroke);
        pFill.setColor(colEye);
        c.drawCircle(cx, cy, Math.max(r * visible * 0.55f, 1f), pFill);
        pFill.setColor(Color.WHITE);
        c.drawCircle(cx + r * 0.2f, cy - r * 0.2f, r * 0.18f, pFill);
    }

    private void drawMouth(Canvas c, float cx, float cy, float r, float open, float wide, float u) {
        if (open < 0) open = 0; else if (open > 1) open = 1;
        float mouthW = r * (0.55f + wide * 0.25f);
        float mouthH = r * (0.07f + open * 0.55f);

        tmpRect.set(cx - mouthW / 2f, cy - mouthH / 2f, cx + mouthW / 2f, cy + mouthH / 2f);
        pFill.setColor(colMouth);
        c.drawOval(tmpRect, pFill);
        pStroke.setColor(colOutline);
        pStroke.setStyle(Paint.Style.STROKE);
        pStroke.setStrokeWidth(u * 0.07f);
        c.drawOval(tmpRect, pStroke);

        if (open > 0.2f) {
            pFill.setColor(0x66FFFFFF);
            tmpRect.set(cx - mouthW * 0.35f, cy + mouthH * 0.15f, cx + mouthW * 0.35f, cy + mouthH * 0.4f);
            c.drawOval(tmpRect, pFill);
        }
    }
}
