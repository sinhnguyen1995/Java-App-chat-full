package server.service.publisher;

import models.HotTopic;
import server.service.listeners.Observer;

import java.util.*;

public class ConcreteSubject implements Subject {
    private final Map<HotTopic, List<Observer>> listeners;
    private final Queue<Integer> queue;
    private String messageContent;
    private HotTopic hotTopic;

    public ConcreteSubject() {
        this.queue = new LinkedList<>();
        this.listeners = new HashMap<>();
        for (HotTopic hotTopic : HotTopic.values()) {
            this.listeners.put(hotTopic, new ArrayList<>());
        }
    }

    @Override
    public boolean registerObserver(HotTopic hotTopic, Observer o) {
        List<Observer> users = this.listeners.get(hotTopic);
        if (!users.contains(o)) {
            users.add(o);
            System.out.println("Subscribe succeed to " + hotTopic + " topic!");
            return true;
        } else {
            System.out.println("Subscribe fail! User already subscribe to " + hotTopic + " topic!");
            return false;
        }
    }

    @Override
    public void removeObserver(HotTopic hotTopic, Observer o) {
        List<Observer> users = this.listeners.get(hotTopic);
        int i = users.indexOf(o);
        if (i > 0) {
            users.remove(i);
        }
    }

    @Override
    public void notifyAllObservers() {
        for (HotTopic key : listeners.keySet()){
            List<Observer> users = listeners.get(key);
            for (Observer listener : users){
                listener.update(this.messageContent);
            }
        }
    }
    @Override
    public void notifyObservers() {
        List<Observer> users = this.listeners.get(this.hotTopic);
        System.out.println("There are: " + users.size() + " clients subscribe to " + hotTopic + " topic.");
        for (Observer listener : users) {
            System.out.println("Hashcode" + listener.hashCode());
            this.queue.add(listener.hashCode());
            System.out.println("Oh");
            listener.update(this.messageContent);
        }
    }

    public void newComingBroadcast() {
        notifyObservers();
    }

    public void setMessageContent(HotTopic hotTopic, String messageContent) {
        this.messageContent = messageContent;
        this.hotTopic = hotTopic;
        newComingBroadcast();
    }

    public void setMessageContentBroadcastToAll(String messageContent) {
        this.messageContent = messageContent;
        notifyAllObservers();
    }
}
