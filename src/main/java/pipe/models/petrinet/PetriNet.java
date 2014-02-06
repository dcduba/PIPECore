package pipe.models.petrinet;

import pipe.exceptions.PetriNetComponentNotFound;
import pipe.io.adapters.modelAdapter.*;
import pipe.models.component.Connectable;
import pipe.models.component.PetriNetComponent;
import pipe.models.component.annotation.Annotation;
import pipe.models.component.arc.Arc;
import pipe.models.component.arc.ArcType;
import pipe.models.component.place.Place;
import pipe.models.component.rate.RateParameter;
import pipe.models.component.token.Token;
import pipe.models.component.transition.Transition;
import pipe.visitor.foo.PetriNetComponentVisitor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

@XmlType(propOrder = {"tokens", "annotations", "rateParameters", "places", "transitions", "arcs"})
public class PetriNet {


    /**
     * Message fired when an annotation is added to the Petri net
     */
    public static final String NEW_ANNOTATION_CHANGE_MESSAGE = "newAnnotation";

    /**
     * Message fired when a place is deleted from the Petri net
     */
    public static final String DELETE_PLACE_CHANGE_MESSAGE = "deletePlace";

    /**
     * Message fired when an arc is deleted from the Petri net
     */
    public static final String DELETE_ARC_CHANGE_MESSAGE = "deleteArc";

    /**
     * Message fired when a transition is deleted from the Petri net
     */
    public static final String DELETE_TRANSITION_CHANGE_MESSAGE = "deleteTransition";

    /**
     * Message fired when an annotation is deleted from the Petri net
     */
    public static final String DELETE_ANNOTATION_CHANGE_MESSAGE = "deleteAnnotation";

    /**
     * Message fired when a Place is added to the Petri net
     */
    public static final String NEW_PLACE_CHANGE_MESSAGE = "newPlace";

    /**
     * Message fired when a transition is added to the Petri net
     */
    public static final String NEW_TRANSITION_CHANGE_MESSAGE = "newTransition";

    /**
     * Message fired when an arc is added to the Petri net
     */
    public static final String NEW_ARC_CHANGE_MESSAGE = "newArc";

    /**
     * Message fired when a token is added to the Petri net
     */
    public static final String NEW_TOKEN_CHANGE_MESSAGE = "newToken";

    public static final String DELETE_TOKEN_CHANGE_MESSAGE = "deleteToken";

    public static final String NEW_RATE_PARAMETER_CHANGE_MESSAGE = "newRateParameter";

    public static final String DELETE_RATE_PARAMETER_CHANGE_MESSAGE = "deleteRateParameter";

    protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    //TODO: CYCLIC DEPENDENCY BETWEEN CREATING THIS AND PETRINET/
    private final PetriNetComponentVisitor deleteVisitor = new PetriNetComponentRemovalVisitor(this);

    @XmlElement(name = "transition")
    @XmlJavaTypeAdapter(TransitionAdapter.class)
    private final Set<Transition> transitions = new HashSet<Transition>();

    @XmlElement(name = "place")
    @XmlJavaTypeAdapter(PlaceAdapter.class)
    private final Set<Place> places = new HashSet<Place>();

    @XmlElement(name = "token")
    @XmlJavaTypeAdapter(TokenAdapter.class)
    private final Set<Token> tokens = new HashSet<Token>();

    @XmlElement(name = "arc")
    @XmlJavaTypeAdapter(ArcAdapter.class)
    private final Set<Arc<? extends Connectable, ? extends Connectable>> arcs =
            new HashSet<Arc<? extends Connectable, ? extends Connectable>>();

    @XmlElement(name = "labels")
    @XmlJavaTypeAdapter(AnnotationAdapter.class)
    private final Set<Annotation> annotations = new HashSet<Annotation>();

    @XmlElement(name="definition")
    @XmlJavaTypeAdapter(RateParameterAdapter.class)
    private final Set<RateParameter> rateParameters = new HashSet<RateParameter>();

    /**
     * Houses the backwards strategies for arcs place -> transition
     * There can be two kinds, normal and inhibitor
     */
    private final Map<ArcType, ArcStrategy<Place, Transition>> backwardsStrategies =
            new HashMap<ArcType, ArcStrategy<Place, Transition>>();

    private final Map<ArcType, ArcStrategy<Transition, Place>> forwardStrategies =
            new HashMap<ArcType, ArcStrategy<Transition, Place>>();

    private final PetriNetComponentVisitor addVisitor = new PetriNetComponentAddVisitor(this);

