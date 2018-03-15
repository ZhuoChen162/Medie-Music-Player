package com.example.liam.flashbackplayer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import static com.example.liam.flashbackplayer.MainActivity.emailAndName;

/**
 * Created by xuzhaokai on 3/14/18.
 */

public class AnnoymousName {

        ArrayList<String> animalName;
        HashMap<String,String> animalNameHM;    //email --- animal
        int count=0;

        AnnoymousName(){
            animalName = new ArrayList<String>();
            animalName.add("dog");
            animalName.add("fish");
            animalName.add("cat");
            animalName.add("tiger");
            animalName.add("wolf");
            animalName.add("pig");
            animalName.add("chicken");
            animalName.add("bird");
            animalName.add("duck");
            animalName.add("cow");
            animalName.add("goose");
        }

        //pass in email, return animal name 1 to 1
        String isAnnomyousName(String AnnoymousEmail){
            //check if has this AnnoymousEmail
            if (animalNameHM.containsKey(AnnoymousEmail))
                return "Annoymous"+ animalNameHM.get(AnnoymousEmail);
            else{
                //assign to an animal and add to HM (key email, value: animal name)
                animalNameHM.put(AnnoymousEmail , animalName.get(count));
                count++;
                return "Annoymous"+ animalName.get(count);
            }
        }
}
