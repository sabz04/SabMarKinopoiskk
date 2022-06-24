package com.example.sabmarkinopoisk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainPage extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();//сборка заготовленных классов и пакетов, имплементацию(присоединение к проекту) сделал в файле build.gradle, открой, посмотри
    private static String responseText = "";//сделал для удобства, сохраняю полученный по get-запросу текст, т.е там хранится json - пакет в текстовом представлении
    private static String responseDesc = "";//ниже получаю описание полученного спарсенного responseText, т.е получаю значение по ключу "description"
    private static String responsewebUrl = "";//принцип такой же, как и в получении описания объекта, только тут использую ключ "posterUrl" для получения ссылки на постер
    private static String webURl = "https://kinopoiskk.ru/film/";//ссылка, к которой будем присоединять айди, который я спарсил нижу
    private static String waitingPhoto = "https://img2.freepng.ru/20180503/peq/kisspng-recycling-symbol-computer-icons-icon-design-clip-a-5aeb6d79cf0649.897215281525378425848.jpg";
    private InputStream inputStream = null;//поток, в который будем хранить полученное изображение из спарсенной ссылки, которую сохранили в responsewebUrl
    private Bitmap bitmap = null;//поток конвертирую в битмап, который потом засуну в imageview, чтобы вывести наше изображение
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

    }

    public void SearchClick(View view){//обрыботчик клика
        runOnUiThread(new Runnable() {//пока данные получаются, этот поток, работающий с интерфейсом, будет выводить в поле описания "Получение данных"
            @Override
            public void run() {
                TextView infoText = (TextView)findViewById(R.id.descriptionLB);
                infoText.setText("Получение данных...");
            }
        });
        System.out.println("clicked");
        TextView searchTB = (TextView) findViewById(R.id.searchTB);
        Request request = new Request.Builder()//создание запроса, добавление заголовков, параметров, принадлежит сборке пакетов и заготовок OKhttp3
                .url("https://kinopoiskapiunofficial.tech/api/v2.2/films/"+searchTB.getText())//сам метод апишки, чтобы получить объект фильма в виде пакета по айди, с другими методами можешь ознакомиться по этой ссылке: https://kinopoiskapiunofficial.tech/
                .addHeader("X-API-KEY", "5786e33d-04ee-424b-9d04-f32dea1df928")//этот ключ не трогай! он мой личный, дает доступ к методам кинопоиска
                .build();
        client.newCall(request).enqueue(new Callback() {//это заготовка, которая дает нам асинхронно отправлять и получать запросы
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {//в случае ошибки получения/отправления(get/post), сработает это
                e.printStackTrace();
                runOnUiThread(new Runnable() {//runOnUiThread - всегда нужно использовать при обращении к объектам интерфейса, напрямую получить объект и изменить его значение не получится - произойдет конфликт потоков
                    @Override
                    public void run() {
                        TextView infoText = (TextView)findViewById(R.id.descriptionLB);
                        infoText.setText("Ошибка при получении данных");
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {//если же наш запрос что-то да сделал, то сработает это
                if(response.isSuccessful()){
                    ResponseBody resp = response.body();//получаю тело запроса
                    responseText = resp.string();//получаю тело запроса в виде строки; resp.string(); можно писать только один раз, напишешь еще - будет ошибка
                    try {
                        JSONObject film = new JSONObject(responseText);//объект JSONobject - позволяет парсить данные из строки

                        responseDesc = film.getString("description");//получаю описание по ключу, описал еще в самом начале

                        responsewebUrl = film.getString("posterUrl");//ссылка на изображение

                        inputStream = new URL((responsewebUrl)).openStream();//открываю поток и передаю туда веб-ссылку на изображение, он скачает его и сохранит временно в объекте inputStream
                        bitmap = BitmapFactory.decodeStream(inputStream);//преобразую в растровое изображение, чтобы потом поместить в imageview и вывести

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView img = (ImageView)findViewById(R.id.posterIMG);
                                img.setImageBitmap(bitmap);//вывод изображения;runOnUiThread - ОБЯЗАТЕЛЬНО ОБРАЩАТЬСЯ К ЭЛЕМЕНТАМ ИНТЕРФЕЙСА ТОЛЬКО ЧЕРЕЗ НЕГО
                            }
                        });

                        System.out.println("ID is = "+responseDesc);
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView infoText = (TextView)findViewById(R.id.descriptionLB);
                                infoText.setText("Ошибка при получении данных");
                            }
                        });
                    }
                    System.out.println(responseText);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView infoText = (TextView)findViewById(R.id.descriptionLB);
                                infoText.setText(responseDesc);//вывожу полученное описание
                        }
                    });

                }
            }
        });
    }
    }
