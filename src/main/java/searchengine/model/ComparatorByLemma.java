package searchengine.model;

import java.util.Comparator;

public class ComparatorByLemma implements Comparator<Lemmas> {
    @Override
    public int compare(Lemmas o1, Lemmas o2) {
        return Double.compare(o1.getFrequency(), o2.getFrequency());
    }
}
