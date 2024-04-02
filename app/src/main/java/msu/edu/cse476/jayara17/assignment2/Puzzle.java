package msu.edu.cse476.jayara17.assignment2;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.Random;

public class Puzzle {

    /**
     * Paint for filling the area the puzzle is in
     */
    private final Paint fillPaint;
    /**
     * Paint for outlining the area the puzzle is in
     */
    private final Paint outlinePaint;

    /**
     * Percentage of the display width or height that
     * is occupied by the puzzle.
     */
    final static float SCALE_IN_VIEW = 0.9f;

    private final Bitmap puzzleComplete;

    /**
     * The size of the puzzle in pixels
     */
    private int puzzleSize;
    /**
     * How much we scale the puzzle pieces
     */
    private float scaleFactor;
    /**
     * Left margin in pixels
     */
    private int marginX;
    /**
     * Top margin in pixels
     */
    private int marginY;


    /**
     * Collection of puzzle pieces
     */
    public ArrayList<PuzzlePiece> pieces = new ArrayList<PuzzlePiece>();

    /**
     * This variable is set to a piece we are dragging. If
     * we are not dragging, the variable is null.
     */
    private PuzzlePiece dragging = null;

    /**
     * Most recent relative X touch when dragging
     */
    private float lastRelX;
    /**
     * Most recent relative Y touch when dragging
     */
    private float lastRelY;

    /**
     * Random number generator
     */
    private static Random random = new Random();

    private float x = 0;
    private float y = 0;
    private int id;

    // Getter for the ID
    public int getId() {
        return id;
    }

    // Getter for the x position
    public float getX() {
        return x;
    }

    // Getter for the y position
    public float getY() {
        return y;
    }

    /**
     * The name of the bundle keys to save the puzzle
     */
    private final static String LOCATIONS = "Puzzle.locations";
    private final static String IDS = "Puzzle.ids";

    private PuzzleView puzzleView;

    private boolean isSolved = false;


    public Puzzle(Context context, PuzzleView puzzleView) {
        this.puzzleView = puzzleView;
// Create paint for filling the area the puzzle will
// be solved in.
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(0xffcccccc);

// Load the solved puzzle image
        puzzleComplete =
                BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.sparty_done);
// int hit = puzzleComplete.getHeight();
// int wid = puzzleComplete.getWidth();

// Load the puzzle pieces
        pieces.add(new PuzzlePiece(context, R.drawable.sparty1,
                0.16756862f,0.20036057f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty2,
                0.464968f,0.19857007f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty3,
                0.7967844f,0.16471355f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty4,
                0.16760255f,0.53092676f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty5, 0.5f, 0.5f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty6,
                0.83092743f,0.46472052f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty7,
                0.20318213f,0.83241105f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty8,
                0.5341893f,0.7982018f));
        pieces.add(new PuzzlePiece(context, R.drawable.sparty9,
                0.8296169f,0.7940514f));

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setColor(0xFF006400);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(10);

