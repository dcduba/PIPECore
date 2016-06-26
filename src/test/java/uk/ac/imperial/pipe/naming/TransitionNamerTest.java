package uk.ac.imperial.pipe.naming;

import org.junit.Before;
import org.junit.Test;
import uk.ac.imperial.pipe.models.petrinet.DiscreteTransition;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;

import static org.junit.Assert.*;

public class TransitionNamerTest {
    PetriNet petriNet;

    TransitionNamer transitionNamer;

    @Before
    public void setUp() {
        petriNet = new PetriNet();
        transitionNamer = new TransitionNamer(petriNet);
    }

    @Test
    public void firstTransitionIsOne() {
        String actual = transitionNamer.getName();
        assertEquals("T1", actual);
    }

    @Test
    public void returnT1IfPlaceNotCreated() {
        String first = transitionNamer.getName();
        assertEquals("T1", first);
        String second = transitionNamer.getName();
        assertEquals("T1", second);
    }

    @Test
    public void returnT1IfPlacesDontConflict() {
        Transition transition = new DiscreteTransition("T2", "T2");
        petriNet.addTransition(transition);
        String actual = transitionNamer.getName();
        assertEquals("T1", actual);
    }

    @Test
    public void returnNextValueIfTwoTransitionsExist() {
        addNConsecutiveTransitions(2);
        String actual = transitionNamer.getName();
        assertEquals("T3", actual);
    }

    private void addNConsecutiveTransitions(int n) {
        for (int i = 1; i <= n; i++) {
            String id = "T" + i;
            Transition transition = new DiscreteTransition(id, id);
            petriNet.addTransition(transition);
        }
    }

    @Test
    public void returnNextValueIfFourTransitionsExist() {
        addNConsecutiveTransitions(4);
        String actual = transitionNamer.getName();
        assertEquals("T5", actual);
    }

    @Test
    public void returnMiddleValue() {
        Transition transition = new DiscreteTransition("T1", "T1");
        Transition transition2 = new DiscreteTransition("T3", "T3");
        petriNet.addTransition(transition);
        petriNet.addTransition(transition2);

        String actual = transitionNamer.getName();
        assertEquals("T2", actual);
    }

    @Test
    public void reUseDeletedValue() {
        Transition transition = new DiscreteTransition("T1", "T1");
        petriNet.addTransition(transition);

        String actual = transitionNamer.getName();
        assertEquals("T2", actual);
        petriNet.removeTransition(transition);

        String actual2 = transitionNamer.getName();
        assertEquals("T1", actual2);
    }

    /**
     * Since the TransitionNamer works via listening for change events
     * we need to make sure if Transition exist in the petrinet on construction
     * it still is aware of their names.
     */
    @Test
    public void returnCorrectValueAfterConstructor() {
        Transition transition = new DiscreteTransition("T1", "T1");
        petriNet.addTransition(transition);
        petriNet.addTransition(transition);

        TransitionNamer newNamer = new TransitionNamer(petriNet);

        String actual = newNamer.getName();
        assertEquals("T2", actual);
    }

    @Test
    public void identifiesNonUniqueName() {
        String name = "Transition 1";
        Transition transition = new DiscreteTransition(name, name);
        petriNet.addTransition(transition);
        TransitionNamer newNamer = new TransitionNamer(petriNet);
        assertFalse(newNamer.isUniqueName(name));
    }


    @Test
    public void identifiesUniqueName() {
        String name = "Transition 1";
        Transition transition = new DiscreteTransition(name, name);
        petriNet.addTransition(transition);
        TransitionNamer newNamer = new TransitionNamer(petriNet);
        assertTrue(newNamer.isUniqueName("Transition 2"));
    }

    @Test
    public void observesTransitionNameChanges() {
        String orignalId = "Transition 1";
        Transition transition = new DiscreteTransition(orignalId, orignalId);
        petriNet.addTransition(transition);
        UniqueNamer newNamer = new TransitionNamer(petriNet);
        String newId = "Transition 2";
        transition.setId(newId);
        assertFalse(newNamer.isUniqueName(newId));
        assertTrue(newNamer.isUniqueName(orignalId));
    }
}