    @XmlTransient
    public String pnmlName = "";

    @XmlTransient
    private boolean validated = false;

    public PetriNet() {
        backwardsStrategies.put(ArcType.NORMAL, new BackwardsNormalStrategy());
        backwardsStrategies.put(ArcType.INHIBITOR, new InhibitorStrategy());
        forwardStrategies.put(ArcType.NORMAL, new ForwardsNormalStrategy());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    @XmlTransient
    public String getPnmlName() {
        return pnmlName;
    }

    public void setPnmlName(String pnmlName) {
        this.pnmlName = pnmlName;
    }

    @XmlTransient
    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public void resetPNML() {
        pnmlName = null;
    }

    public void addPlace(Place place) {
        if (!places.contains(place)) {
            places.add(place);
            changeSupport.firePropertyChange(NEW_PLACE_CHANGE_MESSAGE, null, place);
        }
    }

    public void addTransition(Transition transition) {
        if (!transitions.contains(transition)) {
            transitions.add(transition);
            changeSupport.firePropertyChange(NEW_TRANSITION_CHANGE_MESSAGE, null, transition);
        }
    }

    public void addArc(Arc<? extends Connectable, ? extends Connectable> arc) {
        if (!arcs.contains(arc)) {
            arcs.add(arc);
            changeSupport.firePropertyChange(NEW_ARC_CHANGE_MESSAGE, null, arc);
        }
    }

    public void addToken(Token token) {
        if (!tokens.contains(token)) {
            tokens.add(token);
            changeSupport.firePropertyChange(NEW_TOKEN_CHANGE_MESSAGE, null, token);
        }
    }

    public Collection<Place> getPlaces() {
        return places;
    }

    //    public void addRate(RateParameter parameter) {
    //        rates.add(parameter);
    //        changeSupport.firePropertyChange("newRate", null, parameter);
    //    }

    //    public Collection<RateParameter> getRateParameters() {
    //        return rates;
    //    }

    public void addAnnotaiton(Annotation annotation) {
        if (!annotations.contains(annotation)) {
            annotations.add(annotation);
            changeSupport.firePropertyChange(NEW_ANNOTATION_CHANGE_MESSAGE, null, annotation);
        }
    }

    public void addRateParameter(RateParameter rateParameter) {
        if (!rateParameters.contains(rateParameter)) {
            rateParameters.add(rateParameter);
            changeSupport.firePropertyChange(NEW_RATE_PARAMETER_CHANGE_MESSAGE, null, rateParameter);
        }
    }

    public Collection<Annotation> getAnnotations() {
        return annotations;
    }

    //    public void addStateGroup(StateGroup group) {
    //        stateGroups.add(group);
    //        changeSupport.firePropertyChange("newStateGroup", null, group);
    //    }

    //    public Collection<StateGroup> getStateGroups() {
    //        return stateGroups;
    //    }

    public Collection<Arc<? extends Connectable, ? extends Connectable>> getArcs() {
        return arcs;
    }

    public Collection<Token> getTokens() {
        return tokens;
    }

    public void removePlace(Place place) {
        this.places.remove(place);
        for (Arc<Place, Transition> arc : outboundArcs(place)) {
            removeArc(arc);
        }
        changeSupport.firePropertyChange(DELETE_PLACE_CHANGE_MESSAGE, place, null);
    }

    public void removeArc(Arc<? extends Connectable, ? extends Connectable> arc) {
        this.arcs.remove(arc);
        removeArcFromSourceAndTarget(arc);
        changeSupport.firePropertyChange(DELETE_ARC_CHANGE_MESSAGE, arc, null);
    }

    /**
     * Removes the arc from the source and target inbound/outbound Collections
     */
    private <S extends Connectable, T extends Connectable> void removeArcFromSourceAndTarget(Arc<S, T> arc) {
    }

    /**
     * @param place
     * @return arcs that are outbound from place
     */
    public Collection<Arc<Place, Transition>> outboundArcs(Place place) {
        Collection<Arc<Place, Transition>> outbound = new LinkedList<Arc<Place, Transition>>();
        for (Arc<? extends Connectable, ? extends Connectable> arc : arcs) {
            if (arc.getSource().equals(place)) {
                outbound.add((Arc<Place, Transition>) arc);
            }
        }
        return outbound;
    }

    public void removeTransition(Transition transition) {
        this.transitions.remove(transition);
        for (Arc<Transition, Place> arc : outboundArcs(transition)) {
            removeArc(arc);
        }
        changeSupport.firePropertyChange(DELETE_TRANSITION_CHANGE_MESSAGE, transition, null);
    }

    /**
     * @param transition
     * @return arcs that are outbound from transition
     */
    public Collection<Arc<Transition, Place>> outboundArcs(Transition transition) {
        Collection<Arc<Transition, Place>> outbound = new LinkedList<Arc<Transition, Place>>();
        for (Arc<? extends Connectable, ? extends Connectable> arc : arcs) {
            if (arc.getSource().equals(transition)) {
                outbound.add((Arc<Transition, Place>) arc);
            }
        }
        return outbound;
    }

    public void remove(PetriNetComponent component) {
        component.accept(deleteVisitor);
    }

    public void removeToken(Token token) {
        tokens.remove(token);
        changeSupport.firePropertyChange(DELETE_TOKEN_CHANGE_MESSAGE, token, null);
    }

    public void removeRateParameter(RateParameter parameter) {
        rateParameters.remove(parameter);
        changeSupport.firePropertyChange(DELETE_RATE_PARAMETER_CHANGE_MESSAGE, parameter, null);
    }

    //    public void removeStateGroup(StateGroup group) {
    //        stateGroups.remove(group);
    //    }

    public void removeAnnotaiton(Annotation annotation) {
        annotations.remove(annotation);
        changeSupport.firePropertyChange(DELETE_ANNOTATION_CHANGE_MESSAGE, annotation, null);
    }

    public boolean containsDefaultToken() {
        for (Token token : tokens) {
            if (token.getId().equals("Default")) {
                return true;
            }
        }
        return false;
    }

    public Token getToken(String tokenId) throws PetriNetComponentNotFound {
        //TODO: Find an O(1) name to do this, perhaps Map?
        for (Token token : tokens) {
            if (token.getId().equals(tokenId)) {
                return token;
            }
        }
        throw new PetriNetComponentNotFound("No token " + tokenId + " exists in Petri net.");
    }

    public RateParameter getRateParameter(String rateParameterId) throws PetriNetComponentNotFound {
        for (RateParameter rateParameter : rateParameters) {
            if (rateParameter.getId().equals(rateParameterId)) {
                return rateParameter;
            }
        }
        throw new PetriNetComponentNotFound("No rate parameter " + rateParameterId + " exists in Petri net.");
    }

    public void add(PetriNetComponent component) {
        component.accept(addVisitor);
    }

    public Transition getRandomTransition() {

        Collection<Transition> enabledTransitions = getEnabledTransitions();
        if (enabledTransitions.isEmpty()) {
            throw new RuntimeException("Error - no transitions to fire!");
        }

        Random random = new Random();
        int index = random.nextInt(enabledTransitions.size());

        Iterator<Transition> iter = enabledTransitions.iterator();
        Transition transition = iter.next();
        for (int i = 1; i < index; i++) {
            transition = iter.next();
        }
        return transition;
    }

    /**
     * Finds all of the transitions which are enabled
     * If we have any immediate transitions then these take priority
     * and timed transactions are not counted as enabled
     * <p/>
     * It also disables any immediate transitions with a lower
     * priority than the highest available priority.
     * <p/>
     * @return all transitions that can be enabled
     */
    public Set<Transition> getEnabledTransitions() {
        Set<Transition> enabledTransitions = findEnabledTransitions();
        boolean hasImmediate = areAnyTransitionsImmediate(enabledTransitions);
        int maxPriority = hasImmediate ? getMaxPriority(enabledTransitions) : 0;

        if (hasImmediate) {
            removeTimedTransitions(enabledTransitions);
        }

        removePrioritiesLessThan(maxPriority, enabledTransitions);
        return enabledTransitions;
    }

    /**
     *
     * Note we must use an iterator in order to ensure save removal
     * whilst looping
     *
     * @param priority minimum priority of transitions allowed to remain in the Collection
     * @param transitions to remove if their priority is less than the specified value
     */
    private void removePrioritiesLessThan(int priority, Collection<Transition> transitions) {
        Iterator<Transition> transitionIterator = transitions.iterator();
        while(transitionIterator.hasNext()) {
            Transition transition = transitionIterator.next();
            if (!transition.isTimed() && transition.getPriority() < priority) {
                transitionIterator.remove();
            }
        }
    }

    /**
     * Removes timed transitions from transitions
     *
     * Note have to use an iterator for save deletions whilst
     * iterating through the list
     *
     * @param transitions to remove timed transitions from
     */
    private void removeTimedTransitions(Set<Transition> transitions) {
        Iterator<Transition> transitionIterator = transitions.iterator();
        while(transitionIterator.hasNext()) {
            Transition transition = transitionIterator.next();
            if (transition.isTimed()) {
                transitionIterator.remove();
            }
        }
    }

    /**
     * @param transitions to find max prioirty of
     * @return the maximum priority of immediate transitions in the collection
     */
    private int getMaxPriority(Iterable<Transition> transitions) {
        int maxPriority = 0;
        for (Transition transition : transitions) {
            if (!transition.isTimed()) {
                maxPriority = Math.max(maxPriority, transition.getPriority());
            }
        }
        return maxPriority;
    }

    /**
     * @return all the currently enabed transitions in the petri net
     */
    private Set<Transition> findEnabledTransitions() {

        Set<Transition> enabledTransitions = new HashSet<Transition>();
        for (Transition transition : getTransitions()) {
            if (isEnabled(transition)) {
                enabledTransitions.add(transition);
            }
        }
        return enabledTransitions;
    }

    public Collection<Transition> getTransitions() {
        return transitions;
    }

    /**
     *
     * @param transition
     * @return true if transition is enabled
     */
    private boolean isEnabled(Transition transition) {
        boolean enabledForArcs = true;
        for (Arc<Place, Transition> arc : inboundArcs(transition)) {

            ArcStrategy<Place, Transition> strategy = backwardsStrategies.get(arc.getType());
            enabledForArcs &= strategy.canFire(this, arc);
        }
        for (Arc<Transition, Place> arc : outboundArcs(transition)) {
            ArcStrategy<Transition, Place> strategy = forwardStrategies.get(arc.getType());
            enabledForArcs &= strategy.canFire(this, arc);
        }
        return enabledForArcs;
    }

    /**
     * @param transition
     * @return arcs that are inbound to transition
     */
    public Collection<Arc<Place, Transition>> inboundArcs(Transition transition) {
        Collection<Arc<Place, Transition>> outbound = new LinkedList<Arc<Place, Transition>>();
        for (Arc<? extends Connectable, ? extends Connectable> arc : arcs) {
            if (arc.getTarget().equals(transition)) {
                outbound.add((Arc<Place, Transition>) arc);
            }
        }
        return outbound;
    }

    /**
     * @param transitions to check if any are timed
     * @return true if any of the transitions are timed
     */
    private boolean areAnyTransitionsImmediate(Collection<Transition> transitions) {
        for (Transition transition : transitions) {
            if (!transition.isTimed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes tokens from places into the transition
     * Adds tokens to the places out of the transition according to the arc weight
     * Recalculates enabled transitions
     *
     * @param transition transition to fire
     */
    public void fireTransition(Transition transition) {
        if (transition.isEnabled()) {
            //Decrement previous places
            for (Arc<Place, Transition> arc : inboundArcs(transition)) {
                Place place = arc.getSource();
                for (Token token : arc.getTokenWeights().keySet()) {
                    IncidenceMatrix matrix = getBackwardsIncidenceMatrix(token);
                    int currentCount = place.getTokenCount(token);
                    int newCount = currentCount - matrix.get(place, transition);
                    place.setTokenCount(token, newCount);
                }
            }

            //Increment new places
            for (Arc<Transition, Place> arc : outboundArcs(transition)) {
                Place place = arc.getTarget();
                for (Token token : arc.getTokenWeights().keySet()) {
                    IncidenceMatrix matrix = getForwardsIncidenceMatrix(token);
                    int currentCount = place.getTokenCount(token);
                    int newCount = currentCount + matrix.get(place, transition);
                    place.setTokenCount(token, newCount);
                }
            }
        }
        markEnabledTransitions();
    }

    /**
     * Calculates weights of connections from places to transitions for given token
     *
     * @param token calculates backwards incidence matrix for this token
     */
    public IncidenceMatrix getBackwardsIncidenceMatrix(Token token) {
        ExprEvaluator paser = new ExprEvaluator(this);
        IncidenceMatrix backwardsIncidenceMatrix = new IncidenceMatrix();
        for (Arc<? extends Connectable, ? extends Connectable> arc : arcs) {
            Connectable target = arc.getTarget();
            Connectable source = arc.getSource();
            if (target instanceof Transition) {
                Transition transition = (Transition) target;
                if (source instanceof Place) {
                    Place place = (Place) source;
                    int enablingDegree = transition.isInfiniteServer() ? getEnablingDegree(transition) : 0;


                    String expression = arc.getWeightForToken(token);
                    Integer weight = paser.parseAndEvalExpr(expression, token.getId());
                    int totalWeight = transition.isInfiniteServer() ? weight * enablingDegree : weight;
                    backwardsIncidenceMatrix.put(place, transition, totalWeight);
                }
            }
        }
        return backwardsIncidenceMatrix;
    }

    /**
     * A Transition is enabled if all its input places are marked with at least one token
     * This method calculates the minimium number of tokens needed in order for a transition to be enabeld
     * <p/>
     * The enabling degree is the number of times that a transition is enabled
     *
     * @param transition
     * @return the transitions enabling degree
     */
    public int getEnablingDegree(Transition transition) {
        ExprEvaluator evaluator = new ExprEvaluator(this);
        int enablingDegree = Integer.MAX_VALUE;


        for (Arc<Place, Transition> arc : inboundArcs(transition)) {
            Place place = arc.getSource();
            Map<Token, String> arcWeights = arc.getTokenWeights();
            for (Map.Entry<Token, String> entry : arcWeights.entrySet()) {
                Token arcToken = entry.getKey();
                String arcTokenExpression = entry.getValue();

                int placeTokenCount = place.getTokenCount(arcToken);
                int requiredTokenCount = evaluator.parseAndEvalExpr(arcTokenExpression, arcToken.getId());

                if (requiredTokenCount == 0) {
                    enablingDegree = 0;
                } else {
                    int currentDegree = (int) Math.floor(placeTokenCount / requiredTokenCount);
                    if (currentDegree < enablingDegree) {
                        enablingDegree = currentDegree;
                    }

                }
            }
        }
        return enablingDegree;
    }

    /**
     * Calculates weights of connections from transitions to places for given token
     *
     * @param token
     * @return
     * @throws Exception
     */
    public IncidenceMatrix getForwardsIncidenceMatrix(Token token) {

        IncidenceMatrix forwardsIncidenceMatrix = new IncidenceMatrix();
        for (Arc<? extends Connectable, ? extends Connectable> arc : arcs) {
            Connectable target = arc.getTarget();
            Connectable source = arc.getSource();

            if (target instanceof Place) {
                Place place = (Place) target;
                if (source instanceof Transition) {
                    Transition transition = (Transition) source;

                    String expression = arc.getWeightForToken(token);

                    ExprEvaluator paser = new ExprEvaluator(this);

                    Integer weight = paser.parseAndEvalExpr(expression, token.getId());
                    forwardsIncidenceMatrix.put(place, transition, weight);
                }
            }
        }
        return forwardsIncidenceMatrix;
    }

    /**
     * Calculates enabled transitions and enables them.
     */
    public void markEnabledTransitions() {
        Set<Transition> enabledTransitions = getEnabledTransitions();
        for (Transition transition : transitions) {
            if (enabledTransitions.contains(transition)) {
                transition.enable();
            } else {
                transition.disable();
            }
        }
    }

    /**
     * Removes tokens from places out of the transition
     * Adds tokens to the places into the transition according to the arc weight
     * Enables fired transition
     *
     * @param transition
     */
    //TODO: NOT SURE IF BETTER TO JUST HAVE UNDO/REDO IN ANIMATION HISTORY? HAVE TO STORE ENTIRE PETRI
    //      NET STATES SO MAYBE NOT?
    public void fireTransitionBackwards(Transition transition) {
        //Increment previous places
        for (Arc<Place, Transition> arc : inboundArcs(transition)) {
            Place place = arc.getSource();
            for (Token token : arc.getTokenWeights().keySet()) {
                IncidenceMatrix matrix = getBackwardsIncidenceMatrix(token);
                int currentCount = place.getTokenCount(token);
                int newCount = currentCount + matrix.get(place, transition);
                place.setTokenCount(token, newCount);
            }
        }

        //Decrement new places
        for (Arc<Transition, Place> arc : outboundArcs(transition)) {
            Place place = arc.getTarget();
            for (Token token : arc.getTokenWeights().keySet()) {
                IncidenceMatrix matrix = getForwardsIncidenceMatrix(token);
                int oldCount = place.getTokenCount(token);
                int newCount = oldCount - matrix.get(place, transition);
                place.setTokenCount(token, newCount);
            }
        }
        markEnabledTransitions();
    }

    public Set<RateParameter> getRateParameters() {
        return rateParameters;
    }
}
