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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.exceptions.RealmMigrationNeededException;


public class IntroExampleActivity extends Activity {

    public static final String TAG = IntroExampleActivity.class.getName();
    private LinearLayout rootLayout = null;

    private Realm realm;
    private RealmConfiguration realmConfig;
    Button button_save, button_show, button_delete;
    EditText editText_name, editText_age, editText_city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_basic_example);
        rootLayout = ((LinearLayout) findViewById(R.id.container));

        if (rootLayout != null) {
            rootLayout.removeAllViews();

        } else {

        }

        // These operations are small enough that
        // we can generally safely run them on the UI thread.

        try {
            // Create the Realm configuration
            realmConfig = new RealmConfiguration.Builder(this).build();
            // Open the Realm for the UI thread.
            realm = Realm.getInstance(realmConfig);

        } catch (RealmMigrationNeededException r) {

            Log.e("RealmMigrationException", "" + r.getMessage());

        }


        editText_age = (EditText) findViewById(R.id.editText_age);
        editText_name = (EditText) findViewById(R.id.editText_name);
        editText_city = (EditText) findViewById(R.id.editText_city);


        button_save = (Button) findViewById(R.id.button_save);
        button_show = (Button) findViewById(R.id.button_show);
        button_delete = (Button) findViewById(R.id.button_delete);


        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteAllPersonData();

            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addNewPerson(editText_name.getText().toString(),
                        Integer.parseInt(editText_age.getText().toString()), editText_city.getText().toString());

            }
        });

        button_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                getFirstPerson();
                showAllPerson();

            }
        });

        // basicCRUD(realm);
        // basicQuery(realm);
        // basicLinkQuery(realm);

        // More complex operations can be executed on another thread.
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... voids) {
//                String info;
//                info = complexReadWrite();
//                info += complexQuery();
//                return info;
//            }
//
//            @Override
//            protected void onPostExecute(String result) {
//                //  showStatus(result);
//            }
//        }.execute();
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

    public void addNewPerson(String name, int age, String city) {

        // All writes must be wrapped in a transaction to facilitate safe multi threading
        realm.beginTransaction();
        // Add a person
        Person person = realm.createObject(Person.class);
        // person.setId(1);
        person.setName(name);
        person.setAge(age);
        person.setCity(city);

        // When the transaction is committed, all changes are synced to disk.
        realm.commitTransaction();

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

    private void deleteAllPersonData() {
        // Delete all persons
        realm.beginTransaction();
        realm.allObjects(Person.class).clear();

        showStatus("No Data in Database ...");
        realm.commitTransaction();
    }

    private void basicQuery(String key, String value) {

        RealmResults<Person> results = realm.where(Person.class).equalTo(key, value).findAll();
        showStatus("Size of result set: " + results.size());

    }

    private void showAllPerson() {

        RealmResults<Person> allPersons = realm.allObjects(Person.class);
        String result = "Now Shows all People Date: total =" + allPersons.size();
        for (int i = 0; i < allPersons.size(); i++) {

            result += "\nName: " + allPersons.get(i).getName() + " \t     Age: " + allPersons.get(i).getAge();

        }
        showStatus(result);
    }

    private void basicLinkQuery(Realm realm) {

        showStatus("\nPerforming basic Link Query operation...");
        showStatus("Number of persons: " + realm.allObjects(Person.class).size());

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
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Iterate over all objects
        for (Person pers : realm.allObjects(Person.class)) {
            String dogName;
            if (pers.getDog() == null) {
                dogName = "None";
            } else {
                dogName = pers.getDog().name;
            }
            status += "\n" + pers.getName() + ":" + pers.getAge() + " : " + dogName + " : " + pers.getCats().size();
        }

        // Sorting
        RealmResults<Person> sortedPersons = realm.allObjects(Person.class);
        sortedPersons.sort("age", Sort.DESCENDING);
        status += "\nSorting " + sortedPersons.last().getName() + " == " + realm.allObjects(Person.class).first()
                .getName();

        realm.close();
        return status;
    }

    private String complexQuery() {
        String status = "\n\nPerforming complex Query operation...";

        Realm realm = Realm.getInstance(realmConfig);
        status += "\nNumber of persons: " + realm.allObjects(Person.class).size();

        // Find all persons where age between 7 and 9 and name begins with "Person".
        RealmResults<Person> results = realm.where(Person.class)
                .between("age", 7, 9)       // Notice implicit "and" operation
                .beginsWith("name", "Person").findAll();
        status += "\nSize of result set: " + results.size();

        realm.close();
        return status;
    }
}
