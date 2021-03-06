package com.example.administrator.faceiden0418;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {private final static int GET_PIC_FROM_PHONE = 1;
    final private static String TAG = "MainActivity";
    final private int PICTURE_CHOOSE = 1;

    private ImageView imageView = null;
    private Bitmap img = null;
    private Button buttonDetect = null;
    private TextView textView = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) this.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                if (!isPermitPermission()) {
                    return;
                }
                //get a picture form your phone
                getPictureFromPhone();
            }
        });

        textView = (TextView) this.findViewById(R.id.textView1);

        buttonDetect = (Button) this.findViewById(R.id.button2);
        buttonDetect.setVisibility(View.INVISIBLE);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                textView.setText("请稍等 ...");

                FaceppDetect faceppDetect = new FaceppDetect();
                faceppDetect.setDetectCallback(new DetectCallback() {

                    public void detectResult(JSONObject rst) {
                        //Log.v(TAG, rst.toString());

                        //use the red paint
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight()) / 100f);

                        //create a new canvas
                        Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(img, new Matrix(), null);


                        try {
                            //find out all faces
                            final int count = rst.getJSONArray("face").length();
                            for (int i = 0; i < count; ++i) {
                                float x, y, w, h;
                                //get the center point
                                x = (float) rst.getJSONArray("face").getJSONObject(i)
                                        .getJSONObject("position").getJSONObject("center").getDouble("x");
                                y = (float) rst.getJSONArray("face").getJSONObject(i)
                                        .getJSONObject("position").getJSONObject("center").getDouble("y");

                                //get face size
                                w = (float) rst.getJSONArray("face").getJSONObject(i)
                                        .getJSONObject("position").getDouble("width");
                                h = (float) rst.getJSONArray("face").getJSONObject(i)
                                        .getJSONObject("position").getDouble("height");

                                //change percent value to the real size
                                x = x / 100 * img.getWidth();
                                w = w / 100 * img.getWidth() * 0.7f;
                                y = y / 100 * img.getHeight();
                                h = h / 100 * img.getHeight() * 0.7f;

                                //draw the box to mark it out
                                canvas.drawLine(x - w, y - h, x - w, y + h, paint);
                                canvas.drawLine(x - w, y - h, x + w, y - h, paint);
                                canvas.drawLine(x + w, y + h, x - w, y + h, paint);
                                canvas.drawLine(x + w, y + h, x + w, y - h, paint);
                            }

                            //save new image
                            img = bitmap;

                            MainActivity.this.runOnUiThread(new Runnable() {

                                public void run() {
                                    //show the image
                                    imageView.setImageBitmap(img);
                                    textView.setText("完成, 识别到" + count + " 张面孔。");
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textView.setText("出错。");
                                }
                            });
                        }

                    }
                });
                faceppDetect.detect(img);
            }
        });

        imageView = (ImageView) this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(img);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //the image picker callback
        if (requestCode == PICTURE_CHOOSE) {
            if (intent != null) {

                Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
                cursor.moveToFirst();

                String fileSrc = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                if (!TextUtils.isEmpty(fileSrc)) {
                    img = BitmapUtil.getBitmapFromDisk(fileSrc);
                    if (img != null) {
                        textView.setText("点击识别");
                        imageView.setImageBitmap(img);
                        buttonDetect.setVisibility(View.VISIBLE);
                    }
                }
                cursor.close();
            } else {
                Log.d(TAG, "idButSelPic Photopicker canceled");
            }
        }
    }

    private class FaceppDetect {
        DetectCallback callback = null;

        public void setDetectCallback(DetectCallback detectCallback) {
            callback = detectCallback;
        }

        public void detect(final Bitmap image) {

            new Thread(new Runnable() {

                public void run() {
                    HttpRequests httpRequests = new HttpRequests("4480afa9b8b364e30ba03819f3e9eff5", "Pz9VFT8AP3g_Pz8_dz84cRY_bz8_Pz8M", true, false);
                    //Log.v(TAG, "image size : " + img.getWidth() + " " + img.getHeight());

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    float scale = Math.min(1, Math.min(600f / img.getWidth(), 600f / img.getHeight()));
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);

                    Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
                    //Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());

                    imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] array = stream.toByteArray();

                    try {
                        //detect
                        JSONObject result = httpRequests.detectionDetect(new PostParameters().setImg(array));
                        //finished , then call the callback function
                        if (callback != null) {
                            callback.detectResult(result);
                        }
                    } catch (FaceppParseException e) {
                        e.printStackTrace();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                textView.setText("网络错误");
                            }
                        });
                    }

                }
            }).start();
        }
    }

    private void getPictureFromPhone() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICTURE_CHOOSE);
    }

    private boolean isPermitPermission() {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        GET_PIC_FROM_PHONE);
            }
        }
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GET_PIC_FROM_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    getPictureFromPhone();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    interface DetectCallback {
        void detectResult(JSONObject rst);
    }
}
