package com.example.liam.flashbackplayer;

import java.util.HashMap;
import java.util.LinkedList;
/**
 * This is a AnonymousName class will assign an unique AnonymousName to each Anonymous person 
 */
class AnonymousName {

    LinkedList<String> animalName;
    HashMap<String, String> animalNameHM;    //email --- animal

    AnonymousName() {
        animalNameHM = new HashMap<>();
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
    String getAnonmyousName(String AnonymousEmail) {
        //check if has this AnonymousEmail
        if (animalNameHM.containsKey(AnonymousEmail))
            return "Anonymous " + animalNameHM.get(AnonymousEmail);
        else {
            //assign to an animal and add to HM (key email, value: animal name)
            String name = animalName.pop();
            animalNameHM.put(AnonymousEmail, name);
            return "Anonymous " + name;
        }
    }
}
