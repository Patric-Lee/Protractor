package hk.ust.protractor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.*;

public class MainActivity extends AppCompatActivity {
    private Button addGestures, recognizeGestures;
    private ImageView gestureArea;
    private Canvas canvas;
    private Paint paint;
    private Bitmap baseBitmap;
    private Vector<Points> path = new Vector();
    private Vector<Template> tem = new Vector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);

        gestureArea = (ImageView) findViewById(R.id.imageView);
        addGestures = (Button) findViewById(R.id.addGestures);
        recognizeGestures = (Button) findViewById(R.id.recognizeGestures);

        gestureArea.setOnTouchListener(touch);
        addGestures.setOnClickListener(clickAdd);
        recognizeGestures.setOnClickListener(clickRecognize);
    }
    private View.OnClickListener clickAdd = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Points[] tmp = new Points[path.size()];
            path.toArray(tmp);
            android.util.Log.e("Points", Double.toString(tmp[0].getX()));
            Template newTem = Recognizer.addGesture(tmp, 64, false, tem.size(), "");
            tem.addElement(newTem);
            path.clear();
            resumeCanvas();
        }
    };
    private View.OnClickListener clickRecognize = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Points[] tmp = new Points[path.size()];
            path.toArray(tmp);

            Template[] tmpTemplates = new Template[tem.size()];
            tem.toArray(tmpTemplates);
            Pair p = Recognizer.recognizeGesture(tmp, 64, false, tmpTemplates);
            path.clear();
            for(int i = 0; i < p.getTemplate().getVector().length; ++i)
                Log.d("Recognize", Double.toString(p.getTemplate().getVector()[i]));
            Toast.makeText(getApplicationContext(), Integer.toString(p.getTemplate().getIndex()),
                    Toast.LENGTH_SHORT).show();
            resumeCanvas();
        }
    };
    private View.OnTouchListener touch = new OnTouchListener() {
        // 定义手指开始触摸的坐标
        float startX;
        float startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    // 第一次绘图初始化内存图片，指定背景为白色
                    if (baseBitmap == null) {
                        baseBitmap = Bitmap.createBitmap(gestureArea.getWidth(),
                                gestureArea.getHeight(), Bitmap.Config.ARGB_8888);
                        canvas = new Canvas(baseBitmap);
                        canvas.drawColor(Color.WHITE);
                    }
                    // 记录开始触摸的点的坐标
                    startX = event.getX();
                    startY = event.getY();
                    path.addElement(new Points(startX, startY));
                    break;
                // 用户手指在屏幕上移动的动作
                case MotionEvent.ACTION_MOVE:
                    // 记录移动位置的点的坐标
                    float stopX = event.getX();
                    float stopY = event.getY();

                    //根据两点坐标，绘制连线
                    canvas.drawLine(startX, startY, stopX, stopY, paint);

                    // 更新开始点的位置
                    startX = event.getX();
                    startY = event.getY();
                    path.addElement(new Points(stopX, stopY));
                    // 把图片展示到ImageView中
                    gestureArea.setImageBitmap(baseBitmap);
                    break;
                case MotionEvent.ACTION_UP:

                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected void resumeCanvas() {
        // 手动清除画板的绘图，重新创建一个画板
        if (baseBitmap != null) {
            baseBitmap = Bitmap.createBitmap(gestureArea.getWidth(),
                    gestureArea.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(baseBitmap);
            canvas.drawColor(Color.WHITE);
            gestureArea.setImageBitmap(baseBitmap);
            Toast.makeText(MainActivity.this, "清除画板成功，可以重新开始绘图", 0).show();
        }
    }
}
