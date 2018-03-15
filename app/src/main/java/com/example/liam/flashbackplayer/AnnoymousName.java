package com.example.liam.flashbackplayer;

import java.util.HashMap;
import java.util.LinkedList;

class AnnoymousName {

    LinkedList<String> animalName;
    HashMap<String, String> animalNameHM;    //email --- animal
    int count = 0;

    AnnoymousName() {
        animalName = new LinkedList<>();
        animalName.push("Dog");
        animalName.push("Cat");
        animalName.push("Tiger");
        animalName.push("Wolf");
        animalName.push("Chicken");
        animalName.push("Duck");
        animalName.push("Goose");
        animalName.push("Butterfly");
        animalName.push("Camel");
        animalName.push("Caribou");
        animalName.push("Cassowary");
        animalName.push("Caterpillar");
        animalName.push("Chamois");
        animalName.push("Cheetah");
        animalName.push("Chinchilla");
    }

    //pass in email, return animal name 1 to 1
    String getAnnomyousName(String AnnoymousEmail) {
        //check if has this AnnoymousEmail
        if (animalNameHM.containsKey(AnnoymousEmail))
            return "Annoymous " + animalNameHM.get(AnnoymousEmail);
        else {
            //assign to an animal and add to HM (key email, value: animal name)
            String name = animalName.pop();
            animalNameHM.put(AnnoymousEmail, name);
            return "Annoymous" + name;
        }
    }
}
