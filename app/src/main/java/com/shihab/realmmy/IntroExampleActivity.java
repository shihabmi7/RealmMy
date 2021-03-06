/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shihab.realmmy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;


import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmMigrationNeededException;


public class IntroExampleActivity extends AppCompatActivity {

    public static final String TAG = IntroExampleActivity.class.getName();
    private LinearLayout rootLayout = null;

    private Realm realm;
    private RealmConfiguration realmConfig;
    Button button_save, button_show, button_delete;
    EditText editText_name, editText_age, editText_city, editText_number;
    ListView listview;
    ImageView imageView;
    private static final int PICK_IMAGE_ID = 234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);
        rootLayout = ((LinearLayout) findViewById(R.id.container));
        listview = (ListView) findViewById(R.id.listview);
        imageView = (ImageView) findViewById(R.id.imageView);

        // you need to add toolbar title
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Realm Activity");
        setSupportActionBar(toolbar);

        if (rootLayout != null) {
            rootLayout.removeAllViews();

        } else {

        }

        configureRealm();


        editText_age = (EditText) findViewById(R.id.editText_age);
        editText_name = (EditText) findViewById(R.id.editText_name);
        //editText_city = (EditText) findViewById(R.id.editText_city);
        editText_number = (EditText) findViewById(R.id.editText_number);


        button_save = (Button) findViewById(R.id.button_save);
        button_show = (Button) findViewById(R.id.button_show);
        button_delete = (Button) findViewById(R.id.button_delete);


        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteAllPersonData();

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                onPickImage(v);
            }
        });
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkField()) {

                    addNewPerson(editText_name.getText().toString(),
                            Integer.parseInt(editText_age.getText().toString()), "",editText_number.getText().toString());
                }


            }
        });

        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAllPerson();

            }
        });

        listview.setAdapter(new MyAdapter(getApplicationContext(), 0, showAllPerson()));

    }

    private void configureRealm() {
        try {
            // Create the Realm configuration
            realmConfig = new RealmConfiguration.Builder(this).name("shihab_realm")
                    .schemaVersion(0).migration(new Migration())
                    .build();
            // Open the Realm for the UI thread.
            realm = Realm.getInstance(realmConfig);

        } catch (RealmMigrationNeededException r) {
            Log.e("RealmMigrationException", "" + r.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
        // Remember to close Realm when done.
    }

    private void showStatus(String txt) {
        Log.i(TAG, txt);
        TextView tv = new TextView(this);
        tv.setText(txt);
        rootLayout.removeAllViews();
        rootLayout.addView(tv);
    }

    public void addNewPerson(String name, int age, String city,String number) {

        // All writes must be wrapped in a transaction to facilitate safe multi threading
        realm.beginTransaction();

        Person person = realm.createObject(Person.class);
        person.setId(getUniqueId(realm));
        person.setName(name);
        person.setAge(age);
        //person.setCity(city);
        person.setNumber(number);
        person.setPicture(convertBitMapToByteArray(((BitmapDrawable) imageView.getDrawable()).getBitmap()));

//        Person person = realm.allObjects(Person.class).first();
//        if (person.isValid()) {
//
//
//        } else {
//            // Any operation will throw a IllegalStateException
//
//        }


        // When the transaction is committed, all changes are synced to disk.
        realm.commitTransaction();
        clearText();
        showSnackBar("Saved Successfully");
    }

    public static long getUniqueId(Realm realm) {
        Number num = realm.where(Person.class).max("id");
        if (num == null) {
            return 1;
        } else {
            return ((long) num + 1);
        }
    }


    void clearText() {

        editText_name.setText("");
        editText_age.setText("");
       // editText_city.setText("");
        editText_number.setText("");

    }

    void showSnackBar(String msg) {

        final Snackbar snackBar = Snackbar.make(findViewById(R.id.myCoordinatorLayout), msg, Snackbar.LENGTH_LONG);

        snackBar.
                setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        snackBar.dismiss();

                    }
                })
                .show();

    }


    private void deleteAllPersonData() {
        // Delete all persons
        realm.beginTransaction();

        realm.delete(Person.class);

        showStatus("No Data in Database ...");
        realm.commitTransaction();
    }


    private RealmResults<Person> showAllPerson() {

        RealmResults<Person> allPersons = realm.where(Person.class).findAll();
        String result = "Now Shows all People Date: total =" + allPersons.size();
        for (int i = 0; i < allPersons.size(); i++) {

            result += "\nId: " + allPersons.get(i).getId() + " Name: " + allPersons.get(i).getName() + " \t     Age: " + allPersons.get(i).getAge();

        }
        //showStatus(result);

        return allPersons;
    }

    boolean checkField() {

        if (editText_name.getText().length() > 0 && editText_age.getText().length() > 0 && editText_number.getText().length() > 0) {

            return true;

        } else {

            showSnackBar("Enter all fields...");
            return false;
        }

    }


    public void onPickImage(View view) {
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:

                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                imageView.setImageBitmap(bitmap);

                // TODO use bitmap
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    byte[] convertBitMapToByteArray(Bitmap bmp) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
    private void basicQuery(String key, String value) {

        RealmResults<Person> results = realm.where(Person.class).equalTo(key, value).findAll();
        showStatus("Size of result set: " + results.size());

    }
    private void getFirstPerson() {
        Person person;
        // Find the first person (no query conditions) and read a field
        person = realm.where(Person.class).findFirst();
        showStatus(person.getName() + ":" + person.getAge());
    }

    private void updatePersonData(Person person) {
        // Update person in a transaction
        realm.beginTransaction();
        person.setName("Senior Person");
        person.setAge(99);
        showStatus(person.getName() + " got older: " + person.getAge());
        realm.commitTransaction();
    }

    private void basicLinkQuery(Realm realm) {

        showStatus("\nPerforming basic Link Query operation...");
        showStatus("Number of persons: " + realm.where(Person.class).findAll().size());

        RealmResults<Person> results = realm.where(Person.class).equalTo("cats.name", "Tiger").findAll();
        showStatus("Size of result set: " + results.size());

    }

    private String complexReadWrite() {
        String status = "\nPerforming complex Read/Write operation...";

        // Open the default realm. All threads must use it's own reference to the realm.
        // Those can not be transferred across threads.
        Realm realm = Realm.getInstance(realmConfig);

        // Add ten persons in one transaction
        realm.beginTransaction();
        Dog fido = realm.createObject(Dog.class);
        fido.name = "fido";
        for (int i = 0; i < 10; i++) {
            Person person = realm.createObject(Person.class);
            person.setId(i);
            person.setName("Person no. " + i);
            person.setAge(i);
            person.setDog(fido);

            // The field tempReference is annotated with @Ignore.
            // This means setTempReference sets the Person tempReference
            // field directly. The tempReference is NOT saved as part of
            // the RealmObject:
            person.setTempReference(42);

            for (int j = 0; j < i; j++) {
                Cat cat = realm.createObject(Cat.class);
                cat.name = "Cat_" + j;
                person.getCats().add(cat);
            }
        }
        realm.commitTransaction();

        // Implicit read transactions allow you to access your objects
        status += "\nNumber of persons: " + realm.where(Person.class).findAll().size();

        // Iterate over all objects
        for (Person pers : realm.where(Person.class).findAll()) {
            String dogName;
            if (pers.getDog() == null) {
                dogName = "None";
            } else {
                dogName = pers.getDog().name;
            }
            status += "\n" + pers.getName() + ":" + pers.getAge() + " : " + dogName + " : " + pers.getCats().size();
        }

        // Sorting
        RealmResults<Person> sortedPersons = realm.where(Person.class).findAll();
        sortedPersons.sort("age", Sort.DESCENDING);
        status += "\nSorting " + sortedPersons.last().getName() + " == " + realm.where(Person.class).findAll().first()
                .getName();

        realm.close();
        return status;
    }

    private String complexQuery() {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = Realm.getInstance(realmConfig);
        status += "\nNumber of persons: " + realm.where(Person.class).findAll().size();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();
        status += "\nSize of result set: " + results.size();

        realm.close();
        return status;
    }


}
