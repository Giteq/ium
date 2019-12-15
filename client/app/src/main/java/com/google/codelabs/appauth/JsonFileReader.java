package com.google.codelabs.appauth;

import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.google.codelabs.appauth.MainApplication.LOG_TAG;

public class JsonFileReader {

    String file_root_path;

    public JsonFileReader (String file_root_path){
        this.file_root_path = file_root_path;
    }

    public void clearDirectory() throws IOException {
        File dir = new File(this.file_root_path + "json_objects/");
        FileUtils.cleanDirectory(dir);
    }

    public void clearBackupDirectory() throws IOException {
        File dir = new File(this.file_root_path + "json_objects_backup/");
        if (dir.exists()){
            FileUtils.cleanDirectory(dir);
        }

    }

    public String writeToFile(JSONObject object) throws IOException, JSONException {
        String dir_path = this.file_root_path + "json_objects/";
        String path = getFilePath(object);
        Log.d(LOG_TAG, path);
        File file = new File(dir_path);
        if (!file.exists()){
            file.mkdir();
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
        objectOutputStream.writeObject(object.toString());
        objectOutputStream.close();
        return path;
    }

    public void removeFile(JSONObject object) throws JSONException {
        File file = new File(getFilePath(object));
        file.delete();
    }

    public JSONObject readJsonObjFromFile(String name) throws IOException, ClassNotFoundException, JSONException {
        String object = null;
        JSONObject json_obj = null;

        String path = this.file_root_path + "json_objects/";
        File file = new File(path + name);
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
        object = (String) objectInputStream.readObject();
        json_obj = new JSONObject(object);
        objectInputStream.close();

        return json_obj;
    }

    public JSONObject readJsonObjFromFile(File file) throws IOException, ClassNotFoundException, JSONException {
        String object = null;
        JSONObject json_obj = null;

        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
        object = (String) objectInputStream.readObject();
        json_obj = new JSONObject(object);
        objectInputStream.close();

        return json_obj;
    }

    public Product jsonToProd(JSONObject json_obj) throws JSONException {
        Product product = new Product(
                json_obj.get("man_name").toString(),
                json_obj.get("model_name").toString(),
                Integer.valueOf(json_obj.get("price").toString()),
                Integer.valueOf(json_obj.get("quantity").toString()),
                Integer.valueOf(json_obj.get("id").toString())
        );

        return product;
    }

    public Product readProdFromFile(String name) throws IOException, ClassNotFoundException, JSONException {
        JSONObject json_obj = readJsonObjFromFile(name);
        Product product = new Product(
                json_obj.get("man_name").toString(),
                json_obj.get("model_name").toString(),
                Integer.valueOf(json_obj.get("price").toString()),
                Integer.valueOf(json_obj.get("quantity").toString()),
                Integer.valueOf(json_obj.get("id").toString())
        );

        return product;
    }

    public JSONArray readJsonArrayFromFiles() throws JSONException, IOException, ClassNotFoundException {
        JSONArray jsonArray = new JSONArray();
        String path = this.file_root_path + "json_objects/";
        File dir = new File(path);
        File[] files = dir.listFiles();
        JSONObject jsonObject;


        for (int i = 0; i < files.length; ++i) {
            File file = files[i];
            jsonObject = readJsonObjFromFile(file);
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    public JSONArray readJsonArrayFromBackup() throws JSONException, IOException, ClassNotFoundException {
        JSONArray jsonArray = new JSONArray();
        String path = this.file_root_path + "json_objects_backup/";
        File dir = new File(path);
        File[] files = dir.listFiles();
        JSONObject jsonObject;

        if (dir.exists()){
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                jsonObject = readJsonObjFromFile(file);
                jsonArray.put(jsonObject);
            }
        }else{
            Log.e(LOG_TAG, "BACKUP DOUES NOT EXIST");
        }


        return jsonArray;
    }

    public void backup() throws IOException, JSONException, ClassNotFoundException {
        JSONArray new_prod, backup;

        new_prod = readJsonArrayFromFiles();
        backup = readJsonArrayFromBackup();

        try {
            clearBackupDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String path = this.file_root_path + "json_objects/";
        File srcDir = new File(path);

        path = this.file_root_path + "json_objects_backup/";
        File destDir = new File(path);

        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath(JSONObject object) throws JSONException {
        return this.file_root_path + "json_objects/" + object.get("man_name");
    }



}
