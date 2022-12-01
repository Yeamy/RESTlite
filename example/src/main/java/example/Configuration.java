package example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yeamy.restlite.annotation.Initialization;

@Initialization(response = "yeamy.restlite.example.MyResponse")
public class Configuration {
    public static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss Z").create();
}