        shuffle();
    }

    public void draw(Canvas canvas) {
        int wid = canvas.getWidth();
        int hit = canvas.getHeight();
// Determine the minimum of the two dimensions
        int minDim = Math.min(wid, hit);
        puzzleSize = (int)(minDim * SCALE_IN_VIEW);
        marginX = (wid - puzzleSize) / 2;
        marginY = (hit - puzzleSize) / 2;

//
// Draw the outline of the puzzle
//
        canvas.drawRect(marginX, marginY, marginX + puzzleSize, marginY +
                puzzleSize, fillPaint);

        scaleFactor = (float)puzzleSize /
                (float)puzzleComplete.getWidth();


        canvas.drawRect(marginX, marginY, marginX + puzzleSize, marginY + puzzleSize, fillPaint);

        canvas.drawRect(marginX, marginY, marginX + puzzleSize, marginY + puzzleSize, outlinePaint);

        if (isSolved) {
            canvas.drawBitmap(puzzleComplete, null, new Rect(marginX, marginY, marginX + puzzleSize, marginY + puzzleSize), null);
        } else {
            for (PuzzlePiece piece : pieces) {
                if (piece != dragging && piece.isSnapped()) {
                    piece.draw(canvas, marginX, marginY, puzzleSize, scaleFactor);
                }
            }
            for (PuzzlePiece piece : pieces) {
                if (piece != dragging && !piece.isSnapped()) {
                    piece.draw(canvas, marginX, marginY, puzzleSize, scaleFactor);
                }
            }
        }

        if (dragging != null) {
            dragging.draw(canvas, marginX, marginY, puzzleSize, scaleFactor);
        }
    }

    /**
     * Handle a touch event from the view.
     * @param view The view that is the source of the touch
     * @param event The motion event describing the touch
     * @return true if the touch is handled.
     */
    public boolean onTouchEvent(View view, MotionEvent event) {
//
// Convert an x,y location to a relative location in the
// puzzle.
//
        float relX = (event.getX() - marginX) / puzzleSize;
        float relY = (event.getY() - marginY) / puzzleSize;

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isSolved = false;
                return onTouched(relX, relY);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                checkIfSolved();
                view.invalidate();
                return onReleased(view, relX, relY);
            case MotionEvent.ACTION_MOVE:
// If we are dragging, just set the new location and force a redraw
                if(dragging != null) {
                    isSolved = false;
                    dragging.move(relX - lastRelX, relY - lastRelY);
                    lastRelX = relX;
                    lastRelY = relY;
                    view.invalidate();
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Handle a touch message. This is when we get an initial touch
     * @param x x location for the touch, relative to the puzzle - 0 to 1 over
    the puzzle
     * @param y y location for the touch, relative to the puzzle - 0 to 1 over
    the puzzle
     * @return true if the touch is handled
     */
    private boolean onTouched(float x, float y) {
// Check each piece to see if it has been hit
// We do this in reverse order so we find the pieces in front
        for(int p=pieces.size()-1; p>=0; p--) {
            if(pieces.get(p).hit(x, y, puzzleSize, scaleFactor)) {
// We hit a piece!
                dragging = pieces.get(p);
                lastRelX = x;
                lastRelY = y;
                return true;
            }
        }
        return false;
    }

    /**
     * Handle a release of a touch message.
     * @param x x location for the touch release, relative to the puzzle - 0 to 1
    over the puzzle
     * @param y y location for the touch release, relative to the puzzle - 0 to 1
    over the puzzle
     * @return true if the touch is handled
     */
    private boolean onReleased(View view, float x, float y) {
        if(dragging != null) {
            if(dragging.maybeSnap()) {
// We have snapped into place
                view.invalidate();

                if(isDone()) {
// The puzzle is done
// The puzzle is done
// Instantiate a dialog box builder
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    ShuffleListener listener = new ShuffleListener();
// Parameterize the builder
                    builder.setTitle(R.string.hurrah);
                    builder.setMessage(R.string.completed_puzzle);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setNegativeButton(R.string.shuffle, new ShuffleListener());
// Create the dialog box and show it
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
            dragging = null;
            return true;
        }
        return false;
    }

    private class ShuffleListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            shuffle();
            puzzleView.invalidate();
        }
    }

    /**
     * Determine if the puzzle is done!
     * @return true if puzzle is done
     */
    public boolean isDone() {
        for(PuzzlePiece piece : pieces) {
            if(!piece.isSnapped()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Shuffle the puzzle pieces
     */
    public void shuffle() {
        isSolved = false;
        for(PuzzlePiece piece : pieces) {
            piece.shuffle(random);
        }
        puzzleView.invalidate();

    }

    /**
     * Save the puzzle to a bundle
     * @param bundle The bundle we save to
     */
    public void saveInstanceState(Bundle bundle) {
        float [] locations = new float[pieces.size() * 2];
        int [] ids = new int[pieces.size()];
        for(int i=0; i<pieces.size(); i++) {
            PuzzlePiece piece = pieces.get(i);
            locations[i*2] = piece.getX();
            locations[i*2+1] = piece.getY();
            ids[i] = piece.getId();
        }

        bundle.putFloatArray(LOCATIONS, locations);
        bundle.putIntArray(IDS, ids);
        bundle.putBoolean("isSolved", isSolved);
    }

    /**
     * Read the puzzle from a bundle
     * @param bundle The bundle we save to
     */
    public void loadInstanceState(Bundle bundle) {
        float [] locations = bundle.getFloatArray(LOCATIONS);
        int [] ids = bundle.getIntArray(IDS);
        for(int i=0; i<ids.length-1; i++) {

            for(int j=i+1; j<ids.length; j++) {
                if(ids[i] == pieces.get(j).getId()) {
                    PuzzlePiece t = pieces.get(i);
                    pieces.set(i, pieces.get(j));
                    pieces.set(j, t);
                }
            }
        }
        for(int i=0; i<pieces.size(); i++) {
            PuzzlePiece piece = pieces.get(i);
            piece.setX(locations[i*2]);
            piece.setY(locations[i*2+1]);
        }
        if (bundle.containsKey("isSolved")) {
            isSolved = bundle.getBoolean("isSolved", false);
        }
    }

    public void checkIfSolved() {
        for (PuzzlePiece piece : pieces) {
            if (!piece.isSnapped()) {
                isSolved = false;
                return;
            }
        }
        isSolved = true;
    }

    public boolean isSolved() {
        return isSolved;
    }
}