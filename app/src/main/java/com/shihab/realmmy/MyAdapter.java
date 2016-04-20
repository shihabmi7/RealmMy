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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class MyAdapter extends RealmBaseAdapter<Person> implements ListAdapter {

    private static class MyViewHolder {
        TextView name;
    }


    public MyAdapter(Context context, int resId,
                     RealmResults<Person> realmResults,
                     boolean automaticUpdate) {

        super(context, realmResults, automaticUpdate);

    }

    @Override
    public Person getItem(int i) {
        return super.getItem(i);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MyViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item,
                    parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.text_person);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }

        Person person = realmResults.get(position);
        String result = "\nId: " + person.getId() + " Name: " + person.getName() + " \t     Age: " + person.getAge();

        viewHolder.name.setText(result);
        return convertView;
    }

    public RealmResults<Person> getRealmResults() {
        return realmResults;
    }
}