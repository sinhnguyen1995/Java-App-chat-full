package server.service.publisher;

import models.HotTopic;
import server.service.listeners.Observer;


public interface Subject {

    boolean registerObserver(HotTopic hotTopic, Observer o);

    void removeObserver(HotTopic hotTopic, Observer o);

    void notifyObservers();

    void notifyAllObservers();
}
